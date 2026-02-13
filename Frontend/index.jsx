import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import EventCard from './EventCard.jsx'
import EventWizard from './EventWizard.jsx'

function App() {
    const[data, setData] = useState(null)
    useEffect(() => {
	fetch('/api/studentcontroller/getstudents')
	    .then(res => res.json())
	    .then(json => setData(json));
    }, []);

    return (
	<div>
	    {data ? JSON.stringify(data, null, 2) : "Fetching data..."}
	</div>
    ); 
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
