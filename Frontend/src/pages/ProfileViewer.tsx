import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { GraduationCap, Star, Mail, MapPin } from "lucide-react";

export default function ProfileViewer() {
  const { id } = useParams(); // later: /profile/:id
  const userId = id || "123"; // stub fallback

  const [loading, setLoading] = useState(true);
  const [student, setStudent] = useState<any>(null);

  useEffect(() => {
    async function load() {
      try {
        const res = await fetch(`http://localhost:8080/api/studentcontroller/${userId}`);
        const data = await res.json();
        setStudent(data);
      } catch (err) {
        console.error("Failed to load profile", err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [userId]);

  if (loading) return <p className="p-6">Loading profile...</p>;
  if (!student) return <p className="p-6">Profile not found.</p>;

  const p = student.privacySettings || {};

  return (
    <div className="p-6 max-w-3xl mx-auto">

      {/* Header */}
      <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden mb-5">
        <div className="h-28 bg-gradient-to-r from-blue-800 to-blue-700 relative">
          <div className="absolute inset-0 bg-gradient-to-r from-blue-800/80 to-orange-700/30"></div>
        </div>

        <div className="px-6 pb-6">
          {/* Avatar */}
          <div className="relative w-24 h-24 -mt-12 mb-3">
            {p.showAvatar !== false ? (
              <img
                src={student.avatar || "/default-avatar.png"}
                className="w-24 h-24 rounded-full object-cover border-4 border-white"
              />
            ) : (
              <div className="w-24 h-24 rounded-full bg-slate-300 border-4 border-white"></div>
            )}
          </div>

          {/* Name + Status */}
          <h1 className="text-xl font-bold text-slate-900">{student.name}</h1>

          <div className="flex items-center gap-1 mt-1">
            <div
              className={`w-2 h-2 rounded-full ${
                student.isOnline ? "bg-green-500" : "bg-slate-400"
              }`}
            ></div>
            <span
              className={`text-xs ${
                student.isOnline ? "text-green-600" : "text-slate-500"
              }`}
            >
              {student.isOnline ? "Online" : "Offline"}
            </span>
          </div>

          {/* Program + Year + Email */}
          <div className="flex flex-wrap gap-3 text-sm text-slate-500 mt-3">
            {p.showProgram !== false && (
              <span className="flex items-center gap-1.5">
                <GraduationCap size={14} className="text-blue-500" />
                {student.program}
              </span>
            )}

            {p.showYear !== false && (
              <span className="flex items-center gap-1.5">
                <Star size={14} className="text-orange-400" />
                {student.year}
              </span>
            )}

            {p.showEmail !== false && (
              <span className="flex items-center gap-1.5">
                <Mail size={14} className="text-slate-400" />
                {student.email}
              </span>
            )}
          </div>

          {/* Location */}
          {p.showLocation !== false && (
            <div className="flex items-center gap-1.5 mt-1.5 text-sm text-slate-500">
              <MapPin size={14} className="text-slate-400" />
              <span>{student.location || "No location set"}</span>
            </div>
          )}

          <p className="text-xs text-slate-400 mt-1">
            Member since {student.joinedDate}
          </p>
        </div>
      </div>

      {/* Bio */}
      {p.showBio !== false && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">Bio</h2>
          <p className="text-slate-700 whitespace-pre-line">
            {student.bio || "No bio added yet."}
          </p>
        </div>
      )}

      {/* Courses */}
      {p.showCourses !== false && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">Courses</h2>
          <div className="flex flex-wrap gap-2">
            {student.courses?.map((c: string) => (
              <span
                key={c}
                className="px-3 py-1 bg-blue-50 text-blue-700 border border-blue-200 rounded-full text-sm"
              >
                {c}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Study Vibes */}
      {p.showStudyVibes !== false && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 mb-5">
          <h2 className="text-lg font-semibold text-slate-800 mb-3">Study Vibes</h2>
          <div className="flex flex-wrap gap-2">
            {student.studyVibes?.map((v: string) => (
              <span
                key={v}
                className="px-3 py-1 bg-slate-100 text-slate-700 border border-slate-300 rounded-full text-sm"
              >
                {v}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}