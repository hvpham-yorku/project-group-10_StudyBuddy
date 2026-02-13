import React from "react";
import { Link } from "react-router-dom";

export default function TopBar() {
  return (
    <div className="topbar">
      <div className="topbar-title">StudyBuddy</div>

      <Link to="/profile" className="profile-icon">
        👤
      </Link>
    </div>
  );
}