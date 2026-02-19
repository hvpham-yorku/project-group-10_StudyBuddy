// This code file is necessary for the layout of the application. For instance,
// the user should be able to navigate to the dashboard, profile, and etc. via the sidebar.

import React from 'react'
import { Outlet, Link } from "react-router-dom";

export default function Layout() {
  return (
    <div style={{ display: 'flex' }}>
      {/* Temporary Sidebar */}
      <nav style={{ width: '200px', background: '#f0f0f0', height: '100vh', padding: '20px' }}>
        <h3>Sidebar</h3>
        <ul>
          <li><Link to="/dashboard">Dashboard</Link></li>
          <li><Link to="/profile">Profile</Link></li>
          <li><Link to="/">Logout</Link></li>
        </ul>
      </nav>
      
      {/* Main Content Area */}
      <main style={{ padding: '20px', flex: 1 }}>
        <Outlet /> 
      </main>
    </div>
  );
}