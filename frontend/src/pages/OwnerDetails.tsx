import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getOwner } from '../api/client';
import type { Owner } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';

export default function OwnerDetails() {
  const { ownerId } = useParams();
  const [owner, setOwner] = useState<Owner | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getOwner(Number(ownerId)).then(setOwner).catch(setError);
  }, [ownerId]);

  if (error) return <ErrorMessage error={error} />;
  if (!owner) return <Spinner />;

  return (
    <>
      <h2 id="ownerInformation">Owner Information</h2>
      <table className="table table-striped" aria-describedby="ownerInformation">
        <tbody>
          <tr>
            <th>Name</th>
            <td>
              <strong>
                {owner.firstName} {owner.lastName}
              </strong>
            </td>
          </tr>
          <tr>
            <th>Address</th>
            <td>{owner.address}</td>
          </tr>
          <tr>
            <th>City</th>
            <td>{owner.city}</td>
          </tr>
          <tr>
            <th>Telephone</th>
            <td>{owner.telephone}</td>
          </tr>
        </tbody>
      </table>

      <Link className="btn btn-primary" to={`/owners/${owner.id}/edit`}>
        Edit Owner
      </Link>{' '}
      <Link className="btn btn-primary" to={`/owners/${owner.id}/pets/new`}>
        Add New Pet
      </Link>

      <br />
      <br />
      <h2 id="petsAndVisits">Pets and Visits</h2>
      <table className="table table-striped" aria-describedby="petsAndVisits">
        <tbody>
          {owner.pets.map((pet) => (
            <tr key={pet.id}>
              <th scope="col">
                <dl className="dl-horizontal">
                  <dt>Name</dt>
                  <dd>{pet.name}</dd>
                  <dt>Birth Date</dt>
                  <dd>{pet.birthDate}</dd>
                  <dt>Type</dt>
                  <dd>{pet.type}</dd>
                </dl>
              </th>
              <td>
                <table className="table-condensed">
                  <thead>
                    <tr>
                      <th>Visit Date</th>
                      <th>Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(pet.visits ?? []).map((visit) => (
                      <tr key={visit.id}>
                        <td>{visit.date}</td>
                        <td>{visit.description}</td>
                      </tr>
                    ))}
                    <tr>
                      <td>
                        <Link to={`/owners/${owner.id}/pets/${pet.id}/edit`}>Edit Pet</Link>
                      </td>
                      <td>
                        <Link to={`/owners/${owner.id}/pets/${pet.id}/visits/new`}>Add Visit</Link>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
