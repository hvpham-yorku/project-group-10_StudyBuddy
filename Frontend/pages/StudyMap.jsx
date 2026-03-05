import { useState, useCallback, useRef } from "react";
import {
    APIProvider,
    Map,
    AdvancedMarker,
    Pin,
    InfoWindow,
    useMap,
} from "@vis.gl/react-google-maps";

// ─── Constants ────────────────────────────────────────────────────────────────
const API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
const YORK_CENTER = { lat: 43.7735, lng: -79.5019 };

// ─── Dummy seed events (replace with your API fetch) ─────────────────────────
const SEED_EVENTS = [
    {
        id: "1",
        title: "EECS 2311 Cram Session",
        course: "EECS 2311",
        location: "Steacie Science Library",
        description: "Going through past exams and lab prep.",
        host: "John Doe",
        capacity: 5,
        participants: ["John Doe", "Alice"],
        start: "2025-04-10T14:00",
        end: "2025-04-10T17:00",
        lat: 43.7742,
        lng: -79.5033,
    },
    {
        id: "2",
        title: "MATH 1013 Study Group",
        course: "MATH 1013",
        location: "Scott Library",
        description: "Calculus review — integrals focus.",
        host: "Jane Smith",
        capacity: 4,
        participants: ["Jane Smith"],
        start: "2025-04-11T10:00",
        end: "2025-04-11T12:00",
        lat: 43.7726,
        lng: -79.5008,
    },
];

