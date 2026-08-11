import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function FindOwners() {
  const [lastName, setLastName] = useState('');
  const navigate = useNavigate();

  return (
    <>
      <h2>Find Owners</h2>
      <form
        className="form-horizontal"
        onSubmit={(event) => {
          event.preventDefault();
          navigate(`/owners?lastName=${encodeURIComponent(lastName)}`);
        }}
      >
        <div className="form-group">
          <div className="control-group" id="lastNameGroup">
            <label className="col-sm-2 control-label" htmlFor="lastName">
              Last name
            </label>
            <div className="col-sm-10">
              <input
                id="lastName"
                className="form-control"
                value={lastName}
                onChange={(event) => setLastName(event.target.value)}
              />
            </div>
          </div>
        </div>
        <div className="form-group">
          <div className="col-sm-offset-2 col-sm-10">
            <button type="submit" className="btn btn-primary">
              Find Owner
            </button>
          </div>
        </div>
      </form>
      <br />
      <a className="btn btn-primary" href="/owners/new">
        Add Owner
      </a>
    </>
  );
}
