/* 
 * Events.tsx
 * Page for browsing, searching, and filtering study sessions.
 */

import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus, Search, Filter, CalendarDays, Clock, MapPin, Users,
  ChevronDown, Star
} from "lucide-react";
import { currentUser } from "../data/mockData";

const vibeColors: Record<string, string> = {
  "Quiet Focus": "bg-blue-50 text-blue-700",
  "Group Discussion": "bg-orange-50 text-orange-700",
  "Whiteboard Work": "bg-purple-50 text-purple-700",
  "Problem Solving": "bg-green-50 text-green-700",
  "Lecture Review": "bg-yellow-50 text-yellow-700",
  "Essay Writing": "bg-pink-50 text-pink-700",
  "Lab Work": "bg-cyan-50 text-cyan-700",
  "Exam Prep": "bg-red-50 text-red-700",
};

export default function Events() {
  // States for fetch methods
  const [events, setEvents] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [filterCourse, setFilterCourse] = useState("All");
  const [filterStatus, setFilterStatus] = useState("upcoming");
  const [joinedEvents, setJoinedEvents] = useState<string[]>([]);
  const [showFilters, setShowFilters] = useState(false);

  const uniqueCourses = ["All", ...Array.from(new Set(events.map((e) => e.course)))];

  const filtered = events.filter((ev) => {
    const matchSearch =
      ev.title.toLowerCase().includes(search.toLowerCase()) ||
      ev.course.toLowerCase().includes(search.toLowerCase()) ||
      ev.location.toLowerCase().includes(search.toLowerCase());
    const matchCourse = filterCourse === "All" || ev.course === filterCourse;
    const matchStatus = filterStatus === "all" || ev.status === filterStatus;
    return matchSearch && matchCourse && matchStatus;
  });

  // Fetch data from the API on mount
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/events");
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        // Temporary fix: Map the string 'host' from backend to an object for the frontend
        // TODO: Update backend to send host as an object through the Student class
        const formattedData = data.map((ev: any) => ({
          ...ev,
          host: typeof ev.host === 'string'
            ? { id: "unknown_id", name: ev.host, avatar: null }
            : ev.host
        }));

        setEvents(formattedData);
      } catch (err: any) {
        console.error("Failed to fetch events:", err);
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    fetchEvents();
  }, []);



  const formatDate = (d: string) =>
    new Date(d).toLocaleDateString("en-CA", { weekday: "short", month: "short", day: "numeric" });

  const handleJoin = (eventId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setJoinedEvents((prev) =>
      prev.includes(eventId) ? prev.filter((id) => id !== eventId) : [...prev, eventId]
    );
  };

  const isMyEvent = (ev: typeof events[0]) => ev.host.id === currentUser.id;
  const isJoined = (id: string) => joinedEvents.includes(id);

  return (
    <div className="p-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-slate-900" style={{ fontWeight: 700, fontSize: "1.35rem" }}>Study Sessions</h1>
          <p className="text-slate-500 text-sm mt-0.5">Find and join study sessions near you</p>
        </div>
        <button
          onClick={() => navigate("/events/create")}
          className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-4 py-2.5 rounded-xl text-sm transition-colors"
          style={{ fontWeight: 600 }}
        >
          <Plus size={16} />
          Host Session
        </button>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex gap-3 mb-4">
        <div className="flex-1 relative">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search sessions, courses, locations..."
            className="w-full pl-9 pr-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          />
        </div>
        <button
          onClick={() => setShowFilters(!showFilters)}
          className={`flex items-center gap-2 px-4 py-2.5 border rounded-xl text-sm transition-colors ${showFilters ? "bg-blue-50 border-blue-300 text-blue-700" : "border-slate-200 text-slate-600 hover:bg-slate-50"}`}
        >
          <Filter size={15} />
          Filters
          <ChevronDown size={14} className={`transition-transform ${showFilters ? "rotate-180" : ""}`} />
        </button>
      </div>

      {/* Filters Panel */}
      {showFilters && (
        <div className="bg-white border border-slate-200 rounded-xl p-4 mb-4 flex flex-wrap gap-4">
          <div>
            <label className="block text-xs text-slate-500 mb-1" style={{ fontWeight: 600 }}>Course</label>
            <div className="flex flex-wrap gap-1.5">
              {uniqueCourses.map((c) => (
                <button
                  key={c}
                  onClick={() => setFilterCourse(c)}
                  className={`px-3 py-1 rounded-lg text-xs transition-colors ${filterCourse === c ? "bg-blue-600 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                >
                  {c}
                </button>
              ))}
            </div>
          </div>
          <div>
            <label className="block text-xs text-slate-500 mb-1" style={{ fontWeight: 600 }}>Status</label>
            <div className="flex gap-1.5">
              {["upcoming", "past", "all"].map((s) => (
                <button
                  key={s}
                  onClick={() => setFilterStatus(s)}
                  className={`px-3 py-1 rounded-lg text-xs capitalize transition-colors ${filterStatus === s ? "bg-orange-500 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Status Tabs */}
      <div className="flex gap-1 mb-5 border-b border-slate-200">
        {["upcoming", "past", "all"].map((s) => (
          <button
            key={s}
            onClick={() => setFilterStatus(s)}
            className={`px-4 py-2.5 text-sm capitalize border-b-2 -mb-px transition-colors ${filterStatus === s ? "border-blue-600 text-blue-600" : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            style={{ fontWeight: filterStatus === s ? 600 : 400 }}
          >
            {s}
            <span className={`ml-1.5 text-xs px-1.5 py-0.5 rounded-full ${filterStatus === s ? "bg-blue-100 text-blue-600" : "bg-slate-100 text-slate-400"}`}>
              {events.filter((e) => s === "all" || e.status === s).length}
            </span>
          </button>
        ))}
      </div>

      {/* Events Grid */}
      {filtered.length === 0 ? (
        <div className="text-center py-16">
          <CalendarDays size={40} className="text-slate-300 mx-auto mb-3" />
          <p className="text-slate-500 text-sm">No sessions found</p>
          <button
            onClick={() => navigate("/events/create")}
            className="mt-3 text-blue-600 text-sm hover:underline"
          >
            Create the first one!
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filtered.map((ev) => (
            <div
              key={ev.id}
              className="bg-white rounded-xl border border-slate-200 hover:shadow-md transition-all cursor-pointer group"
              onClick={() => navigate(`/events/${ev.id}`)}
            >
              <div className="p-5">
                {/* Top Row */}
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1.5">
                      <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full" style={{ fontWeight: 600 }}>
                        {ev.course}
                      </span>
                      {ev.status === "past" && (
                        <span className="text-xs bg-slate-100 text-slate-500 px-2 py-0.5 rounded-full">Past</span>
                      )}
                      {isMyEvent(ev) && (
                        <span className="text-xs bg-orange-100 text-orange-600 px-2 py-0.5 rounded-full">Your event</span>
                      )}
                    </div>
                    <h3 className="text-slate-800 text-sm leading-snug" style={{ fontWeight: 600 }}>{ev.title}</h3>
                  </div>
                  <div className="flex items-center gap-1 text-slate-400 shrink-0">
                    <Star size={12} />
                    <span className="text-xs">{ev.reviews.length}</span>
                  </div>
                </div>

                {/* Details */}
                <div className="space-y-1.5 mb-3">
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <CalendarDays size={12} className="shrink-0 text-blue-400" />
                    {formatDate(ev.date)} at {ev.time}
                    <span className="text-slate-400">·</span>
                    <Clock size={12} className="shrink-0 text-blue-400" />
                    {ev.duration} min
                  </div>
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <MapPin size={12} className="shrink-0 text-orange-400" />
                    <span className="truncate">{ev.location}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <Users size={12} className="shrink-0 text-blue-400" />
                    <span>{ev.attendees.length}/{ev.maxParticipants} attending</span>
                    <div className="flex-1 bg-slate-100 rounded-full h-1.5 overflow-hidden">
                      <div
                        className="bg-blue-500 h-full rounded-full"
                        style={{ width: `${Math.min((ev.attendees.length / ev.maxParticipants) * 100, 100)}%` }}
                      ></div>
                    </div>
                  </div>
                </div>

                {/* Description */}
                <p className="text-xs text-slate-500 leading-relaxed line-clamp-2 mb-3">{ev.description}</p>

                {/* Tags */}
                <div className="flex flex-wrap gap-1.5 mb-4">
                  {ev.tags.map((tag: string) => (
                    <span key={tag} className={`text-xs px-2 py-0.5 rounded-full ${vibeColors[tag] || "bg-slate-100 text-slate-600"}`}>
                      {tag}
                    </span>
                  ))}
                </div>

                {/* Host & Action */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-full bg-blue-100 overflow-hidden">
                      {ev.host.avatar ? (
                        <img src={ev.host.avatar} alt={ev.host.name} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "10px", fontWeight: 700 }}>
                          {ev.host.name.charAt(0)}
                        </div>
                      )}
                    </div>
                    <span className="text-xs text-slate-500">by <span style={{ fontWeight: 600 }} className="text-slate-700">{ev.host.name}</span></span>
                  </div>

                  {ev.status === "upcoming" && (
                    <div className="flex items-center gap-2">

                      {/* Delete User's Own Event  */}
                      {ev.host.name == currentUser.name &&(
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          console.log(ev.host)
                          console.log(currentUser)
                        }}
                        className="px-4 py-1.5 rounded-lg text-xs bg-slate-100 text-slate-600 hover:bg-slate-200 bg-red-400 text-white transition-colors"
                        style={{ fontWeight: 600 }}
                      >
                        Cancel My Event
                      </button>
                      )}

                      {/* Join an event */}
                      <button
                        onClick={(e) => handleJoin(ev.id, e)}
                        className={`px-4 py-1.5 rounded-lg text-xs transition-colors ${isJoined(ev.id)
                          ? "bg-slate-100 text-slate-600 hover:bg-red-50 hover:text-red-600"
                          : "bg-blue-600 text-white hover:bg-blue-700"
                          }`}
                        style={{ fontWeight: 600 }}
                      >
                        {isJoined(ev.id) ? "Cancel" : "Join"}
                      </button>
                    </div>
                  )}
                  {isMyEvent(ev) && ev.status === "upcoming" && (
                    <button
                      onClick={(e) => { e.stopPropagation(); }}
                      className="px-4 py-1.5 rounded-lg text-xs bg-red-50 text-red-600 hover:bg-red-100 transition-colors"
                      style={{ fontWeight: 600 }}
                    >
                      Cancel Event
                    </button>
                  )}
                  {ev.status === "past" && (
                    <button
                      onClick={(e) => { e.stopPropagation(); navigate(`/events/${ev.id}`); }}
                      className="px-4 py-1.5 rounded-lg text-xs bg-orange-50 text-orange-600 hover:bg-orange-100 transition-colors"
                      style={{ fontWeight: 600 }}
                    >
                      Review
                    </button>
                  )}
                </div>
        </div>
      </div>
    ))}
</div>
      )}
    </div>
  );
}