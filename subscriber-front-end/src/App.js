import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import './App.css';

import Home from './pages/Home';
import Subscribe from "./pages/Subscribe";
import Jobs from "./pages/Jobs";

function App() {
    return(
      <div>
        <Router>
          <Routes>
            <Route path="/" element={<Home />}></Route>
            <Route path="/subscribe" element={<Subscribe />}></Route>
            <Route path="/jobs" element={<Jobs />}></Route>
          </Routes>
        </Router>
      </div>
    )
}

export default App;