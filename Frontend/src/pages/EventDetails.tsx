/* 
 * EventDetails.tsx
 * This page shows detailed information regarding a specific study session.
 */

import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft, CalendarDays, Clock, MapPin, Users, Star, MessageSquare,
  Send, UserPlus, X, Check, AlertTriangle, ChevronDown
} from "lucide-react";
import { events, currentUser } from "../data/mockData";

const vibeColors: Record<string, string> = {
  "Quiet Focus": "bg-blue-50 text-blue-700",
  "Group Discussion": "bg-orange-50 text-orange-700",
  "Whiteboard Work": "bg-purple-50 text-purple-700",
  "Problem Solving": "bg-green-50 text-green-700",
};

export default function EventDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [event, setEvent] = useState<any>(null);
  const [student, setStudent] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [joined, setJoined] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewText, setReviewText] = useState("");
  const [localReviews, setLocalReviews] = useState<any[]>([]);
  const [commentText, setCommentText] = useState<Record<string, string>>({});
  const [showCommentInput, setShowCommentInput] = useState<string | null>(null);
  const [cancelConfirm, setCancelConfirm] = useState(false);

  useEffect(() => {
    const fetchEventAndStudents = async () => {
      try {
        const token = localStorage.getItem("studyBuddyToken");
        const headers: HeadersInit | undefined = token ? { Authorization: "Bearer " + token } : undefined;

        // 1. Fetch Current Student Profile
        let currentStudent = null;
        if (token) {
          const userRes = await fetch("/api/studentcontroller/profile", { headers });
          if (userRes.ok) currentStudent = await userRes.json();
          setStudent(currentStudent);
        }

        // 2. Fetch Event Details
        const response = await fetch(`/api/events/${id}`, { headers });
        if (!response.ok) {
          if (response.status === 404) throw new Error("Event not found");
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();

        // 3. ASYNC HELPER: Fetches a single user profile directly from Firestore
        const resolveUserAsync = async (userOrId: any) => {
          if (!userOrId) return { id: "unknown", name: "Student", avatar: null };
          
          // If the backend already populated the name (like it does for Host), use it
          if (typeof userOrId === 'object' && userOrId.name) return userOrId;
          
          const targetId = typeof userOrId === 'string' ? userOrId : (userOrId.id || userOrId.userId);

          try {
            // Ask the backend for this specific student's profile
            const res = await fetch(`/api/studentcontroller/${targetId}`, { headers });
            if (res.ok) {
              const profile = await res.json();
              return {
                id: targetId,
                name: profile.fullName || profile.name || `${profile.firstName || ''} ${profile.lastName || ''}`.trim() || targetId,
                avatar: profile.avatar || null
              };
            }
          } catch (e) {
            console.warn("Could not fetch profile for", targetId);
          }

          // Ultimate fallback
          return { id: targetId, name: "Unknown Student", avatar: null };
        };

        const resolvedHost = await resolveUserAsync(data.host);
        
        const resolvedAttendees = data.attendees 
          ? await Promise.all(data.attendees.map((att: any) => resolveUserAsync(att)))
          : [];

        const resolvedReviews = data.reviews 
          ? await Promise.all(data.reviews.map(async (r: any) => ({
              ...r,
              author: await resolveUserAsync(r.author),
              comments: r.comments 
                ? await Promise.all(r.comments.map(async (c: any) => ({
                    ...c,
                    author: await resolveUserAsync(c.author)
                  })))
                : []
            })))
          : [];

        // 5. Update State
        const formattedEvent = {
          ...data,
          host: resolvedHost,
          attendees: resolvedAttendees,
          reviews: resolvedReviews
        };

        setEvent(formattedEvent);
        setLocalReviews(formattedEvent.reviews || []);

        if (currentStudent) {
          setJoined(formattedEvent.attendees.some((a: any) => a.id === currentStudent.userId));
        }

      } catch (err: any) {
        console.error("Failed to fetch event:", err);
        setError(err.message);
      } finally {
        setIsLoading(false);
      }
    };

    if (id) fetchEventAndStudents();
  }, [id]);

  if (isLoading) return <div className="p-10 text-center text-slate-500 mt-10">Loading session details...</div>;
  if (error) return <div className="p-10 text-center text-red-500 mt-10">{error}</div>;
  if (!event) return <div className="p-10 text-center text-slate-500 mt-10">Event not found.</div>;

  const formatDate = (d: string) =>
    new Date(d).toLocaleDateString("en-CA", { weekday: "long", month: "long", day: "numeric", year: "numeric" });
  const isMyEvent = student && event.host.id === student.userId;

  const isParticipating = isMyEvent || joined;

  const submitReview = async () => {
    if (!reviewText.trim() || !student) return;

    const previousReviews = [...localReviews];

    const newReview = {
      id: `r_temp_${Date.now()}`,
      author: {
        id: student.userId,
        name: student.fullName || student.name || "You",
        avatar: student.avatar
      },
      rating: reviewRating,
      text: reviewText.trim(),
      date: new Date().toISOString().split("T")[0],
      comments: [],
    };

    setLocalReviews((prev) => [...prev, newReview]);
    setReviewText("");
    setShowReviewForm(false);

    try {
      const token = localStorage.getItem("studyBuddyToken");
      const response = await fetch(`/api/events/${id}/reviews`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ rating: reviewRating, text: reviewText.trim() })
      });

      if (!response.ok) throw new Error("Failed to post review");
      
    } catch (err) {
      console.error("Error submitting review:", err);
      setLocalReviews(previousReviews); // Revert if the server fails
      alert("Failed to post review. Please try again.");
    }
  };

  const submitComment = async (reviewId: string) => {
    const text = commentText[reviewId];
    if (!text?.trim() || !student) return;

    // 1. Snapshot for safety
    const previousReviews = [...localReviews];

    // 2. Create the mock comment for Optimistic Update
    const newComment = { 
      id: `c_temp_${Date.now()}`, 
      author: { 
        id: student.userId, 
        name: student.fullName || student.name || "You", 
        avatar: student.avatar 
      }, 
      text: text.trim(), 
      date: new Date().toISOString().split("T")[0] 
    };

    // 3. Update the UI instantly
    setLocalReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? { ...r, comments: [...(r.comments || []), newComment] }
          : r
      )
    );
    setCommentText((prev) => ({ ...prev, [reviewId]: "" }));
    setShowCommentInput(null);

    // 4. Send to the Backend
    try {
      const token = localStorage.getItem("studyBuddyToken");
      const response = await fetch(`/api/events/${id}/reviews/${reviewId}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({ text: text.trim() })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to post comment: ${errorText}`);
      }
      
    } catch (err) {
      console.error("Error submitting comment:", err);
      // Revert the UI if the network fails
      setLocalReviews(previousReviews);
      alert("Failed to post comment. Please try again.");
    }
  };

  const handleJoinToggle = async () => {
    // 1. Safety check so we don't crash if data isn't loaded yet
    if (!student || !event) return;

    const wasJoined = joined;
    const previousAttendees = [...event.attendees];
    const previousCount = event.attendeeCount ?? event.attendees.length ?? 0;
    const newCount = wasJoined ? Math.max(0, previousCount - 1) : previousCount + 1;

    // 2. Optimistically update button state, the array, AND the explicit count
    setJoined(!wasJoined);
    setEvent((prev: any) => ({
      ...prev,
      attendeeCount: newCount,
      attendees: wasJoined
        // If leaving: filter signed-in ID out of the array
        ? prev.attendees.filter((a: any) => a.id !== student.userId)
        // Otherwise, inject the mock "You"
        : [...prev.attendees, {
          id: student.userId,
          name: student.fullName || student.name || "You",
          avatar: student.avatar
        }]
    }));

    try {
      const token = localStorage.getItem("studyBuddyToken");
      const endpoint = wasJoined ? `/api/events/leave` : `/api/events/join`;

      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
          eventId: event.id,
          userId: student.userId
        })
      });

      if (!response.ok) {
        // 3. Revert everything if the backend fails
        setJoined(wasJoined);
        setEvent((prev: any) => ({ ...prev, attendees: previousAttendees, attendeeCount: previousCount }));
        console.error("Failed to update attendance status");
      }
    } catch (err) {
      // 4. Revert if the network crashes
      setJoined(wasJoined);
      setEvent((prev: any) => ({ ...prev, attendees: previousAttendees, attendeeCount: previousCount }));
      console.error("Error updating attendance:", err);
    }
  };

  const handleKick = async (targetUserId: string, e: React.MouseEvent) => {
    e.stopPropagation(); // Prevents clicking through to the profile page
    if (!window.confirm("Are you sure you want to remove this user from the session?")) return;

    // 1. Snapshot for safety
    const previousAttendees = [...event.attendees];
    const previousCount = event.attendeeCount ?? event.attendees.length;

    // 2. Optimistic UI update: instantly remove them from the screen
    setEvent((prev: any) => ({
      ...prev,
      attendees: prev.attendees.filter((a: any) => a.id !== targetUserId),
      attendeeCount: Math.max(0, previousCount - 1)
    }));

    // 3. Tell the backend to drop them
    try {
      const token = localStorage.getItem("studyBuddyToken");
      const response = await fetch("/api/events/leave", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
          eventId: event.id,
          userId: targetUserId // Passing the target's ID instead of the host's ID
        })
      });

      if (!response.ok) throw new Error("Failed to kick user");

    } catch (err) {
      console.error("Error kicking user:", err);
      // Revert if the server fails
      setEvent((prev: any) => ({
        ...prev,
        attendees: previousAttendees,
        attendeeCount: previousCount
      }));
      alert("Failed to remove user. Please try again.");
    }
  };
  return (
    <div className="p-6 max-w-3xl mx-auto">
      {/* Back */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-slate-500 hover:text-slate-700 mb-5 text-sm transition-colors"
      >
        <ArrowLeft size={16} />
        Back to Events
      </button>

      {/* Header Card */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden mb-5">
        <div className="h-3 bg-gradient-to-r from-blue-700 to-orange-500"></div>
        <div className="p-6">
          <div className="flex items-start justify-between gap-4 mb-4">
            <div className="flex-1">
              <div className="flex flex-wrap items-center gap-2 mb-2">
                <span className="text-xs bg-blue-100 text-blue-700 px-2.5 py-1 rounded-full" style={{ fontWeight: 600 }}>
                  {event.course}
                </span>
                {event.status === "past" && (
                  <span className="text-xs bg-slate-100 text-slate-500 px-2.5 py-1 rounded-full">Past Session</span>
                )}
                {isMyEvent && (
                  <span className="text-xs bg-orange-100 text-orange-600 px-2.5 py-1 rounded-full">You're hosting</span>
                )}
              </div>
              <h1 className="text-slate-900 mb-3" style={{ fontWeight: 700, fontSize: "1.25rem" }}>{event.title}</h1>
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <CalendarDays size={15} className="text-blue-500 shrink-0" />
                  {formatDate(event.date)} at {event.time}
                </div>
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <Clock size={15} className="text-blue-500 shrink-0" />
                  {event.duration} minutes
                </div>
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <MapPin size={15} className="text-orange-500 shrink-0" />
                  {event.location}
                </div>
                <div className="flex items-center gap-2 text-sm text-slate-600">
                  <Users size={15} className="text-blue-500 shrink-0" />
                  {event.attendeeCount ?? event.attendees?.length ?? 0} / {event.maxParticipants} attending
                  <div className="flex-1 max-w-24 bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-blue-500 h-full rounded-full"
                      style={{ width: `${((event.attendeeCount ?? event.attendees?.length ?? 0) / event.maxParticipants) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </div>
            </div>

            {/* Host */}
            <div className="shrink-0 text-right">
              <p className="text-xs text-slate-400 mb-1">Hosted by</p>
              <div
                className="flex flex-col items-end gap-1 cursor-pointer hover:opacity-80 transition-opacity"
                onClick={(e) => { e.stopPropagation(); navigate(`/profile/${event.host.id}`); }}
              >
                <div className="w-10 h-10 rounded-xl overflow-hidden bg-blue-100">
                  {event.host.avatar ? (
                    <img src={event.host.avatar} alt={event.host.name} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontWeight: 700 }}>
                      {event.host.name.charAt(0)}
                    </div>
                  )}
                </div>
                <p className="text-xs text-slate-700" style={{ fontWeight: 600 }}>{event.host.name}</p>
              </div>
            </div>
          </div>

          {/* Tags */}
          <div className="flex flex-wrap gap-2 mb-4">
            {event.tags.map((tag: string) => (
              <span key={tag} className={`text-xs px-2.5 py-1 rounded-full ${vibeColors[tag] || "bg-slate-100 text-slate-600"}`} style={{ fontWeight: 500 }}>
                {tag}
              </span>
            ))}
          </div>

          {/* Description */}
          <p className="text-sm text-slate-600 leading-relaxed mb-5">{event.description}</p>

          {/* Actions */}
          <div className="flex gap-3">
            {event.status === "upcoming" && !isMyEvent && (
              <button
                onClick={handleJoinToggle}
                className={`flex-1 py-2.5 rounded-xl text-sm transition-colors ${joined
                    ? "bg-slate-100 text-slate-600 hover:bg-red-50 hover:text-red-600"
                    : "bg-blue-700 hover:bg-blue-800 text-white"
                  }`}
                style={{ fontWeight: 600 }}
              >
                {joined ? "Cancel Attendance" : "Join Session"}
              </button>
            )}
            {isMyEvent && event.status === "upcoming" && (
              <>
                {cancelConfirm ? (
                  <div className="flex-1 flex gap-2">
                    <button
                      onClick={() => { navigate("/events"); }}
                      className="flex-1 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-sm transition-colors"
                      style={{ fontWeight: 600 }}
                    >
                      <span className="flex items-center justify-center gap-1"><Check size={14} />Confirm Cancel</span>
                    </button>
                    <button
                      onClick={() => setCancelConfirm(false)}
                      className="px-4 py-2.5 border border-slate-200 text-slate-600 rounded-xl text-sm hover:bg-slate-50 transition-colors"
                    >
                      Keep
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={() => setCancelConfirm(true)}
                    className="flex-1 py-2.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl text-sm transition-colors flex items-center justify-center gap-2"
                    style={{ fontWeight: 600 }}
                  >
                    <AlertTriangle size={14} />
                    Cancel Event
                  </button>
                )}
              </>
            )}
            <button
              onClick={() => navigate("/chat")}
              className="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm transition-colors flex items-center gap-2"
            >
              <MessageSquare size={15} />
              Chat
            </button>
          </div>
        </div>
      </div>

      {/* Attendees */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 mb-5">
        <h2 className="text-slate-800 text-sm mb-4 flex items-center gap-2" style={{ fontWeight: 600 }}>
          <Users size={16} className="text-blue-500" />
          Attendees ({event.attendeeCount ?? event.attendees?.length ?? 0}/{event.maxParticipants})
        </h2>

        {isParticipating ? (
          <div className="flex flex-wrap gap-3">
            {event.attendees.map((a: any) => (
              <div
                key={a.id}
                className="group relative flex items-center gap-2 bg-slate-50 hover:bg-slate-100 rounded-xl px-3 py-2 cursor-pointer transition-colors"
                onClick={() => navigate(`/profile/${a.id}`)}
              >
                <div className="w-7 h-7 rounded-full overflow-hidden bg-blue-100">
                  {a.avatar ? (
                    <img src={a.avatar} alt={a.name} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "11px", fontWeight: 700 }}>
                      {(a.name || "?").charAt(0).toUpperCase()}
                    </div>
                  )}
                </div>
                <span className="text-xs text-slate-700" style={{ fontWeight: 500 }}>{a.name}</span>
                
                {/* Kick button */}
                {isMyEvent && a.id !== student.userId && (
                  <button
                    onClick={(e) => handleKick(a.id, e)}
                    className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-red-500 hover:bg-red-600 text-white rounded-full opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity shadow-sm"
                    title="Remove user"
                  >
                    <X size={12} />
                  </button>
                )}
              </div>
            ))}
            {event.attendees.length === 0 && (
              <p className="text-sm text-slate-400">No attendees yet. Be the first to join!</p>
            )}
          </div>
        ) : (
          <div className="bg-slate-50 border border-slate-100 rounded-xl p-4 text-center">
            <p className="text-sm text-slate-500">
              Join this session to see who else is attending.
            </p>
          </div>
        )}
      </div>

      {/* Reviews */}
      <div className="bg-white rounded-xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-slate-800 text-sm flex items-center gap-2" style={{ fontWeight: 600 }}>
            <Star size={16} className="text-orange-400" />
            Reviews ({localReviews.length})
          </h2>
          {event.status === "past" && (joined || isMyEvent) && !showReviewForm && (
            <button
              onClick={() => setShowReviewForm(true)}
              className="text-xs bg-orange-50 text-orange-600 hover:bg-orange-100 px-3 py-1.5 rounded-lg transition-colors"
              style={{ fontWeight: 600 }}
            >
              Write Review
            </button>
          )}
        </div>

        {/* Review Form */}
        {showReviewForm && (
          <div className="bg-blue-50 rounded-xl p-4 mb-5 border border-blue-100">
            <div className="flex items-center justify-between mb-3">
              <p className="text-sm text-slate-700" style={{ fontWeight: 600 }}>Your Review</p>
              <button onClick={() => setShowReviewForm(false)}><X size={15} className="text-slate-400" /></button>
            </div>
            {/* Star Rating */}
            <div className="flex gap-1 mb-3">
              {[1, 2, 3, 4, 5].map((n) => (
                <button key={n} type="button" onClick={() => setReviewRating(n)}>
                  <Star size={20} className={n <= reviewRating ? "text-orange-400 fill-orange-400" : "text-slate-300"} />
                </button>
              ))}
            </div>
            <textarea
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
              rows={3}
              placeholder="Share your experience..."
              className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            />
            <div className="flex gap-2 mt-2">
              <button
                onClick={() => setShowReviewForm(false)}
                className="px-4 py-2 text-xs text-slate-600 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={submitReview}
                className="px-4 py-2 text-xs bg-blue-700 text-white rounded-lg hover:bg-blue-800 transition-colors"
                style={{ fontWeight: 600 }}
              >
                Submit Review
              </button>
            </div>
          </div>
        )}

        {/* Review List */}
        <div className="space-y-4">
          {localReviews.map((r) => (
            <div key={r.id} className="border border-slate-100 rounded-xl p-4">
              <div className="flex items-start justify-between gap-3 mb-2">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-full bg-blue-100 overflow-hidden">
                    {r.author.avatar ? (
                      <img src={r.author.avatar} alt={r.author.name} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "11px", fontWeight: 700 }}>
                        {r.author.name.charAt(0)}
                      </div>
                    )}
                  </div>
                  <span className="text-xs text-slate-700" style={{ fontWeight: 600 }}>{r.author.name}</span>
                  <span className="text-xs text-slate-400">{r.date}</span>
                </div>
                <div className="flex gap-0.5">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <Star key={n} size={12} className={n <= r.rating ? "text-orange-400 fill-orange-400" : "text-slate-200"} />
                  ))}
                </div>
              </div>
              <p className="text-sm text-slate-600 leading-relaxed mb-3">{r.text}</p>

              {/* Comments */}
              {r.comments.length > 0 && (
                <div className="pl-4 border-l-2 border-slate-100 space-y-2 mb-3">
                  {r.comments.map((c: any) => (
                    <div key={c.id} className="flex items-start gap-2">
                      <div className="w-5 h-5 rounded-full bg-blue-100 flex items-center justify-center shrink-0 mt-0.5">
                        <span className="text-blue-600" style={{ fontSize: "9px", fontWeight: 700 }}>
                          {c.author.name.charAt(0)}
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-slate-700" style={{ fontWeight: 600 }}>{c.author.name}</span>
                        <span className="text-xs text-slate-500 ml-1">{c.text}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Add Comment */}
              {showCommentInput === r.id ? (
                <div className="flex gap-2 mt-2">
                  <input
                    value={commentText[r.id] || ""}
                    onChange={(e) => setCommentText((prev) => ({ ...prev, [r.id]: e.target.value }))}
                    placeholder="Add a comment..."
                    className="flex-1 px-3 py-1.5 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                    onKeyDown={(e) => e.key === "Enter" && submitComment(r.id)}
                  />
                  <button onClick={() => submitComment(r.id)} className="text-blue-600 hover:text-blue-700">
                    <Send size={14} />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setShowCommentInput(r.id)}
                  className="text-xs text-slate-400 hover:text-blue-500 flex items-center gap-1 transition-colors mt-1"
                >
                  <MessageSquare size={12} />
                  Comment
                </button>
              )}
            </div>
          ))}

          {localReviews.length === 0 && (
            <div className="text-center py-8">
              <Star size={28} className="text-slate-200 mx-auto mb-2" />
              <p className="text-sm text-slate-400">No reviews yet.</p>
              {event.status === "past" && (joined || isMyEvent) && (
                <button
                  onClick={() => setShowReviewForm(true)}
                  className="text-xs text-blue-600 hover:underline mt-1"
                >
                  Be the first to review
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
