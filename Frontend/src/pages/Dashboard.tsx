/* * Dashboard.tsx
 * This component acts as the main for the user once they login.
 */

import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays, Clock, Users, MapPin, Plus, ArrowRight, BookOpen, Star, TrendingUp } from "lucide-react";
import {
  formatDistance,
  getLocationPreference,
  isGeolocationPermissionDenied,
  requestCurrentCampusLocation,
  setLocationPreference,
  setOnceLocationActive,
  syncTrackedLocationToProfile,
  watchCampusLocation,
  addLocationPreferenceListener
} from "../lib/locationTracking";

interface SessionLogResponse {
  summary?: {
    totalMinutes?: number;
  };
}

function StatCard({ icon: Icon, label, value, color }: { icon: any; label: string; value: string | number; color: string }) {
  return (
    <div className="bg-white rounded-xl p-4 border border-slate-200 flex items-center gap-4">
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center ${color}`}>
        <Icon size={22} className="text-white" />
      </div>
      <div>
        <p className="text-slate-500 text-xs">{label}</p>
        <p className="text-slate-800" style={{ fontSize: "1.25rem", fontWeight: 700 }}>{value}</p>
      </div>
    </div>
  );
}

export default function Dashboard() {
  const navigate = useNavigate();

  const [events, setEvents] = useState<any[]>([]);
  const [userProfile, setUserProfile] = useState<any>(null);
  const [activeConnections, setActiveConnections] = useState<any[]>([]);
  const [totalConnectionsCount, setTotalConnectionsCount] = useState(0);
  const [totalStudyMinutes, setTotalStudyMinutes] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showLocationPrompt, setShowLocationPrompt] = useState(false);
  const [locationPromptError, setLocationPromptError] = useState("");
  const [liveCampusLocation, setLiveCampusLocation] = useState<any>(null); // Only set after user chooses
  const [trackCampusLocation, setTrackCampusLocation] = useState(false);

  // Fetch data from the API on mount
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem("studyBuddyToken");
        if (!token) {
          setIsLoading(false);
          return;
        }

        // 1. Fetch Profile
        const profileRes = await fetch("/api/studentcontroller/profile", {
          headers: { "Authorization": `Bearer ${token}` }
        });
        
        if (!profileRes.ok) throw new Error("Failed to load profile");
        const profileData = await profileRes.json();
        setUserProfile(profileData);

        // 1.5 Fetch Session Log Summary for total study time
        const sessionLogRes = await fetch("/api/studentcontroller/profile/session-log", {
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (sessionLogRes.ok) {
          const sessionData: SessionLogResponse = await sessionLogRes.json();
          setTotalStudyMinutes(sessionData?.summary?.totalMinutes ?? 0);
        }

        // 2. Fetch Events
        const eventsRes = await fetch("/api/events");
        if (eventsRes.ok) {
          const eventsData = await eventsRes.json();
          const formattedEvents = eventsData.map((ev: any) => ({
            ...ev,
            host: typeof ev.host === 'string'
              ? { id: ev.host, name: ev.host, avatar: null }
              : ev.host
          }));
          setEvents(formattedEvents);
        }

        // 3. Fetch Connections
        const connRes = await fetch(`/api/connections?userId=${encodeURIComponent(profileData.userId)}`);
        if (connRes.ok) {
          const connData = await connRes.json();
          setTotalConnectionsCount(connData.length);
          // Filter out offline users to show who is online/idle
          const online = connData.filter((c: any) => c.activityStatus === "online" || c.activityStatus === "idle" || c.isOnline !== false);
          setActiveConnections(online);
        }

      } catch (err: any) {
        console.error("Failed to fetch dashboard data:", err);
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  useEffect(() => {
    // Function to evaluate and update tracking state based on current preferences
    const updateLocationState = () => {
      const pref = getLocationPreference();

      // NEVER track on initial load if preference is null (first time user)
      // Only show prompt to let user decide
      if (pref === null) {
        setTrackCampusLocation(false);
        setShowLocationPrompt(true);
        setLiveCampusLocation(null); // Don't show cached location until user chooses
        return;
      }

      // If preference is "always", resume tracking
      if (pref === "always") {
        setTrackCampusLocation(true);
        setShowLocationPrompt(false);
        return;
      }

      // If preference is "reject", never track
      if (pref === "reject") {
        setTrackCampusLocation(false);
        setShowLocationPrompt(false);
        setLiveCampusLocation(null);
      }
    };

    // Initial state evaluation
    updateLocationState();

    // Listen for preference changes (e.g., from Profile page)
    const unsubscribe = addLocationPreferenceListener(updateLocationState);

    return unsubscribe;
  }, []);

  useEffect(() => {
    if (!trackCampusLocation) {
      return;
    }

    const stopWatch = watchCampusLocation({
      onUpdate: (reading) => {
        setLiveCampusLocation(reading);
        syncTrackedLocationToProfile(reading.buildingName, {
          latitude: reading.latitude,
          longitude: reading.longitude
        }).catch((err) => {
          console.error("Failed to sync tracked location", err);
        });
      },
      onError: (error) => {
        if (isGeolocationPermissionDenied(error)) {
          setLocationPreference("reject");
          setTrackCampusLocation(false);
          setShowLocationPrompt(false);
          setLiveCampusLocation(null);
          setLocationPromptError("Location access denied. Manual location mode is active.");
          return;
        }
        setLocationPromptError("Location access was blocked by your browser.");
      }
    });

    return stopWatch;
  }, [trackCampusLocation]);

  const handleAllowOnce = async () => {
    setLocationPromptError("");
    try {
      const token = localStorage.getItem("studyBuddyToken");
      // Set "once" BEFORE requesting location so tracking immediately starts
      setOnceLocationActive(true, token);
      const reading = await requestCurrentCampusLocation();
      setLiveCampusLocation(reading);
      await syncTrackedLocationToProfile(reading.buildingName, {
        latitude: reading.latitude,
        longitude: reading.longitude
      });
      setTrackCampusLocation(true);
      setShowLocationPrompt(false);
    } catch (error) {
      if (isGeolocationPermissionDenied(error)) {
        setLocationPreference("reject");
        setTrackCampusLocation(false);
        setShowLocationPrompt(false);
        setLiveCampusLocation(null);
        setLocationPromptError("Location access denied. Manual location mode is active.");
      } else {
        setLocationPromptError("Could not access your location. You can continue with manual location.");
      }
      setOnceLocationActive(false);
    }
  };

  const handleAllowAlways = async () => {
    setLocationPromptError("");
    try {
      // Set preference BEFORE requesting location
      setLocationPreference("always");
      const reading = await requestCurrentCampusLocation();
      setLiveCampusLocation(reading);
      await syncTrackedLocationToProfile(reading.buildingName, {
        latitude: reading.latitude,
        longitude: reading.longitude
      });
      setOnceLocationActive(false);
      setTrackCampusLocation(true);
      setShowLocationPrompt(false);
    } catch (error) {
      if (isGeolocationPermissionDenied(error)) {
        setLocationPreference("reject");
        setTrackCampusLocation(false);
        setShowLocationPrompt(false);
        setLiveCampusLocation(null);
        setLocationPromptError("Location access denied. Manual location mode is active.");
      } else {
        setLocationPromptError("Could not access your location. Please check browser location permissions.");
        setLocationPreference(null);
      }
    }
  };

  const handleRejectLocation = () => {
    // Set preference to reject (clears "once" internally)
    setLocationPreference("reject");
    setTrackCampusLocation(false);
    setShowLocationPrompt(false);
    setLocationPromptError("");
    setLiveCampusLocation(null); // Clear any cached location data
  };

  if (isLoading) {
    return <p className="p-6 text-center text-slate-500 mt-10">Loading your dashboard...</p>;
  }

  if (!userProfile) {
    return <p className="p-6 text-center text-slate-500 mt-10">Please log in to view your dashboard.</p>;
  }

  const upcomingEvents = events.filter((e) => e.status === "upcoming").slice(0, 3);
  const userCourses = userProfile.courses || [];

  // Calculate how many events the logged-in user is hosting or attending
  const myTotalSessions = events.filter((e: any) => 
    e.host?.id === userProfile.userId || 
    e.host?.userId === userProfile.userId || 
    (e.attendees && e.attendees.includes(userProfile.userId))
  ).length;

  const formatDate = (d: string) => {
    if (!d) return "";
    const date = new Date(d);
    return date.toLocaleDateString("en-CA", { weekday: "short", month: "short", day: "numeric" });
  };

  const formatStudyTime = () => {
    if (totalStudyMinutes <= 0) return "0 min";
    const totalHours = Math.floor(totalStudyMinutes / 60);
    const remainingMinutes = totalStudyMinutes % 60;
    if (totalHours <= 0) return `${totalStudyMinutes} min`;
    return `${totalHours}h ${remainingMinutes}m`;
  };

  const displayName = userProfile.fullName || `${userProfile.firstName || ""} ${userProfile.lastName || ""}`.trim() || userProfile.userId;

  return (
    <div className="p-6 max-w-6xl mx-auto">
      {/* Welcome Banner */}
      <div className="relative bg-gradient-to-r from-blue-800 to-blue-700 rounded-2xl p-6 mb-6 overflow-hidden">
        <div className="absolute right-0 top-0 w-48 h-48 bg-orange-500/20 rounded-full -translate-y-12 translate-x-12 blur-2xl pointer-events-none"></div>
        <div className="absolute right-8 bottom-0 w-32 h-32 bg-blue-400/20 rounded-full translate-y-8 pointer-events-none blur-2xl"></div>
        <div className="relative flex items-center justify-between">
          <div>
            <p className="text-blue-200 text-sm mb-1">Welcome back 👋</p>
            <h1 className="text-white" style={{ fontSize: "1.5rem", fontWeight: 700 }}>
              {displayName}
            </h1>
            <p className="text-blue-200 text-sm mt-1">{userProfile.program || "Student"} · {userProfile.year || "Unknown Year"}</p>
            <div className="flex items-center gap-2 mt-3">
              <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
              <span className="text-blue-100 text-xs">
                {liveCampusLocation?.buildingName || userProfile.location || "Campus"}
              </span>
              {liveCampusLocation && (
                <span className="text-blue-200 text-xs">({formatDistance(liveCampusLocation.distanceMeters)})</span>
              )}
            </div>
          </div>
          <div className="hidden md:flex flex-col items-end gap-2">
            <button
              onClick={() => navigate("/events/create")}
              className="flex items-center gap-2 bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-lg text-sm transition-colors"
              style={{ fontWeight: 600 }}
            >
              <Plus size={16} />
              Host a Session
            </button>
            <button
              onClick={() => navigate("/events")}
              className="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-4 py-2 rounded-lg text-sm transition-colors"
            >
              Browse Sessions
            </button>
          </div>
        </div>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6 text-x">
        <StatCard icon={BookOpen} label="Study Hours" value={formatStudyTime()} color="bg-blue-600" />
        <StatCard icon={CalendarDays} label="Joined Sessions" value={myTotalSessions} color="bg-orange-500" />
        <StatCard icon={Users} label="Connections" value={totalConnectionsCount} color="bg-blue-700" />
        <StatCard icon={Star} label="Courses" value={userCourses.length} color="bg-orange-600" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Upcoming Sessions */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200">
          <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
            <h2 className="text-slate-800" style={{ fontWeight: 600, fontSize: "1rem" }}>Upcoming Sessions</h2>
            <button
              onClick={() => navigate("/events")}
              className="text-xs text-blue-600 hover:underline flex items-center gap-1"
            >
              View all <ArrowRight size={13} />
            </button>
          </div>
          <div className="divide-y divide-slate-50">
            {upcomingEvents.map((ev) => (
              <div
                key={ev.id}
                className="px-5 py-4 hover:bg-slate-50 cursor-pointer transition-colors group"
                onClick={() => navigate(`/events/${ev.id}`)}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full" style={{ fontWeight: 600 }}>
                        {ev.course}
                      </span>
                      {ev.tags && ev.tags.length > 0 && (
                        <span className="text-xs text-slate-400">{ev.tags[0]}</span>
                      )}
                    </div>
                    <p className="text-sm text-slate-800 truncate" style={{ fontWeight: 600 }}>{ev.title}</p>
                    <div className="flex items-center gap-3 mt-1.5">
                      <span className="flex items-center gap-1 text-xs text-slate-500">
                        <CalendarDays size={12} />
                        {formatDate(ev.date)}
                      </span>
                      <span className="flex items-center gap-1 text-xs text-slate-500">
                        <Clock size={12} />
                        {ev.time} · {ev.duration}min
                      </span>
                    </div>
                    <div className="flex items-center gap-1 mt-1">
                      <MapPin size={12} className="text-slate-400" />
                      <span className="text-xs text-slate-500 truncate">{ev.location}</span>
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="text-xs text-slate-400">{(ev.attendees || []).length}/{ev.maxParticipants}</div>
                    <div className="text-xs text-slate-400">attendees</div>
                    <ArrowRight size={14} className="text-slate-300 group-hover:text-blue-500 transition-colors ml-auto mt-1" />
                  </div>
                </div>
              </div>
            ))}
            {upcomingEvents.length === 0 && (
              <div className="px-5 py-6 text-center text-sm text-slate-500">
                No upcoming sessions. Why not host one?
              </div>
            )}
          </div>
          <div className="px-5 py-3 border-t border-slate-100">
            <button
              onClick={() => navigate("/events/create")}
              className="w-full py-2 border-2 border-dashed border-slate-200 rounded-lg text-sm text-slate-400 hover:border-blue-400 hover:text-blue-600 transition-colors flex items-center justify-center gap-2"
            >
              <Plus size={15} />
              Create a new session
            </button>
          </div>
        </div>

        {/* Right Column */}
        <div className="space-y-4">
          {/* Online Friends */}
          <div className="bg-white rounded-xl border border-slate-200">
            <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
              <h3 className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>Online Now</h3>
              <button onClick={() => navigate("/network")} className="text-xs text-blue-600 hover:underline flex items-center gap-1">
                All <ArrowRight size={12} />
              </button>
            </div>
            <div className="px-4 py-2 space-y-2.5">
              {activeConnections.map((c) => (
                <div key={c.userId} className="flex items-center gap-3">
                  <div className="relative">
                    <div 
                      className="w-8 h-8 rounded-full bg-blue-100 overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                      onClick={() => navigate(`/profile/${c.userId}`)}
                    >
                      {c.profilePic ? (
                        <img src={c.profilePic} alt={c.fullName} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontWeight: 700, fontSize: "0.875rem" }}>
                          {(c.fullName || c.userId).charAt(0).toUpperCase()}
                        </div>
                      )}
                    </div>
                    <div className={`absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border-2 border-white ${c.activityStatus === 'idle' ? 'bg-yellow-400' : 'bg-green-500'}`}></div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p 
                      className="text-xs text-slate-700 truncate cursor-pointer hover:text-blue-600 transition-colors" 
                      style={{ fontWeight: 600 }}
                      onClick={() => navigate(`/profile/${c.userId}`)}
                    >
                      {c.fullName || c.userId}
                    </p>
                    <p className="text-xs text-slate-400 truncate">{c.program || "Student"}</p>
                  </div>
                  <button
                    onClick={() => navigate("/chat")}
                    className="text-xs bg-blue-50 hover:bg-blue-100 text-blue-600 px-2 py-1 rounded-lg transition-colors"
                  >
                    Chat
                  </button>
                </div>
              ))}
              {activeConnections.length === 0 && (
                <p className="text-xs text-slate-400 py-2">No connections online.</p>
              )}
            </div>
          </div>

          {/* Study Streak */}
          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-4 text-white">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp size={18} />
              <span className="text-sm" style={{ fontWeight: 600 }}>Study Streak</span>
            </div>
            <p style={{ fontSize: "2rem", fontWeight: 700, lineHeight: 1 }}>
              {userProfile.loginStreak ?? 0}
            </p>
            <p className="text-orange-100 text-xs mt-1">
              🔥 {(userProfile.loginStreak ?? 0) === 1 ? "day" : "days"} in a row
            </p>
          </div>
        </div>
      </div>

      {/* My Courses */}
      <div className="mt-6 bg-white rounded-xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-slate-800" style={{ fontWeight: 600 }}>My Courses</h2>
          <button onClick={() => navigate("/profile")} className="text-xs text-blue-600 hover:underline flex items-center gap-1">
            Manage <ArrowRight size={12} />
          </button>
        </div>
        <div className="flex flex-wrap gap-2">
          {userCourses.map((course: string) => (
            <span
              key={course}
              className="px-3 py-1.5 bg-blue-50 text-blue-700 rounded-lg text-sm border border-blue-200 cursor-pointer hover:bg-blue-100 transition-colors"
              style={{ fontWeight: 500 }}
            >
              {course}
            </span>
          ))}
          {userCourses.length === 0 && (
            <span className="text-sm text-slate-400 py-1.5">No courses added yet.</span>
          )}
          <button
            onClick={() => navigate("/profile")}
            className="px-3 py-1.5 border-2 border-dashed border-slate-200 text-slate-400 rounded-lg text-sm hover:border-blue-400 hover:text-blue-600 transition-colors flex items-center gap-1"
          >
            <Plus size={13} /> Add course
          </button>
        </div>
      </div>

      {showLocationPrompt && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 flex items-center justify-center p-4">
          <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl">
            <h2 className="text-lg text-slate-900" style={{ fontWeight: 700 }}>Share your exact location?</h2>
            <p className="text-sm text-slate-600 mt-2">
              We use your coordinates to detect the nearest YorkU building and keep your profile location updated while you move around campus.
            </p>

            <div className="mt-4 space-y-2">
              <button
                onClick={handleAllowOnce}
                className="w-full px-4 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm transition-colors"
                style={{ fontWeight: 600 }}
              >
                Allow Once
              </button>
              <button
                onClick={handleAllowAlways}
                className="w-full px-4 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-sm transition-colors"
                style={{ fontWeight: 600 }}
              >
                Allow Always
              </button>
              <button
                onClick={handleRejectLocation}
                className="w-full px-4 py-2.5 rounded-lg bg-slate-200 hover:bg-slate-300 text-slate-700 text-sm transition-colors"
                style={{ fontWeight: 600 }}
              >
                Reject
              </button>
            </div>

            {locationPromptError && (
              <p className="text-sm text-red-600 mt-3">{locationPromptError}</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}