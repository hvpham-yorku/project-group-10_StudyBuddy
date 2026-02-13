import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import EventCard from './EventCard.jsx'
import EventWizard from './EventWizard.jsx'

function App() {

    // TODO: Get the actual user that's logged in
    const USER = "bobby_lee"

    const EVENTS_API = "http://localhost:8080/api/events"
    
    const [events, setEvents] = useState([]);

    // Synchronize with backend (which is connected to the database)
    useEffect(() => {
	fetch(EVENTS_API)
	    .then(res => res.json())
	    .then(data => {
		console.log("Events fetched!")
		setEvents(data)
	    })
    }, []);

    const addEvent = async (newEvent) => {
	const payload = {
            hostId: USER,
            title: newEvent.title,
            course: newEvent.course,
            location: newEvent.location,
            description: newEvent.description,
            maxCapacity: newEvent.maxCapacity || 5,
            startTime: newEvent.startTime,
            endTime: newEvent.endTime,
	    participantIds: newEvent.participantIds
        }

	const request = await(fetch(EVENTS_API, {
	    method: 'POST',
	    headers: { 'Content-Type': 'application/json'},
	    body: JSON.stringify(payload)
	}))

	// Update Event Cards after successful addition
	if (request.ok) {
	    const savedEvent = await request.json();
	    setEvents([...events, savedEvent]);
	}
	else {
	    console.log(request)
	}
    }

    // Syncs Frontend Event cards with actual database
    const deleteEvent = async (eventId) => {
	const deletionRequest = `${EVENTS_API}/${eventId}?userId=${USER}`
	const request = await(fetch(deletionRequest, {
	    method: 'DELETE',
	    headers: { 'Content-Type' : 'application/json'},
	}))

	if (request.ok) {
	    setEvents(events.filter(event => event.eventId !== eventId))
	}
	else {
	    alert("You cannot delete this event.")
	}
    }


    // MAIN APPLICATION RENDER //
    return (	
	<div className="app-container" style={{ padding: '20px' }}>
	    <h1>Study Buddy</h1>
	    <hr></hr>
	    <h2>Welcome, {USER}!</h2>
	    
	    {/* Events View Wizard*/}
	    <EventWizard onAddEvent={addEvent} />

	    {/* Events View Grid */}
	    <div className="events-grid"
		 style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }} >
	    {/* Go through each event...and then show the card */}
	    {events.map((event) => (
		<EventCard
		    eventId={event.eventId}
		    title={event.title}
		    description={event.description}
		    maxCapacity={event.maxCapacity}
		    participantIds={event.participantIds}
		    onDelete={deleteEvent}
		/>
	    ))}
	    </div>
	</div>
    ); 
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
