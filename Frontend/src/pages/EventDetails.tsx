/* 
 * EventDetails.tsx
 * This page shows detailed information regarding a specific study session.
 */

import { useState } from "react";
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
  const event = events.find((e) => e.id === id) || events[0];

  const [joined, setJoined] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewText, setReviewText] = useState("");
  const [localReviews, setLocalReviews] = useState(event.reviews);
  const [commentText, setCommentText] = useState<Record<string, string>>({});
  const [showCommentInput, setShowCommentInput] = useState<string | null>(null);
  const [cancelConfirm, setCancelConfirm] = useState(false);

  const formatDate = (d: string) =>
    new Date(d).toLocaleDateString("en-CA", { weekday: "long", month: "long", day: "numeric", year: "numeric" });
  const isMyEvent = event.host.id === currentUser.id;

  const submitReview = () => {
    if (!reviewText.trim()) return;
    const newReview = {
      id: `r_new_${Date.now()}`,
      author: currentUser as any,
      rating: reviewRating,
      text: reviewText,
      date: new Date().toISOString().split("T")[0],
      comments: [],
    };
    setLocalReviews((prev) => [...prev, newReview]);
    setReviewText("");
    setShowReviewForm(false);
  };

  const submitComment = (reviewId: string) => {
    const text = commentText[reviewId];
    if (!text?.trim()) return;
    setLocalReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? { ...r, comments: [...r.comments, { id: `c_${Date.now()}`, author: currentUser as any, text, date: new Date().toISOString().split("T")[0] }] }
          : r
      )
    );
    setCommentText((prev) => ({ ...prev, [reviewId]: "" }));
    setShowCommentInput(null);
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
                  {event.attendees.length} / {event.maxParticipants} attending
                  <div className="flex-1 max-w-24 bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-blue-500 h-full rounded-full"
                      style={{ width: `${(event.attendees.length / event.maxParticipants) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </div>
            </div>

            {/* Host */}
            <div className="shrink-0 text-right">
              <p className="text-xs text-slate-400 mb-1">Hosted by</p>
              <div className="flex flex-col items-end gap-1">
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
            {event.tags.map((tag) => (
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
                onClick={() => setJoined(!joined)}
                className={`flex-1 py-2.5 rounded-xl text-sm transition-colors ${
                  joined
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
          Attendees ({event.attendees.length}/{event.maxParticipants})
        </h2>
        <div className="flex flex-wrap gap-3">
          {event.attendees.map((a) => (
            <div key={a.id} className="flex items-center gap-2 bg-slate-50 rounded-xl px-3 py-2">
              <div className="w-7 h-7 rounded-full overflow-hidden bg-blue-100">
                {a.avatar ? (
                  <img src={a.avatar} alt={a.name} className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-blue-600" style={{ fontSize: "11px", fontWeight: 700 }}>
                    {a.name.charAt(0)}
                  </div>
                )}
              </div>
              <span className="text-xs text-slate-700" style={{ fontWeight: 500 }}>{a.name}</span>
              <button className="ml-1 text-slate-400 hover:text-blue-500 transition-colors">
                <UserPlus size={13} />
              </button>
            </div>
          ))}
          {event.attendees.length === 0 && (
            <p className="text-sm text-slate-400">No attendees yet. Be the first to join!</p>
          )}
        </div>
      </div>

      {/* Reviews */}
      <div className="bg-white rounded-xl border border-slate-200 p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-slate-800 text-sm flex items-center gap-2" style={{ fontWeight: 600 }}>
            <Star size={16} className="text-orange-400" />
            Reviews ({localReviews.length})
          </h2>
          {event.status === "past" && !showReviewForm && (
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
                  {r.comments.map((c) => (
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
              {event.status === "past" && (
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
