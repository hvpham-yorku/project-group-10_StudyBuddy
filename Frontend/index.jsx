import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import EventCard from './EventCard.jsx'

function App() {
    // const[data, setData] = useState(null)
    // useEffect(() => {
    // 	fetch('http://localhost:8080/api/studentcontroller/getstudents')
    // 	    .then(res => res.json())
    // 	    .then(json => setData(json));
    // }, []);

    // return (
    // 	<div>
    // 	    {data ? JSON.stringify(data, null, 2) : "Fetching data..."}
    // 	</div>
    // )
    
    // Stub Data
    const events =  [
	{
	    "eventId": "12345",
	    "hostId": "bhavya99",
	    "title": "EECS 2311 Midterm Prep",
	    "course": "EECS 2311",
	    "location": "Scott Library, 2nd Floor",
	    "description": "Come join us for a 1 hr study session covering everything from git to system design!",
	    "start": "2026-02-13T14:00:00Z",
	    "end": "2026-02-13T15:00:00Z",
	    "participantIds": [
		"bhavya99"
	    ],
	    "maxCapacity": 6
	},
	{
	    "eventId": "12346",
	    "hostId": "chan69",
	    "title": "Peace Vibes",
	    "course": "Don't know bro",
	    "location": "Wherever",
	    "description": "Vibin",
	    "start": "2026-02-13T14:00:00Z",
	    "end": "2026-02-13T15:00:00Z",
	    "participantIds": [
		"bhavya99"
	    ],
	    "maxCapacity": 200
	},
	{
	    "eventId": "12347",
	    "hostId": "xxxd69",
	    "title": "Even bigger Peace Vibes",
	    "course": "Don't know bro, just wing it",
	    "location": "Wherever",
	    "description": "Vibin' harder",
	    "start": "2026-02-13T14:00:00Z",
	    "end": "2026-02-13T15:00:00Z",
	    "participantIds": [
		"bhavya99"
	    ],
	    "maxCapacity": 200
	}
	
    ]

    // return (
    // 	<div>
    // 	    {data ? JSON.stringify(data, null, 2) : "Fetching data..."}
    // 	</div>
    // )

    // Whops, it's my first time playing around with React. This here
    // can be thought of as the main application renderer. It takes in
    // components from the other files (we'll style eventually...)
    return (	
	<div className="app-container" style={{ padding: '20px' }}>
	    <h1>Welcome to Studdy Buddy!</h1>

	    <div
		className="events-grid"
		style={{
		    display: 'grid',
		    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
		    gap: '20px'
		}}
		>
	    {/* Go through each event...and then show the card */}
	    {events.map((event) => (
		<EventCard
		    eventId={event.eventId}
		    title={event.title}
		    description={event.description}
		    maxCapacity={event.maxCapacity}
		    participantIds={event.participantIds}
		/>
	    ))}
	    </div>
	</div>
    ); 
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
