import { YORK_BUILDINGS } from "../data/mockData";

export type LocationPermissionPreference = "always" | "reject" | null;

export interface CampusLocationReading {
  latitude: number;
  longitude: number;
  accuracyMeters: number;
  buildingName: string;
  buildingAcronym: string;
  distanceMeters: number;
  updatedAt: number;
}

export const LOCATION_PREF_KEY = "studyBuddyLocationPermission";
export const LOCATION_ONCE_ACTIVE_KEY = "studyBuddyLocationOnceActive";
export const LOCATION_ONCE_TOKEN_KEY = "studyBuddyLocationOnceToken";
export const LAST_LOCATION_KEY = "studyBuddyLastCampusLocation";

export const isGeolocationPermissionDenied = (error: unknown): boolean => {
  if (!error || typeof error !== "object") {
    return false;
  }

  const maybeError = error as { code?: number };
  return maybeError.code === 1;
};

// Event emitter for preference changes - allows components to react globally
type PreferenceChangeListener = () => void;
const preferenceListeners = new Set<PreferenceChangeListener>();

export const addLocationPreferenceListener = (listener: PreferenceChangeListener): (() => void) => {
  preferenceListeners.add(listener);
  return () => {
    preferenceListeners.delete(listener);
  };
};

const notifyPreferenceChange = () => {
  preferenceListeners.forEach(listener => listener());
};

// Get user preference for location tracking
export const getLocationPreference = (): LocationPermissionPreference => {
  const raw = localStorage.getItem(LOCATION_PREF_KEY);
  return raw === "always" || raw === "reject" ? raw : null;
};

// Set user preference for location tracking - notifies all listeners when changed
export const setLocationPreference = (value: LocationPermissionPreference) => {
  const oldPref = getLocationPreference();
  
  if (!value) {
    localStorage.removeItem(LOCATION_PREF_KEY);
  } else {
    localStorage.setItem(LOCATION_PREF_KEY, value);
  }
  
  // When setting to "reject", immediately clear any "once" state to prevent conflicts
  if (value === "reject") {
    setOnceLocationActive(false);
    // Also clear cached location data when rejecting
    localStorage.removeItem(LAST_LOCATION_KEY);
  }
  
  // Notify listeners only if preference actually changed
  if (oldPref !== value) {
    notifyPreferenceChange();
  }
};

// Access to location for current session
export const isOnceLocationActive = () =>
  sessionStorage.getItem(LOCATION_ONCE_ACTIVE_KEY) === "true";

// Check if once location is active and matches the given token (if any)
export const isOnceLocationActiveForToken = (token: string | null) => {
  if (!token) {
    return false;
  }
  return (
    isOnceLocationActive() &&
    sessionStorage.getItem(LOCATION_ONCE_TOKEN_KEY) === token
  );
};

// Activate or deactivate one-time location access for the current session
export const setOnceLocationActive = (active: boolean, token?: string | null) => {
  if (active) {
    sessionStorage.setItem(LOCATION_ONCE_ACTIVE_KEY, "true");
    if (token) {
      sessionStorage.setItem(LOCATION_ONCE_TOKEN_KEY, token);
    }
  } else {
    sessionStorage.removeItem(LOCATION_ONCE_ACTIVE_KEY);
    sessionStorage.removeItem(LOCATION_ONCE_TOKEN_KEY);
  }
};

// Determine if user location should track based on preference
// Returns true only if preference is "always" OR "once" is active for the current token
// Returns false if preference is "reject" or if "once" is not active
export const shouldTrackLocationNow = (token?: string | null): boolean => {
  const pref = getLocationPreference();
  
  // Never track if explicitly rejected
  if (pref === "reject") {
    return false;
  }
  
  // Track if "always" preference is set
  if (pref === "always") {
    return true;
  }
  
  // Track if "once" is active for this token (only when no permanent preference set)
  // This is the session-scoped tracking after user clicks "Allow Once"
  if (pref === null) {
    return isOnceLocationActiveForToken(token ?? null);
  }
  
  return false;
};

// Get the last known campus location reading from localStorage, if available
export const getLastCampusLocation = (): CampusLocationReading | null => {
  const raw = localStorage.getItem(LAST_LOCATION_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as CampusLocationReading;
  } catch {
    return null;
  }
};

const persistLastCampusLocation = (reading: CampusLocationReading) => {
  localStorage.setItem(LAST_LOCATION_KEY, JSON.stringify(reading));
};

interface BuildingAnchor {
  lat: number;
  lng: number;
}

const BUILDING_ANCHOR_REFINEMENTS: Record<string, BuildingAnchor[]> = {
  // Separate Steacie Library vs nearby science buildings with more specific points.
  STL: [
    { lat: 43.77382, lng: -79.50444 },
    { lat: 43.77366, lng: -79.50456 }
  ],
  SSL: [
    { lat: 43.77384, lng: -79.50448 },
    { lat: 43.77372, lng: -79.50460 }
  ],
  FRG: [
    { lat: 43.77404, lng: -79.50388 },
    { lat: 43.77412, lng: -79.50402 }
  ],
  LSB: [
    { lat: 43.77478, lng: -79.50486 },
    { lat: 43.77469, lng: -79.50501 }
  ]
};

