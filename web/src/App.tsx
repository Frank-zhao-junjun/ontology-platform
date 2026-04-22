import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import OntologyList from './pages/OntologyList';
import OntologyDetail from './pages/OntologyDetail';
import ObjectTypeList from './pages/ObjectTypeList';
import GraphTraversal from './pages/GraphTraversal';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/ontologies" replace />} />
        <Route path="ontologies" element={<OntologyList />} />
        <Route path="ontologies/:id" element={<OntologyDetail />} />
        <Route path="ontologies/:id/object-types" element={<ObjectTypeList />} />
        <Route path="ontologies/:id/graph" element={<GraphTraversal />} />
      </Route>
    </Routes>
  );
}

export default App;
