/* 
 * Profile.tsx
 * Renders the user's profile page and displays their information
 */

import { useState, useEffect } from "react";   
import { useNavigate } from "react-router-dom";
import {
  Edit2, Camera, Plus, X, Check, BookOpen, Clock, CalendarDays,
  Mail, GraduationCap, MapPin, Star
} from "lucide-react";
import { currentUser, studyVibeOptions, courseOptions, sessionHistory } from "../data/mockData";

export default function Profile() {
  const navigate = useNavigate();

  const userId = "123"; // replace with real user ID
  const [loading, setLoading] = useState(true);

  const [editingBio, setEditingBio] = useState(false);
  const [bio, setBio] = useState("");
  const [tempBio, setTempBio] = useState(currentUser.bio);
  const [editingCourses, setEditingCourses] = useState(false);
  const [courses, setCourses] = useState<string[]>([]);
  const [courseInput, setCourseInput] = useState("");
  const [vibes, setVibes] = useState<string[]>([]);
  const [editingVibes, setEditingVibes] = useState(false);
  const [activeTab, setActiveTab] = useState<"overview" | "log">("overview");

  const totalMinutes = sessionHistory.reduce((acc, s) => acc + s.duration, 0);
  const totalHours = Math.floor(totalMinutes / 60);

  const removeCourse = (c: string) => setCourses((prev) => prev.filter((x) => x !== c));
  const addCourse = (c: string) => {
    if (c && !courses.includes(c)) setCourses((prev) => [...prev, c]);
    setCourseInput("");
  };
  const toggleVibe = (v: string) => {
    setVibes((prev) => prev.includes(v) ? prev.filter((x) => x !== v) : [...prev, v]);
  };

  const vibeColors: Record<string, string> = {
    "Quiet Focus": "bg-blue-50 text-blue-700 border-blue-200",
    "Group Discussion": "bg-orange-50 text-orange-700 border-orange-200",
    "Whiteboard Work": "bg-purple-50 text-purple-700 border-purple-200",
    "Problem Solving": "bg-green-50 text-green-700 border-green-200",
    "Lecture Review": "bg-yellow-50 text-yellow-700 border-yellow-200",
    "Essay Writing": "bg-pink-50 text-pink-700 border-pink-200",
    "Lab Work": "bg-cyan-50 text-cyan-700 border-cyan-200",
    "Exam Prep": "bg-red-50 text-red-700 border-red-200",
  };

  const formatDate = (d: string) => new Date(d).toLocaleDateString("en-CA", { month: "short", day: "numeric", year: "numeric" });

  // Load profile from backend
  useEffect(() => {
    async function loadProfile() {
      try {
        const res = await fetch(`http://localhost:8080/api/studentcontroller/${userId}`);
        const data = await res.json();

        setBio(data.bio || "");
        setTempBio(data.bio || "");
        setCourses(data.courses || []);
        setVibes(data.studyVibes || []);
      } catch (err) {
        console.error("Failed to load profile", err);
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, []);

  // Save profile to backend
  async function saveProfile(updatedBio: string) {
    try {
      await fetch(`http://localhost:8080/api/studentcontroller/profile/update/${userId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          courses,
          studyVibes: vibes,
          privacySettings: {},
          bio: updatedBio
        })
      });
    } catch (err) {
      console.error("Failed to save profile", err);
    }
  }

  if (loading) return <p className="p-6">Loading profile...</p>; 

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {/* Profile Header Card */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden mb-5">
        {/* Cover Banner */}
        <div className="h-28 bg-gradient-to-r from-blue-800 to-blue-700 relative">
          <div className="absolute inset-0 bg-gradient-to-r from-blue-800/80 to-orange-700/30"></div>
        </div>

        <div className="px-6 pb-6">
          {/* Avatar */}
          <div className="relative -mt-12 mb-4 w-fit">
            <div className="w-20 h-20 rounded-2xl border-4 border-white shadow-md overflow-hidden bg-blue-100">
              <img src={currentUser.avatar} alt="profile" className="w-full h-full object-cover" />
            </div>
            <button className="absolute bottom-0 right-0 w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center shadow border-2 border-white hover:bg-orange-600 transition-colors">
              <Camera size={13} className="text-white" />
            </button>
          </div>

          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <h1 className="text-slate-900" style={{ fontSize: "1.35rem", fontWeight: 700 }}>{currentUser.name}</h1>
                <div className="flex items-center gap-1">
                  <div className="w-2 h-2 rounded-full bg-green-500"></div>
                  <span className="text-xs text-green-600">Online</span>
                </div>
              </div>
              <div className="flex flex-wrap gap-3 text-sm text-slate-500">
                <span className="flex items-center gap-1.5"><GraduationCap size={14} className="text-blue-500" />{currentUser.major}</span>
                <span className="flex items-center gap-1.5"><Star size={14} className="text-orange-400" />{currentUser.year}</span>
                <span className="flex items-center gap-1.5"><Mail size={14} className="text-slate-400" />{currentUser.email}</span>
              </div>
              <div className="flex items-center gap-1.5 mt-1.5 text-sm text-slate-500">
                <MapPin size={14} className="text-slate-400" />
                <span>{currentUser.location}</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">Member since {currentUser.joinedDate}</p>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setEditingBio(true)}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm transition-colors"
                style={{ fontWeight: 500 }}
              >
                <Edit2 size={14} />
                Edit Profile
              </button>
            </div>
          </div>

          {/* Stats Row */}
          <div className="grid grid-cols-3 gap-4 mt-4 pt-4 border-t border-slate-100">
            <div className="text-center">
              <p className="text-slate-800" style={{ fontSize: "1.5rem", fontWeight: 700 }}>{currentUser.totalStudyHours}h</p>
              <p className="text-xs text-slate-500">Total Study Time</p>
            </div>
            <div className="text-center border-x border-slate-100">
              <p className="text-slate-800" style={{ fontSize: "1.5rem", fontWeight: 700 }}>{currentUser.totalSessions}</p>
              <p className="text-xs text-slate-500">Sessions</p>
            </div>
            <div className="text-center">
              <p className="text-slate-800" style={{ fontSize: "1.5rem", fontWeight: 700 }}>{sessionHistory.filter(s => s.role === "host").length}</p>
              <p className="text-xs text-slate-500">Hosted</p>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 rounded-xl p-1 mb-5 w-fit">
        {(["overview", "log"] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-5 py-2 rounded-lg text-sm transition-colors ${activeTab === tab ? "bg-white text-blue-700 shadow-sm" : "text-slate-500 hover:text-slate-700"}`}
            style={{ fontWeight: activeTab === tab ? 600 : 400 }}
          >
            {tab === "overview" ? "Overview" : "Session Log"}
          </button>
        ))}
      </div>

      {activeTab === "overview" && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

          {/* Bio */}
          <div className="bg-white rounded-xl border border-slate-200 p-5">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>About Me</h2>
              <button onClick={() => { setTempBio(bio); setEditingBio(true); }} className="text-blue-500 hover:text-blue-600">
                <Edit2 size={14} />
              </button>
            </div>
            {editingBio ? (
              <div>
                <textarea
                  value={tempBio}
                  onChange={(e) => setTempBio(e.target.value.slice(0, 200))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={4}
                  placeholder="Write a short bio (max 200 characters)"
                />
                <div className="flex items-center justify-between mt-2">
                  <span className="text-xs text-slate-400">{tempBio.length}/200</span>
                  <div className="flex gap-2">
                    <button onClick={() => setEditingBio(false)} className="px-3 py-1.5 text-xs text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">Cancel</button>

                    {/* Save bio + backend */}
                    <button
                      onClick={async () => {
                        await saveProfile(tempBio);   
                        setBio(tempBio);
                        setEditingBio(false);
                      }}
                      className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-1"
                    >
                      <Check size={12} /> Save
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <p className="text-sm text-slate-600 leading-relaxed">{bio || "No bio yet. Click edit to add one."}</p>
            )}
          </div>

          {/* Study Vibes */}
          <div className="bg-white rounded-xl border border-slate-200 p-5">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>Study Vibe</h2>
              <button onClick={() => setEditingVibes(!editingVibes)} className="text-blue-500 hover:text-blue-600">
                <Edit2 size={14} />
              </button>
            </div>
            {editingVibes ? (
              <div>
                <div className="flex flex-wrap gap-2">
                  {studyVibeOptions.map((v) => (
                    <button
                      key={v}
                      onClick={() => toggleVibe(v)}
                      className={`px-3 py-1.5 rounded-lg text-xs border transition-colors ${vibes.includes(v) ? "bg-blue-600 text-white border-blue-600" : "bg-slate-50 text-slate-600 border-slate-200 hover:border-blue-300"}`}
                    >
                      {v}
                    </button>
                  ))}
                </div>

                {/* Save vibes */}
                <button
                  onClick={async () => {
                    setEditingVibes(false);
                    await saveProfile();   
                  }}
                  className="mt-3 px-4 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-1"
                >
                  <Check size={12} /> Done
                </button>
              </div>
            ) : (
              <div className="flex flex-wrap gap-2">
                {vibes.map((v) => (
                  <span key={v} className={`px-3 py-1.5 rounded-lg text-xs border ${vibeColors[v] || "bg-slate-50 text-slate-600 border-slate-200"}`} style={{ fontWeight: 500 }}>
                    {v}
                  </span>
                ))}
                {vibes.length === 0 && <p className="text-xs text-slate-400">No study vibes set. Click edit to add some.</p>}
              </div>
            )}
          </div>

          {/* My Courses */}
          <div className="bg-white rounded-xl border border-slate-200 p-5 md:col-span-2">
            <div className="flex items-center justify-between mb-3">
              <h2 className="text-slate-800 text-sm" style={{ fontWeight: 600 }}>My Courses</h2>

              {/* Save courses when finishing edit */}
              <button
                onClick={async () => {
                  if (editingCourses) {
                    await saveProfile();   
                  }
                  setEditingCourses(!editingCourses);
                }}
                className="text-blue-500 hover:text-blue-600"
              >
                {editingCourses ? <Check size={14} /> : <Edit2 size={14} />}
              </button>
            </div>

            <div className="flex flex-wrap gap-2">
              {courses.map((c) => (
                <div
                  key={c}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg text-sm"
                  style={{ fontWeight: 500 }}
                >
                  <BookOpen size={12} />
                  {c}
                  {editingCourses && (
                    <button onClick={() => removeCourse(c)} className="hover:text-red-500 ml-1 transition-colors">
                      <X size={12} />
                    </button>
                  )}
                </div>
              ))}

              {editingCourses && (
                <div className="flex gap-2">
                  <select
                    value={courseInput}
                    onChange={(e) => setCourseInput(e.target.value)}
                    className="px-3 py-1.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                  >
                    <option value="">Add a course...</option>
                    {courseOptions.filter((c) => !courses.includes(c)).map((c) => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>

                  <button
                    onClick={() => addCourse(courseInput)}
                    className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition-colors flex items-center gap-1"
                  >
                    <Plus size={13} /> Add
                  </button>
                </div>
              )}
            </div>
          </div>

        </div>
      )}
    </div>
  );
}