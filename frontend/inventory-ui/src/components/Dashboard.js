import React from 'react';
import { Tab, Tabs } from 'react-bootstrap';
import InventoryList from './InventoryList';
import SalesForm from './SalesForm';
import ABCAnalysis from './ABCAnalysis';
import ReorderRecommendations from './ReorderRecommendations';
import axios from 'axios';

const Dashboard = ({ user, token }) => {

  const axiosConfig = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };

  return (
    <div>

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
