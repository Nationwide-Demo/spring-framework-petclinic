import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createOwner, getOwner, updateOwner } from '../api/client';
import type { OwnerFields } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';

const EMPTY: OwnerFields = { firstName: '', lastName: '', address: '', city: '', telephone: '' };

export default function OwnerForm() {
  const { ownerId } = useParams();
  const navigate = useNavigate();
  const [fields, setFields] = useState<OwnerFields>(EMPTY);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    if (!ownerId) return;
    getOwner(Number(ownerId))
      .then(({ firstName, lastName, address, city, telephone }) =>
        setFields({ firstName, lastName, address, city, telephone }),
      )
      .catch(setError);
  }, [ownerId]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      const owner = ownerId ? await updateOwner(Number(ownerId), fields) : await createOwner(fields);
      navigate(`/owners/${owner.id}`);
    } catch (submitError) {
      setError(submitError);
    }
  };

  const field = (name: keyof OwnerFields, label: string) => (
    <div className="form-group">
      <label className="col-sm-2 control-label" htmlFor={name}>
        {label}
      </label>
      <div className="col-sm-10">
        <input
          id={name}
          className="form-control"
          value={fields[name]}
          onChange={(event) => setFields({ ...fields, [name]: event.target.value })}
          required
        />
      </div>
    </div>
  );

  return (
    <>
      <h2>{ownerId ? 'Update' : 'New'} Owner</h2>
      {error ? <ErrorMessage error={error} /> : null}
      <form className="form-horizontal" onSubmit={submit}>
        {field('firstName', 'First Name')}
        {field('lastName', 'Last Name')}
        {field('address', 'Address')}
        {field('city', 'City')}
        {field('telephone', 'Telephone')}
        <div className="form-group">
          <div className="col-sm-offset-2 col-sm-10">
            <button className="btn btn-primary" type="submit">
              {ownerId ? 'Update Owner' : 'Add Owner'}
            </button>
          </div>
        </div>
      </form>
    </>
  );
}
