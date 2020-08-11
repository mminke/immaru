import React from 'react';
import ReactDOM from 'react-dom';
import Button from '@material-ui/core/Button'
import AccessAlarmIcon from '@material-ui/icons/AccessAlarm';

function App() {
  return (
    <div>
        <h1>Media Manager</h1>
        <AccessAlarmIcon/>
        <Button variant="contained" color="primary">
            Media Manager
        </Button>
    </div>
  );
}

export default App;
