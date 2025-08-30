import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import "../styles/App.css";

export default function Navbar() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  // Charger l'utilisateur connecté depuis la session
  useEffect(() => {
    axios.get("http://localhost:8081/api/utilisateurs/me", { withCredentials: true })
      .then(res => setUser(res.data))
      .catch(() => setUser(null));
  }, []);

  // Déconnexion
  const handleLogout = async () => {
    await axios.post("http://localhost:8081/api/utilisateurs/logout", {}, { withCredentials: true });
    setUser(null);
    navigate("/login");
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand" to="/">Reservation Salles</Link>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav ms-auto">

            <li className="nav-item">
              <Link className="nav-link" to="/salles">Salles</Link>
            </li>

            {user && (
              <li className="nav-item">
                <Link className="nav-link" to="/reservations">Mes Réservations</Link>
              </li>
            )}

            {/* Si ADMIN → lien gérer salles */}
            {user?.role === "ADMIN" && (
              <li className="nav-item">
                <Link className="nav-link" to="/admin/salles">Gérer Salles</Link>
              </li>
            )}

            {!user ? (
              <>
                <li className="nav-item"><Link className="nav-link" to="/login">Login</Link></li>
                <li className="nav-item"><Link className="nav-link" to="/signup">Signup</Link></li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  <span className="nav-link">👤 {user.nom} ({user.role})</span>
                </li>
                <li className="nav-item">
                  <button className="btn btn-outline-light btn-sm ms-2" onClick={handleLogout}>
                    Logout
                  </button>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
}
