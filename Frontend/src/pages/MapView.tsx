/* 
 * MapView.tsx
 * Interactive campus map showing building locations
 * TODO: Replace SVG map with Google Maps or OpenStreetMap.
 */

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  MapPin, Users, Clock, Navigation, Search, X, ZoomIn, ZoomOut,
  Locate, BookOpen, ArrowRight
} from "lucide-react";
import { mapSessions } from "../data/mockData";

// Campus building layout data
const buildings = [
  { id: "scott", name: "Scott Library", x: 45, y: 28, w: 16, h: 14, color: "#1E40AF" },
  { id: "vari", name: "Vari Hall", x: 32, y: 46, w: 14, h: 12, color: "#1E40AF" },
  { id: "lassonde", name: "Lassonde", x: 62, y: 32, w: 15, h: 12, color: "#1E40AF" },
  { id: "bennett", name: "Bennett Ctr", x: 60, y: 52, w: 14, h: 10, color: "#1E40AF" },
  { id: "central", name: "Central Sq.", x: 44, y: 44, w: 10, h: 10, color: "#374151" },
  { id: "tel", name: "TEL Bldg", x: 24, y: 33, w: 12, h: 10, color: "#1E40AF" },
  { id: "atkinson", name: "Atkinson", x: 22, y: 56, w: 12, h: 10, color: "#1E40AF" },
  { id: "rob", name: "Rob Intl", x: 72, y: 44, w: 12, h: 10, color: "#1E40AF" },
];

const paths = [
  { x1: 50, y1: 40, x2: 50, y2: 46 },
  { x1: 46, y1: 44, x2: 34, y2: 44 },
  { x1: 54, y1: 44, x2: 62, y2: 44 },
  { x1: 50, y1: 46, x2: 50, y2: 56 },
  { x1: 46, y1: 34, x2: 36, y2: 40 },
  { x1: 61, y1: 38, x2: 61, y2: 46 },
];