// ─── Utility ──────────────────────────────────────────────────────────────────
function fmt(dt) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("en-CA", {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function cap(n, max) {
    const pct = Math.round((n / max) * 100);
    return { pct, color: pct >= 100 ? "#ef4444" : pct >= 75 ? "#f59e0b" : "#22c55e" };
}

// ─── Root Export ──────────────────────────────────────────────────────────────
export default function StudyMap() {
    const [events, setEvents] = useState(SEED_EVENTS);
    const [pendingPin, setPendingPin] = useState(null);   // { lat, lng, address }
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [activeTab, setActiveTab] = useState("events"); // "events" | "create"

    const handleMapClick = useCallback(async (e) => {
        const lat = e.detail.latLng.lat;
        const lng = e.detail.latLng.lng;

        // Reverse geocode via Geocoding API
        let address = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        try {
            const res = await fetch(
                `https://maps.googleapis.com/maps/api/geocode/json?latlng=${lat},${lng}&key=${API_KEY}`
            );
            const data = await res.json();
            if (data.results?.[0]) address = data.results[0].formatted_address;
        } catch (_) { }

        setPendingPin({ lat, lng, address });
        setSelectedEvent(null);
        setShowForm(true);
        setActiveTab("create");
    }, []);

    const handleAddEvent = (newEvent) => {
        setEvents((prev) => [...prev, { ...newEvent, id: String(Date.now()) }]);
        setPendingPin(null);
        setShowForm(false);
        setActiveTab("events");
    };

    const handleCancelCreate = () => {
        setPendingPin(null);
        setShowForm(false);
        setActiveTab("events");
    };

    return (
        <APIProvider apiKey={API_KEY}>
            <div style={styles.root}>
                {/* ── Top bar ── */}
                <header style={styles.topbar}>
                    <div style={styles.topbarBrand}>
                        <span style={styles.topbarDot} />
                        <span style={styles.topbarTitle}>StudyBuddy</span>
                        <span style={styles.topbarSub}>/ Map</span>
                    </div>
                    <nav style={styles.topbarNav}>
                        <a href="/home" style={styles.navLink}>Home</a>
                        <a href="/profile" style={styles.navLink}>Profile</a>
                        <a href="/events" style={styles.navLink}>Events</a>
                    </nav>
                </header>

                {/* ── Body ── */}
                <div style={styles.body}>

                    {/* ── Sidebar ── */}
                    <aside style={styles.sidebar}>
                        {/* Tab pills */}
                        <div style={styles.tabs}>
                            <button
                                style={{ ...styles.tab, ...(activeTab === "events" ? styles.tabActive : {}) }}
                                onClick={() => setActiveTab("events")}
                            >
                                📚 Events ({events.length})
                            </button>
                            <button
                                style={{ ...styles.tab, ...(activeTab === "create" ? styles.tabActive : {}) }}
                                onClick={() => { setActiveTab("create"); }}
                            >
                                ＋ Host
                            </button>
                        </div>

                        {/* Events list */}
                        {activeTab === "events" && (
                            <div style={styles.eventList}>
                                {events.length === 0 && (
                                    <div style={styles.empty}>
                                        <span style={{ fontSize: 32 }}>🗺️</span>
                                        <p>No events yet.<br />Click the map to host one!</p>
                                    </div>
                                )}
                                {events.map((ev) => {
                                    const { pct, color } = cap(ev.participants.length, ev.capacity);
                                    const isSelected = selectedEvent?.id === ev.id;
                                    return (
                                        <div
                                            key={ev.id}
                                            style={{ ...styles.eventCard, ...(isSelected ? styles.eventCardSelected : {}) }}
                                            onClick={() => setSelectedEvent(isSelected ? null : ev)}
                                        >
                                            <div style={styles.eventCardTop}>
                                                <span style={styles.coursePill}>{ev.course}</span>
                                                <span style={{ fontSize: 11, color: "#94a3b8" }}>
                                                    {ev.participants.length}/{ev.capacity} spots
                                                </span>
                                            </div>
                                            <h3 style={styles.eventTitle}>{ev.title}</h3>
                                            <p style={styles.eventMeta}>📍 {ev.location}</p>
                                            <p style={styles.eventMeta}>🕐 {fmt(ev.start)}</p>
                                            {/* Capacity bar */}
                                            <div style={styles.barTrack}>
                                                <div style={{ ...styles.barFill, width: `${pct}%`, background: color }} />
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}

                        {/* Create form */}
                        {activeTab === "create" && (
                            <CreateForm
                                pendingPin={pendingPin}
                                onAdd={handleAddEvent}
                                onCancel={handleCancelCreate}
                            />
                        )}
                    </aside>

                    {/* ── Map ── */}
                    <div style={styles.mapWrap}>
                        {/* Hint banner */}
                        <div style={styles.hint}>
                            🖱️ Click anywhere on the map to drop a pin and host a study event
                        </div>

                        <Map
                            style={{ width: "100%", height: "100%" }}
                            defaultCenter={YORK_CENTER}
                            defaultZoom={15}
                            mapId="studybuddy-map"
                            onClick={handleMapClick}
                            clickableIcons={false}
                            gestureHandling="greedy"
                        >
                            {/* Existing event markers */}
                            {events.map((ev) => (
                                <AdvancedMarker
                                    key={ev.id}
                                    position={{ lat: ev.lat, lng: ev.lng }}
                                    onClick={() => {
                                        setSelectedEvent(ev);
                                        setActiveTab("events");
                                    }}
                                >
                                    <Pin
                                        background="#215EBA"
                                        borderColor="#0f2e6b"
                                        glyphColor="white"
                                        scale={selectedEvent?.id === ev.id ? 1.4 : 1.1}
                                    />
                                </AdvancedMarker>
                            ))}

                            {/* Pending (new) pin */}
                            {pendingPin && (
                                <AdvancedMarker position={{ lat: pendingPin.lat, lng: pendingPin.lng }}>
                                    <Pin
                                        background="#f97316"
                                        borderColor="#b45309"
                                        glyphColor="white"
                                        scale={1.3}
                                    />
                                </AdvancedMarker>
                            )}

                            {/* Info window for selected event */}
                            {selectedEvent && (
                                <InfoWindow
                                    position={{ lat: selectedEvent.lat, lng: selectedEvent.lng }}
                                    onCloseClick={() => setSelectedEvent(null)}
                                >
                                    <div style={styles.infoWindow}>
                                        <span style={styles.coursePill}>{selectedEvent.course}</span>
                                        <h4 style={{ margin: "6px 0 4px", fontSize: 14 }}>{selectedEvent.title}</h4>
                                        <p style={{ margin: "0 0 3px", fontSize: 12, color: "#475569" }}>
                                            📍 {selectedEvent.location}
                                        </p>
                                        <p style={{ margin: "0 0 3px", fontSize: 12, color: "#475569" }}>
                                            🕐 {fmt(selectedEvent.start)} → {fmt(selectedEvent.end)}
                                        </p>
                                        <p style={{ margin: "0 0 3px", fontSize: 12, color: "#475569" }}>
                                            👥 {selectedEvent.participants.length}/{selectedEvent.capacity} students
                                        </p>
                                        {selectedEvent.description && (
                                            <p style={{ margin: "6px 0 0", fontSize: 12, color: "#64748b", fontStyle: "italic" }}>
                                                {selectedEvent.description}
                                            </p>
                                        )}
                                    </div>
                                </InfoWindow>
                            )}
                        </Map>
                    </div>
                </div>
            </div>
        </APIProvider>
    );
}

// ─── Create Event Form ────────────────────────────────────────────────────────
function CreateForm({ pendingPin, onAdd, onCancel }) {
    const [form, setForm] = useState({
        title: "",
        course: "",
        location: pendingPin?.address ?? "",
        description: "",
        start: "",
        end: "",
        capacity: "5",
    });

    // Keep location field in sync if pin changes
    const prevPin = useRef(pendingPin);
    if (pendingPin && pendingPin !== prevPin.current) {
        prevPin.current = pendingPin;
        if (form.location === "" || form.location === prevPin.current?.address) {
            form.location = pendingPin.address;
        }
    }

    const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!form.title || !form.start || !form.end) return;
        onAdd({
            title: form.title,
            course: form.course,
            location: form.location,
            description: form.description,
            host: "John Doe",
            capacity: Number(form.capacity) || 5,
            participants: ["John Doe"],
            start: form.start,
            end: form.end,
            lat: pendingPin?.lat ?? YORK_CENTER.lat,
            lng: pendingPin?.lng ?? YORK_CENTER.lng,
        });
    };

    return (
        <div style={styles.formWrap}>
            {pendingPin ? (
                <div style={styles.pinBanner}>
                    <span style={{ fontSize: 18 }}>📍</span>
                    <div>
                        <div style={{ fontWeight: 600, fontSize: 12, color: "#f97316" }}>Pin dropped</div>
                        <div style={{ fontSize: 11, color: "#64748b", marginTop: 1 }}>
                            {pendingPin.address}
                        </div>
                    </div>
                </div>
            ) : (
                <div style={styles.noPinBanner}>
                    Click anywhere on the map to set a location, or fill in the address manually.
                </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                <Field label="Event Title *">
                    <input style={styles.input} value={form.title} onChange={set("title")} placeholder="e.g. EECS 2311 Cram" required />
                </Field>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
                    <Field label="Course">
                        <input style={styles.input} value={form.course} onChange={set("course")} placeholder="EECS 2311" />
                    </Field>
                    <Field label="Max Students">
                        <input style={styles.input} type="number" min="1" max="20" value={form.capacity} onChange={set("capacity")} />
                    </Field>
                </div>

                <Field label="Location">
                    <input
                        style={{ ...styles.input, ...(pendingPin ? styles.inputPinned : {}) }}
                        value={form.location}
                        onChange={set("location")}
                        placeholder="e.g. Steacie Library"
                    />
                </Field>

                <Field label="Description">
                    <textarea
                        style={{ ...styles.input, resize: "none", height: 60 }}
                        value={form.description}
                        onChange={set("description")}
                        placeholder="What will you study?"
                    />
                </Field>

                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
                    <Field label="Start *">
                        <input style={styles.input} type="datetime-local" value={form.start} onChange={set("start")} required />
                    </Field>
                    <Field label="End *">
                        <input style={styles.input} type="datetime-local" value={form.end} onChange={set("end")} required />
                    </Field>
                </div>

                <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
                    <button type="submit" style={styles.btnPrimary}>🚀 Create Event</button>
                    <button type="button" onClick={onCancel} style={styles.btnSecondary}>Cancel</button>
                </div>
            </form>
        </div>
    );
}

function Field({ label, children }) {
    return (
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
            <label style={{ fontSize: 11, fontWeight: 700, color: "#64748b", textTransform: "uppercase", letterSpacing: "0.06em" }}>
                {label}
            </label>
            {children}
        </div>
    );
}

// ─── Styles ───────────────────────────────────────────────────────────────────
const styles = {
    root: {
        display: "flex",
        flexDirection: "column",
        height: "100vh",
        fontFamily: "'DM Sans', 'Segoe UI', sans-serif",
        background: "#f1f5f9",
        overflow: "hidden",
    },
    topbar: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "0 24px",
        height: 56,
        background: "#0f172a",
        flexShrink: 0,
    },
    topbarBrand: {
        display: "flex",
        alignItems: "center",
        gap: 10,
    },
    topbarDot: {
        width: 10,
        height: 10,
        borderRadius: "50%",
        background: "#215EBA",
        boxShadow: "0 0 8px #215EBA",
    },
    topbarTitle: {
        color: "white",
        fontWeight: 700,
        fontSize: 17,
        letterSpacing: "-0.02em",
    },
    topbarSub: {
        color: "#475569",
        fontSize: 14,
    },
    topbarNav: {
        display: "flex",
        gap: 20,
    },
    navLink: {
        color: "#94a3b8",
        textDecoration: "none",
        fontSize: 13,
        fontWeight: 500,
        transition: "color 0.15s",
    },
    body: {
        display: "flex",
        flex: 1,
        overflow: "hidden",
    },
    sidebar: {
        width: 340,
        flexShrink: 0,
        background: "white",
        borderRight: "1px solid #e2e8f0",
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
    },
    tabs: {
        display: "flex",
        gap: 6,
        padding: "12px 14px",
        borderBottom: "1px solid #f1f5f9",
        flexShrink: 0,
    },
    tab: {
        flex: 1,
        padding: "8px 12px",
        borderRadius: 8,
        border: "1px solid #e2e8f0",
        background: "#f8fafc",
        color: "#64748b",
        fontSize: 13,
        fontWeight: 600,
        cursor: "pointer",
        transition: "all 0.15s",
    },
    tabActive: {
        background: "#0f172a",
        color: "white",
        borderColor: "#0f172a",
    },
    eventList: {
        flex: 1,
        overflowY: "auto",
        padding: "10px 12px",
        display: "flex",
        flexDirection: "column",
        gap: 10,
    },
    empty: {
        textAlign: "center",
        padding: "40px 20px",
        color: "#94a3b8",
        fontSize: 14,
        lineHeight: 1.6,
    },
    eventCard: {
        padding: "12px 14px",
        borderRadius: 10,
        border: "1px solid #e2e8f0",
        background: "#fafafa",
        cursor: "pointer",
        transition: "all 0.15s",
    },
    eventCardSelected: {
        border: "1.5px solid #215EBA",
        background: "#eff6ff",
        boxShadow: "0 0 0 3px rgba(33,94,186,0.08)",
    },
    eventCardTop: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 6,
    },
    coursePill: {
        background: "#dbeafe",
        color: "#1d4ed8",
        padding: "2px 8px",
        borderRadius: 20,
        fontSize: 11,
        fontWeight: 700,
    },
    eventTitle: {
        margin: "0 0 4px",
        fontSize: 14,
        fontWeight: 700,
        color: "#0f172a",
    },
    eventMeta: {
        margin: "0 0 3px",
        fontSize: 12,
        color: "#64748b",
    },
    barTrack: {
        marginTop: 8,
        height: 4,
        borderRadius: 99,
        background: "#e2e8f0",
        overflow: "hidden",
    },
    barFill: {
        height: "100%",
        borderRadius: 99,
        transition: "width 0.3s ease",
    },
    mapWrap: {
        flex: 1,
        position: "relative",
        overflow: "hidden",
    },
    hint: {
        position: "absolute",
        top: 14,
        left: "50%",
        transform: "translateX(-50%)",
        zIndex: 10,
        background: "rgba(15,23,42,0.82)",
        color: "white",
        padding: "8px 18px",
        borderRadius: 99,
        fontSize: 12,
        fontWeight: 500,
        backdropFilter: "blur(6px)",
        pointerEvents: "none",
        whiteSpace: "nowrap",
    },
    infoWindow: {
        minWidth: 180,
        maxWidth: 240,
        fontFamily: "'DM Sans', sans-serif",
    },
    formWrap: {
        flex: 1,
        overflowY: "auto",
        padding: "14px",
        display: "flex",
        flexDirection: "column",
        gap: 14,
    },
    pinBanner: {
        display: "flex",
        alignItems: "flex-start",
        gap: 10,
        background: "#fff7ed",
        border: "1px solid #fed7aa",
        borderRadius: 8,
        padding: "10px 12px",
    },
    noPinBanner: {
        background: "#f1f5f9",
        border: "1px dashed #cbd5e1",
        borderRadius: 8,
        padding: "10px 12px",
        fontSize: 12,
        color: "#64748b",
        lineHeight: 1.5,
    },
    input: {
        padding: "9px 11px",
        borderRadius: 7,
        border: "1px solid #e2e8f0",
        fontSize: 13,
        background: "#f8fafc",
        color: "#0f172a",
        outline: "none",
        width: "100%",
        boxSizing: "border-box",
        fontFamily: "inherit",
    },
    inputPinned: {
        background: "#fff7ed",
        borderColor: "#fdba74",
    },
    btnPrimary: {
        flex: 1,
        padding: "11px",
        background: "#215EBA",
        color: "white",
        border: "none",
        borderRadius: 8,
        fontWeight: 700,
        fontSize: 13,
        cursor: "pointer",
        fontFamily: "inherit",
    },
    btnSecondary: {
        padding: "11px 16px",
        background: "#f1f5f9",
        color: "#64748b",
        border: "none",
        borderRadius: 8,
        fontWeight: 600,
        fontSize: 13,
        cursor: "pointer",
        fontFamily: "inherit",
    },
};