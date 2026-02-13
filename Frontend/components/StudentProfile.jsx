import React, { useState } from "react";
import TopBar from "./TopBar";
import SessionLog from "./SessionLog";

// Default student profile 
export default function StudentProfile() {
  const [student, setStudent] = useState({
    name: "John Doe",
    email: "john@my.yorku.ca",
    major: "Software Engineering",
    bio: "",
    courses: ["EECS 2311", "MATH 1013"],
    preferences: ["Quiet Focus"],
  });

  const [newCourse, setNewCourse] = useState("");
  const [newPref, setNewPref] = useState("");

// Allows user to add a course to their profile 
  function addCourse() {
    const c = newCourse.trim();
    if (!c) return;
    if (student.courses.includes(c)) return;

    setStudent({ ...student, courses: [...student.courses, c] });
    setNewCourse("");
  }

// Allows student to remove a course from their profile 
  function removeCourse(course) {
    setStudent({ ...student, courses: student.courses.filter((x) => x !== course) });
  }

// Allows student to add a preference on their profile 
  function addPref() {
    const p = newPref.trim();
    if (!p) return;
    if (student.preferences.includes(p)) return;

    setStudent({ ...student, preferences: [...student.preferences, p] });
    setNewPref("");
  }

// Allows student to remove a preference on their profile 
  function removePref(pref) {
    setStudent({ ...student, preferences: student.preferences.filter((x) => x !== pref) });
  }

// Displays details of profile 
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
      </div>

      <div className="session-log-card">
        <SessionLog studentId="student-1" />
      </div>
    </div>
  </div>
);
}
