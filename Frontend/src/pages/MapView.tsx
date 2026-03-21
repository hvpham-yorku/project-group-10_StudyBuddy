import React, { useState, useEffect, useMemo, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  MapPin, Users, Clock, Navigation, Search, X, BookOpen, ArrowRight
} from "lucide-react";
import {
  APIProvider,
  Map,
  AdvancedMarker,
  Pin,
  InfoWindow,
  useMap,
  useMapsLibrary,
} from "@vis.gl/react-google-maps";
import { YORK_BUILDINGS } from "../data/mockData";

const API_KEY = (import.meta as any).env?.VITE_GOOGLE_MAPS_API_KEY || "";
const YORK_CENTER = { lat: 43.7735, lng: -79.5019 };

// ─── Directions Layer ─────────────────────────────────────────────────────────
// Renders a walking route as a blue polyline directly on the map.
// Must be a child of <Map> so it can access the map instance via useMap().
interface DirectionsLayerProps {
  origin: { lat: number; lng: number };
  destination: { lat: number; lng: number };
  onRouteResult?: (distance: string, duration: string) => void;
}

function DirectionsLayer({ origin, destination, onRouteResult }: DirectionsLayerProps) {
  const map = useMap();
  const routesLib = useMapsLibrary("routes");
  const rendererRef = useRef<any>(null);

  useEffect(() => {
    if (!routesLib || !map) return;
    if (!rendererRef.current) {
      rendererRef.current = new (routesLib as any).DirectionsRenderer({
        map,
        suppressMarkers: true,
        polylineOptions: {
          strokeColor: "#215EBA",
          strokeWeight: 5,
          strokeOpacity: 0.85,
        },
      });
    }
    return () => {
      rendererRef.current?.setMap(null);
      rendererRef.current = null;
    };
  }, [routesLib, map]);

  useEffect(() => {
    if (!routesLib || !map || !origin || !destination || !rendererRef.current) return;
    const service = new (routesLib as any).DirectionsService();
    service.route(
      { origin, destination, travelMode: "WALKING" },
      (result: any, status: string) => {
        if (status === "OK" && result && rendererRef.current) {
          rendererRef.current.setDirections(result);
          const leg = result.routes[0]?.legs[0];
          if (leg && onRouteResult) {
            onRouteResult(leg.distance?.text ?? "", leg.duration?.text ?? "");
          }
        }
      }
    );
  }, [origin, destination, routesLib, map]);

  return null;
}

