import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

export default function ProfileViewer() {
  const { id } = useParams(); // /profile/:id
  const [student, setStudent] = useState<Student | null>(null);
  const [loading, setLoading] = useState(true);

type Student = {
  id: string;
  name: string;
  email: string;
  major?: string;
  year?: string;
  bio?: string;
  profilePic?: string;
  courses?: string[];
  studyVibes?: string[];
  totalStudyHours?: number;
  totalSessions?: number;
  isOnline?: boolean;
  location?: string;
  joinedDate?: string;
  privacySettings: Record<string, boolean>;
};

  useEffect(() => {
    async function loadStudent() {
      try {
        const res = await fetch(`http://localhost:8080/api/studentcontroller/${id}`);
        const data = await res.json();
        setStudent(data);
      } catch (err) {
        console.error("Failed to load student", err);
      } finally {
        setLoading(false);
      }
    }

    loadStudent();
  }, [id]);

  if (loading) return <p className="p-6">Loading...</p>;
  if (!student) return <p className="p-6">Student not found.</p>;

  const privacy = student.privacySettings || {};

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">{student.name}</h1>

      {/* PROFILE PIC */}
      {privacy.showProfilePic !== false && (
        <img
          src={student.profilePic}
          alt="Profile"
          className="w-32 h-32 rounded-full mb-4"
        />
      )}

      {/* EMAIL */}
      {privacy.showEmail !== false && (
        <p className="text-slate-700 mb-2">
          <strong>Email:</strong> {student.email}
        </p>
      )}

      {/* LOCATION */}
      {privacy.showLocation !== false && (
        <p className="text-slate-700 mb-2">
          <strong>Location:</strong> {student.location}
        </p>
      )}

      {/* COURSES */}
      {privacy.showCourses !== false && (
        <div className="mb-4">
          <h2 className="font-semibold mb-1">Courses</h2>
          <ul className="list-disc ml-5">
            {student.courses?.map((c) => (
              <li key={c}>{c}</li>
            ))}
          </ul>
        </div>
      )}

      {/* STUDY VIBES */}
      {privacy.showStudyVibes !== false && (
        <div className="mb-4">
          <h2 className="font-semibold mb-1">Study Vibes</h2>
          <ul className="list-disc ml-5">
            {student.studyVibes?.map((v) => (
              <li key={v}>{v}</li>
            ))}
          </ul>
        </div>
      )}

      {/* SESSION HISTORY */}
      {privacy.showSessionHistory !== false && (
        <p className="text-slate-700">
          <strong>Total Sessions:</strong> {student.totalSessions}
        </p>
      )}
    </div>
  );
}