import React, { useState } from 'react';
import ReactDOM from 'react-dom/client';
import './style.css'; 

function App() {
    const [isLoginView, setIsLoginView] = useState(true);
    const [user, setUser] = useState(null);

    const handleReset = async () => {
        const email = prompt("Enter your YorkU email for the reset link:");
        if (!email) return;
        try {
            const response = await fetch(`http://localhost:8080/api/auth/reset-password?email=${email}`, { method: 'POST' });
            const message = await response.text();
            alert(response.ok ? "Success! Check your YorkU inbox." : "Error: " + message);
        } catch (error) { alert("Backend unreachable."); }
    };

    const handleSignUp = async (e) => {
        e.preventDefault();
        const { fullname, email, password } = e.target.elements;
        try {
            const res = await fetch(`http://localhost:8080/api/auth/register?password=${password.value}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullName: fullname.value, email: email.value })
            });
            const msg = await res.text();
            alert(msg); 
            if (res.ok) setIsLoginView(true);
        } catch (err) { alert("Registration failed. Server unreachable."); }
    };

    const handleSignIn = async (e) => {
        e.preventDefault();
        const email = e.target.elements.email.value;
        try {
            const response = await fetch(`http://localhost:8080/api/auth/login?email=${email}`, { method: 'POST' });
            const message = await response.text();
            if (response.ok) {
                setUser(email); // ONLY logs in if verified
            } else {
                alert(message); // Displays "Access Denied" if unverified
            }
        } catch (error) { alert("Login error. Check your connection."); }
    };

    if (user) return <div className="hub"><h1>Welcome, {user}!</h1><p>You are verified.</p></div>;

    return (
        <div className="login-page">
            <div className="login-card">
                <h1>{isLoginView ? "Welcome Back" : "Create Account"}</h1>
                <form onSubmit={isLoginView ? handleSignIn : handleSignUp}>
                    {!isLoginView && <input name="fullname" placeholder="Full Name" required className="input-field" />}
                    <input name="email" type="email" placeholder="YorkU Email" required className="input-field" />
                    <input name="password" type="password" placeholder="Password" required className="input-field" />
                    <button type="submit" className="sign-in-btn">{isLoginView ? "Sign In" : "Sign Up"}</button>
                </form>
                {/* RESTORED: Forgot Password button */}
                {isLoginView && <button onClick={handleReset} className="link-btn">Forgot Password?</button>}
                <p onClick={() => setIsLoginView(!isLoginView)} className="toggle-text">
                    {isLoginView ? "Don't have an account? Sign Up" : "Have an account? Sign In"}
                </p>
            </div>
        </div>
    );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);