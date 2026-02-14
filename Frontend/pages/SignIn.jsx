import './SignIn.css';

import { useNavigate } from 'react-router-dom';
export default function SignInPage() {
    const navigate = useNavigate();

    return (

        <div className="login-page">
            <div className="top-bar"></div>
            <div className="container">

                {/* The left side of the page contains the logo and a list of features that highlight the benefits of using Study Buddy. Each feature is accompanied by a checkmark for visual emphasis. */}
                <div className="left-side">
                    <div className="logo-section">
                    <img src="/shrunken_logo.webp" alt="Study Buddy Logo"/>
                    </div>

                    <div className="features">
                        <div className="feature-item">
                            <div className="checkmark">✓</div>
                            <div className="feature-text">Connect with students in your exact courses using location-based matching
                            </div>
                        </div>
                        <div className="feature-item">
                            <div className="checkmark">✓</div>
                            <div className="feature-text">Find study buddies at local libraries, cafes, and campus study spots</div>
                        </div>
                        <div className="feature-item">
                            <div className="checkmark">✓</div>
                            <div className="feature-text">Create or join study sessions with 2-5 students for maximum participation
                            </div>
                        </div>
                    </div>
                </div>

                {/* The right side of the page features a clean and modern login form. It includes input fields for email and password, each with an accompanying icon for better user experience. The "Sign In" button is prominently displayed, and there are links for "Forgot Password?" and "Sign Up" to assist users who may need help accessing their accounts or creating new ones. */}
                <div className="right-side">
                    <div className="login-card">
                        <div className="login-header">
                            <h1>Welcome Back</h1>
                            <p>Sign in to continue</p>
                        </div>

                        <form>
                            <div className="form-group">
                                <label htmlFor="email">Email</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">📧</span>
                                    <input type="email" id="email" placeholder="Enter your email" required/>
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="password">Password</label>
                                <div className="input-wrapper">
                                    <span className="input-icon">🔒</span>
                                    <input type="password" id="password" placeholder="Enter your password" required/>
                                </div>
                            </div>

                            <button type="button" 
                                className="sign-in-btn"
                                onClick={() => { navigate('/home'); }}>
                                Sign In
                                <span className="arrow">→</span>
                            </button>
                            <div className="forgot-password">
                                <a href="forgot-password.html">Forgot Password?</a>
                            </div>
                            <div className="signup-link">
                                Don't have an account? <a href="signup.html">Sign Up</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        )}