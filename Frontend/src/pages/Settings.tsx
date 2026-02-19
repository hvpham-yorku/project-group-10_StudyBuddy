/*
 * Settings.tsx
 * Place where users can change their account settings
 */

import { useState } from "react";
import {
  Shield, Bell, Eye, Clock, Lock, UserX, Trash2, ChevronRight,
  Mail, Smartphone, Check, AlertTriangle, Globe
} from "lucide-react";
import { currentUser } from "../data/mockData";

function Toggle({ value, onChange }: { value: boolean; onChange: (v: boolean) => void }) {
  return (
    <button
      onClick={() => onChange(!value)}
      className={`relative w-10 h-5.5 rounded-full transition-colors ${value ? "bg-blue-600" : "bg-slate-200"}`}
      style={{ height: "22px", width: "40px" }}
    >
      <div
        className={`absolute top-0.5 w-4.5 h-4.5 bg-white rounded-full shadow transition-transform`}
        style={{
          width: "18px",
          height: "18px",
          transform: value ? "translateX(20px)" : "translateX(2px)",
        }}
      ></div>
    </button>
  );
}

function SectionHeader({ icon: Icon, title, description, color }: { icon: any; title: string; description?: string; color: string }) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${color}`}>
        <Icon size={18} className="text-white" />
      </div>
      <div>
        <h2 className="text-slate-800 text-sm" style={{ fontWeight: 700 }}>{title}</h2>
        {description && <p className="text-xs text-slate-400">{description}</p>}
      </div>
    </div>
  );
}

export default function Settings() {
  const [privacy, setPrivacy] = useState({ ...currentUser.privacySettings });
  const [notifications, setNotifications] = useState({ ...currentUser.notifications });
  const [twoFA, setTwoFA] = useState(currentUser.twoFAEnabled);
  const [autoTimeout, setAutoTimeout] = useState(currentUser.autoTimeout);
  const [reportModal, setReportModal] = useState(false);
  const [reportReason, setReportReason] = useState("");
  const [reportUser, setReportUser] = useState("");
  const [reportSent, setReportSent] = useState(false);
  const [saved, setSaved] = useState(false);

  const setPriv = (k: keyof typeof privacy, v: boolean) => setPrivacy((p) => ({ ...p, [k]: v }));
  const setNotif = (k: keyof typeof notifications, v: boolean) => setNotifications((n) => ({ ...n, [k]: v }));

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const submitReport = () => {
    setReportSent(true);
    setTimeout(() => {
      setReportModal(false);
      setReportSent(false);
      setReportReason("");
      setReportUser("");
    }, 2000);
  };

  return (
    <div className="p-6 max-w-2xl mx-auto">
      <div className="mb-6">
        <h1 className="text-slate-900" style={{ fontWeight: 700, fontSize: "1.35rem" }}>Settings & Privacy</h1>
        <p className="text-slate-500 text-sm mt-0.5">Manage your account preferences and security</p>
      </div>

      <div className="space-y-5">
        {/* Privacy Settings */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={Eye} title="Privacy Settings" description="Control what others can see about you" color="bg-blue-600" />
          <div className="space-y-3">
            {[
              { key: "showEmail" as const, label: "Show Email Address", desc: "Let other students see your email" },
              { key: "showLocation" as const, label: "Show Current Location", desc: "Display your location in the app" },
              { key: "showCourses" as const, label: "Show My Courses", desc: "Make your course list visible to others" },
              { key: "showStudyVibes" as const, label: "Show Study Vibes", desc: "Display your study preferences on your profile" },
              { key: "showSessionHistory" as const, label: "Show Session History", desc: "Let others view your study session history" },
            ].map(({ key, label, desc }) => (
              <div key={key} className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0">
                <div>
                  <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>{label}</p>
                  <p className="text-xs text-slate-400">{desc}</p>
                </div>
                <Toggle value={privacy[key]} onChange={(v) => setPriv(key, v)} />
              </div>
            ))}
          </div>
        </div>

        {/* Security */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={Shield} title="Security" description="Keep your account safe" color="bg-orange-500" />
          <div className="space-y-3">
            {/* 2FA */}
            <div className="flex items-center justify-between py-2 border-b border-slate-50">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-green-50 flex items-center justify-center shrink-0">
                  <Smartphone size={15} className="text-green-600" />
                </div>
                <div>
                  <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>Two-Factor Authentication</p>
                  <p className="text-xs text-slate-400">Require email verification on login</p>
                  {twoFA && (
                    <div className="flex items-center gap-1 mt-1">
                      <Check size={11} className="text-green-500" />
                      <span className="text-xs text-green-600">Enabled · via email</span>
                    </div>
                  )}
                </div>
              </div>
              <Toggle value={twoFA} onChange={setTwoFA} />
            </div>

            {/* Domain Filter */}
            <div className="flex items-center justify-between py-2 border-b border-slate-50">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
                  <Globe size={15} className="text-blue-600" />
                </div>
                <div>
                  <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>York University Domain Filter</p>
                  <p className="text-xs text-slate-400">Only @my.yorku.ca and @yorku.ca emails allowed</p>
                  <div className="flex items-center gap-1 mt-1">
                    <Check size={11} className="text-green-500" />
                    <span className="text-xs text-green-600">Always enforced · Cannot be disabled</span>
                  </div>
                </div>
              </div>
              <div className="text-xs bg-slate-100 text-slate-400 px-2 py-1 rounded-lg">Required</div>
            </div>

            {/* Change Password */}
            <div className="flex items-center justify-between py-2">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-slate-50 flex items-center justify-center shrink-0">
                  <Lock size={15} className="text-slate-500" />
                </div>
                <div>
                  <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>Change Password</p>
                  <p className="text-xs text-slate-400">Update your account password</p>
                </div>
              </div>
              <button className="text-sm text-blue-600 hover:underline flex items-center gap-1">
                Change <ChevronRight size={14} />
              </button>
            </div>
          </div>
        </div>

        {/* Notifications */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={Bell} title="Notifications" description="Manage what you get notified about" color="bg-blue-700" />
          <div className="space-y-3">
            {[
              { key: "chatMessages" as const, label: "Chat Messages", desc: "Get notified for new messages" },
              { key: "sessionUpdates" as const, label: "Session Updates", desc: "Reminders and changes to your sessions" },
              { key: "connectionRequests" as const, label: "Connection Requests", desc: "Notifications for friend requests" },
            ].map(({ key, label, desc }) => (
              <div key={key} className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0">
                <div>
                  <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>{label}</p>
                  <p className="text-xs text-slate-400">{desc}</p>
                </div>
                <Toggle value={notifications[key]} onChange={(v) => setNotif(key, v)} />
              </div>
            ))}
          </div>
        </div>

        {/* Auto-Timeout */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={Clock} title="Auto-Timeout" description="Automatic session logout on inactivity" color="bg-slate-500" />
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm text-slate-700" style={{ fontWeight: 500 }}>Timeout after inactivity</span>
              <span className="text-sm text-blue-600" style={{ fontWeight: 600 }}>{autoTimeout} minutes</span>
            </div>
            <input
              type="range"
              min={5}
              max={30}
              step={5}
              value={autoTimeout}
              onChange={(e) => setAutoTimeout(Number(e.target.value))}
              className="w-full accent-blue-600"
            />
            <div className="flex justify-between text-xs text-slate-400 mt-1">
              <span>5 min</span>
              <span>30 min</span>
            </div>
            <div className="flex gap-2 mt-3">
              {[5, 10, 15, 20, 30].map((v) => (
                <button
                  key={v}
                  onClick={() => setAutoTimeout(v)}
                  className={`px-3 py-1.5 rounded-lg text-xs transition-colors ${autoTimeout === v ? "bg-blue-600 text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}
                >
                  {v}m
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Report a User */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={AlertTriangle} title="Report & Safety" description="Keep our community safe" color="bg-red-500" />
          <div className="space-y-3">
            <div className="flex items-center justify-between py-2 border-b border-slate-50">
              <div>
                <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>Report a User</p>
                <p className="text-xs text-slate-400">Flag inappropriate behavior or content</p>
              </div>
              <button
                onClick={() => setReportModal(true)}
                className="text-sm text-red-500 hover:text-red-600 flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 rounded-lg transition-colors"
              >
                <UserX size={14} />
                Report
              </button>
            </div>
            <div className="flex items-center justify-between py-2">
              <div>
                <p className="text-sm text-slate-700" style={{ fontWeight: 500 }}>Delete Account</p>
                <p className="text-xs text-slate-400">Permanently remove your account and data</p>
              </div>
              <button className="text-sm text-red-400 hover:text-red-600 flex items-center gap-1 px-3 py-1.5 bg-red-50 hover:bg-red-100 rounded-lg transition-colors">
                <Trash2 size={14} />
                Delete
              </button>
            </div>
          </div>
        </div>

        {/* Account Info */}
        <div className="bg-white rounded-xl border border-slate-200 p-5">
          <SectionHeader icon={Mail} title="Account Information" color="bg-blue-600" />
          <div className="space-y-2">
            <div className="flex justify-between py-1.5 border-b border-slate-50">
              <span className="text-xs text-slate-500">Email</span>
              <span className="text-xs text-slate-700" style={{ fontWeight: 500 }}>{currentUser.email}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-50">
              <span className="text-xs text-slate-500">Domain</span>
              <span className="text-xs text-green-600 flex items-center gap-1"><Check size={11} />Verified York University</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-50">
              <span className="text-xs text-slate-500">Member since</span>
              <span className="text-xs text-slate-700">{currentUser.joinedDate}</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-xs text-slate-500">Account status</span>
              <span className="text-xs text-green-600 flex items-center gap-1"><Check size={11} />Active</span>
            </div>
          </div>
        </div>

        {/* Save Button */}
        <button
          onClick={handleSave}
          className={`w-full py-3 rounded-xl text-sm transition-colors ${saved ? "bg-green-600 text-white" : "bg-blue-700 hover:bg-blue-800 text-white"}`}
          style={{ fontWeight: 600 }}
        >
          {saved ? (
            <span className="flex items-center justify-center gap-2"><Check size={16} />Settings Saved!</span>
          ) : (
            "Save Changes"
          )}
        </button>
      </div>

      {/* Report Modal */}
      {reportModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl w-full max-w-sm p-6">
            {reportSent ? (
              <div className="text-center py-4">
                <div className="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-3">
                  <Check size={28} className="text-green-600" />
                </div>
                <h3 className="text-slate-800 text-sm" style={{ fontWeight: 700 }}>Report Submitted</h3>
                <p className="text-xs text-slate-500 mt-1">Our team will review this report. Thank you for keeping StudyBuddy safe.</p>
              </div>
            ) : (
              <>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-slate-800 text-sm flex items-center gap-2" style={{ fontWeight: 700 }}>
                    <AlertTriangle size={16} className="text-red-500" />
                    Report a User
                  </h3>
                  <button onClick={() => setReportModal(false)}>
                    <span className="text-slate-400 text-lg leading-none">×</span>
                  </button>
                </div>
                <div className="space-y-4">
                  <div>
                    <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>User to report</label>
                    <input
                      value={reportUser}
                      onChange={(e) => setReportUser(e.target.value)}
                      placeholder="Email or name"
                      className="w-full px-3 py-2.5 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                    />
                  </div>
                  <div>
                    <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Reason</label>
                    <div className="space-y-1.5">
                      {["Inappropriate behavior", "Harassment", "Spam or fake profile", "Other"].map((r) => (
                        <label key={r} className="flex items-center gap-2 cursor-pointer">
                          <input
                            type="radio"
                            name="reason"
                            value={r}
                            onChange={(e) => setReportReason(e.target.value)}
                            className="accent-blue-600"
                          />
                          <span className="text-sm text-slate-600">{r}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-slate-600 mb-1" style={{ fontWeight: 500 }}>Additional details</label>
                    <textarea
                      rows={3}
                      placeholder="Describe what happened..."
                      className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 bg-slate-50"
                    />
                  </div>
                  <p className="text-xs text-slate-400 bg-slate-50 rounded-lg p-2 leading-relaxed">
                    Reports are stored in a secure database and reviewed only by our moderation team.
                  </p>
                  <div className="flex gap-2">
                    <button onClick={() => setReportModal(false)} className="flex-1 py-2.5 border border-slate-200 text-slate-600 rounded-xl text-sm">Cancel</button>
                    <button onClick={submitReport} className="flex-1 py-2.5 bg-red-600 text-white rounded-xl text-sm hover:bg-red-700" style={{ fontWeight: 600 }}>
                      Submit Report
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
