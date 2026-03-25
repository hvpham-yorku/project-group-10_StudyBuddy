import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import React from 'react';
import { useEffect, useState } from "react";
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
import { clearAuthState, getAuthToken } from "./lib/auth";

// This component checks for a token. If it's missing, it kicks the user to Login.
const ProtectedRoute = () => {
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
  const [isAuthorized, setIsAuthorized] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const validateAuth = async () => {
      const token = getAuthToken();
      if (!token) {
        if (!cancelled) {
          setIsAuthorized(false);
          setIsCheckingAuth(false);
        }
        return;
      }

      try {
        const response = await fetch("/api/studentcontroller/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (cancelled) return;

        if (!response.ok) {
          clearAuthState();
          setIsAuthorized(false);
        } else {
          setIsAuthorized(true);
        }
      } catch {
        if (cancelled) return;
        clearAuthState();
        setIsAuthorized(false);
      } finally {
        if (!cancelled) {
          setIsCheckingAuth(false);
        }
      }
    };

    void validateAuth();

    return () => {
      cancelled = true;
    };
  }, []);

  if (isCheckingAuth) {
    return <div className="min-h-screen flex items-center justify-center text-slate-500">Checking session...</div>;
  }

  if (!isAuthorized) {
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
  {
    path: "/map",
    element: <MapView />,
  },

  // --- App Routes (With Sidebar) ---
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <RootLayout />,
        children: [
          {
            element: <Layout />,
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