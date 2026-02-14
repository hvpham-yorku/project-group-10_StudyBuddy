import React from "react";
import TopBar from "../components/TopBar";

export default function Home() {
  return (
    <div>
      <TopBar/>

      <div className="container">
        <div className="left-side">
            <div className="logo">
                <img src="shrunken_logo.webp" alt="Study Buddy Logo"/>
            </div>
          </div>

          <div className="right-side">
            This is the home page. Click the profile icon top-right.
          </div>
        </div>
    </div>
  );
}
