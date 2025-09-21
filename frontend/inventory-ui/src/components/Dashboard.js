import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Tab, Tabs } from 'react-bootstrap';
import InventoryList from './InventoryList';
import SalesForm from './SalesForm';
import ABCAnalysis from './ABCAnalysis';
import ReorderRecommendations from './ReorderRecommendations';
import axios from 'axios';

const Dashboard = ({ user, token }) => {
  const [stats, setStats] = useState({
    totalProducts: 0,
    lowStockItems: 0,
    todaySales: 0,
    reorderAlerts: 0
  });

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      // Mock stats for now - you can implement these endpoints later
      setStats({
        totalProducts: 156,
        lowStockItems: 12,
        todaySales: 2450.00,
        reorderAlerts: 8
      });
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
    }
  };

  const axiosConfig = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };

  return (
    <div>
      {/* Stats Cards */}
      <Row className="mb-4">
        <Col md={3}>
          <Card className="text-center bg-primary text-white">
            <Card.Body>
              <h3>{stats.totalProducts}</h3>
              <p>Total Products</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="text-center bg-warning text-dark">
            <Card.Body>
              <h3>{stats.lowStockItems}</h3>
              <p>Low Stock Items</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="text-center bg-success text-white">
            <Card.Body>
              <h3>${stats.todaySales}</h3>
              <p>Today's Sales</p>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="text-center bg-danger text-white">
            <Card.Body>
              <h3>{stats.reorderAlerts}</h3>
              <p>Reorder Alerts</p>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Main Content Tabs */}
      <Tabs defaultActiveKey="inventory" id="dashboard-tabs" className="mb-3">
        <Tab eventKey="inventory" title="📦 Inventory">
          <InventoryList token={token} axiosConfig={axiosConfig} />
        </Tab>
        
        <Tab eventKey="sales" title="💰 Record Sale">
          <SalesForm token={token} axiosConfig={axiosConfig} />
        </Tab>
        
        <Tab eventKey="abc-analysis" title="📊 ABC Analysis">
          <ABCAnalysis token={token} axiosConfig={axiosConfig} />
        </Tab>
        
        <Tab eventKey="reorder" title="🔄 Reorder Recommendations">
          <ReorderRecommendations token={token} axiosConfig={axiosConfig} />
        </Tab>
      </Tabs>
    </div>
  );
};

export default Dashboard;
