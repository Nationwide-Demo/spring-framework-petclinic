import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { findOwners } from '../api/client';
import type { Owner } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';

export default function OwnersList() {
  const [searchParams] = useSearchParams();
  const lastName = searchParams.get('lastName') ?? '';
  const [owners, setOwners] = useState<Owner[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    setOwners(null);
    findOwners(lastName).then(setOwners).catch(setError);
  }, [lastName]);

  if (error) return <ErrorMessage error={error} />;
  if (!owners) return <Spinner />;

  if (owners.length === 0) {
    return (
      <>
        <h2>Owners</h2>
        <p>No owners found for &quot;{lastName}&quot;.</p>
        <Link className="btn btn-primary" to="/owners/find">
          Search again
        </Link>
      </>
    );
  }

  return (
    <>
      <h2>Owners</h2>
      <table id="owners" className="table table-striped">
        <thead>
          <tr>
            <th>Name</th>
            <th>Address</th>
            <th>City</th>
            <th>Telephone</th>
            <th>Pets</th>
          </tr>
        </thead>
        <tbody>
          {owners.map((owner) => (
            <tr key={owner.id}>
              <td>
                <Link to={`/owners/${owner.id}`}>
                  {owner.firstName} {owner.lastName}
                </Link>
              </td>
              <td>{owner.address}</td>
              <td>{owner.city}</td>
              <td>{owner.telephone}</td>
              <td>{owner.pets.map((pet) => pet.name).join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
