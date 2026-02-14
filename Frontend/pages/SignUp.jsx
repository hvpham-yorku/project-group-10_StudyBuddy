import { useNavigate } from 'react-router-dom';
import './SignIn.css';

export default function SignUpPage() {
    const navigate = useNavigate();
    return (
    <div className="top-bar">
    <div className="container">

        <div className="left-side">
            <div className="logo-section">
                <img src="shrunken_logo.webp" alt="Study Buddy Logo"/>
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

        <div className="right-side">
            <div className="login-card">
                <div className="login-header">
                    <h1>Create Account</h1>
                    <p>Join Study Buddy today</p>
                </div>

                <form>
                    <div className="form-group">
                        <label htmlFor="fullname">Full Name</label>
                        <div className="input-wrapper">
                            <span className="input-icon">👤</span>
                            <input type="text" id="fullname" placeholder="Enter your full name" required/>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <div className="input-wrapper">
                            <span className="input-icon">📧</span>
                            <input type="email" id="email" placeholder="Enter your York University email" required/>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <div className="input-wrapper">
                            <span className="input-icon">🔒</span>
                            <input type="password" id="password" placeholder="Create a password" required/>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirm-password">Confirm Password</label>
                        <div className="input-wrapper">
                            <span className="input-icon">🔒</span>
                            <input type="password" id="confirm-password" placeholder="Re-enter your password" required/>
                        </div>
                    </div>

                    <button type="button" 
                    className="sign-in-btn" 
                    onClick={() => { navigate('/'); }}>
                        Sign Up
                        <span className="arrow">→</span>
                    </button>

                    <div className="signup-link">
                        Already have an account? <a href="signin.html">Sign In</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    </div>
)
}