// ─── MapView ──────────────────────────────────────────────────────────────────
export default function MapView() {
  const navigate = useNavigate();
  const [events, setEvents] = useState<any[]>([]);
  const [student, setStudent] = useState<any>(null);

  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);
  const [selectedHubName, setSelectedHubName] = useState<string | null>(null);

  const [search, setSearch] = useState("");
  const [filterCourse, setFilterCourse] = useState("All");

  // Navigation state
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [navigatingToHub, setNavigatingToHub] = useState<string | null>(null);
  const [routeDistance, setRouteDistance] = useState<string>("");
  const [routeDuration, setRouteDuration] = useState<string>("");

  // 1. Fetch events + student
  useEffect(() => {
    const loadData = async () => {
      try {
        const token = localStorage.getItem("studyBuddyToken");
        if (token) {
          const userRes = await fetch("/api/studentcontroller/profile", {
            headers: { Authorization: "Bearer " + token },
          });
          if (userRes.ok) setStudent(await userRes.json());
        }
        const evRes = await fetch("/api/events");
        if (evRes.ok) setEvents(await evRes.json());
      } catch (e) {
        console.error("Failed to load map data", e);
      }
    };
    loadData();
  }, []);

  // 2. Get GPS location
  useEffect(() => {
    if (!navigator.geolocation) {
      setLocationError("Geolocation not supported");
      setUserLocation(YORK_CENTER);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      (err) => {
        console.warn("Location denied:", err.message);
        setLocationError("Location access denied — using York University as origin");
        setUserLocation(YORK_CENTER);
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }, []);

  const uniqueCourses = ["All", ...Array.from(new Set(events.map((s) => s.course).filter(Boolean)))];

  const filteredSessions = events.filter((s: any) => {
    const matchSearch =
      s.title?.toLowerCase().includes(search.toLowerCase()) ||
      s.course?.toLowerCase().includes(search.toLowerCase()) ||
      s.location?.toLowerCase().includes(search.toLowerCase());
    const matchCourse = filterCourse === "All" || s.course === filterCourse;
    return matchSearch && matchCourse;
  });

  // 3. Group events by building
  const hubs = useMemo(() => {
    const map = new globalThis.Map<string, any>();
    filteredSessions.forEach((e) => {
      if (!e.location) return;
      const building = YORK_BUILDINGS.find((b) => e.location === b.name);
      if (!building) return;
      if (!map.has(building.name)) {
        map.set(building.name, {
          coords: { lat: building.lat, lng: building.lng },
          locationName: building.name,
          acronym: building.acronym,
          events: [],
        });
      }
      map.get(building.name).events.push(e);
    });
    return Array.from(map.values());
  }, [filteredSessions]);

  const activeHub = hubs.find((h: any) => h.locationName === selectedHubName) as any;
  const activeEvent = events.find((e) => e.id === selectedEventId);
  const navigatingHub = hubs.find((h: any) => h.locationName === navigatingToHub) as any;

  // 4. Join / Leave
  const handleJoin = async (eventId: string, isJoined: boolean, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!student) return;
    const token = localStorage.getItem("studyBuddyToken");
    try {
      const endpoint = isJoined ? "/api/events/leave" : "/api/events/join";
      const response = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: "Bearer " + token },
        body: JSON.stringify({ eventId, userId: student.userId }),
      });
      if (response.ok) {
        setEvents((prev: any[]) =>
          prev.map((ev: any) => {
            if (ev.id === eventId) {
              const cur = ev.attendees || [];
              const next = isJoined
                ? cur.filter((id: string) => id !== student.userId)
                : [...cur, student.userId];
              return { ...ev, attendees: next };
            }
            return ev;
          })
        );
      }
    } catch (err) {
      console.error("Error joining/leaving event:", err);
    }
  };

  // 5. Navigation
  const handleNavigate = (hub: any) => {
    if (!hub) return;
    setNavigatingToHub(hub.locationName);
    setRouteDistance("");
    setRouteDuration("");
    setSelectedHubName(null);
  };

  const handleStopNavigation = () => {
    setNavigatingToHub(null);
    setRouteDistance("");
    setRouteDuration("");
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden">

      {/* Top filter bar */}
      <div className="bg-white border-b border-slate-200 px-4 py-3 flex gap-3 items-center shrink-0">
        <div className="flex-1 relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search sessions or buildings..."
            className="w-full pl-8 pr-4 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="flex gap-1.5 overflow-x-auto pb-1">
          {uniqueCourses.map((c) => (
            <button
              key={c}
              onClick={() => setFilterCourse(c)}
              className={`px-3 py-1.5 rounded-lg text-xs whitespace-nowrap transition-colors ${filterCourse === c ? "bg-blue-700 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
              style={{ fontWeight: filterCourse === c ? 600 : 400 }}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">

        {/* Map Area */}
        <div className="flex-1 relative bg-slate-100 overflow-hidden">

          {/* Navigation active banner */}
          {navigatingToHub && (
            <div className="absolute top-4 left-4 right-4 z-10 bg-blue-700 text-white rounded-xl px-4 py-3 flex items-center gap-3 shadow-lg">
              <Navigation size={18} className="animate-pulse shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold truncate">Navigating to {navigatingToHub}</p>
                {routeDuration && routeDistance ? (
                  <p className="text-xs text-blue-200">🚶 {routeDuration} · {routeDistance}</p>
                ) : (
                  <p className="text-xs text-blue-200">Calculating walking route...</p>
                )}
                {locationError && (
                  <p className="text-xs text-yellow-300 mt-0.5">⚠️ {locationError}</p>
                )}
              </div>
              <button onClick={handleStopNavigation} className="ml-auto shrink-0 text-blue-200 hover:text-white">
                <X size={16} />
              </button>
            </div>
          )}

          {/* Location chip */}
          {!navigatingToHub && (
            <div className="absolute bottom-12 left-4 z-10 bg-white/90 backdrop-blur-sm border border-slate-200 rounded-lg px-3 py-1.5 text-xs text-slate-500 flex items-center gap-1.5 shadow-sm">
              <span className={`w-2 h-2 rounded-full ${userLocation && !locationError ? "bg-green-400" : "bg-yellow-400"}`} />
              {userLocation && !locationError ? "Using your location" : locationError ?? "Detecting location..."}
            </div>
          )}

          {/* Legend */}
          <div className="absolute bottom-4 left-4 z-10 bg-white/90 backdrop-blur-sm border border-slate-200 rounded-lg px-3 py-2 text-xs text-slate-500 flex flex-col gap-1 shadow-sm">
            <div className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-full bg-[#215EBA] inline-block" /> Study session</div>
            <div className="flex items-center gap-1.5"><span className="w-3 h-3 rounded-full bg-[#EA580C] inline-block" /> Selected / navigating</div>
          </div>

          <APIProvider apiKey={API_KEY}>
            <Map
              defaultCenter={YORK_CENTER}
              defaultZoom={15.5}
              mapId="studybuddy-map"
              gestureHandling="greedy"
              disableDefaultUI={false}
            >
              {/* Walking route polyline */}
              {navigatingToHub && userLocation && navigatingHub && (
                <DirectionsLayer
                  origin={userLocation}
                  destination={navigatingHub.coords}
                  onRouteResult={(dist, dur) => {
                    setRouteDistance(dist);
                    setRouteDuration(dur);
                  }}
                />
              )}

              {/* User location dot */}
              {userLocation && (
                <AdvancedMarker position={userLocation} title="You are here">
                  <div style={{ width: 20, height: 20, borderRadius: "50%", background: "rgba(33,94,186,0.25)", display: "flex", alignItems: "center", justifyContent: "center", animation: "pulse 2s infinite" }}>
                    <div style={{ width: 11, height: 11, borderRadius: "50%", background: "#215EBA", border: "2.5px solid white", boxShadow: "0 0 6px rgba(33,94,186,0.8)" }} />
                  </div>
                </AdvancedMarker>
              )}

              {/* Hub pins */}
              {hubs.map((hub: any, i) => {
                const isSelected = selectedHubName === hub.locationName;
                const isNavTarget = navigatingToHub === hub.locationName;
                return (
                  <AdvancedMarker
                    key={i}
                    position={hub.coords}
                    title={`${hub.locationName} — ${hub.events.length} session${hub.events.length > 1 ? "s" : ""}`}
                    onClick={() => {
                      setSelectedHubName(hub.locationName);
                      if (hub.events.length > 0) setSelectedEventId(hub.events[0].id);
                    }}
                  >
                    <Pin
                      background={isSelected || isNavTarget ? "#EA580C" : "#215EBA"}
                      borderColor={isSelected || isNavTarget ? "#9A3412" : "#0f2e6b"}
                      glyphColor="white"
                      scale={isSelected || isNavTarget ? 1.3 : 1.0}
                    />
                  </AdvancedMarker>
                );
              })}

              {/* Info window */}
              {activeHub && (
                <InfoWindow position={activeHub.coords} onCloseClick={() => setSelectedHubName(null)}>
                  <div className="p-2 min-w-[220px] font-sans">
                    <h3 className="font-bold text-slate-800 text-sm mb-1">{activeHub.locationName}</h3>
                    <p className="text-xs text-slate-600 mb-3">📍 {activeHub.events.length} session{activeHub.events.length > 1 ? "s" : ""} here</p>

                    <div className="flex flex-col gap-2 mb-3 max-h-40 overflow-y-auto pr-1">
                      {activeHub.events.map((ev: any) => {
                        const isSel = activeEvent?.id === ev.id;
                        return (
                          <div
                            key={ev.id}
                            onClick={() => setSelectedEventId(ev.id)}
                            className={`text-xs p-2 rounded border cursor-pointer transition-colors ${isSel ? "bg-blue-50 border-blue-500 shadow-sm" : "bg-slate-50 border-slate-200 hover:bg-blue-50"}`}
                          >
                            <div className="font-semibold text-blue-700 mb-0.5">{ev.course}</div>
                            <div className="text-slate-700 font-semibold truncate mb-1">{ev.title}</div>
                            <div className="text-slate-500 flex justify-between items-center">
                              <span>{ev.time}</span>
                              <span className="flex items-center gap-1">{ev.attendees?.length || 1}/{ev.maxParticipants} <Users size={10} /></span>
                            </div>
                          </div>
                        );
                      })}
                    </div>

                    {activeEvent && activeHub.events.find((e: any) => e.id === activeEvent.id) && (() => {
                      const isHost = activeEvent.host?.id === student?.userId;
                      const isJoined = activeEvent.attendees?.includes(student?.userId);
                      const isFull = (activeEvent.attendees?.length || 1) >= activeEvent.maxParticipants;
                      if (isHost) return <button disabled className="w-full py-1.5 bg-slate-200 text-slate-500 rounded text-xs font-bold cursor-not-allowed mb-2">You are Hosting</button>;
                      if (isJoined) return <button onClick={(e) => handleJoin(activeEvent.id, true, e)} className="w-full py-1.5 bg-slate-200 hover:bg-red-50 hover:text-red-600 border border-transparent text-slate-700 rounded text-xs font-bold transition-colors mb-2">Leave Session</button>;
                      if (isFull) return <button disabled className="w-full py-1.5 bg-slate-200 text-slate-500 rounded text-xs font-bold cursor-not-allowed mb-2">Session Full</button>;
                      return <button onClick={(e) => handleJoin(activeEvent.id, false, e)} className="w-full py-1.5 bg-orange-500 hover:bg-orange-600 text-white rounded text-xs font-bold transition-colors shadow-sm mb-2">Join Session</button>;
                    })()}

                    <button
                      onClick={() => handleNavigate(activeHub)}
                      className="w-full py-1.5 bg-blue-700 hover:bg-blue-800 text-white rounded text-xs font-bold transition-colors flex items-center justify-center gap-1.5"
                    >
                      <Navigation size={12} /> Navigate Here
                    </button>
                  </div>
                </InfoWindow>
              )}
            </Map>
          </APIProvider>
        </div>

        {/* Right Sidebar */}
        <div className="w-80 bg-white border-l border-slate-200 flex flex-col overflow-hidden shrink-0 z-10">

          {navigatingToHub && (
            <div className="px-4 py-3 bg-blue-50 border-b border-blue-100 shrink-0">
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs font-bold text-blue-700 flex items-center gap-1.5">
                  <Navigation size={13} className="animate-pulse" /> Navigation Active
                </span>
                <button onClick={handleStopNavigation} className="text-blue-400 hover:text-blue-700 text-xs font-semibold">Stop</button>
              </div>
              <p className="text-xs text-blue-600 font-medium truncate">{navigatingToHub}</p>
              {routeDuration && routeDistance && (
                <p className="text-xs text-blue-500 mt-0.5">🚶 {routeDuration} · {routeDistance}</p>
              )}
            </div>
          )}

          {activeEvent ? (
            <div className="flex flex-col h-full">
              <div className="px-4 py-3 border-b border-slate-100 flex items-center justify-between shrink-0">
                <h2 className="text-sm text-slate-800 font-semibold">Session Details</h2>
                <button onClick={() => setSelectedEventId(null)} className="text-slate-400 hover:text-slate-600"><X size={16} /></button>
              </div>
              <div className="p-4 flex-1 overflow-y-auto">
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-semibold">{activeEvent.course}</span>
                <h3 className="text-slate-800 mt-2 mb-3 leading-snug font-bold text-[0.95rem]">{activeEvent.title}</h3>
                <div className="space-y-2.5">
                  <div className="flex items-center gap-2 text-sm text-slate-600"><MapPin size={15} className="text-orange-500 shrink-0" />{activeEvent.location}</div>
                  <div className="flex items-center gap-2 text-sm text-slate-600"><Clock size={15} className="text-blue-500 shrink-0" />{activeEvent.date} at {activeEvent.time}</div>
                  <div className="flex items-center gap-2 text-sm text-slate-600"><Users size={15} className="text-blue-500 shrink-0" />{activeEvent.attendees?.length || 1}/{activeEvent.maxParticipants} attending</div>
                  <div className="flex items-center gap-2 text-sm text-slate-600"><BookOpen size={15} className="text-blue-500 shrink-0" />Hosted by {activeEvent.host?.name || activeEvent.host?.id || "Student"}</div>
                </div>
                <div className="mt-4 p-3 bg-blue-50 rounded-xl">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs text-blue-700 font-semibold">Capacity</span>
                    <span className="text-xs text-blue-600">{activeEvent.attendees?.length || 1}/{activeEvent.maxParticipants}</span>
                  </div>
                  <div className="bg-blue-200 rounded-full h-1.5">
                    <div className="bg-blue-600 h-full rounded-full" style={{ width: `${((activeEvent.attendees?.length || 1) / (activeEvent.maxParticipants || 10)) * 100}%` }} />
                  </div>
                </div>
              </div>
              <div className="p-4 border-t border-slate-100 space-y-2 shrink-0">
                {(() => {
                  const hub = hubs.find((h: any) => h.locationName === activeEvent.location);
                  const isNavTarget = navigatingToHub === activeEvent.location;
                  if (!hub) return null;
                  return isNavTarget ? (
                    <button onClick={handleStopNavigation} className="w-full py-2.5 bg-orange-500 hover:bg-orange-600 text-white rounded-xl text-sm transition-colors flex items-center justify-center gap-2 font-semibold">
                      <X size={14} /> Stop Navigation
                    </button>
                  ) : (
                    <button onClick={() => handleNavigate(hub)} className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-xl text-sm transition-colors flex items-center justify-center gap-2 font-semibold">
                      <Navigation size={14} /> Navigate Here
                    </button>
                  );
                })()}
                <button onClick={() => navigate(`/events/${activeEvent.id}`)} className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm transition-colors flex items-center justify-center gap-2 font-medium">
                  View Details <ArrowRight size={14} />
                </button>
              </div>
            </div>
          ) : (
            <div className="flex flex-col h-full">
              <div className="px-4 py-3 border-b border-slate-100 shrink-0">
                <h2 className="text-sm text-slate-800 font-semibold">
                  Nearby Sessions
                  <span className="ml-2 text-xs bg-orange-100 text-orange-600 px-1.5 py-0.5 rounded-full">{filteredSessions.length}</span>
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">Click a pin or session to view details</p>
              </div>
              <div className="flex-1 overflow-y-auto divide-y divide-slate-50">
                {filteredSessions.map((s) => (
                  <div
                    key={s.id}
                    className="px-4 py-3.5 hover:bg-slate-50 cursor-pointer transition-colors"
                    onClick={() => {
                      setSelectedEventId(s.id);
                      const building = YORK_BUILDINGS.find((b) => s.location === b.name);
                      if (building) setSelectedHubName(building.name);
                    }}
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-8 h-8 rounded-xl bg-orange-100 flex items-center justify-center shrink-0"><MapPin size={16} className="text-orange-500" /></div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 mb-0.5">
                          <span className="text-xs bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded font-semibold">{s.course}</span>
                        </div>
                        <p className="text-sm text-slate-700 truncate font-semibold">{s.title}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <span className="flex items-center gap-1 text-xs text-slate-400"><Clock size={11} /> {s.time}</span>
                          <span className="flex items-center gap-1 text-xs text-slate-400"><Users size={11} /> {s.attendees?.length || 1}/{s.maxParticipants}</span>
                        </div>
                        <p className="text-xs text-slate-400 truncate mt-0.5">{s.location}</p>
                      </div>
                    </div>
                  </div>
                ))}
                {filteredSessions.length === 0 && (
                  <div className="p-6 text-center">
                    <MapPin size={28} className="text-slate-200 mx-auto mb-2" />
                    <p className="text-sm text-slate-400">No active physical sessions found</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <style>{`@keyframes pulse{0%{transform:scale(1);opacity:1}50%{transform:scale(1.6);opacity:0.4}100%{transform:scale(1);opacity:1}}`}</style>
    </div>
  );
}