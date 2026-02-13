import React from "react";
import TopBar from "../components/TopBar";

export default function Home() {
  return (
    <div>
      <TopBar />
      <div style={{ padding: "20px" }}>
        <h1>Home</h1>
        <p>This is the home page. Click the profile icon top-right.</p>
      </div>
    </div>
  );
}
