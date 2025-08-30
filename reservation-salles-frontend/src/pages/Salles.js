import React, { useEffect, useState } from "react";
import api from "../api";

function Salles() {
  const [salles, setSalles] = useState([]);

  useEffect(() => {
    api.get("/salles")
      .then((res) => setSalles(res.data))
      .catch((err) => console.error(err));
  }, []);

  const reserver = (salleId) => {
    const reservation = {
      salle: { id: salleId },
      dateDebut: "2025-08-27T09:00:00",
      dateFin: "2025-08-27T11:00:00"
    };

    api.post("/reservations", reservation)
      .then(() => alert("Réservation réussie ✅"))
      .catch(() => alert("Erreur ❌"));
  };

  return (
    <div className="page">
      <h2>Liste des Salles</h2>
      <ul className="card-list">
        {salles.map((salle) => (
          <li key={salle.id} className="card">
            <h3>{salle.nom}</h3>
            <p>Capacité: {salle.capacite}</p>
            <button onClick={() => reserver(salle.id)}>Réserver</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Salles;
