import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Welcome from './pages/Welcome';
import FindOwners from './pages/FindOwners';
import OwnersList from './pages/OwnersList';
import OwnerDetails from './pages/OwnerDetails';
import OwnerForm from './pages/OwnerForm';
import PetForm from './pages/PetForm';
import VisitForm from './pages/VisitForm';
import VetsList from './pages/VetsList';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Welcome />} />
        <Route path="/owners/find" element={<FindOwners />} />
        <Route path="/owners" element={<OwnersList />} />
        <Route path="/owners/new" element={<OwnerForm />} />
        <Route path="/owners/:ownerId" element={<OwnerDetails />} />
        <Route path="/owners/:ownerId/edit" element={<OwnerForm />} />
        <Route path="/owners/:ownerId/pets/new" element={<PetForm />} />
        <Route path="/owners/:ownerId/pets/:petId/edit" element={<PetForm />} />
        <Route path="/owners/:ownerId/pets/:petId/visits/new" element={<VisitForm />} />
        <Route path="/vets" element={<VetsList />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
