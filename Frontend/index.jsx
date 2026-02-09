import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';

function App() {
    const[data, setData] = useState(null)
    useEffect(() => {
	fetch('http://localhost:8080/api/studentcontroller/getstudents')
	    .then(res => res.json())
	    .then(json => setData(json));
    }, []);

    return (
	<div>
	    {data ? JSON.stringify(data, null, 2) : "Fetching data..."}
	</div>
    )
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
