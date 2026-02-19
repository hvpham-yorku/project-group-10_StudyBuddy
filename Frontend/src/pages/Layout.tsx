import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import {
  LayoutDashboard, User, CalendarDays, MapPin, MessageSquare,
  Users, Settings, Bell, BookOpen, LogOut, ChevronLeft, ChevronRight,
  X
} from "lucide-react";
import { currentUser, notifications } from "../data/mockData";

const navItems = [
  { to: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { to: "/profile", icon: User, label: "My Profile" },
  { to: "/events", icon: CalendarDays, label: "Events" },
  { to: "/map", icon: MapPin, label: "Campus Map" },
  { to: "/chat", icon: MessageSquare, label: "Chat" },
  { to: "/network", icon: Users, label: "My Network" },
  { to: "/settings", icon: Settings, label: "Settings" },
];

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const navigate = useNavigate();
  const unreadCount = notifications.filter((n) => !n.read).length;

  const handleLogout = () => {
    navigate("/");
  };

  const formatTime = (ts: string) => {
    const d = new Date(ts);
    return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const notifIcon = (type: string) => {
    if (type === "connection_request") return <Users size={14} className="text-blue-600" />;
    if (type === "chat") return <MessageSquare size={14} className="text-orange-500" />;
    return <CalendarDays size={14} className="text-blue-600" />;
  };

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
                  {!collapsed && label === "Chat" && unreadCount > 0 && (
                    <span className="ml-auto bg-orange-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                      {unreadCount}
                    </span>
                  )}
                </>
              )}
            </NavLink>
          ))}
        </nav>

        {/* User & Logout */}
        <div className={`border-t border-blue-800 px-3 py-3 flex items-center gap-3 ${collapsed ? "justify-center" : ""}`}>
          <div className="w-8 h-8 rounded-full bg-blue-600 shrink-0 overflow-hidden">
            <img src={currentUser.avatar} alt="avatar" className="w-full h-full object-cover" />
          </div>
          {!collapsed && (
            <div className="flex-1 min-w-0">
              <p className="text-xs text-white truncate" style={{ fontWeight: 600 }}>{currentUser.name}</p>
              <p className="text-xs text-blue-300 truncate">{currentUser.major}</p>
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
              {currentUser.isOnline ? `Online · ${currentUser.location}` : "Offline"}
            </span>
          </div>
          <div className="flex items-center gap-3">
            {/* Notifications */}
            <div className="relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center hover:bg-slate-200 transition-colors"
              >
                <Bell size={18} className="text-slate-600" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 w-4 h-4 bg-orange-500 rounded-full text-white flex items-center justify-center" style={{ fontSize: "10px" }}>
                    {unreadCount}
                  </span>
                )}
              </button>

              {showNotifications && (
                <div className="absolute right-0 top-11 w-80 bg-white rounded-xl shadow-xl border border-slate-200 z-50">
                  <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
                    <span className="text-sm" style={{ fontWeight: 600 }}>Notifications</span>
                    <button onClick={() => setShowNotifications(false)}>
                      <X size={16} className="text-slate-400" />
                    </button>
                  </div>
                  <div className="max-h-72 overflow-y-auto">
                    {notifications.map((n) => (
                      <div key={n.id} className={`px-4 py-3 border-b border-slate-50 hover:bg-slate-50 cursor-pointer flex gap-3 items-start ${!n.read ? "bg-blue-50/60" : ""}`}>
                        <div className="w-6 h-6 rounded-full bg-white border border-slate-200 flex items-center justify-center shrink-0 mt-0.5">
                          {notifIcon(n.type)}
                        </div>
                        <div className="flex-1">
                          <p className="text-xs text-slate-700 leading-relaxed">{n.message}</p>
                          <p className="text-xs text-slate-400 mt-0.5">{formatTime(n.timestamp)}</p>
                        </div>
                        {!n.read && <div className="w-2 h-2 rounded-full bg-orange-500 shrink-0 mt-1.5"></div>}
                      </div>
                    ))}
                  </div>
                  <div className="px-4 py-2 text-center">
                    <button className="text-xs text-blue-600 hover:underline">Mark all as read</button>
                  </div>
                </div>
              )}
            </div>

            <div className="w-9 h-9 rounded-full overflow-hidden border-2 border-blue-600">
              <img src={currentUser.avatar} alt="avatar" className="w-full h-full object-cover" />
            </div>
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