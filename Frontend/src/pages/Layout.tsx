import { useState, useEffect } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import {
  LayoutDashboard, User, CalendarDays, MapPin, MessageSquare,
  Users, Settings, Bell, BookOpen, LogOut, ChevronLeft, ChevronRight,
  X
} from "lucide-react";

const navItems = [
  { to: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/profile", icon: User, label: "My Profile" },
  { to: "/events", icon: CalendarDays, label: "Events" },
  { to: "/map", icon: MapPin, label: "Campus Map" },
  { to: "/chat", icon: MessageSquare, label: "Chat" },
  { to: "/network", icon: Users, label: "My Network" },
];

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();

  const [student, setStudent] = useState<any>(null);

  const handleLogout = () => {
    localStorage.removeItem("studyBuddyToken");
    navigate("/");
  };

  useEffect(() => {
    async function fetchUser() {
      try {
        const token = localStorage.getItem("studyBuddyToken");
        if (!token) return;
        
        const res = await fetch("/api/studentcontroller/profile", {
          headers: { "Authorization": "Bearer " + token }
        });
        
        if (res.ok) {
          const data = await res.json();
          setStudent(data);
        }
      } catch (err) {
        console.error("Failed to load user for layout", err);
      }
    }
    fetchUser();
  }, []);

  const formatTime = (ts: string) => {
    const d = new Date(ts);
    return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const notifIcon = (type: string) => {
    if (type === "connection_request") return <Users size={14} className="text-blue-600" />;
    if (type === "chat") return <MessageSquare size={14} className="text-orange-500" />;
    return <CalendarDays size={14} className="text-blue-600" />;
  };

  if (!student) {
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      {/* Sidebar */}
      <aside
        className={`relative flex flex-col bg-blue-900 text-white transition-all duration-300 ${
          collapsed ? "w-16" : "w-60"
        } shrink-0`}
      >
        {/* Logo */}
        <div className={`flex items-center gap-3 px-4 py-5 border-b border-blue-800 ${collapsed ? "justify-center" : ""}`}>
          <div className="w-9 h-9 rounded-xl bg-orange-500 flex items-center justify-center shrink-0">
            <BookOpen size={20} className="text-white" />
          </div>
          {!collapsed && (
            <span className="text-white tracking-tight" style={{ fontSize: "1.1rem", fontWeight: 700 }}>
              StudyBuddy
            </span>
          )}
        </div>

        {/* Nav Items */}
        <nav className="flex-1 py-4 px-2 space-y-1 overflow-y-auto">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all group ${
                  isActive
                    ? "bg-orange-500 text-white"
                    : "text-blue-200 hover:bg-blue-800 hover:text-white"
                } ${collapsed ? "justify-center" : ""}`
              }
              title={collapsed ? label : undefined}
            >
              {({ isActive }) => (
                <>
                  <Icon size={20} className={isActive ? "text-white" : "text-blue-300 group-hover:text-white"} />
                  {!collapsed && <span className="text-sm truncate">{label}</span>}
                 
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* User & Logout */}
        <div className={`border-t border-blue-800 px-3 py-3 flex items-center gap-3 ${collapsed ? "justify-center" : ""}`}>
          <div className="w-8 h-8 rounded-full bg-blue-600 shrink-0 overflow-hidden">
            <img src={student.avatar} alt="avatar" className="w-full h-full object-cover" />
          </div>
          {!collapsed && (
            <div className="flex-1 min-w-0">
              <p className="text-xs text-white truncate" style={{ fontWeight: 600 }}>{student.fullName}</p>
              <p className="text-xs text-blue-300 truncate">{student.program}</p>
            </div>
          )}
          {!collapsed && (
            <button onClick={handleLogout} className="text-blue-300 hover:text-red-400 transition-colors" title="Logout">
              <LogOut size={16} />
            </button>
          )}
        </div>

        {/* Collapse toggle */}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="absolute -right-3 top-1/2 -translate-y-1/2 w-6 h-6 bg-blue-700 border-2 border-blue-900 rounded-full flex items-center justify-center hover:bg-orange-500 transition-colors z-10"
        >
          {collapsed ? <ChevronRight size={12} className="text-white" /> : <ChevronLeft size={12} className="text-white" />}
        </button>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Bar */}
        <header className="bg-white border-b border-slate-200 px-6 py-3 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-green-500"></div>
            <span className="text-sm text-slate-500">
              {student.isOnline ? `Online · ${student.location}` : "Offline"}
            </span>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto flex flex-col">
          <Outlet />
        </main>
      </div>
    </div>
  );
}