// Approximate lat/lng to meters then use Euclidean distance.
const euclideanDistanceMeters = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
) => {
  const metersPerDegreeLat = 111_320;
  const avgLatRadians = ((lat1 + lat2) / 2) * (Math.PI / 180);
  const metersPerDegreeLon = 111_320 * Math.cos(avgLatRadians);

  const dx = (lon2 - lon1) * metersPerDegreeLon;
  const dy = (lat2 - lat1) * metersPerDegreeLat;

  return Math.sqrt(dx * dx + dy * dy);
};

// Format distance in meters to a user-friendly string (e.g. "150 m away" or "1.2 km away")
export const formatDistance = (distanceMeters: number) => {
  if (distanceMeters < 1000) {
    return `${Math.round(distanceMeters)} m away`;
  }
  return `${(distanceMeters / 1000).toFixed(2)} km away`;
};

const getAnchorsForBuilding = (building: {
  acronym: string;
  lat: number;
  lng: number;
}) => {
  const refined = BUILDING_ANCHOR_REFINEMENTS[building.acronym];
  if (refined && refined.length > 0) {
    return refined;
  }
  return [{ lat: building.lat, lng: building.lng }];
};

// Calculate the minimum distance from the given lat/lng to any of the building's 
const getMinDistanceToBuilding = (
  latitude: number,
  longitude: number,
  building: { acronym: string; lat: number; lng: number }
) => {
  const anchors = getAnchorsForBuilding(building);
  let minDistance = Number.POSITIVE_INFINITY;
  for (const anchor of anchors) {
    const distance = euclideanDistanceMeters(
      latitude,
      longitude,
      anchor.lat,
      anchor.lng
    );
    if (distance < minDistance) {
      minDistance = distance;
    }
  }
  return minDistance;
};

// Find the nearest York building to the given latitude and longitude, returning its name, acronym, and distance in meters
export const findNearestYorkBuilding = (latitude: number, longitude: number) => {
  if (!YORK_BUILDINGS.length) {
    return null;
  }

  let nearest = YORK_BUILDINGS[0];
  let nearestDistance = getMinDistanceToBuilding(latitude, longitude, nearest);

  for (const building of YORK_BUILDINGS.slice(1)) {
    const distance = getMinDistanceToBuilding(latitude, longitude, building);
    if (distance < nearestDistance) {
      nearest = building;
      nearestDistance = distance;
    }
  }

  return {
    buildingName: nearest.name,
    buildingAcronym: nearest.acronym,
    distanceMeters: nearestDistance
  };
};

// Given a latitude and longitude, determine the nearest York building 
export const readNearestCampusLocation = (
  latitude: number,
  longitude: number,
  accuracyMeters = 50,
  lastReading?: CampusLocationReading | null
): CampusLocationReading | null => {
  const nearest = findNearestYorkBuilding(latitude, longitude);
  if (!nearest) {
    return null;
  }

  let resolvedName = nearest.buildingName;
  let resolvedAcronym = nearest.buildingAcronym;
  let resolvedDistance = nearest.distanceMeters;

  // Keep last building unless the new candidate is meaningfully closer.
  if (lastReading && lastReading.buildingAcronym !== nearest.buildingAcronym) {
    const previousBuilding = YORK_BUILDINGS.find(
      (building) => building.acronym === lastReading.buildingAcronym
    );

    if (previousBuilding) {
      const previousDistance = getMinDistanceToBuilding(
        latitude,
        longitude,
        previousBuilding
      );
      const switchMarginMeters = Math.max(12, Math.min(35, accuracyMeters * 0.35));

      if (previousDistance <= nearest.distanceMeters + switchMarginMeters) {
        resolvedName = previousBuilding.name;
        resolvedAcronym = previousBuilding.acronym;
        resolvedDistance = previousDistance;
      }
    }
  }

  return {
    latitude,
    longitude,
    accuracyMeters,
    buildingName: resolvedName,
    buildingAcronym: resolvedAcronym,
    distanceMeters: resolvedDistance,
    updatedAt: Date.now()
  };
};

// Request user location and reject if Geolocation not supported or location cannot be mapped to a York building.
export const requestCurrentCampusLocation = () =>
  new Promise<CampusLocationReading>((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Geolocation is not supported in this browser."));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const reading = readNearestCampusLocation(
          position.coords.latitude,
          position.coords.longitude,
          position.coords.accuracy,
          getLastCampusLocation()
        );
        if (!reading) {
          reject(new Error("Could not map your location to a York building."));
          return;
        }
        persistLastCampusLocation(reading);
        resolve(reading);
      },
      (error) => {
        reject(error);
      },
      {
        enableHighAccuracy: true,
        maximumAge: 10000,
        timeout: 10000
      }
    );
  });

// Update user location in real-time as they move around campus
interface WatchCampusLocationOptions {
  onUpdate: (reading: CampusLocationReading) => void;
  onError?: (error: GeolocationPositionError) => void;
}

export const watchCampusLocation = ({
  onUpdate,
  onError
}: WatchCampusLocationOptions) => {
  if (!navigator.geolocation) {
    return () => {};
  }

  const watchId = navigator.geolocation.watchPosition(
    (position) => {
      const reading = readNearestCampusLocation(
        position.coords.latitude,
        position.coords.longitude,
        position.coords.accuracy,
        getLastCampusLocation()
      );
      if (!reading) {
        return;
      }
      persistLastCampusLocation(reading);
      onUpdate(reading);
    },
    (error) => {
      if (onError) {
        onError(error);
      }
    },
    {
      enableHighAccuracy: true,
      maximumAge: 5000,
      timeout: 10000
    }
  );

  return () => {
    navigator.geolocation.clearWatch(watchId);
  };
};
