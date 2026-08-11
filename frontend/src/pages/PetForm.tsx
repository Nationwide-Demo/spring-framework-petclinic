import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createPet, getPet, getPetTypes, updatePet } from '../api/client';
import type { PetFields, PetType } from '../api/types';
import ErrorMessage from '../components/ErrorMessage';

export default function PetForm() {
  const { ownerId, petId } = useParams();
  const navigate = useNavigate();
  const [types, setTypes] = useState<PetType[]>([]);
  const [fields, setFields] = useState<PetFields>({ name: '', birthDate: '', typeId: 0 });
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    getPetTypes()
      .then((petTypes) => {
        setTypes(petTypes);
        setFields((current) => (current.typeId ? current : { ...current, typeId: petTypes[0]?.id ?? 0 }));
      })
      .catch(setError);
  }, []);

  useEffect(() => {
    if (!petId) return;
    getPet(Number(petId))
      .then((pet) => setFields({ name: pet.name, birthDate: pet.birthDate ?? '', typeId: pet.typeId ?? 0 }))
      .catch(setError);
  }, [petId]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    const payload = { ...fields, birthDate: fields.birthDate || null };
    try {
      if (petId) {
        await updatePet(Number(ownerId), Number(petId), payload);
      } else {
        await createPet(Number(ownerId), payload);
      }
      navigate(`/owners/${ownerId}`);
    } catch (submitError) {
      setError(submitError);
    }
  };

  return (
    <>
      <h2>{petId ? 'Update' : 'New'} Pet</h2>
      {error ? <ErrorMessage error={error} /> : null}
      <form className="form-horizontal" onSubmit={submit}>
        <div className="form-group">
          <label className="col-sm-2 control-label" htmlFor="name">
            Name
          </label>
          <div className="col-sm-10">
            <input
              id="name"
              className="form-control"
              value={fields.name}
              onChange={(event) => setFields({ ...fields, name: event.target.value })}
              required
            />
          </div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label" htmlFor="birthDate">
            Birth Date
          </label>
          <div className="col-sm-10">
            <input
              id="birthDate"
              type="date"
              className="form-control"
              value={fields.birthDate ?? ''}
              onChange={(event) => setFields({ ...fields, birthDate: event.target.value })}
            />
          </div>
        </div>
        <div className="form-group">
          <label className="col-sm-2 control-label" htmlFor="typeId">
            Type
          </label>
          <div className="col-sm-10">
            <select
              id="typeId"
              className="form-control"
              value={fields.typeId}
              onChange={(event) => setFields({ ...fields, typeId: Number(event.target.value) })}
            >
              {types.map((type) => (
                <option key={type.id} value={type.id}>
                  {type.name}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className="form-group">
          <div className="col-sm-offset-2 col-sm-10">
            <button className="btn btn-primary" type="submit">
              {petId ? 'Update Pet' : 'Add Pet'}
            </button>
          </div>
        </div>
      </form>
    </>
  );
}
