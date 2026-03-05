/*
 * Network.tsx
 * Manage user's network (REAL backend + Firebase Auth UID)
 */

import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Search,
  Users,
  UserCheck,
  UserPlus,
  MessageSquare,
  CalendarPlus,
  X,
  Check,
  BookOpen,
} from "lucide-react";
import { useAuthUid } from "../hooks/useAuthUid"; // ✅ auth-ready + uid

type Connection = {
  userId: string;
  fullName: string | null;
  program: string | null;
  profilePic: string | null;
  courses: string[];
  lastActiveAt: number | null;
  activityStatus?: "online" | "idle" | "offline";
};

type PendingRequest = {
  id: string;
  name: string;
  major: string;
  year: string;
  mutualConnections: number;
  courses: string[];
  studyVibes: string[];
};

const vibeColors: Record<string, string> = {
  "Quiet Focus": "bg-blue-50 text-blue-600",
  "Group Discussion": "bg-orange-50 text-orange-600",
  "Whiteboard Work": "bg-purple-50 text-purple-600",
  "Problem Solving": "bg-green-50 text-green-600",
};

// ✅ Production-ready: use relative base (works in Docker + same-origin)
const API_BASE = "";

async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`);
  if (!res.ok) throw new Error(`API error ${res.status}`);
  return (await res.json()) as T;
}

async function apiPost(path: string): Promise<void> {
  const res = await fetch(`${API_BASE}${path}`, { method: "POST" });
  if (!res.ok) throw new Error(`API error ${res.status}`);
}

function computePresence(c: Connection) {
  // Prefer backend-provided status if present
  if (c.activityStatus === "online") return { color: "bg-green-500", label: "Online", isOnline: true };
  if (c.activityStatus === "idle") return { color: "bg-yellow-400", label: "Idle", isOnline: false };
  if (c.activityStatus === "offline") return { color: "bg-slate-300", label: "Offline", isOnline: false };

  // Fallback: compute from lastActiveAt
  const last = c.lastActiveAt;
  if (!last) return { color: "bg-slate-300", label: "Offline", isOnline: false };

  const diff = Date.now() - last;
  if (diff <= 2 * 60 * 1000) return { color: "bg-green-500", label: "Online", isOnline: true };
  if (diff <= 10 * 60 * 1000) return { color: "bg-yellow-400", label: "Idle", isOnline: false };
  return { color: "bg-slate-300", label: "Offline", isOnline: false };
}

export default function Network() {
  const navigate = useNavigate();

  // ✅ Firebase Auth UID wiring
  // const { uid, authReady } = useAuthUid();
  const authReady = true;
  const uid = "testUser123";

  // ✅ quick sanity log (remove later if you want)
  console.log("authReady:", authReady, "uid:", uid);

  const [search, setSearch] = useState("");
  const [activeTab, setActiveTab] = useState<"connections" | "requests" | "find">("connections");

  // Backend connections
  const [connections, setConnections] = useState<Connection[]>([]);
  const [loading, setLoading] = useState(true);

  // Requests tab (still UI-only for now)
  const [pendingRequests] = useState<PendingRequest[]>([]);
  const [accepted, setAccepted] = useState<string[]>([]);
  const [declined, setDeclined] = useState<string[]>([]);

  // Keep track of users
  const [availableUsers, setAvailableUsers] = useState<Connection[]>([]);

  const [inviteModal, setInviteModal] = useState<string | null>(null);

  // Handle connections
  const handleConnect = async (targetId: string) => {
    try {
      await fetch(`/api/connections/request`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ myUserId: uid, targetUserId: targetId })
      });
      alert("Connection request sent!");
    } catch (e) {
      console.error("Failed to send request", e);
    }
  };

  // ✅ Load connections from backend ONLY after auth is ready, using UID
  useEffect(() => {
    if (!authReady) return;

    // Not logged in
    if (!uid) {
      setConnections([]);
      setLoading(false);
      return;
    }

    let cancelled = false;

    const load = async () => {
      setLoading(true);
      try {
        // Update my own presence
        await apiPost(`/api/presence/heartbeat?userId=${encodeURIComponent(uid)}`);

        // Fetch existing connections AND available users
        const data = await apiGet<Connection[]>(
          `/api/connections?userId=${encodeURIComponent(uid)}`
        );
        const availableData = await apiGet<Connection[]>(
          `/api/connections/available?userId=${encodeURIComponent(uid)}`
        );

        if (cancelled) return;

        setConnections(
          (data ?? []).map((c) => ({
            ...c,
            courses: c.courses ?? [],
          }))
        );

        // Save the available users to the state variable you already created
        setAvailableUsers(
          (availableData ?? []).map((c) => ({
            ...c,
            courses: c.courses ?? [],
          }))
        );

      } catch (e) {
        console.error(e);
        if (!cancelled) setConnections([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();

    // Keep heartbeat alive while logged in
    const id = window.setInterval(() => {
      apiPost(`/api/presence/heartbeat?userId=${encodeURIComponent(uid)}`).catch(() => { });
    }, 45_000);

    return () => {
      cancelled = true;
      window.clearInterval(id);
    };
  }, [authReady, uid]);

  const filteredConnections = useMemo(() => {
    const q = search.toLowerCase().trim();

    return connections.filter((c) => {
      const name = (c.fullName ?? c.userId).toLowerCase();
      const program = (c.program ?? "").toLowerCase();
      const courses = (c.courses ?? []).some((co) => co.toLowerCase().includes(q));

      if (!q) return true;
      return name.includes(q) || program.includes(q) || courses;
    });
  }, [connections, search]);

  const pendingFiltered = pendingRequests.filter((r) => !accepted.includes(r.id) && !declined.includes(r.id));

  const onlineNow = filteredConnections.filter((c) => computePresence(c).isOnline);
  const offlineNow = filteredConnections.filter((c) => !computePresence(c).isOnline);

  const getStatusColor = (c: Connection) => computePresence(c).color;
  const getStatusLabel = (c: Connection) => computePresence(c).label;


  // Optional: show auth loading state (keeps UI deterministic)
  if (!authReady) {
    return (
      <div className="p-6 max-w-4xl mx-auto">
        <div className="text-center py-12">
          <Users size={36} className="text-slate-200 mx-auto mb-3" />
          <p className="text-slate-400 text-sm">Loading account...</p>
        </div>
      </div>
    );
  }

  // Optional: user not signed in
  if (!uid) {
    return (
      <div className="p-6 max-w-4xl mx-auto">
        <div className="text-center py-12">
          <Users size={36} className="text-slate-200 mx-auto mb-3" />
          <p className="text-slate-500 text-sm">Please sign in to view your network.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div>
          <h1 className="text-slate-900" style={{ fontWeight: 700, fontSize: "1.35rem" }}>
            My Network
          </h1>
          <p className="text-slate-500 text-sm mt-0.5">
            {loading ? "Loading..." : `${connections.length} connections`}
          </p>
        </div>
      </div>

      {/* Search & Tabs */}
      <div className="relative mb-4">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, program, or course..."
          className="w-full pl-9 pr-4 py-2.5 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
        />
      </div>

      <div className="flex gap-1 border-b border-slate-200 mb-5">
        {(["connections", "requests", "find"] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2.5 text-sm capitalize border-b-2 -mb-px transition-colors flex items-center gap-2 ${activeTab === tab
                ? "border-blue-600 text-blue-600"
                : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            style={{ fontWeight: activeTab === tab ? 600 : 400 }}
          >
            {tab === "connections" && <UserCheck size={14} />}
            {tab === "requests" && <UserPlus size={14} />}
            {tab === "find" && <Search size={14} />}
            {tab === "connections" ? "My Connections" : tab === "requests" ? "Requests" : "Find People"}
            {tab === "requests" && pendingFiltered.length > 0 && (
              <span className="bg-orange-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {pendingFiltered.length}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Connections Tab */}
      {activeTab === "connections" && (
        <div className="space-y-3">
          {loading ? (
            <div className="text-center py-12">
              <Users size={36} className="text-slate-200 mx-auto mb-3" />
              <p className="text-slate-400 text-sm">Loading connections...</p>
            </div>
          ) : (
            <>
              {/* Online header */}
              {onlineNow.length > 0 && (
                <>
                  <p className="text-xs text-slate-400 uppercase tracking-wide px-1" style={{ fontWeight: 600 }}>
                    Online Now ({onlineNow.length})
                  </p>
                  {onlineNow.map((c) => (
                    <ConnectionCard
                      key={c.userId}
                      connection={c}
                      navigate={navigate}
                      setInviteModal={setInviteModal}
                      getStatusColor={getStatusColor}
                      getStatusLabel={getStatusLabel}
                    />
                  ))}
                  <p className="text-xs text-slate-400 uppercase tracking-wide px-1 mt-4" style={{ fontWeight: 600 }}>
                    Offline ({offlineNow.length})
                  </p>
                </>
              )}

              {offlineNow.map((c) => (
                <ConnectionCard
                  key={c.userId}
                  connection={c}
                  navigate={navigate}
                  setInviteModal={setInviteModal}
                  getStatusColor={getStatusColor}
                  getStatusLabel={getStatusLabel}
                />
              ))}

              {filteredConnections.length === 0 && (
                <div className="text-center py-12">
                  <Users size={36} className="text-slate-200 mx-auto mb-3" />
                  <p className="text-slate-400 text-sm">No connections found</p>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* Requests Tab (UI placeholder) */}
      {activeTab === "requests" && (
        <div className="space-y-3">
          {pendingFiltered.length === 0 ? (
            <div className="text-center py-12">
              <UserPlus size={36} className="text-slate-200 mx-auto mb-3" />
              <p className="text-slate-400 text-sm">No pending requests</p>
              <p className="text-xs text-slate-400 mt-2">
                (Requests aren’t wired to backend yet — this tab is UI-only for now.)
              </p>
            </div>
          ) : (
            pendingFiltered.map((r) => (
              <div key={r.id} className="bg-white rounded-xl border border-slate-200 p-4">
                <div className="flex items-start gap-4">
                  <div
                    className="w-11 h-11 rounded-xl bg-blue-100 flex items-center justify-center shrink-0"
                    style={{ fontWeight: 700, color: "#1D4ED8" }}
                  >
                    {r.name.charAt(0)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>
                      {r.name}
                    </p>
                    <p className="text-xs text-slate-500">
                      {r.major} · {r.year}
                    </p>
                    <p className="text-xs text-slate-400 mt-1">{r.mutualConnections} mutual connections</p>
                    <div className="flex flex-wrap gap-1 mt-2">
                      {r.courses.slice(0, 3).map((c) => (
                        <span
                          key={c}
                          className="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded"
                          style={{ fontWeight: 500 }}
                        >
                          {c}
                        </span>
                      ))}
                    </div>
                    <div className="flex flex-wrap gap-1 mt-1">
                      {r.studyVibes.map((v) => (
                        <span
                          key={v}
                          className={`text-xs px-2 py-0.5 rounded ${vibeColors[v] || "bg-slate-50 text-slate-500"}`}
                        >
                          {v}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div className="flex flex-col gap-2 shrink-0">
                    <button
                      onClick={() => setAccepted((prev) => [...prev, r.id])}
                      className="px-4 py-1.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-xs transition-colors flex items-center gap-1"
                      style={{ fontWeight: 600 }}
                    >
                      <Check size={12} />
                      Accept
                    </button>
                    <button
                      onClick={() => setDeclined((prev) => [...prev, r.id])}
                      className="px-4 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-lg text-xs transition-colors flex items-center gap-1"
                    >
                      <X size={12} />
                      Decline
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}

          {/* Show accepted notices */}
          {accepted.map((id) => {
            const r = pendingRequests.find((p) => p.id === id);
            if (!r) return null;
            return (
              <div key={id} className="bg-green-50 border border-green-200 rounded-xl p-3 flex items-center gap-3">
                <Check size={16} className="text-green-600" />
                <p className="text-sm text-green-700">
                  You're now connected with <span style={{ fontWeight: 600 }}>{r.name}</span>
                </p>
              </div>
            );
          })}
        </div>
      )}

      {/* Find People Tab */}
      {activeTab === "find" && (
        <div className="space-y-3">
          {availableUsers.map((c) => {
            const displayName = c.fullName ?? c.userId;
            return (
              <div key={c.userId} className="bg-white rounded-xl border border-slate-200 p-4 flex items-start gap-4">
                <div className="w-11 h-11 rounded-xl overflow-hidden bg-blue-100 shrink-0">
                  {c.profilePic ? (
                    <img src={c.profilePic} alt={displayName} className="w-full h-full object-cover" />
                  ) : (
                    <div
                      className="w-full h-full flex items-center justify-center text-blue-600"
                      style={{ fontWeight: 700 }}
                    >
                      {displayName.charAt(0)}
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>
                    {displayName}
                  </p>
                  <p className="text-xs text-slate-500">{c.program ?? ""}</p>
                </div>
                <button
                  onClick={() => handleConnect(c.userId)}
                  className="px-4 py-1.5 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-xs transition-colors flex items-center gap-1 shrink-0"
                  style={{ fontWeight: 600 }}
                >
                  <UserPlus size={12} />
                  Connect
                </button>
              </div>
            );
          })}
        </div>
      )}

      {/* Invite Modal */}
      {inviteModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl w-full max-w-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-slate-800 text-sm" style={{ fontWeight: 700 }}>
                Invite to Session
              </h3>
              <button onClick={() => setInviteModal(null)}>
                <X size={18} className="text-slate-400" />
              </button>
            </div>

            <p className="text-xs text-slate-500 mb-4">Select a session to invite this person to:</p>

            <div className="space-y-2 mb-4">
              {[
                "EECS 3311 Design Patterns · Feb 21, 2:00 PM",
                "MATH 2030 Midterm Prep · Feb 22, 10:00 AM",
                "EECS 4080 Final Project · Feb 25, 3:00 PM",
              ].map((session) => (
                <label
                  key={session}
                  className="flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer hover:bg-slate-50"
                >
                  <input type="radio" name="session" className="accent-blue-600" />
                  <span className="text-sm text-slate-700">{session}</span>
                </label>
              ))}
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setInviteModal(null)}
                className="flex-1 py-2.5 border border-slate-200 text-slate-600 rounded-xl text-sm hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                onClick={() => setInviteModal(null)}
                className="flex-1 py-2.5 bg-blue-700 text-white rounded-xl text-sm hover:bg-blue-800"
                style={{ fontWeight: 600 }}
              >
                Send Invite
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ConnectionCard({
  connection,
  navigate,
  setInviteModal,
  getStatusColor,
  getStatusLabel,
}: {
  connection: Connection;
  navigate: (path: string) => void;
  setInviteModal: (id: string) => void;
  getStatusColor: (c: Connection) => string;
  getStatusLabel: (c: Connection) => string;
}) {
  const displayName = connection.fullName ?? connection.userId;
  const program = connection.program ?? "";

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-4 hover:shadow-sm transition-shadow">
      <div className="flex items-start gap-4">
        <div className="relative shrink-0">
          <div className="w-11 h-11 rounded-xl overflow-hidden bg-blue-100">
            {connection.profilePic ? (
              <img src={connection.profilePic} alt={displayName} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontWeight: 700 }}>
                {displayName.charAt(0)}
              </div>
            )}
          </div>
          <div
            className={`absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-white ${getStatusColor(
              connection
            )}`}
          ></div>
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <p className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>
              {displayName}
            </p>
          </div>

          <p className="text-xs text-slate-500">{program}</p>

          <div className="flex items-center gap-1.5 mt-1">
            <div className={`w-1.5 h-1.5 rounded-full ${getStatusColor(connection)}`}></div>
            <span className="text-xs text-slate-400">{getStatusLabel(connection)}</span>
          </div>

          <div className="flex flex-wrap gap-1 mt-2">
            {(connection.courses ?? []).slice(0, 4).map((co) => (
              <span
                key={co}
                className="text-xs bg-slate-50 text-slate-600 px-2 py-0.5 rounded border border-slate-100"
                style={{ fontWeight: 500 }}
              >
                {co}
              </span>
            ))}
          </div>
        </div>

        <div className="flex flex-col gap-1.5 shrink-0">
          <button
            onClick={() => navigate("/chat")}
            className="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition-colors"
            title="Message"
          >
            <MessageSquare size={15} />
          </button>
          <button
            onClick={() => setInviteModal(connection.userId)}
            className="w-8 h-8 rounded-lg bg-orange-50 hover:bg-orange-100 flex items-center justify-center text-orange-500 transition-colors"
            title="Invite to session"
          >
            <CalendarPlus size={15} />
          </button>
        </div>
      </div>
    </div>
  );
}