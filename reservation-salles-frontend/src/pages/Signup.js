import React, { useState } from "react";
import api from "../api";

function Signup() {
  const [formData, setFormData] = useState({ nom: "", email: "", motDePasse: "" });

  const handleSubmit = (e) => {
    e.preventDefault();

    api.post("/auth/signup", formData)
      .then((res) => {
        alert(res.data.message);
      })
      .catch((err) => {
        alert(err.response?.data?.error || "Erreur lors de l'inscription ❌");
      });
  };

  return (
    <div className="page">
      <h2>Créer un compte</h2>
      <form className="form" onSubmit={handleSubmit}>
        <input type="text" placeholder="Nom"
               onChange={(e)=>setFormData({...formData, nom:e.target.value})} required />
        <input type="email" placeholder="Email"
               onChange={(e)=>setFormData({...formData, email:e.target.value})} required />
        <input type="password" placeholder="Mot de passe"
               onChange={(e)=>setFormData({...formData, motDePasse:e.target.value})} required />
        <button type="submit">S'inscrire</button>
      </form>
    </div>
  );
}

export default Signup;
