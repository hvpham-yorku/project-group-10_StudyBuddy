import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  MapPin, Users, Clock, Navigation, Search, X, BookOpen, ArrowRight
} from "lucide-react";
import { APIProvider, Map, AdvancedMarker, Pin, InfoWindow } from "@vis.gl/react-google-maps";

// Make sure to add VITE_GOOGLE_MAPS_API_KEY to your Frontend/.env file!
const API_KEY = (import.meta as any).env?.VITE_GOOGLE_MAPS_API_KEY || "";
const YORK_CENTER = { lat: 43.7735, lng: -79.5019 };

// Translates your database string locations into real-world coordinates!
const getCoords = (locationString: string) => {
  if (!locationString) return null;
  const str = locationString.toLowerCase();
  if (str.includes("scott")) return { lat: 43.7731, lng: -79.5036 };
  if (str.includes("vari")) return { lat: 43.7731, lng: -79.5018 };
  if (str.includes("bennett")) return { lat: 43.7738, lng: -79.4975 };
  if (str.includes("lassonde")) return { lat: 43.7733, lng: -79.5049 };
  if (str.includes("bergeron")) return { lat: 43.7737, lng: -79.5052 };
  if (str.includes("pond")) return { lat: 43.7690, lng: -79.5020 };
  if (str.includes("tel") || str.includes("dahdaleh")) return { lat: 43.7725, lng: -79.5002 };
  if (str.includes("curtis")) return { lat: 43.7730, lng: -79.5024 };
  return null; // Ignore online events on the physical map
};

