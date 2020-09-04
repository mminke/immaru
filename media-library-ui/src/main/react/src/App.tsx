import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';

import {BrowserRouter as Router, Switch, Route, Link} from "react-router-dom"

import MainAppBar from './MainAppBar'
import MainDrawer from './MainDrawer'
import ImageList from './ImageList'
import FileUpload from './FileUpload'

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
  },
  content: {
    flexGrow: 1,
    height: '100vh',
    overflow: 'auto',
    paddingTop: '20px'
  },
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  },
}));

function App() {
  const classes = useStyles();
  const [open, setOpen] = React.useState(false);
  const handleDrawerOpen = () => {
    setOpen(true);
  };
  const handleDrawerClose = () => {
    setOpen(false);
  };

  return (
    <div className={classes.root}>
        <Router>
            <MainAppBar open={open} handleDrawerOpen={handleDrawerOpen}/>
            <MainDrawer open={open} handleDrawerClose={handleDrawerClose}/>

            <main className={classes.content}>
                <Container maxWidth="xl" className={classes.container}>
                    <Switch>
                        <Route path="/upload">
                            <FileUpload/>
                        </Route>
                       <Route path={["/", "/media"]}>
                            <ImageList/>
                        </Route>
                    </Switch>
                </Container>
            </main>
        </Router>
    </div>
  );
}

export default App;
