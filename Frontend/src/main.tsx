// Serves as the entry point for the React application, rendering the App component into the DOM.
// Once done, App.tsx will handle the routing and rendering of different pages based on the URL path.
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './styles/index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)