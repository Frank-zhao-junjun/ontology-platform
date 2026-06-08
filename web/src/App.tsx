import { Route, Router, Switch, Redirect } from 'wouter';
import Layout from './components/Layout';
import OntologyList from './pages/OntologyList';
import OntologyDetail from './pages/OntologyDetail';
import ObjectTypeList from './pages/ObjectTypeList';
import GraphTraversal from './pages/GraphTraversal';

function App() {
  return (
    <Router>
      <Switch>
        <Route path="/" component={() => <Redirect to="/ontologies" />} />
        <Route path="/ontologies">
          <Layout><OntologyList /></Layout>
        </Route>
        <Route path="/ontologies/:id">
          <Layout><OntologyDetail /></Layout>
        </Route>
        <Route path="/ontologies/:id/object-types">
          <Layout><ObjectTypeList /></Layout>
        </Route>
        <Route path="/ontologies/:id/graph">
          <Layout><GraphTraversal /></Layout>
        </Route>
        <Route>
          <Layout><OntologyList /></Layout>
        </Route>
      </Switch>
    </Router>
  );
}

export default App;
