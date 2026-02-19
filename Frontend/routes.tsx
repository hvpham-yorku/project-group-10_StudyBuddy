import { createBrowserRouter } from "react-router-dom";
import React from 'react';
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import Layout from "./pages/Layout";

export const router = createBrowserRouter([
  // --- Public Routes (No Sidebar) ---
  {
    path: "/",
    element: <Login />,
  },
  {
    path: "/register",
    element: <Register />,
  },

  // --- App Routes (With Sidebar) ---
  {
    element: <Layout />, // This wraps all children below
    children: [
      {
        path: "/dashboard",
        element: <Dashboard />,
      },
      {
        path: "/profile",
        element: <Profile />,
      },
    ],
  },
]);