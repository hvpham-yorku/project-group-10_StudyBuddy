import React, { useState } from 'react';

export default function EventWizard({ onAddEvent }) {

    // Basically like HTML form
    const handleSubmit = (e) => {
	e.preventDefault();

	const form = e.target;

	const newEvent = {
	    id: 0, // TODO: Automatically generate an ID
	    title: form.title.value,
	    description: form.description.value,
	    maxCapacity: Number(form.maxCapacity.value) || 5, // Default to 5 if empty
	    hostId: "vaughn_test_1",
	    participantIds: [] // Start empty
	};

	onAddEvent(newEvent);

	form.reset();
    }

    return (
	<div className="event-wizard">
	    <h3>Add New Event</h3>
	    <form onSubmit={handleSubmit} style={{
		      display: 'flex',
		      flexDirection: 'column',
		      maxWidth: '300px'
		  }}>
		<div style={{ display:'flex', flexDirection: 'row' }}>
		    <label for="title">Event title:   </label>
		    <input name="title" placeholder="(e.g Software Development)" />
		</div>
		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label for="description">Description: </label>
		    <input name="description" placeholder="(e.g Group for SE Students to solve problems)" />
		</div>
		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label for="maxCapacity">Max Students:  </label>
		    <input name="maxCapacity" type="number" placeholder="Max spots"/>
		</div>
		<button type="submit">
		    Add event
		</button>
	    </form>
	</div>
    )
}
