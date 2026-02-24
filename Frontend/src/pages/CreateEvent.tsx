/* 
 * CreateEvent.tsx
 * Page for creating a new study session event
 */

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft, CalendarDays, Clock, MapPin, Users, BookOpen,
  FileText, Tag, ChevronDown, Check
} from "lucide-react";
import { campusLocations, courseOptions, studyVibeOptions, currentUser } from "../data/mockData";

export default function CreateEvent() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    course: "",
    location: "",
    date: "",
    time: "",
    duration: "60",
    description: "",
    maxParticipants: "8",
    tags: [] as string[],
    isPrivate: false,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitted, setSubmitted] = useState(false);

  // For handling API submission
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const set = (k: string, v: string | boolean) => setForm((f) => ({ ...f, [k]: v }));
  const toggleTag = (tag: string) => {
    setForm((f) => ({
      ...f,
      tags: f.tags.includes(tag) ? f.tags.filter((t) => t !== tag) : [...f.tags, tag],
    }));
  };

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.title.trim()) e.title = "Session title is required";
    if (!form.course) e.course = "Please select a course";
    if (!form.location) e.location = "Please select a location";
    if (!form.date) e.date = "Please select a date";
    if (!form.time) e.time = "Please select a time";
    return e;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setIsSubmitting(true);
    setApiError(null);

    // Note that backend and frontend types are slightly different
    const payload = {
      title: form.title,
      course: form.course,
      host: currentUser.name, 
      location: form.location,
      date: form.date,
      time: form.time,
      duration: Number(form.duration),
      description: form.description,
      maxParticipants: Number(form.maxParticipants),
      attendees: [currentUser.name], 
      tags: form.tags,
      status: "upcoming",
      reviews: []
    };

    try {
      const response = await fetch("http://localhost:8080/api/events", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      setSubmitted(true);
      setTimeout(() => navigate("/events"), 1500);
      
    } catch (err: any) {
      console.error("Failed to create event:", err);
      setApiError("Failed to create session. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="p-6 max-w-2xl mx-auto">
        <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center">
          <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
            <Check size={32} className="text-green-600" />
          </div>
          <h2 className="text-slate-800 mb-2" style={{ fontWeight: 700, fontSize: "1.25rem" }}>Session Created!</h2>
          <p className="text-slate-500 text-sm">Your study session has been posted. Redirecting...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 max-w-2xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate(-1)}
          className="w-9 h-9 rounded-xl bg-slate-100 hover:bg-slate-200 flex items-center justify-center transition-colors"
        >
          <ArrowLeft size={18} className="text-slate-600" />
        </button>
        <div>
          <h1 className="text-slate-900" style={{ fontWeight: 700, fontSize: "1.35rem" }}>Host a Study Session</h1>
          <p className="text-slate-500 text-sm">Fill in the details to create your session</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Session Title */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="text-slate-700 text-sm mb-4 flex items-center gap-2" style={{ fontWeight: 600 }}>
            <FileText size={16} className="text-blue-500" />
            Session Details
          </h2>
          <div className="space-y-4">
            <div>
              <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>Session Title *</label>
              <input
                value={form.title}
                onChange={(e) => set("title", e.target.value)}
                placeholder="e.g. EECS 3311 Design Patterns Study Group"
                className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
              />
              {errors.title && <p className="text-xs text-red-500 mt-0.5">{errors.title}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                  <span className="flex items-center gap-1"><BookOpen size={12} />Course *</span>
                </label>
                <div className="relative">
                  <select
                    value={form.course}
                    onChange={(e) => set("course", e.target.value)}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 appearance-none pr-8"
                  >
                    <option value="">Select course...</option>
                    {courseOptions.map((c) => <option key={c} value={c}>{c}</option>)}
                  </select>
                  <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                </div>
                {errors.course && <p className="text-xs text-red-500 mt-0.5">{errors.course}</p>}
              </div>

              <div>
                <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>Max Participants</label>
                <div className="relative">
                  <Users size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="number"
                    min={2}
                    max={20}
                    value={form.maxParticipants}
                    onChange={(e) => set("maxParticipants", e.target.value)}
                    className="w-full pl-8 pr-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                  />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>Description</label>
              <textarea
                value={form.description}
                onChange={(e) => set("description", e.target.value)}
                rows={3}
                placeholder="What will you cover? What should attendees bring or prepare?"
                className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 resize-none"
              />
            </div>
          </div>
        </div>

        {/* Date, Time & Location */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="text-slate-700 text-sm mb-4 flex items-center gap-2" style={{ fontWeight: 600 }}>
            <MapPin size={16} className="text-orange-500" />
            When & Where
          </h2>
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                  <span className="flex items-center gap-1"><CalendarDays size={12} />Date *</span>
                </label>
                <input
                  type="date"
                  value={form.date}
                  onChange={(e) => set("date", e.target.value)}
                  min={new Date().toISOString().split("T")[0]}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                />
                {errors.date && <p className="text-xs text-red-500 mt-0.5">{errors.date}</p>}
              </div>
              <div>
                <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                  <span className="flex items-center gap-1"><Clock size={12} />Time *</span>
                </label>
                <input
                  type="time"
                  value={form.time}
                  onChange={(e) => set("time", e.target.value)}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                />
                {errors.time && <p className="text-xs text-red-500 mt-0.5">{errors.time}</p>}
              </div>
              <div>
                <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>Duration</label>
                <div className="relative">
                  <select
                    value={form.duration}
                    onChange={(e) => set("duration", e.target.value)}
                    className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 appearance-none"
                  >
                    {["30", "45", "60", "90", "120", "150", "180", "240"].map((d) => (
                      <option key={d} value={d}>{d === "60" ? "1 hour" : d === "120" ? "2 hours" : d === "180" ? "3 hours" : d === "240" ? "4 hours" : `${d} min`}</option>
                    ))}
                  </select>
                  <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-xs text-slate-600 mb-1.5" style={{ fontWeight: 500 }}>
                <span className="flex items-center gap-1"><MapPin size={12} />Location *</span>
              </label>
              <div className="relative">
                <select
                  value={form.location}
                  onChange={(e) => set("location", e.target.value)}
                  className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50 appearance-none pr-8"
                >
                  <option value="">Select a location on campus...</option>
                  {campusLocations.map((l) => <option key={l} value={l}>{l}</option>)}
                </select>
                <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
              </div>
              {errors.location && <p className="text-xs text-red-500 mt-0.5">{errors.location}</p>}
            </div>
          </div>
        </div>

        {/* Study Vibes */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="text-slate-700 text-sm mb-4 flex items-center gap-2" style={{ fontWeight: 600 }}>
            <Tag size={16} className="text-purple-500" />
            Study Vibe Tags
          </h2>
          <div className="flex flex-wrap gap-2">
            {studyVibeOptions.map((vibe) => (
              <button
                key={vibe}
                type="button"
                onClick={() => toggleTag(vibe)}
                className={`px-3 py-1.5 rounded-lg text-xs border transition-colors ${
                  form.tags.includes(vibe)
                    ? "bg-blue-600 text-white border-blue-600"
                    : "bg-slate-50 text-slate-600 border-slate-200 hover:border-blue-300 hover:text-blue-600"
                }`}
              >
                {vibe}
              </button>
            ))}
          </div>
        </div>

        {/* Privacy */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-slate-700 text-sm" style={{ fontWeight: 600 }}>Private Session</h2>
              <p className="text-xs text-slate-400 mt-0.5">Only invited students can join. Won't appear on the map.</p>
            </div>
            <button
              type="button"
              onClick={() => set("isPrivate", !form.isPrivate)}
              className={`relative w-11 h-6 rounded-full transition-colors ${form.isPrivate ? "bg-blue-600" : "bg-slate-200"}`}
            >
              <div className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${form.isPrivate ? "translate-x-5" : "translate-x-0.5"}`}></div>
            </button>
          </div>
        </div>

        {/* Submit */}
        <div className="flex gap-3">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="flex-1 py-3 border border-slate-200 text-slate-600 rounded-xl text-sm hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="flex-1 py-3 bg-blue-700 hover:bg-blue-800 text-white rounded-xl text-sm transition-colors"
            style={{ fontWeight: 600 }}
          >
            Create Session
          </button>
        </div>
      </form>
    </div>
  );
}
