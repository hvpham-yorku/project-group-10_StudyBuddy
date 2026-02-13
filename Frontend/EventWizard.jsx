import React, { useState } from 'react';

export default function EventWizard({ onAddEvent }) {

    // Basically like HTML form
    const handleSubmit = (e) => {
	e.preventDefault();

	const form = e.target;

	const newEvent = {
	    title: form.title.value,
	    description: form.description.value,
	    course: form.course.value,
	    location: form.location.value,
	    // datetime-local returns the format required by the backend, but with an extra T
	    startTime: form.startTime.value.replace("T", " "),
	    endTime: form.endTime.value.replace("T", " "),
	    maxCapacity: Number(form.maxCapacity.value),
	    participantIds: []
	};

	onAddEvent(newEvent);
	form.reset();
    }

    return (
	<div className="event-wizard">
	    <h3>Add New Event</h3>
	    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', maxWidth: '600px' }}>
		<div style={{ display:'flex', flexDirection: 'row' }}>
		    <label htmlFor="title">Event title:   </label>
		    <input name="title" placeholder="(e.g Software Development)" />
		</div>
		
		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="description">Description: </label>
		    <input name="description" placeholder="(e.g Group for SE Students to solve problems)" />
		</div>
		
		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="course">Course:  </label>
		    <input name="course" placeholder="(e.g 2311, 4413, ...)"/>
		</div>

		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="location">Location: </label>
		    <input name="location" placeholder="(e.g VH, Steacies Library, ...)"/>
		</div>

		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="startTime">Start Time:  </label>
		    <input name="startTime" type="datetime-local"/>
		</div>

		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="endTime">End Time:  </label>
		    <input name="endTime" type="datetime-local"/>
		</div>

		<div style={{ display:'flex', flexDirection: 'row'}}>
		    <label htmlFor="maxCapacity">Max Students:  </label>
		    <input name="maxCapacity" type="number" placeholder="Max spots"/>
		</div>
		
		<button type="submit">
		    Add event
		</button>
	    </form>
	</div>
    )
}
