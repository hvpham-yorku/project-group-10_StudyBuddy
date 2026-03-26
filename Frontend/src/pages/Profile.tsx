import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Edit2, X, BookOpen, Clock, CalendarDays,
  Mail, GraduationCap, MapPin, Star, Flame
} from "lucide-react";
import { studyVibeOptions } from "../data/mockData";
import { yorkPrograms } from "../data/yorkPrograms";
import {
  formatDistance,
  getLastCampusLocation,
  isGeolocationPermissionDenied,
  getLocationPreference,
  setLocationPreference,
  shouldTrackLocationNow,
  syncTrackedLocationToProfile,
  watchCampusLocation,
  addLocationPreferenceListener,
  requestCurrentCampusLocation,
  setOnceLocationActive
} from "../lib/locationTracking";

interface SessionLogEvent {
  id: string;
  title: string;
  course: string;
  location: string;
  date: string;
  time: string;
  duration: number;
  status: string;
  role: string;
}

interface SessionLogSummary {
  totalMinutes: number;
  totalEvents: number;
  hostedCount: number;
  attendedCount: number;
}

interface SessionLogResponse {
  summary: SessionLogSummary;
  events: SessionLogEvent[];
}

export default function Profile() {
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);

  // STUDENT DATA
  const [student, setStudent] = useState<any>(null);
  const [joinedDate, setJoinedDate] = useState("");
  const [sessionLog, setSessionLog] = useState<SessionLogResponse | null>(null);
  const [sessionLogError, setSessionLogError] = useState("");
  const [loginStreak, setLoginStreak] = useState(0);

  // BIO
  const [editingBio, setEditingBio] = useState(false);
  const [bio, setBio] = useState("");
  const [tempBio, setTempBio] = useState("");

  // COURSES
  const [editingCourses, setEditingCourses] = useState(false);
  const [courses, setCourses] = useState<string[]>([]);
  const [courseInput, setCourseInput] = useState("");
  const [courseSuggestions, setCourseSuggestions] = useState<string[]>([]);
  const [courseSearchLoading, setCourseSearchLoading] = useState(false);
  const [courseInputError, setCourseInputError] = useState("");

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
  const [showProgramSuggestions, setShowProgramSuggestions] = useState(false);
  const programDropdownRef = useRef<HTMLDivElement>(null);

  // EMAIL
  const [email, setEmail] = useState("");


  // SECURITY
  const [twoFAEnabled, setTwoFAEnabled] = useState(false);
  const [autoTimeout, setAutoTimeout] = useState(0);
  const [isOnline, setIsOnline] = useState(false);
  const [location, setLocation] = useState("");
  const [liveCampusLocation, setLiveCampusLocation] = useState<any>(null); // Only populate when tracking is enabled
  const [locationTrackingEnabled, setLocationTrackingEnabled] = useState(false); // Start as false
  const [locationTrackingError, setLocationTrackingError] = useState("");
  const [locationPermissionPref, setLocationPermissionPref] = useState(getLocationPreference());

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
  const normalizeCourseValue = (value: string) =>
    value.toUpperCase().replace(/[^A-Z0-9]/g, "");

  const filteredPrograms = yorkPrograms
    .filter((p) => p.toLowerCase().includes(tempProgram.toLowerCase()))
    .slice(0, 8);

  const isValidProgram = yorkPrograms.includes(tempProgram);

  const removeCourse = (c: string) => setCourses((prev) => prev.filter((x) => x !== c));
  const addCourse = (c: string) => {
    const normalizedCourse = c.trim();
    if (normalizedCourse && !courses.includes(normalizedCourse)) {
      setCourses((prev) => [...prev, normalizedCourse]);
    }
    setCourseInput("");
    setCourseSuggestions([]);
    setCourseInputError("");
  };

  const addCourseFromInput = () => {
    const typed = courseInput.trim();
    if (!typed) {
      return;
    }

    const normalizedTyped = normalizeCourseValue(typed);
    const exactMatch = courseSuggestions.find(
      (course) => normalizeCourseValue(course) === normalizedTyped
    );

    if (exactMatch) {
      addCourse(exactMatch);
      return;
    }

    if (courseSuggestions.length === 1) {
      addCourse(courseSuggestions[0]);
      return;
    }

    setCourseInputError("Select a York course from the suggestions.");
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
        const token = localStorage.getItem("studyBuddyToken");
        
        const res = await fetch(`/api/studentcontroller/profile`, {
          headers: {
            "Authorization": "Bearer " + token
          }
        });
        const data = await res.json();
        
        setStudent(data);
        setBio(data.bio || "");
        setTempBio(data.bio || "");
        setCourses(data.courses || []);
        setVibes(data.studyVibes || []);
        setProgram(data.program || "");
        setTempProgram(data.program || "");
        setEmail(data.email || "");
        setYear(data.year || "");
        setTempYear(data.year || "");
        setAvatar(data.avatar || "");
        setPrivacySettings(data.privacySettings || {});
        setLocation(data.location || "");
        setIsOnline(data.isOnline ?? false);
        setTwoFAEnabled(data.twoFAEnabled ?? false);
        setAutoTimeout(data.autoTimeout ?? 0);
        setJoinedDate(data.joinedDate || "");
        setLoginStreak(data.loginStreak ?? 0);
      
        const sessionRes = await fetch(`/api/studentcontroller/profile/session-log`, {
          headers: {
            "Authorization": "Bearer " + token
          }
        });

        if (!sessionRes.ok) {
          throw new Error("Failed to load study session log");
        }

        const sessionData: SessionLogResponse = await sessionRes.json();
        setSessionLog(sessionData);

      } catch (err) {
        console.error("Failed to load profile", err);
        setSessionLogError("Could not load study session history.");
      } finally {
        setLoading(false);
      }
    }
    loadProfile();
  }, []);

  useEffect(() => {
    // Function to evaluate and update location tracking state
    const updateLocationState = () => {
      const token = localStorage.getItem("studyBuddyToken");
      const pref = getLocationPreference();
      const canTrack = shouldTrackLocationNow(token);

      setLocationPermissionPref(pref);
      setLocationTrackingEnabled(canTrack);
      
      // When tracking is disabled, clear all location data
      if (!canTrack) {
        setLiveCampusLocation(null);
        // If preference is explicitly "reject", also clear the input field to ensure clean manual mode
        if (pref === "reject") {
          setLocation("");
        }
      } else {
        // Restore last known location if tracking is enabled
        const lastLocation = getLastCampusLocation();
        setLiveCampusLocation(lastLocation);
        if (lastLocation) {
          setLocation(lastLocation.buildingName);
        }
      }
    };

    // Initial state evaluation
    updateLocationState();

    // Listen for preference changes (e.g., from Dashboard or settings change)
    const unsubscribe = addLocationPreferenceListener(updateLocationState);

    return unsubscribe;
  }, []);

  useEffect(() => {
    if (!locationTrackingEnabled) {
      return;
    }

    const stopWatch = watchCampusLocation({
      onUpdate: (reading) => {
        // Only update location display if tracking is still enabled
        // (safety check in case state changed between watch callback and execution)
        if (getLocationPreference() !== "reject") {
          setLiveCampusLocation(reading);
          setLocation(reading.buildingName);
          setLocationTrackingError("");
          syncTrackedLocationToProfile(reading.buildingName, {
            latitude: reading.latitude,
            longitude: reading.longitude
          }).catch((err) => {
            console.error("Failed to sync tracked location", err);
          });
        }
      },
      onError: (error) => {
        if (isGeolocationPermissionDenied(error)) {
          setLocationPreference("reject");
          setLocationTrackingEnabled(false);
          setLiveCampusLocation(null);
          setLocationTrackingError("Location access denied. You can set location manually.");
          return;
        }

        setLocationTrackingError("Location tracking is blocked in your browser settings.");
      }
    });

    return stopWatch;
  }, [locationTrackingEnabled]);

  useEffect(() => {
    const query = courseInput.trim();
    if (!query) {
      setCourseSuggestions([]);
      setCourseSearchLoading(false);
      setCourseInputError("");
      return;
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(async () => {
      setCourseSearchLoading(true);
      try {
        const response = await fetch(
          `/api/courses/search?q=${encodeURIComponent(query)}&limit=20`,
          { signal: controller.signal }
        );

        if (!response.ok) {
          throw new Error("Failed to load course suggestions");
        }

        const suggestions: string[] = await response.json();
        setCourseSuggestions(suggestions);
      } catch (error: any) {
        if (error?.name !== "AbortError") {
          setCourseSuggestions([]);
        }
      } finally {
        setCourseSearchLoading(false);
      }
    }, 180);

    return () => {
      clearTimeout(timeoutId);
      controller.abort();
    };
  }, [courseInput]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        programDropdownRef.current &&
        !programDropdownRef.current.contains(event.target as Node)
      ) {
        setShowProgramSuggestions(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);
  // Save profile to backend
 async function saveProfile(payload: {
    courses?: string[];
    studyVibes?: string[];
    bio?: string;
    program?: string;
    email?: string;
    year?: string;
    avatar?: string;
    privacySettings?: Record<string, boolean>;
    location?: string;
    isOnline?: boolean;
    twoFAEnabled?: boolean;
    autoTimeout?: number;
  } = {}) {

    const fullPayload = {
      courses,
      studyVibes: vibes,
      bio,
      program,
      year,
      email,
      avatar,
      privacySettings,
      location,
      isOnline,
      twoFAEnabled,
      autoTimeout,
      ...payload
    };

    try {
      const token = localStorage.getItem("studyBuddyToken");
      
      await fetch(`/api/studentcontroller/profile/update`, {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify(fullPayload)
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
      const token = localStorage.getItem("studyBuddyToken");

      try {
        // Send to backend
        const response = await fetch(`/api/studentcontroller/profile/avatar`, {
          method: "PUT",
          headers: { 
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
          },
          body: JSON.stringify({ avatar: base64 })
        });

        if (!response.ok) {
          throw new Error("Failed to save avatar");
        }

        // Update UI immediately
        setAvatar(base64);
      } catch (err) {
        console.error("Failed to update avatar", err);
      } finally {
        // Reset so selecting the same file again still triggers onChange
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
      }
    };

    reader.readAsDataURL(file);
  }

  const totalMinutes = sessionLog?.summary.totalMinutes ?? 0;
  const totalHours = Math.floor(totalMinutes / 60);
  const remainingMinutes = totalMinutes % 60;

  const formatStudyTime = () => {
    if (totalMinutes <= 0) return "0 min";
    if (totalHours <= 0) return `${totalMinutes} min`;
    return `${totalHours}h ${remainingMinutes}m`;
  };

  const formatEventDate = (date: string, time: string) => {
    const parsed = new Date(`${date}T${time}`);
    if (Number.isNaN(parsed.getTime())) {
      return `${date || "Unknown date"} ${time || ""}`.trim();
    }
    return parsed.toLocaleString("en-CA", {
      weekday: "short",
      month: "short",
      day: "numeric",
      hour: "numeric",
      minute: "2-digit"
    });
  };

  const roleStyles: Record<string, string> = {
    "Hosted": "bg-blue-100 text-blue-700 border-blue-200",
    "Attended": "bg-emerald-100 text-emerald-700 border-emerald-200",
    "Hosted & Attended": "bg-amber-100 text-amber-700 border-amber-200"
  };

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

          {/* Avatar + Streak Badge */}
          <div className="flex items-end gap-4">
            <div className="relative w-24 h-24">
              <div 
                onClick={handleAvatarClick}
                className="w-24 h-24 rounded-full overflow-hidden bg-blue-100 cursor-pointer hover:opacity-80 transition flex items-center justify-center border-4 border-white shadow-sm"
              >
                {avatar ? (
                  <img src={avatar} alt="avatar" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-blue-600" style={{ fontWeight: 700, fontSize: "2rem" }}>
                    {(student?.fullName || student?.userId || "?").charAt(0).toUpperCase()}
                  </span>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleAvatarChange}
              />
            </div>

            {/* Streak Badge */}
            <div className="mb-1 flex flex-col items-center px-4 py-2 bg-gradient-to-br from-orange-50 to-orange-100 border border-orange-200 rounded-2xl shadow-sm min-w-[70px]">
              <Flame size={22} className="text-orange-500 mb-0.5" />
              <span className="text-orange-700 leading-none" style={{ fontWeight: 700, fontSize: "1.4rem" }}>
                {loginStreak}
              </span>
              <span className="text-orange-400 text-xs mt-0.5" style={{ fontWeight: 500 }}>
                {loginStreak === 1 ? "day" : "days"}
              </span>
              <span className="text-orange-300 text-xs">streak</span>
            </div>
          </div>

          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
            <div className="flex-1">

              {/* Name + Status */}
              <div className="flex items-center gap-2 mb-1">
                <h1 className="text-slate-900" style={{ fontSize: "1.35rem", fontWeight: 700 }}>
                  {student?.fullName}
                </h1>
                <div className="flex items-center gap-1">
                  <div
                    className={`w-2 h-2 rounded-full ${isOnline ? "bg-green-500" : "bg-slate-400"
                      }`}
                  ></div>

                  <span
                    className={`text-xs ${isOnline ? "text-green-600" : "text-slate-500"
                      }`}
                  >
                    {isOnline ? "Online" : "Offline"}
                  </span>
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
                  {student?.email}
                </span>
              </div>

              <div className="flex items-center gap-1.5 mt-1.5 text-sm text-slate-500">
                <MapPin size={14} className="text-slate-400" />
                <span>{liveCampusLocation?.buildingName || location}</span>
                {liveCampusLocation && (
                  <span className="text-xs text-slate-400">({formatDistance(liveCampusLocation.distanceMeters)})</span>
                )}
              </div>

              <p className="text-xs text-slate-400 mt-1">Member since {joinedDate || "Recently"}</p>
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
            <div ref={programDropdownRef}>
              <div className="flex items-center gap-2 text-sm text-slate-600 mb-1">
                <GraduationCap size={14} className="text-blue-500" />
                <span className="font-medium">Major</span>
              </div>

              {editingProgram ? (
                <div className="relative w-80">
                  <div className="flex items-center gap-3">
                    <input
                      value={tempProgram}
                      onChange={(e) => {
                        setTempProgram(e.target.value);
                        setShowProgramSuggestions(true);
                      }}
                      onFocus={() => setShowProgramSuggestions(true)}
                      placeholder="Search York major..."
                      className="border border-slate-300 rounded-lg px-3 py-2 w-full focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    />

                    <button
                      onClick={async () => {
                        if (!yorkPrograms.includes(tempProgram)) {
                          alert("Please select a valid York University major.");
                          return;
                        }

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
                        setShowProgramSuggestions(false);
                      }}
                      className="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
                    >
                      Save
                    </button>

                    <button
                      onClick={() => {
                        setTempProgram(program);
                        setEditingProgram(false);
                        setShowProgramSuggestions(false);
                      }}
                      className="px-3 py-1.5 bg-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-300 transition"
                    >
                      Cancel
                    </button>
                  </div>

                  {showProgramSuggestions && tempProgram.trim() !== "" && (
                    <div className="absolute z-20 mt-1 w-full bg-white border border-slate-200 rounded-lg shadow-lg max-h-60 overflow-y-auto">
                      {filteredPrograms.length > 0 ? (
                        filteredPrograms.map((option) => (
                          <button
                            key={option}
                            type="button"
                            onClick={() => {
                              setTempProgram(option);
                              setShowProgramSuggestions(false);
                            }}
                            className="block w-full text-left px-3 py-2 hover:bg-slate-100 text-sm"
                          >
                            {option}
                          </button>
                        ))
                      ) : (
                        <div className="px-3 py-2 text-sm text-slate-500">
                          No matching York program found
                        </div>
                      )}
                    </div>
                  )}

                  {!isValidProgram && tempProgram.trim() !== "" && (
                    <p className="text-sm text-red-500 mt-2">
                      Select a valid York University major from the list.
                    </p>
                  )}
                </div>
              ) : (
                <button
                  onClick={() => {
                    setTempProgram(program);
                    setEditingProgram(true);
                    setShowProgramSuggestions(false);
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

      {/* STUDY SESSION LOG */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
        <div className="flex items-start justify-between gap-4 mb-4">
          <div>
            <h2 className="text-lg font-semibold text-slate-800">Study Session Log</h2>
            <p className="text-sm text-slate-500">Your hosted and attended past sessions</p>
          </div>
          <div className="text-xs px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
            Past events only
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-5">
          <div className="rounded-xl border border-blue-200 bg-blue-50 p-4">
            <div className="text-xs text-blue-700 mb-1" style={{ fontWeight: 600 }}>Total Study Time</div>
            <div className="flex items-center gap-2 text-blue-900">
              <Clock size={16} />
              <span style={{ fontWeight: 700, fontSize: "1.1rem" }}>{formatStudyTime()}</span>
            </div>
          </div>
          <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4">
            <div className="text-xs text-emerald-700 mb-1" style={{ fontWeight: 600 }}>Total Events</div>
            <div className="flex items-center gap-2 text-emerald-900">
              <CalendarDays size={16} />
              <span style={{ fontWeight: 700, fontSize: "1.1rem" }}>{sessionLog?.summary.totalEvents ?? 0}</span>
            </div>
          </div>
          <div className="rounded-xl border border-orange-200 bg-orange-50 p-4">
            <div className="text-xs text-orange-700 mb-1" style={{ fontWeight: 600 }}>Hosted / Attended</div>
            <div className="text-orange-900" style={{ fontWeight: 700, fontSize: "1.1rem" }}>
              {sessionLog?.summary.hostedCount ?? 0} / {sessionLog?.summary.attendedCount ?? 0}
            </div>
          </div>
        </div>

        {sessionLogError ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {sessionLogError}
          </div>
        ) : (
          <div className="max-h-80 overflow-y-auto overscroll-y-contain pr-1 space-y-3">
            {sessionLog?.events && sessionLog.events.length > 0 ? (
              sessionLog.events.map((session) => (
                <div key={session.id} className="rounded-xl border border-slate-200 p-4 hover:border-slate-300 transition-colors">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <h3 className="text-slate-800" style={{ fontWeight: 600 }}>{session.title || "Untitled Session"}</h3>
                      <div className="flex flex-wrap items-center gap-3 mt-1 text-sm text-slate-500">
                        <span className="flex items-center gap-1.5"><BookOpen size={14} />{session.course || "No course"}</span>
                        <span className="flex items-center gap-1.5"><MapPin size={14} />{session.location || "No location"}</span>
                      </div>
                    </div>
                    <span className={`px-2.5 py-1 rounded-full border text-xs ${roleStyles[session.role] || "bg-slate-100 text-slate-700 border-slate-200"}`}>
                      {session.role}
                    </span>
                  </div>

                  <div className="mt-2 flex flex-wrap items-center gap-4 text-sm text-slate-600">
                    <span className="flex items-center gap-1.5"><CalendarDays size={14} />{formatEventDate(session.date, session.time)}</span>
                    <span className="flex items-center gap-1.5"><Clock size={14} />{session.duration} min</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-500 text-center">
                No past hosted or attended sessions yet.
              </div>
            )}
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

        <label className="flex items-center justify-between py-2">
          <span>Show Location</span>
          <input
            type="checkbox"
            checked={privacySettings.showLocation}
            onChange={(e) =>
              setPrivacySettings({
                ...privacySettings,
                showLocation: e.target.checked
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
              email,
              privacySettings,
              location
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

        {/* Add course (York catalog autocomplete) */}
        <div className="flex flex-col gap-2">
          <div className="flex items-start gap-3">
            <div className="relative w-full max-w-xs">
              <input
                value={courseInput}
                onChange={(e) => {
                  setCourseInput(e.target.value);
                  setCourseInputError("");
                }}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    addCourseFromInput();
                  }
                }}
                placeholder="Type a York course (e.g. EECS 1021)..."
                className="border border-slate-300 rounded-lg px-3 py-2 w-full"
              />

              {courseInput.trim() && (
                <div className="absolute z-20 mt-1 w-full max-h-56 overflow-y-auto rounded-lg border border-slate-200 bg-white shadow-lg">
                  {courseSearchLoading ? (
                    <div className="px-3 py-2 text-sm text-slate-500">Searching courses...</div>
                  ) : courseSuggestions.length > 0 ? (
                    courseSuggestions.map((course) => (
                      <button
                        key={course}
                        type="button"
                        onClick={() => addCourse(course)}
                        className="w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-blue-50"
                      >
                        {course}
                      </button>
                    ))
                  ) : (
                    <div className="px-3 py-2 text-sm text-slate-500">No matching York courses.</div>
                  )}
                </div>
              )}
            </div>

            <button
              onClick={addCourseFromInput}
              className="px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
            >
              Add
            </button>
          </div>

          {courseInputError && (
            <p className="text-sm text-red-600">{courseInputError}</p>
          )}
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

        <div className="mb-3 text-sm text-slate-600">
          {locationTrackingEnabled ? (
            <span className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
              Auto-updating from GPS ({locationPermissionPref === "always" ? "Allow Always" : "Allow Once"})
            </span>
          ) : (
            <span className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 border border-slate-200">
              Manual location mode
            </span>
          )}
        </div>

        <input
          value={locationTrackingEnabled ? (liveCampusLocation?.buildingName || location) : location}
          onChange={(e) => setLocation(e.target.value)}
          placeholder={locationTrackingEnabled ? "Location is auto-detected" : "Enter your current study location..."}
          readOnly={locationTrackingEnabled}
          className={`border rounded-lg px-3 py-2 w-full ${locationTrackingEnabled ? "bg-slate-100 border-slate-200 text-slate-600" : "border-slate-300"}`}
        />

        {locationTrackingError && (
          <p className="text-sm text-red-600 mt-2">{locationTrackingError}</p>
        )}

        {locationPermissionPref === "reject" && (
          <div className="mt-3 space-y-2">
            <p className="text-xs text-slate-500">Location tracking is disabled. You can change this:</p>
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setLocationPreference(null);
                  setLocationTrackingError("");
                }}
                className="flex-1 text-xs px-3 py-1.5 bg-blue-500 hover:bg-blue-600 text-white rounded transition"
              >
                Ask Again on Dashboard
              </button>
              <button
                onClick={async () => {
                  try {
                    const token = localStorage.getItem("studyBuddyToken");
                    // Set "once" active first before requesting
                    setOnceLocationActive(true, token);
                    const reading = await requestCurrentCampusLocation();
                    setLiveCampusLocation(reading);
                    await syncTrackedLocationToProfile(reading.buildingName, {
                      latitude: reading.latitude,
                      longitude: reading.longitude
                    });
                    setLocationPreference(null); // Reset preference so prompt shows on dashboard next time
                    setLocationTrackingEnabled(true);
                    setLocationTrackingError("");
                  } catch {
                    setLocationTrackingError("Could not access your location. Allow in browser settings.");
                    setOnceLocationActive(false);
                  }
                }}
                className="flex-1 text-xs px-3 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded transition"
              >
                Enable for This Session
              </button>
            </div>
          </div>
        )}

        <button
          disabled={locationTrackingEnabled}
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
          className={`mt-4 px-4 py-2 rounded-lg text-sm transition ${locationTrackingEnabled ? "bg-slate-200 text-slate-500 cursor-not-allowed" : "bg-blue-600 text-white hover:bg-blue-700"}`}
        >
          {locationTrackingEnabled ? "Location Auto-Synced" : "Save Location"}
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

    </div>

  );
}
