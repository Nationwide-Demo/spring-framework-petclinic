import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createVisit, getPet } from '../api/client';
import type { Pet } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';

export default function VisitForm() {
  const { ownerId, petId } = useParams();
  const navigate = useNavigate();
  const [pet, setPet] = useState<Pet | null>(null);
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [description, setDescription] = useState('');
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getPet(Number(petId)).then(setPet).catch(setError);
  }, [petId]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      await createVisit(Number(petId), { date, description });
      navigate(`/owners/${ownerId}`);
    } catch (submitError) {
      setError(submitError);
    }
  };

  return (
    <>
      <h2>New Visit</h2>
      {error ? <ErrorMessage error={error} /> : null}
      {pet ? (
        <>
          <b>Pet</b>
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Name</th>
                <th>Birth Date</th>
                <th>Type</th>
                <th>Owner</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{pet.name}</td>
                <td>{pet.birthDate}</td>
                <td>{pet.type}</td>
                <td>{pet.ownerName}</td>
              </tr>
            </tbody>
          </table>
        </>
      ) : null}
      <form className="form-horizontal" onSubmit={submit}>
        <div className="form-group">
          <label className="col-sm-2 control-label" htmlFor="date">
            Date
          </label>
          <div className="col-sm-10">
            <input
              id="date"
              type="date"
              className="form-control"
              value={date}
              onChange={(event) => setDate(event.target.value)}
            />
          </div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label" htmlFor="description">
            Description
          </label>
          <div className="col-sm-10">
            <input
              id="description"
              className="form-control"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              required
            />
          </div>
        </div>
        <div className="form-group">
          <div className="col-sm-offset-2 col-sm-10">
            <button className="btn btn-primary" type="submit">
              Add Visit
            </button>
          </div>
        </div>
      </form>
    </>
  );
}
