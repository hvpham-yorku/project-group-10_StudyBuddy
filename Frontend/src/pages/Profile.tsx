import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Edit2, Camera, Plus, X, Check, BookOpen, Clock, CalendarDays,
  Mail, GraduationCap, MapPin, Star
} from "lucide-react";
import { currentUser, studyVibeOptions, courseOptions, sessionHistory } from "../data/mockData";
import { set } from "date-fns";
import { Avatar } from "@radix-ui/react-avatar";

export default function Profile() {
  const navigate = useNavigate();

  const userId = "123"; // replace with real user ID
  const [loading, setLoading] = useState(true);

  // BIO
  const [editingBio, setEditingBio] = useState(false);
  const [bio, setBio] = useState("");
  const [tempBio, setTempBio] = useState("");

  // COURSES
  const [editingCourses, setEditingCourses] = useState(false);
  const [courses, setCourses] = useState<string[]>([]);
  const [courseInput, setCourseInput] = useState("");

  // STUDY VIBES
  const [vibes, setVibes] = useState<string[]>([]);
  const [editingVibes, setEditingVibes] = useState(false);
  const [vibeInput, setVibeInput] = useState("");

  // PROGRAM + YEAR
  const [program, setProgram] = useState("");
  const [year, setYear] = useState("");

  const [editingProgram, setEditingProgram] = useState(false);
  const [editingYear, setEditingYear] = useState(false);

  const [tempProgram, setTempProgram] = useState("");
  const [tempYear, setTempYear] = useState("");

  // SECURITY + NOTIFICATIONS
  const [twoFAEnabled, setTwoFAEnabled] = useState(false);
  const [autoTimeout, setAutoTimeout] = useState(0);
  const [isOnline, setIsOnline] = useState(false);
  const [location, setLocation] = useState("");
  const [notifications, setNotifications] = useState<Record<string, boolean>>({});

  // AVATAR
  const [avatar, setAvatar] = useState("");
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // PRIVACY SETTINGS
  const [privacySettings, setPrivacySettings] = useState({
    showBio: true,
    showProgram: true,
    showYear: true,
    showEmail: true,
    showCourses: true,
    showStudyVibes: true,
    showSessionHistory: true,
    showLocation: true,
    showAvatar: true
  });

  const [activeTab, setActiveTab] = useState<"overview" | "log">("overview");

  const totalMinutes = sessionHistory.reduce((acc, s) => acc + s.duration, 0);
  const totalHours = Math.floor(totalMinutes / 60);

  const removeCourse = (c: string) => setCourses((prev) => prev.filter((x) => x !== c));
  const addCourse = (c: string) => {
    if (c && !courses.includes(c)) setCourses((prev) => [...prev, c]);
    setCourseInput("");
  };

  const removeVibe = (v: string) => setVibes((prev) => prev.filter((x) => x !== v));
  const addVibe = (v: string) => {
    if (v && !vibes.includes(v)) setVibes((prev) => [...prev, v]);
    setVibeInput("");
  };

  // Preset options for study vibes
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

        setProgram(data.program || "");
        setTempProgram(data.program || "");

        setYear(data.year || "");
        setTempYear(data.year || "");

        setAvatar(data.avatar || "");

        setPrivacySettings(data.privacySettings || {});

      } catch (err) {
        console.error("Failed to load profile", err);
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, []);

  // Save profile to backend
  async function saveProfile(payload: {
    courses?: string[];
    studyVibes?: string[];
    bio?: string;
    program?: string;
    year?: string;
    avatar?: string;
    privacySettings?: Record<string, boolean>;
    notifications?: Record<string, boolean>;
    location?: string;
    isOnline?: boolean;
    twoFAEnabled?: boolean;
    autoTimeout?: number;


  } = {}) {

    try {
      await fetch(`http://localhost:8080/api/studentcontroller/profile/update/${userId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } catch (err) {
      console.error("Failed to save profile", err);
    }
  }

function handleAvatarClick() {
  fileInputRef.current?.click();
}

async function handleAvatarChange(e: React.ChangeEvent<HTMLInputElement>) {
  const file = e.target.files?.[0];
  if (!file) return;

  // Read file 
  const reader = new FileReader();
  reader.onloadend = async () => {
    const base64 = reader.result as string;

    // Send to backend
    await fetch(`http://localhost:8080/api/studentcontroller/${userId}/profile-picture`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ Avatar: base64 })
    });

    // Update UI immediately
    setAvatar(base64);
  };

  reader.readAsDataURL(file);
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
          <div className="relative w-24 h-24">
            <img
            src={avatar || "/default-avatar.png"}
            onClick={handleAvatarClick}
            className="w-24 h-24 rounded-full object-cover cursor-pointer hover:opacity-80 transition"
          />

            <input
              type="file"
              accept="image/*"
              ref={fileInputRef}
              onChange={handleAvatarChange}
              className="hidden"
            />
          </div>

          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
            <div className="flex-1">

              {/* Name + Status */}
              <div className="flex items-center gap-2 mb-1">
                <h1 className="text-slate-900" style={{ fontSize: "1.35rem", fontWeight: 700 }}>
                  {currentUser.name}
                </h1>
                <div className="flex items-center gap-1">
                  <div className="w-2 h-2 rounded-full bg-green-500"></div>
                  <span className="text-xs text-green-600">Online</span>
                </div>
              </div>

              {/* Program + Year + Email */}
              <div className="flex flex-wrap gap-3 text-sm text-slate-500">

                {/* PROGRAM */}
                <span className="flex items-center gap-1.5">
                  <GraduationCap size={14} className="text-blue-500" />
                  {program}
                </span>

                {/* YEAR */}
                <span className="flex items-center gap-1.5">
                  <Star size={14} className="text-orange-400" />
                  {year}
                </span>

                <span className="flex items-center gap-1.5">
                  <Mail size={14} className="text-slate-400" />
                  {currentUser.email}
                </span>
              </div>

              <div className="flex items-center gap-1.5 mt-1.5 text-sm text-slate-500">
                <MapPin size={14} className="text-slate-400" />
                <span>{currentUser.location}</span>
              </div>

              <p className="text-xs text-slate-400 mt-1">Member since {currentUser.joinedDate}</p>
            </div>
             {/* Edit Profile Button */}
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
          </div><br></br>
          {/* INLINE EDITORS FOR PROGRAM + YEAR */}
          <div className="mt-4 space-y-4">

            {/* PROGRAM EDITOR */}
            <div>
              <div className="flex items-center gap-2 text-sm text-slate-600 mb-1">
                <GraduationCap size={14} className="text-blue-500" />
                <span className="font-medium">Major</span>
              </div>

              {editingProgram ? (
                <div className="flex items-center gap-3">
                  <input
                    value={tempProgram}
                    onChange={(e) => setTempProgram(e.target.value)}
                    className="border border-slate-300 rounded-lg px-3 py-2 w-64 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  />

                  <button
                    onClick={async () => {
                      await saveProfile({
                        courses,
                        studyVibes: vibes,
                        bio,
                        program: tempProgram,
                        year,
                        privacySettings
                      });
                      setProgram(tempProgram);                  
                      setEditingProgram(false);
                    }}
                    className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
                  >
                    Save
                  </button>

                  <button
                    onClick={() => setEditingProgram(false)}
                    className="px-3 py-1.5 bg-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-300 transition"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => {
                    setTempProgram(program);
                    setEditingProgram(true);
                  }}
                  className="text-blue-600 text-sm underline hover:text-blue-700"
                >
                  Edit Major
                </button>
              )}
            </div>

            {/* YEAR EDITOR */}
            <div>
              <div className="flex items-center gap-2 text-sm text-slate-600 mb-1">
                <Star size={14} className="text-orange-400" />
                <span className="font-medium">Year</span>
              </div>

              {editingYear ? (
                <div className="flex items-center gap-3">
                  <select
                    value={tempYear}
                    onChange={(e) => setTempYear(e.target.value)}
                    className="border border-slate-300 rounded-lg px-3 py-2 w-40 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  >
                    <option value="1st Year">1st Year</option>
                    <option value="2nd Year">2nd Year</option>
                    <option value="3rd Year">3rd Year</option>
                    <option value="4th Year">4th Year</option>
                  </select>

                  <button
                    onClick={async () => {
                      await saveProfile({
                       courses,
                       studyVibes: vibes,
                       bio,
                       program,
                       year: tempYear,
                       privacySettings       
                       });
                      setYear(tempYear);
                      setEditingYear(false);
                    }}
                    className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
                  >
                    Save
                  </button>

                  <button
                    onClick={() => setEditingYear(false)}
                    className="px-3 py-1.5 bg-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-300 transition"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => {
                    setTempYear(year);
                    setEditingYear(true);
                  }}
                  className="text-blue-600 text-sm underline hover:text-blue-700"
                >
                  Edit Year
                </button>
              )}
            </div>

          </div>

        </div>
      </div>
                    <h2 className="text-lg font-semibold text-slate-800 mb-3">Bio</h2> 
          {/* BIO CARD */} <div
          className="bg-white rounded-2xl border border-slate-200 p-6 mb-5 cursor-pointer"
          onClick={() => !editingBio && setEditingBio(true)}
        >
          {/* DISPLAY BIO */}
          {!editingBio && (
            <p className="text-slate-700 whitespace-pre-line">
              {bio || "No bio added yet."}
            </p>
          )}

              {/* EDIT BIO */}
              {editingBio && (
                <div>
                  <textarea
                    value={tempBio}
                    onChange={(e) => setTempBio(e.target.value)}
                    className="w-full border border-slate-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-500"
                    rows={4}
                  />

                  <div className="flex gap-3 mt-3">
                    <button
                      onClick={async () => {
                        await saveProfile({
                          courses,
                          studyVibes: vibes,
                          bio: tempBio,
                          program,
                          year,
                          privacySettings
                        });
                        setBio(tempBio);
                        setEditingBio(false);
                      }}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
        >
          Save
        </button>

        <button
          onClick={() => {
            setTempBio(bio);
            setEditingBio(false);
          }}
          className="px-4 py-2 bg-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-300 transition"
        >
          Cancel
        </button>
      </div>
    </div>
  )}
