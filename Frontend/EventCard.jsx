import React from 'react';
import ProgressBar from './ProgressBar.jsx'

export default function EventCard(data) {
    return (
	<div className="eventCard">
	    <h3>{data.title}</h3>
	    <p>{data.description}</p>
	    <p>{data.hostId}</p>

	    <ProgressBar
		current={data.participantIds.length}
		max={data.maxCapacity}
	    />

	    {/* Handles the deletion of the card  */}
	    <button onClick={() => data.onDelete(data.eventId)}>
		Delete Event
	    </button>
	    
	</div>
    )
}
