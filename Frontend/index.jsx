import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./style.css";

/* The Pages */
import Home from "./pages/Home";
import StudentProfile from "./pages/StudentProfile";
import EventsPage from "./pages/Events";
import SignInPage from "./pages/SignIn";
import SignUpPage from "./pages/SignUp";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/profile" element={<StudentProfile />} />
		    <Route path="/events" element={<EventsPage/>} />
        <Route path="/signin" element={<SignInPage />} />
        <Route path="/signup" element={<SignUpPage />} />
      </Routes>
    </BrowserRouter>
  );
}

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