</div>

                {/* PRIVACY SETTINGS CARD */}
                <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
                  <h2 className="text-lg font-semibold text-slate-800 mb-3">Privacy Settings</h2>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Bio</span>
                    <input
                    type="checkbox"
                    checked={privacySettings.showBio}
                    onChange={(e) =>
                      setPrivacySettings({
                        ...privacySettings,
                        showBio: e.target.checked
                      })
                    }
                  />
                  </label>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Major</span>
                    <input
                    type="checkbox"
                    checked={privacySettings.showProgram}
                    onChange={(e) =>
                      setPrivacySettings({
                        ...privacySettings,
                        showProgram: e.target.checked
                      })
                    }
/>
                  </label>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Year</span>
                    <input
                    type="checkbox"
                    checked={privacySettings.showYear}
                    onChange={(e) =>
                      setPrivacySettings({
                        ...privacySettings,
                        showYear: e.target.checked
                      })
                    }
                  />
                  </label>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Courses</span>
                    <input
                    type="checkbox"
                    checked={privacySettings.showCourses}
                    onChange={(e) =>
                      setPrivacySettings({
                        ...privacySettings,
                        showCourses: e.target.checked
                      })
                    }
                  />
                  </label>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Study Vibes</span>
                    <input
                    type="checkbox"
                    checked={privacySettings.showStudyVibes}
                    onChange={(e) =>
                      setPrivacySettings({
                        ...privacySettings,
                        showStudyVibes: e.target.checked
                      })
                    }
                  />
                  </label>

                  <label className="flex items-center justify-between py-2">
                    <span>Show Email</span>
                      <input
                      type="checkbox"
                      checked={privacySettings.showEmail}
                      onChange={(e) =>
                        setPrivacySettings({
                          ...privacySettings,
                          showEmail: e.target.checked
                        })
                      }
                    />
                    
                  </label>

                  <button
                    onClick={() =>
                      saveProfile({
                        courses,
                        studyVibes: vibes,
                        bio,
                        program,
                        year,
                        privacySettings
                      })
                    }
                    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
                  >
                    Save Privacy Settings
                  </button>
                </div>

          

      {/* COURSES SECTION */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
        <h2 className="text-lg font-semibold text-slate-800 mb-3">Courses</h2>

        {/* Existing courses */}
        <div className="flex flex-wrap gap-2 mb-4">
          {courses.map((c) => (
            <span
              key={c}
              className="px-3 py-1 bg-blue-50 text-blue-700 border border-blue-200 rounded-full text-sm flex items-center gap-2"
            >
              {c}
              <X
                size={14}
                className="cursor-pointer hover:text-blue-900"
                onClick={() => removeCourse(c)}
              />
            </span>
          ))}
        </div>

        {/* Add course (preset + custom) */}
        <div className="flex items-center gap-3">
          <select
            onChange={(e) => addCourse(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 w-48"
          >
            <option value="">Add preset course...</option>
            {courseOptions.map((opt) => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>

          <input
            value={courseInput}
            onChange={(e) => setCourseInput(e.target.value)}
            placeholder="Custom course..."
            className="border border-slate-300 rounded-lg px-3 py-2 w-48"
          />

          <button
            onClick={() => addCourse(courseInput)}
            className="px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
          >
            Add
          </button>
        </div>
        <button
           onClick={() =>
            saveProfile({
            courses,
            studyVibes: vibes,
            bio, 
            program,
            year,
            privacySettings
      })
  }
  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
>
  Save Changes
</button>

</div>

      {/* STUDY VIBES SECTION */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
        <h2 className="text-lg font-semibold text-slate-800 mb-3">Study Vibes</h2>

        {/* Existing vibes */}
        <div className="flex flex-wrap gap-2 mb-4">
          {vibes.map((v) => (
            <span
              key={v}
              className={`px-3 py-1 border rounded-full text-sm flex items-center gap-2 ${vibeColors[v] || "bg-slate-100 text-slate-700 border-slate-300"}`}
            >
              {v}
              <X
                size={14}
                className="cursor-pointer hover:text-slate-900"
                onClick={() => removeVibe(v)}
              />
            </span>
          ))}
        </div>

        {/* Add vibe (preset + custom) */}
        <div className="flex items-center gap-3">
          <select
            onChange={(e) => addVibe(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 w-48"
          >
            <option value="">Add preset vibe...</option>
            {studyVibeOptions.map((opt) => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>

          <input
            value={vibeInput}
            onChange={(e) => setVibeInput(e.target.value)}
            placeholder="Custom vibe..."
            className="border border-slate-300 rounded-lg px-3 py-2 w-48"
          />

          <button
            onClick={() => addVibe(vibeInput)}
            className="px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
          >
            Add
          </button>
        </div>
      <button
        onClick={() =>
          saveProfile({
           courses,
           studyVibes: vibes,
           bio,
           program,
           year,
           privacySettings
       })
  }
  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
>
  Save Changes
</button>

</div>

{/* LOCATION SECTION */}
<div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
  <h2 className="text-lg font-semibold text-slate-800 mb-3">Location</h2>

  <input
    value={location}
    onChange={(e) => setLocation(e.target.value)}
    placeholder="Enter your current study location..."
    className="border border-slate-300 rounded-lg px-3 py-2 w-full"
  />

  <button
    onClick={() =>
      saveProfile({
        courses,
        studyVibes: vibes,
        bio,
        program,
        year,
        privacySettings,
        location,
      })
    }
    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
  >
    Save Location
  </button>
</div>

{/* ONLINE STATUS */}
<div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
  <h2 className="text-lg font-semibold text-slate-800 mb-3">Online Status</h2>

  <label className="flex items-center justify-between py-2">
    <span>Show as Online</span>
    <input
      type="checkbox"
      checked={isOnline}
      onChange={(e) => setIsOnline(e.target.checked)}
    />
  </label>

  <button
    onClick={() =>
      saveProfile({
        courses,
        studyVibes: vibes,
        bio,
        program,
        year,
        privacySettings,
        isOnline,
      })
    }
    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
  >
    Save Online Status
  </button>
</div>

{/* SECURITY SETTINGS */}
<div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
  <h2 className="text-lg font-semibold text-slate-800 mb-3">Security Settings</h2>

  <label className="flex items-center justify-between py-2">
    <span>Enable Two-Factor Authentication</span>
    <input
      type="checkbox"
      checked={twoFAEnabled}
      onChange={(e) => setTwoFAEnabled(e.target.checked)}
    />
  </label>

  <label className="flex items-center justify-between py-2">
    <span>Auto Timeout (minutes)</span>
    <input
      type="number"
      value={autoTimeout}
      onChange={(e) => setAutoTimeout(Number(e.target.value))}
      className="border border-slate-300 rounded-lg px-3 py-1 w-24"
    />
  </label>

  <button
    onClick={() =>
      saveProfile({
        courses,
        studyVibes: vibes,
        bio,
        program,
        year,
        privacySettings,
        twoFAEnabled,
        autoTimeout,
      })
    }
    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
  >
    Save Security Settings
  </button>
</div>

{/* NOTIFICATION SETTINGS */}
<div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
  <h2 className="text-lg font-semibold text-slate-800 mb-3">Notifications</h2>

  {Object.keys(notifications).map((key) => (
    <label key={key} className="flex items-center justify-between py-2">
      <span>{key}</span>
      <input
        type="checkbox"
        checked={notifications[key]}
        onChange={(e) =>
          setNotifications({
            ...notifications,
            [key]: e.target.checked,
          })
        }
      />
    </label>
  ))}

  <button
    onClick={() =>
      saveProfile({
        courses,
        studyVibes: vibes,
        bio,
        program,
        year,
        privacySettings,
        notifications,
      })
    }
    className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
  >
    Save Notification Settings
  </button>
</div>
    </div>

    
  );
}