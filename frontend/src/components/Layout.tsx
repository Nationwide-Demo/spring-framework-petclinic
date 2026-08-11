import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';

const links = [
  { to: '/', label: 'Home', end: true },
  { to: '/owners/find', label: 'Find owners', end: false },
  { to: '/vets', label: 'Veterinarians', end: false },
];

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark">
        <div className="container-fluid">
          <NavLink className="navbar-brand" to="/">
            <span />
          </NavLink>
          <div className="collapse navbar-collapse show" id="main-navbar">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              {links.map((link) => (
                <li className="nav-item" key={link.to}>
                  <NavLink
                    className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
                    to={link.to}
                    end={link.end}
                  >
                    <span>{link.label}</span>
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </nav>
      <div className="container-fluid">
        <div className="container xd-container">
          {children}
          <br />
          <br />
        </div>
      </div>
    </>
  );
}
