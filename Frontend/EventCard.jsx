import React from 'react';

export default function EventCard(data) {
    return (
	<div className="eventCard">
	    <h3>{data.title}</h3>
	    <p>{data.description}</p>
	    <p>{data.hostId}</p>
	</div>
    )
}
