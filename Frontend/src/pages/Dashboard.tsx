/* 
 * Dashboard.tsx
 * This component acts as the main for the user once they login.
 */

import { useNavigate } from "react-router-dom";
import { CalendarDays, Clock, Users, MapPin, Plus, ArrowRight, BookOpen, Star, TrendingUp } from "lucide-react";

// TODO: Make sure to replace hardcoded values with actual calls to springboot backend.
import { currentUser, events, connections, sessionHistory, notifications } from "../data/mockData";

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
  const upcomingEvents = events.filter((e) => e.status === "upcoming").slice(0, 3);
  const recentSessions = sessionHistory.slice(0, 3);
  const onlineConnections = connections.filter((c) => c.isOnline);

  const formatDate = (d: string) => {
    const date = new Date(d);
    return date.toLocaleDateString("en-CA", { weekday: "short", month: "short", day: "numeric" });
  };

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
              {currentUser.name}
            </h1>
            <p className="text-blue-200 text-sm mt-1">{currentUser.major} · {currentUser.year}</p>
            <div className="flex items-center gap-2 mt-3">
              <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
              <span className="text-blue-100 text-xs">{currentUser.location}</span>
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
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <StatCard icon={BookOpen} label="Study Hours" value={`${currentUser.totalStudyHours}h`} color="bg-blue-600" />
        <StatCard icon={CalendarDays} label="Sessions" value={currentUser.totalSessions} color="bg-orange-500" />
        <StatCard icon={Users} label="Connections" value={connections.length} color="bg-blue-700" />
        <StatCard icon={Star} label="Courses" value={currentUser.courses.length} color="bg-orange-600" />
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
                      <span className="text-xs text-slate-400">{ev.tags[0]}</span>
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
                    <div className="text-xs text-slate-400">{ev.attendees.length}/{ev.maxParticipants}</div>
                    <div className="text-xs text-slate-400">attendees</div>
                    <ArrowRight size={14} className="text-slate-300 group-hover:text-blue-500 transition-colors ml-auto mt-1" />
                  </div>
                </div>
              </div>
            ))}
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
              {onlineConnections.map((c) => (
                <div key={c.id} className="flex items-center gap-3">
                  <div className="relative">
                    <div className="w-8 h-8 rounded-full bg-blue-100 overflow-hidden">
                      {c.avatar ? (
                        <img src={c.avatar} alt={c.name} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontWeight: 700, fontSize: "0.875rem" }}>
                          {c.name.charAt(0)}
                        </div>
                      )}
                    </div>
                    <div className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-green-500 border-2 border-white"></div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs text-slate-700 truncate" style={{ fontWeight: 600 }}>{c.name}</p>
                    <p className="text-xs text-slate-400 truncate">{c.location || c.status}</p>
                  </div>
                  <button
                    onClick={() => navigate("/chat")}
                    className="text-xs bg-blue-50 hover:bg-blue-100 text-blue-600 px-2 py-1 rounded-lg transition-colors"
                  >
                    Chat
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Recent Sessions */}
          <div className="bg-white rounded-xl border border-slate-200">
            <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
              <h3 className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>Recent Sessions</h3>
              <button onClick={() => navigate("/sessions")} className="text-xs text-blue-600 hover:underline flex items-center gap-1">
                Log <ArrowRight size={12} />
              </button>
            </div>
            <div className="px-4 py-2 space-y-2.5">
              {recentSessions.map((s) => (
                <div key={s.id} className="flex items-start gap-2.5">
                  <div className="w-1.5 h-1.5 rounded-full bg-orange-400 shrink-0 mt-1.5"></div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs text-slate-700 truncate" style={{ fontWeight: 600 }}>{s.title}</p>
                    <p className="text-xs text-slate-400">{formatDate(s.date)} · {s.duration}min</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Study Streak */}
          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-4 text-white">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp size={18} />
              <span className="text-sm" style={{ fontWeight: 600 }}>Study Streak</span>
            </div>
            <p style={{ fontSize: "2rem", fontWeight: 700, lineHeight: 1 }}>7 days</p>
            <p className="text-orange-100 text-xs mt-1">Keep it up! You're on a roll 🔥</p>
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
          {currentUser.courses.map((course) => (
            <span
              key={course}
              className="px-3 py-1.5 bg-blue-50 text-blue-700 rounded-lg text-sm border border-blue-200 cursor-pointer hover:bg-blue-100 transition-colors"
              style={{ fontWeight: 500 }}
            >
              {course}
            </span>
          ))}
          <button
            onClick={() => navigate("/profile")}
            className="px-3 py-1.5 border-2 border-dashed border-slate-200 text-slate-400 rounded-lg text-sm hover:border-blue-400 hover:text-blue-600 transition-colors flex items-center gap-1"
          >
            <Plus size={13} /> Add course
          </button>
        </div>
      </div>
    </div>
  );
}