export default function MapView() {
  const navigate = useNavigate();
  const [selectedSession, setSelectedSession] = useState<typeof mapSessions[0] | null>(null);
  const [zoom, setZoom] = useState(1);
  const [search, setSearch] = useState("");
  const [navigating, setNavigating] = useState(false);
  const [navTarget, setNavTarget] = useState<string | null>(null);
  const [filterCourse, setFilterCourse] = useState("All");

  // Current user location (mock pin)
  const userLocation = { x: 50, y: 50 };

  const uniqueCourses = ["All", ...Array.from(new Set(mapSessions.map((s) => s.course)))];

  const filteredSessions = mapSessions.filter((s) => {
    const matchSearch =
      s.title.toLowerCase().includes(search.toLowerCase()) ||
      s.course.toLowerCase().includes(search.toLowerCase()) ||
      s.location.toLowerCase().includes(search.toLowerCase());
    const matchCourse = filterCourse === "All" || s.course === filterCourse;
    return matchSearch && matchCourse;
  });

  const handleNavigate = (session: typeof mapSessions[0]) => {
    setNavigating(true);
    setNavTarget(session.location);
    setTimeout(() => {
      setNavigating(false);
    }, 2000);
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
        <div className="flex gap-1.5">
          {uniqueCourses.map((c) => (
            <button
              key={c}
              onClick={() => setFilterCourse(c)}
              className={`px-3 py-1.5 rounded-lg text-xs transition-colors ${filterCourse === c ? "bg-blue-700 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
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
          {/* Zoom Controls */}
          <div className="absolute top-4 right-4 z-10 flex flex-col gap-1">
            <button
              onClick={() => setZoom((z) => Math.min(z + 0.2, 2))}
              className="w-9 h-9 bg-white rounded-xl shadow border border-slate-200 flex items-center justify-center hover:bg-slate-50 transition-colors"
            >
              <ZoomIn size={16} className="text-slate-600" />
            </button>
            <button
              onClick={() => setZoom((z) => Math.max(z - 0.2, 0.5))}
              className="w-9 h-9 bg-white rounded-xl shadow border border-slate-200 flex items-center justify-center hover:bg-slate-50 transition-colors"
            >
              <ZoomOut size={16} className="text-slate-600" />
            </button>
            <button
              onClick={() => setZoom(1)}
              className="w-9 h-9 bg-white rounded-xl shadow border border-slate-200 flex items-center justify-center hover:bg-slate-50 transition-colors"
              title="My Location"
            >
              <Locate size={16} className="text-blue-600" />
            </button>
          </div>

          {/* Navigation Banner */}
          {navigating && (
            <div className="absolute top-4 left-4 right-14 z-10 bg-blue-700 text-white rounded-xl px-4 py-3 flex items-center gap-3 shadow-lg">
              <Navigation size={18} className="animate-pulse" />
              <div>
                <p className="text-sm" style={{ fontWeight: 600 }}>Navigating to {navTarget}</p>
                <p className="text-xs text-blue-200">Estimated walk: ~3 min · 250m</p>
              </div>
              <button onClick={() => setNavigating(false)} className="ml-auto">
                <X size={16} className="text-blue-200" />
              </button>
            </div>
          )}

          {/* SVG Campus Map */}
          <svg
            viewBox="0 0 100 80"
            className="w-full h-full"
            style={{ transform: `scale(${zoom})`, transformOrigin: "center" }}
          >
            {/* Background */}
            <rect width="100" height="80" fill="#e8f0f7" />

            {/* Green spaces */}
            <ellipse cx="50" cy="40" rx="18" ry="14" fill="#c8e6c9" opacity="0.6" />
            <rect x="5" y="5" width="90" height="70" rx="4" fill="none" stroke="#cbd5e1" strokeWidth="0.5" />

            {/* Paths */}
            {paths.map((p, i) => (
              <line key={i} x1={p.x1} y1={p.y1} x2={p.x2} y2={p.y2} stroke="#94a3b8" strokeWidth="1.5" strokeLinecap="round" />
            ))}

            {/* Navigation path (if active) */}
            {navigating && (
              <line
                x1={userLocation.x}
                y1={userLocation.y}
                x2={mapSessions[0].x}
                y2={mapSessions[0].y}
                stroke="#1D4ED8"
                strokeWidth="1.5"
                strokeDasharray="2,1"
                opacity="0.8"
              />
            )}

            {/* Buildings */}
            {buildings.map((b) => (
              <g key={b.id}>
                <rect
                  x={b.x}
                  y={b.y}
                  width={b.w}
                  height={b.h}
                  rx={0.8}
                  fill={b.color}
                  fillOpacity={0.15}
                  stroke={b.color}
                  strokeWidth={0.5}
                />
                <text
                  x={b.x + b.w / 2}
                  y={b.y + b.h / 2 + 0.5}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fontSize="1.8"
                  fill="#1e3a8a"
                  fontWeight="600"
                >
                  {b.name}
                </text>
              </g>
            ))}

            {/* "York University" label */}
            <text x="50" y="8" textAnchor="middle" fontSize="3" fill="#64748b" fontWeight="700">
              York University · Keele Campus
            </text>

            {/* User's current location */}
            <g>
              <circle cx={userLocation.x} cy={userLocation.y} r="2.5" fill="#1D4ED8" opacity="0.2" className="animate-pulse" />
              <circle cx={userLocation.x} cy={userLocation.y} r="1.5" fill="#1D4ED8" />
              <circle cx={userLocation.x} cy={userLocation.y} r="0.8" fill="white" />
            </g>

            {/* Session Pins */}
            {filteredSessions.map((s) => {
              const isSelected = selectedSession?.id === s.id;
              return (
                <g
                  key={s.id}
                  className="cursor-pointer"
                  onClick={() => setSelectedSession(isSelected ? null : s)}
                  style={{ filter: isSelected ? "drop-shadow(0 2px 4px rgba(0,0,0,0.3))" : "" }}
                >
                  <circle cx={s.x} cy={s.y} r={isSelected ? 4 : 3} fill={isSelected ? "#EA580C" : "#F97316"} />
                  <circle cx={s.x} cy={s.y} r={isSelected ? 4 : 3} fill="none" stroke="white" strokeWidth="0.8" />
                  <text
                    x={s.x}
                    y={s.y - 4.5}
                    textAnchor="middle"
                    fontSize="1.6"
                    fill="#1e3a8a"
                    fontWeight="700"
                    style={{ pointerEvents: "none" }}
                  >
                    {s.course}
                  </text>
                </g>
              );
            })}
          </svg>

          {/* Map Legend */}
          <div className="absolute bottom-4 left-4 bg-white/90 backdrop-blur-sm rounded-xl border border-slate-200 px-3 py-2.5 shadow">
            <p className="text-xs text-slate-500 mb-1.5" style={{ fontWeight: 600 }}>Legend</p>
            <div className="flex flex-col gap-1">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-blue-700"></div>
                <span className="text-xs text-slate-600">You</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-orange-500"></div>
                <span className="text-xs text-slate-600">Study Session</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded bg-blue-900 opacity-20 border border-blue-900 border-opacity-50"></div>
                <span className="text-xs text-slate-600">Building</span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Sidebar */}
        <div className="w-80 bg-white border-l border-slate-200 flex flex-col overflow-hidden shrink-0">
          {selectedSession ? (
            /* Session Detail Panel */
            <div className="flex flex-col h-full">
              <div className="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
                <h2 className="text-sm text-slate-800" style={{ fontWeight: 600 }}>Session Details</h2>
                <button
                  onClick={() => setSelectedSession(null)}
                  className="text-slate-400 hover:text-slate-600"
                >
                  <X size={16} />
                </button>
              </div>
              <div className="p-4 flex-1">
                <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full" style={{ fontWeight: 600 }}>
                  {selectedSession.course}
                </span>
                <h3 className="text-slate-800 mt-2 mb-3 leading-snug" style={{ fontWeight: 700, fontSize: "0.95rem" }}>
                  {selectedSession.title}
                </h3>
                <div className="space-y-2.5">
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <MapPin size={15} className="text-orange-500 shrink-0" />
                    {selectedSession.location}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <Clock size={15} className="text-blue-500 shrink-0" />
                    Today at {selectedSession.time} · {selectedSession.duration} min
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <Users size={15} className="text-blue-500 shrink-0" />
                    {selectedSession.attendees}/{selectedSession.maxAttendees} attending
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600">
                    <BookOpen size={15} className="text-blue-500 shrink-0" />
                    Hosted by {selectedSession.host}
                  </div>
                </div>

                <div className="mt-4 p-3 bg-blue-50 rounded-xl">
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs text-blue-700" style={{ fontWeight: 600 }}>Capacity</span>
                    <span className="text-xs text-blue-600">{selectedSession.attendees}/{selectedSession.maxAttendees}</span>
                  </div>
                  <div className="bg-blue-200 rounded-full h-1.5">
                    <div
                      className="bg-blue-600 h-full rounded-full"
                      style={{ width: `${(selectedSession.attendees / selectedSession.maxAttendees) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </div>

              <div className="p-4 border-t border-slate-100 space-y-2">
                <button
                  onClick={() => handleNavigate(selectedSession)}
                  className="w-full py-2.5 bg-blue-700 hover:bg-blue-800 text-white rounded-xl text-sm transition-colors flex items-center justify-center gap-2"
                  style={{ fontWeight: 600 }}
                >
                  <Navigation size={15} />
                  Navigate Here
                </button>
                <button
                  onClick={() => navigate(`/events/${selectedSession.eventId}`)}
                  className="w-full py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm transition-colors flex items-center justify-center gap-2"
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
                <h2 className="text-sm text-slate-800" style={{ fontWeight: 600 }}>
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
                    onClick={() => setSelectedSession(s)}
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-8 h-8 rounded-xl bg-orange-100 flex items-center justify-center shrink-0">
                        <MapPin size={16} className="text-orange-500" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 mb-0.5">
                          <span className="text-xs bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded" style={{ fontWeight: 600 }}>
                            {s.course}
                          </span>
                        </div>
                        <p className="text-sm text-slate-700 truncate" style={{ fontWeight: 600 }}>{s.title}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <span className="flex items-center gap-1 text-xs text-slate-400">
                            <Clock size={11} />{s.time}
                          </span>
                          <span className="flex items-center gap-1 text-xs text-slate-400">
                            <Users size={11} />{s.attendees}/{s.maxAttendees}
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
                    <p className="text-sm text-slate-400">No sessions found</p>
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