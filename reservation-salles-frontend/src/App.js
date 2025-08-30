import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Salles from "./pages/Salles";
import Reservations from "./pages/Reservations";
import AdminSalles from "./pages/AdminSalles";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/salles" element={<Salles />} />
        <Route path="/reservations" element={
          <ProtectedRoute>
            <Reservations />
          </ProtectedRoute>
        } />
        <Route path="/admin" element={
          <ProtectedRoute>
            <AdminSalles />
          </ProtectedRoute>
        } />
      </Routes>
      <Footer />
    </Router>
  );
}

export default App;
