import { useEffect, useState } from 'react';
import { getVets } from '../api/client';
import type { Vet } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';

export default function VetsList() {
  const [vets, setVets] = useState<Vet[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getVets().then(setVets).catch(setError);
  }, []);

  if (error) return <ErrorMessage error={error} />;
  if (!vets) return <Spinner />;

  return (
    <>
      <h2>Veterinarians</h2>
      <table id="vets" className="table table-striped">
        <thead>
          <tr>
            <th>Name</th>
            <th>Specialties</th>
          </tr>
        </thead>
        <tbody>
          {vets.map((vet) => (
            <tr key={vet.id}>
              <td>
                {vet.firstName} {vet.lastName}
              </td>
              <td>{vet.specialties.length ? vet.specialties.join(' ') : 'none'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
