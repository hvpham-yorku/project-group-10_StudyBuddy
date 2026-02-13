import React, { useState, useEffect } from "react";
import TopBar from "./TopBar";
import SessionLog from "./SessionLog";

const STUDENT_STORAGE_KEY = "studentProfile";

// Default profile data
const DEFAULT_STUDENT = {
  name: "John Doe",
  email: "john@my.yorku.ca",
  major: "Software Engineering",
  bio: "",
  courses: ["EECS 2311", "MATH 1013"],
  preferences: ["Quiet Focus"],
};

export default function StudentProfile() {
  const [student, setStudent] = useState(DEFAULT_STUDENT);
  const [newCourse, setNewCourse] = useState("");
  const [newPref, setNewPref] = useState("");

  // Load from localStorage on first mount
  useEffect(() => {
    const saved = localStorage.getItem(STUDENT_STORAGE_KEY);
    if (saved) {
      try {
        setStudent(JSON.parse(saved));
      } catch (err) {
        console.error("Failed to parse saved profile:", err);
      }
    }
  }, []);

  // Save to localStorage whenever student changes
  useEffect(() => {
    localStorage.setItem(STUDENT_STORAGE_KEY, JSON.stringify(student));
  }, [student]);

// Manage courses and preferences with add/remove functions
  function addCourse() {
    const c = newCourse.trim();
    if (!c || student.courses.includes(c)) return;

    setStudent({ ...student, courses: [...student.courses, c] });
    setNewCourse("");
  }

  function removeCourse(course) {
    setStudent({
      ...student,
      courses: student.courses.filter((x) => x !== course),
    });
  }

  function addPref() {
    const p = newPref.trim();
    if (!p || student.preferences.includes(p)) return;

    setStudent({ ...student, preferences: [...student.preferences, p] });
    setNewPref("");
  }

  function removePref(pref) {
    setStudent({
      ...student,
      preferences: student.preferences.filter((x) => x !== pref),
    });
  }

  // Clear profile data and localStorage
  function clearProfile() {
    localStorage.removeItem(STUDENT_STORAGE_KEY);
    setStudent(DEFAULT_STUDENT);
  }

// Display
  return (
    <div>
      <TopBar />

      <div className="page-container">
        <div className="profile-card">
          <h1>Student Profile</h1>

          <h2>Basic Info</h2>
          <p>Name: {student.name}</p>
          <p>Email: {student.email}</p>
          <p>Major: {student.major}</p>

          <h2>Bio</h2>
          <textarea
            value={student.bio}
            maxLength={200}
            placeholder="Write a short bio (max 200 chars)"
            onChange={(e) =>
              setStudent({ ...student, bio: e.target.value })
            }
          />
          <div style={{ fontSize: "12px" }}>
            {student.bio.length}/200
          </div>

          <h2>Courses</h2>
          <div style={{ display: "flex", gap: "8px", marginBottom: "10px" }}>
            <input
              value={newCourse}
              placeholder="e.g., EECS 2311"
              onChange={(e) => setNewCourse(e.target.value)}
            />
            <button onClick={addCourse}>Add</button>
          </div>

          <ul>
            {student.courses.map((course) => (
              <li key={course}>
                {course}
                <button
                  className="remove-btn"
                  onClick={() => removeCourse(course)}
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>

          <h2>Study Preferences</h2>
          <div style={{ display: "flex", gap: "8px", marginBottom: "10px" }}>
            <input
              value={newPref}
              placeholder="e.g., Quiet Focus"
              onChange={(e) => setNewPref(e.target.value)}
            />
            <button onClick={addPref}>Add</button>
          </div>

          <ul>
            {student.preferences.map((pref) => (
              <li key={pref}>
                {pref}
                <button
                  className="remove-btn"
                  onClick={() => removePref(pref)}
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>

          <div
            style={{
              marginTop: "20px",
              paddingTop: "20px",
              borderTop: "1px solid #e5e7eb",
            }}
          >
            <button
              className="remove-btn"
              onClick={clearProfile}
              style={{ width: "100%" }}
            >
              Clear Saved Profile
            </button>
            <p
              style={{
                fontSize: "12px",
                color: "#6b7280",
                marginTop: "10px",
              }}
            >
              💾 Your profile is saved in your browser and will persist even
              after closing it.
            </p>
          </div>
        </div>

        <div className="session-log-card">
          <SessionLog studentId="student-1" />
        </div>
      </div>
    </div>
  );
}
