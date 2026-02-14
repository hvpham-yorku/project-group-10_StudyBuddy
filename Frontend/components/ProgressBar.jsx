import React from 'react';

export default function ProgressBar(data) {
    return (
	<div className="progressBar">
	    <progress value={data.current} max={data.max} style={{width: '100%'}} />
	    <small>{data.current} / {data.max}</small>
	</div>
    )
}