export default function MapView() {
  const navigate = useNavigate();
  const [events, setEvents] = useState<any[]>([]);
  const [student, setStudent] = useState<any>(null);
  
  // Storing IDs instead of objects prevents "stale state" bugs when data updates!
  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);
  const [selectedHubName, setSelectedHubName] = useState<string | null>(null);
  
  const [search, setSearch] = useState("");
  const [filterCourse, setFilterCourse] = useState("All");
  const [navigating, setNavigating] = useState(false);
  const [navTarget, setNavTarget] = useState<string | null>(null);

  // 1. Fetch real events & user from your backend
  useEffect(() => {
    const loadData = async () => {
      try {
        const token = localStorage.getItem("studyBuddyToken");
        if (token) {
          const userRes = await fetch("/api/studentcontroller/profile", {
            headers: { "Authorization": "Bearer " + token }
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

  const uniqueCourses = ["All", ...Array.from(new Set(events.map((s) => s.course).filter(Boolean)))];

  const filteredSessions = events.filter((s) => {
    const matchSearch =
      s.title?.toLowerCase().includes(search.toLowerCase()) ||
      s.course?.toLowerCase().includes(search.toLowerCase()) ||
      s.location?.toLowerCase().includes(search.toLowerCase());
    const matchCourse = filterCourse === "All" || s.course === filterCourse;
    return matchSearch && matchCourse;
  });

  // 2. Group the events into map pins ("Study Hubs") based on their coordinates
  const hubs = useMemo(() => {
    const map = new globalThis.Map(); // Use globalThis to avoid collision with Google Map
    filteredSessions.forEach(e => {
      const coords = getCoords(e.location);
      if (!coords) return; 
      
      const key = `${coords.lat},${coords.lng}`;
      if (!map.has(key)) {
        const buildingName = e.location.split(",")[0];
        map.set(key, { coords, locationName: buildingName, events: [] });
      }
      map.get(key).events.push(e);
    });
    return Array.from(map.values());
  }, [filteredSessions]);

  // Derived active state
  const activeHub = hubs.find((h: any) => h.locationName === selectedHubName) as any;
  const activeEvent = events.find(e => e.id === selectedEventId);

  // 3. Join/Leave Logic (Just like Events.tsx!)
  const handleJoin = async (eventId: string, isJoined: boolean, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!student) return; 
    
    const token = localStorage.getItem("studyBuddyToken");
    try {
      const endpoint = isJoined ? "/api/events/leave" : "/api/events/join";
      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ eventId: eventId, userId: student.userId })
      });

      if (response.ok) {
        setEvents((prev) => prev.map((ev) => {
          if (ev.id === eventId) {
            const currentAttendees = ev.attendees || [];
            const newAttendees = isJoined
              ? currentAttendees.filter((id: string) => id !== student.userId)
              : [...currentAttendees, student.userId];
            return { ...ev, attendees: newAttendees };
          }
          return ev;
        }));
      }
    } catch (err) {
      console.error("Error joining/leaving event:", err);
    }
  };

  const handleNavigate = (eventLocation: string) => {
    setNavigating(true);
    setNavTarget(eventLocation);
    setTimeout(() => setNavigating(false), 3000);
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden">
      {/* Top Bar */}
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
          {navigating && (
            <div className="absolute top-4 left-4 right-4 z-10 bg-blue-700 text-white rounded-xl px-4 py-3 flex items-center gap-3 shadow-lg">
              <Navigation size={18} className="animate-pulse" />
              <div>
                <p className="text-sm font-semibold">Navigating to {navTarget}</p>
                <p className="text-xs text-blue-200">Google Maps routing initiated...</p>
              </div>
              <button onClick={() => setNavigating(false)} className="ml-auto">
                <X size={16} className="text-blue-200 hover:text-white" />
              </button>
            </div>
          )}

          <APIProvider apiKey={API_KEY}>
            <Map
              defaultCenter={YORK_CENTER}
              defaultZoom={15.5}
              mapId="studybuddy-map"
              gestureHandling="greedy"
              disableDefaultUI={false}
            >
              {hubs.map((hub: any, i) => (
                <AdvancedMarker 
                  key={i} 
                  position={hub.coords} 
                  onClick={() => {
                    setSelectedHubName(hub.locationName);
                    // Automatically select the first event in this hub so the UI populates
                    if (hub.events.length > 0) setSelectedEventId(hub.events[0].id);
                  }}
                >
                  <Pin 
                    background={selectedHubName === hub.locationName ? "#EA580C" : "#215EBA"} 
                    borderColor={selectedHubName === hub.locationName ? "#9A3412" : "#0f2e6b"} 
                    glyphColor="white" 
                    scale={selectedHubName === hub.locationName ? 1.2 : 1.0}
                  />
                </AdvancedMarker>
              ))}

              {activeHub && (
                <InfoWindow
                  position={activeHub.coords}
                  onCloseClick={() => setSelectedHubName(null)}
                >
                  <div className="p-2 min-w-[220px] font-sans">
                    <h3 className="font-bold text-slate-800 text-sm mb-1">{activeHub.locationName}</h3>
                    <p className="text-xs text-slate-600 mb-3">📍 {activeHub.events.length} session{activeHub.events.length > 1 ? 's' : ''} here</p>
                    
                    {/* Selectable list of events in this hub */}
                    <div className="flex flex-col gap-2 mb-3 max-h-40 overflow-y-auto pr-1">
                       {activeHub.events.map((ev: any) => {
                         const isSelected = activeEvent?.id === ev.id;
                         return (
                           <div 
                             key={ev.id} 
                             onClick={() => setSelectedEventId(ev.id)}
                             className={`text-xs p-2 rounded border cursor-pointer transition-colors ${isSelected ? 'bg-blue-50 border-blue-500 shadow-sm' : 'bg-slate-50 border-slate-200 hover:bg-blue-50'}`}
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

                    {/* Dynamic Join/Leave Button for the Highlighted Event */}
                    {activeEvent && activeHub.events.find((e: any) => e.id === activeEvent.id) && (() => {
                      const isHost = activeEvent.host?.id === student?.userId;
                      const isJoined = activeEvent.attendees?.includes(student?.userId);
                      const isFull = (activeEvent.attendees?.length || 1) >= activeEvent.maxParticipants;

                      if (isHost) {
                         return (
                           <button disabled className="w-full py-1.5 bg-slate-200 text-slate-500 rounded text-xs font-bold cursor-not-allowed">
                             You are Hosting
                           </button>
                         );
                      }
                      if (isJoined) {
                         return (
                           <button onClick={(e) => handleJoin(activeEvent.id, true, e)} className="w-full py-1.5 bg-slate-200 hover:bg-red-50 hover:text-red-600 hover:border-red-200 border border-transparent text-slate-700 rounded text-xs font-bold transition-colors">
                             Leave Session
                           </button>
                         );
                      }
                      if (isFull) {
                         return (
                           <button disabled className="w-full py-1.5 bg-slate-200 text-slate-500 rounded text-xs font-bold cursor-not-allowed">
                             Session Full
                           </button>
                         );
                      }
                      return (
                         <button onClick={(e) => handleJoin(activeEvent.id, false, e)} className="w-full py-1.5 bg-orange-500 hover:bg-orange-600 text-white rounded text-xs font-bold transition-colors shadow-sm">
                           Join Session
                         </button>
                      );
                    })()}
                  </div>
                </InfoWindow>
              )}
            </Map>
          </APIProvider>
        </div>

        {/* Right Sidebar */}
        <div className="w-80 bg-white border-l border-slate-200 flex flex-col overflow-hidden shrink-0 z-10">
          {activeEvent ? (
            /* Session Detail Panel */
            <div className="flex flex-col h-full">
              <div className="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
                <h2 className="text-sm text-slate-800 font-semibold">Session Details</h2>
                <button
                  onClick={() => setSelectedEventId(null)}
                  className="text-slate-400 hover:text-slate-600"
                >
                  <X size={16} />
                </button>
              </div>
              <div className="p-4 flex-1">
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full font-semibold">
                  {activeEvent.course}
                </span>
                <h3 className="text-slate-800 mt-2 mb-3 leading-snug font-bold text-[0.95rem]">
                  {activeEvent.title}
                </h3>
                <div className="space-y-2.5">
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <MapPin size={15} className="text-orange-500 shrink-0" />
                    {activeEvent.location}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <Clock size={15} className="text-blue-500 shrink-0" />
                    {activeEvent.date} at {activeEvent.time}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <Users size={15} className="text-blue-500 shrink-0" />
                    {activeEvent.attendees?.length || 1}/{activeEvent.maxParticipants} attending
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <BookOpen size={15} className="text-blue-500 shrink-0" />
                    Hosted by {activeEvent.host?.name || activeEvent.host?.id || "Student"}
                  </div>
                </div>

                <div className="mt-4 p-3 bg-blue-50 rounded-xl">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs text-blue-700 font-semibold">Capacity</span>
                    <span className="text-xs text-blue-600">{activeEvent.attendees?.length || 1}/{activeEvent.maxParticipants}</span>
                  </div>
                  <div className="bg-blue-200 rounded-full h-1.5">
                    <div
                      className="bg-blue-600 h-full rounded-full"
                      style={{ width: `${((activeEvent.attendees?.length || 1) / (activeEvent.maxParticipants || 10)) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </div>

              <div className="p-4 border-t border-slate-100 space-y-2">
                <button
                  onClick={() => handleNavigate(activeEvent.location)}
                  className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-xl text-sm font-semibold transition-colors flex items-center justify-center gap-2"
                >
                  <Navigation size={15} />
                  Navigate Here
                </button>
                <button
                  onClick={() => navigate(`/events/${activeEvent.id}`)}
                  className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm transition-colors flex items-center justify-center gap-2 font-medium"
                >
                  View Details
                  <ArrowRight size={14} />
                </button>
              </div>
            </div>
          ) : (
            /* Sessions List */
            <div className="flex flex-col h-full">
              <div className="px-4 py-3 border-b border-slate-100">
                <h2 className="text-sm text-slate-800 font-semibold">
                  Nearby Sessions
                  <span className="ml-2 text-xs bg-orange-100 text-orange-600 px-1.5 py-0.5 rounded-full">
                    {filteredSessions.length}
                  </span>
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
                      const coords = getCoords(s.location);
                      if (coords) setSelectedHubName(s.location.split(",")[0]);
                    }}
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-8 h-8 rounded-xl bg-orange-100 flex items-center justify-center shrink-0">
                        <MapPin size={16} className="text-orange-500" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 mb-0.5">
                          <span className="text-xs bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded font-semibold">
                            {s.course}
                          </span>
                        </div>
                        <p className="text-sm text-slate-700 truncate font-semibold">{s.title}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <span className="flex items-center gap-1 text-xs text-slate-400">
                            <Clock size={11} />{s.time}
                          </span>
                          <span className="flex items-center gap-1 text-xs text-slate-400">
                            <Users size={11} />{s.attendees?.length || 1}/{s.maxParticipants}
                          </span>
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
    </div>
  );
}