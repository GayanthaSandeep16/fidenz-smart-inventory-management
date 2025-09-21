import React, { useState, useRef } from 'react';
import { Tab, Tabs } from 'react-bootstrap';
import InventoryList from './InventoryList';
import SalesForm from './SalesForm';
import ABCAnalysis from './ABCAnalysis';
import ReorderRecommendations from './ReorderRecommendations';
import axios from 'axios';

const Dashboard = ({ user, token }) => {
  const [activeTab, setActiveTab] = useState('inventory');
  const inventoryListRef = useRef(null);

  const axiosConfig = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };

  const handleTabSelect = (eventKey) => {
    setActiveTab(eventKey);
    
    // Trigger refresh when inventory tab is selected
    if (eventKey === 'inventory' && inventoryListRef.current) {
      // Small delay to ensure the component is fully mounted
      setTimeout(() => {
        if (inventoryListRef.current) {
          inventoryListRef.current.refreshInventory();
        }
      }, 100);
    }
  };

  return (
    <div>

      {/* Main Content Tabs */}
      <Tabs 
        activeKey={activeTab} 
        onSelect={handleTabSelect} 
        id="dashboard-tabs" 
        className="mb-3"
      >
        <Tab eventKey="inventory" title="📦 Inventory">
          <InventoryList 
            ref={inventoryListRef}
            token={token} 
            axiosConfig={axiosConfig} 
          />
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
