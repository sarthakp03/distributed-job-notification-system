import React from 'react';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import './App.css';

import Home from "./pages/Home";
import Register from "./pages/Register";
import Company from "./pages/Company";

function App() {
  return (
    <div>
      <Router>
        <Routes>
          <Route path="/" element={<Home />}></Route>
          <Route path="/register" element={<Register />}></Route>
          <Route path="/company" element={<Company />}></Route>
        </Routes>
      </Router>
    </div>
  );
}

export default App;
