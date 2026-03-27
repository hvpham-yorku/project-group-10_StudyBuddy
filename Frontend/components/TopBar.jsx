import React from "react";
import { Link } from "react-router-dom";

export default function TopBar() {
  return (
    <div className="topbar">
      <div className="topbar-title">
        <a href="/" style={{ textDecoration: 'none' }}>StudyBuddy</a>
      </div>
      <Link to="/map">
        🗺️ Map
      </Link>
      <Link to="/events">
        Events
      </Link>
      <Link to="/profile" className="profile-icon">
        👤
      </Link>
    </div>
  );
}