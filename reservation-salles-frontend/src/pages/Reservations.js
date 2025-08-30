import React, { useEffect, useState } from "react";
import api from "../api";

function Reservations() {
  const [reservations, setReservations] = useState([]);

  useEffect(() => {
    api.get("/reservations")
      .then((res) => setReservations(res.data))
      .catch((err) => console.error(err));
  }, []);

  return (
    <div className="page">
      <h2>Mes Réservations</h2>
      <ul className="card-list">
        {reservations.map((res) => (
          <li key={res.id} className="card">
            <h3>{res.salle.nom}</h3>
            <p>
              {new Date(res.dateDebut).toLocaleString()} -{" "}
              {new Date(res.dateFin).toLocaleString()}
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Reservations;
