import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import React from 'react';
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import ProfileViewer from "./pages/ProfileViewer";
import Events from "./pages/Events";
import CreateEvent from "./pages/CreateEvent";
import Chat from "./pages/Chat";
import MapView from "./pages/MapView";
import Network from "./pages/Network";
import TwoFA from "./pages/TwoFA";
import NotFound from "./pages/NotFound";
import EventDetails from "./pages/EventDetails";
import Layout from "./pages/Layout";
import { RootLayout } from "./layouts/RootLayout";
import Inactive from "./pages/Inactive";

// This component checks for a token. If it's missing, it kicks the user to Login.
const ProtectedRoute = () => {
  const token = localStorage.getItem("studyBuddyToken");
  if (!token) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
};

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
  {
    path: "/2fa",
    element: <TwoFA />,
  },
  

  // --- App Routes (With Sidebar) ---
  {
    element: <ProtectedRoute />, // This wraps all protected routes
    children: [
    {
      element: <RootLayout />,
      children: [
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
        {
          path: "/profile/:id",
          element: <ProfileViewer />,
        },
        {
          path: "/events",
          element: <Events />,
        },
        {
          path: "/events/create",
          element: <CreateEvent />,
        },
        {
          path: "/events/:id",
          element: <EventDetails />,
        },
        {
          path: "map",
          element: <MapView />,
        },
        {
          path: "chat",
          element: <Chat />,
        },
        {
          path: "chat/:id",
          element: <Chat />,
        },
        {
          path: "network",
          element: <Network />,
        },
      ],
    }],
  }],
  },
  {
    path: "/inactive", element: <Inactive /> 
  },
  {
    path: "/*",
    element: <NotFound />,
  },
]);