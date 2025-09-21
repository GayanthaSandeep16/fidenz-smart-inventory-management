import React, { useState, useEffect } from 'react';
import { Container } from 'react-bootstrap';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Navigation from './components/Navigation';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  useEffect(() => {
    // Check if user is already logged in
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
      setIsAuthenticated(true);
    }
  }, []);

  const handleLogin = (authData) => {
    setToken(authData.token);
    setUser({ username: authData.username, role: authData.role });
    setIsAuthenticated(true);
    
    // Save to localStorage
    localStorage.setItem('token', authData.token);
    localStorage.setItem('user', JSON.stringify({ username: authData.username, role: authData.role }));
  };

  const handleLogout = () => {
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
    
    // Clear localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  return (
    <div className="App">
      {isAuthenticated && <Navigation user={user} onLogout={handleLogout} />}
      
      <Container className="mt-4">
        {!isAuthenticated ? (
          <Login onLogin={handleLogin} />
        ) : (
          <Dashboard user={user} token={token} />
        )}
      </Container>
    </div>
  );
}

export default App;