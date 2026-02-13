import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import EventCard from './EventCard.jsx'
import EventWizard from './EventWizard.jsx'

function App() {

    // TODO: Get the actual user that's logged in
    const user = "Chris P. Bacon"
    
    const [events, setEvents] = useState([]);

    // Synchronize with backend (which is connected to the database)
    useEffect(() => {
	fetch("http://localhost:8080/api/events")
	    .then(res => res.json())
	    .then(data => {
		console.log("Events fetched!")
		setEvents(data)
	    })
    }, []);

    const addEvent = (newEvent) => {
	setEvents([...events, newEvent]);
    }

    // Syncs Frontend Event cards with actual database
    const deleteEvent = (id) => {
	setEvents(events.filter(event => event.eventId !== id))
    }


    // MAIN APPLICATION RENDER //
    return (	
	<div className="app-container" style={{ padding: '20px' }}>
	    <h1>Study Buddy</h1>
	    <hr></hr>
	    <h2>Welcome, {user}!</h2>
	    
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
