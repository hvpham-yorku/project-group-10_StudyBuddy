import React, { useState, useEffect } from "react";
import "../pages/style.css";

// Default session log data
export default function SessionLog({ studentId }) {
  const [sessions, setSessions] = useState([]);
  const [totalStudyMinutes, setTotalStudyMinutes] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filterCourse, setFilterCourse] = useState("");

  useEffect(() => {
    fetchSessionLog();
    fetchTotalStudyTime();
  }, [studentId]);

// Call backend to find details
  async function fetchSessionLog() {
    try {
      setLoading(true);
      const endpoint = filterCourse
        ? `/api/studentcontroller/getstudent/${studentId}/sessionlog/course/${filterCourse}`
        : `/api/studentcontroller/getstudent/${studentId}/sessionlog`;
      
      const response = await fetch(endpoint);
      const data = await response.json();
      setSessions(data || []);
      setError(null);
    } catch (err) {
      setError("Failed to load session log");
      setSessions([]);
    } finally {
      setLoading(false);
    }
  }

// Get total study time of student
  async function fetchTotalStudyTime() {
    try {
      const response = await fetch(
        `/api/studentcontroller/getstudent/${studentId}/totalstudytime`
      );
      const minutes = await response.json();
      setTotalStudyMinutes(minutes);
    } catch (err) {
      console.error("Failed to fetch total study time:", err);
    }
  }

// Format date and time for user to read
  function formatDateTime(dateTimeString) {
    if (!dateTimeString) return "N/A";
    try {
      const date = new Date(dateTimeString);
      return date.toLocaleString();
    } catch {
      return dateTimeString;
    }
  }

// Calculate durtion 
  function calculateDuration(startTime, endTime) {
    if (!startTime || !endTime) return "N/A";
    try {
      const start = new Date(startTime);
      const end = new Date(endTime);
      const minutes = Math.round((end - start) / 60000);
      const hours = Math.floor(minutes / 60);
      const mins = minutes % 60;
      return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
    } catch {
      return "N/A";
    }
  }

// Format the total study time for the user to be able to read clearly
  function formatTotalStudyTime(minutes) {
    if (minutes === 0) return "0 minutes";
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours} hour${hours > 1 ? "s" : ""} ${mins} minute${mins !== 1 ? "s" : ""}`;
    }
    return `${mins} minute${mins !== 1 ? "s" : ""}`;
  }

  function handleFilterChange(e) {
    setFilterCourse(e.target.value);
  }
  useEffect(() => {
    if (filterCourse !== "") {
      fetchSessionLog();
    }
  }, [filterCourse]);

// Display all details to user
  return (
    <div className="session-log-container">
      <h2>Study Session Log</h2>

      <div className="session-stats">
        <div className="stat-box">
          <h3>Total Study Time</h3>
          <p className="stat-value">{formatTotalStudyTime(totalStudyMinutes)}</p>
        </div>
        <div className="stat-box">
          <h3>Sessions Attended</h3>
          <p className="stat-value">{sessions.length}</p>
        </div>
      </div>

      <div className="filter-section">
        <label htmlFor="course-filter">Filter by Course: </label>
        <select
          id="course-filter"
          value={filterCourse}
          onChange={handleFilterChange}
        >
          <option value="">All Courses</option>
          {[...new Set(sessions.map((s) => s.course))].map((course) => (
            <option key={course} value={course}>
              {course}
            </option>
          ))}
        </select>
      </div>

      {loading && <p className="loading">Loading sessions...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && sessions.length === 0 && (
        <p className="no-sessions">No study sessions logged yet.</p>
      )}

      <div className="sessions-list">
        {sessions.map((session) => (
          <div key={session.eventId} className="session-card">
            <div className="session-header">
              <h3>{session.title}</h3>
              <span className="session-course">{session.course}</span>
            </div>

            <div className="session-details">
              <div className="detail-item">
                <strong>Location:</strong> {session.location}
              </div>
              <div className="detail-item">
                <strong>Start:</strong> {formatDateTime(session.startTime)}
              </div>
              <div className="detail-item">
                <strong>End:</strong> {formatDateTime(session.endTime)}
              </div>
              <div className="detail-item">
                <strong>Duration:</strong>{" "}
                {calculateDuration(session.startTime, session.endTime)}
              </div>
            </div>

            {session.description && (
              <div className="session-description">
                <strong>Description:</strong> {session.description}
              </div>
            )}

            {session.participantIds && session.participantIds.length > 0 && (
              <div className="session-participants">
                <strong>Participants:</strong>{" "}
                {session.participantIds.join(", ")}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
