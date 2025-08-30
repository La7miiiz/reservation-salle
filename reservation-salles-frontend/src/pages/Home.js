// src/pages/Home.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

const Home = () => {
  return (
    <div className="container mt-5">
      <div className="text-center mb-5">
        <h1>Bienvenue sur l'application de réservation</h1>
        <p className="lead">Gérez facilement vos réservations et consultez les salles disponibles.</p>
        <Link to="/salles" className="btn btn-primary m-2">Voir les Salles</Link>
        <Link to="/reservations" className="btn btn-success m-2">Mes Réservations</Link>
      </div>

      <div className="row">
        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-body">
              <h5 className="card-title">Réservation Rapide</h5>
              <p className="card-text">Réservez une salle rapidement en quelques clics.</p>
              <Link to="/reservations" className="btn btn-outline-primary">Réserver</Link>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4 shadow-sm">
            <div className="card-body">
              <h5 className="card-title">Consulter les Salles</h5>
              <p className="card-text">Consultez toutes les salles disponibles avec leurs détails.</p>
              <Link to="/salles" className="btn btn-outline-success">Voir les Salles</Link>
            </div>
          </div>
        </div>
      </div>

      <footer className="text-center mt-5">
        <p>&copy; 2025 Tunisie Télécom. Tous droits réservés.</p>
      </footer>
    </div>
  );
};

export default Home;
