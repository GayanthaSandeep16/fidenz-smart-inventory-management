import React from 'react';
import { Navbar, Nav, Button, Container } from 'react-bootstrap';

const Navigation = ({ user, onLogout }) => {
  return (
    <Navbar bg="primary" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand>
          Inventory Management System
        </Navbar.Brand>
        
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link href="#dashboard">Dashboard</Nav.Link>
            <Nav.Link href="#inventory">Inventory</Nav.Link>
            <Nav.Link href="#sales">Sales</Nav.Link>
            <Nav.Link href="#analytics">Analytics</Nav.Link>
          </Nav>
          
          <Nav>
            <Navbar.Text className="me-3">
              Welcome, <strong>{user.username}</strong> ({user.role})
            </Navbar.Text>
            <Button variant="outline-light" size="sm" onClick={onLogout}>
              Logout
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation;
