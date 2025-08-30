import React, { useState } from "react";
import api from "../api";
import { useNavigate } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    api.post("/auth/login", { email, motDePasse })
      .then((res) => {
        alert(res.data.message);
        navigate("/reservations"); // redirige après login
      })
      .catch((err) => {
        alert(err.response?.data?.error || "Erreur de connexion ❌");
      });
  };

  return (
    <div className="page">
      <h2>Connexion</h2>
      <form className="form" onSubmit={handleLogin}>
        <input type="email" placeholder="Email"
               value={email} onChange={(e)=>setEmail(e.target.value)} required />
        <input type="password" placeholder="Mot de passe"
               value={motDePasse} onChange={(e)=>setMotDePasse(e.target.value)} required />
        <button type="submit">Se connecter</button>
      </form>
    </div>
  );
}

export default Login;
