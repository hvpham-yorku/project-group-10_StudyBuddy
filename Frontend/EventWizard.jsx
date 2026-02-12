import React, { useState } from 'react';

export default function EventForm({ onAddEvent }) {
  
  // This function runs when you click "Add Event"
  const handleSubmit = (e) => {
    e.preventDefault(); // Prevents the page from refreshing (default HTML behavior)

    // 1. Get the data from the form fields
    const form = e.target;
    const newEvent = {
      id: Date.now(), // Generate a unique ID based on time
      title: form.title.value,
      course: form.course.value,
      location: form.location.value,
      description: form.description.value,
      // Convert capacity to a Number (inputs return strings)
      maxCapacity: Number(form.maxCapacity.value),
      hostId: "vaughn_test_1" // Hardcoded for now (Task 3 MVP Catch)
    };

    // 2. Send this data BACK up to index.jsx
    onAddEvent(newEvent);

    // 3. Clear the form
    form.reset();
  };

  return (
    <div className="form-container" style={{ border: '2px dashed #ccc', padding: '20px', marginBottom: '20px' }}>
      <h3>Create New Event</h3>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxWidth: '400px' }}>
        
        <label>Title: <input type="text" name="title" required /></label>
        <label>Course: <input type="text" name="course" required /></label>
        <label>Location: <input type="text" name="location" required /></label>
        <label>Description: <textarea name="description" required /></label>
        
        <div style={{ display: 'flex', gap: '10px' }}>
             <label>Start: <input type="datetime-local" name="startTime" /></label>
             <label>End: <input type="datetime-local" name="endTime" /></label>
        </div>

        <label>Max Capacity: <input type="number" name="maxCapacity" min="1" defaultValue="3" /></label>

        {/* The Button! */}
        <button type="submit" style={{ 
            backgroundColor: '#4CAF50', 
            color: 'white', 
            padding: '10px', 
            border: 'none', 
            cursor: 'pointer',
            fontSize: '16px'
        }}>
          Add Event
        </button>

      </form>
    </div>
  );
}
