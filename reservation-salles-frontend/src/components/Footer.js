import React from "react";
import "../styles/App.css";

function Footer() {
  return (
    <footer className="footer">
      <p>© {new Date().getFullYear()} - Réservation de Salles | Tous droits réservés</p>
    </footer>
  );
}

export default Footer;
