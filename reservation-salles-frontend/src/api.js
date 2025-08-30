import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true // ⚡ permet d'envoyer les cookies de session
});

export default api;
