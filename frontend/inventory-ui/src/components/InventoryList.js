import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { Table, Card, Alert, Spinner, Form, Row, Col } from 'react-bootstrap';
import axios from 'axios';

const InventoryList = forwardRef(({ token, axiosConfig }, ref) => {
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedStore, setSelectedStore] = useState('1');

  useEffect(() => {
    fetchInventory();
  }, [selectedStore]);

  // Expose refreshInventory function to parent component
  useImperativeHandle(ref, () => ({
    refreshInventory: () => {
      fetchInventory();
    }
  }));

  const fetchInventory = async () => {
    setLoading(true);
    try {
      const response = await axios.get(
        `/api/inventory/${selectedStore}`, 
        axiosConfig
      );
      setInventory(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch inventory data');
      console.error('Error fetching inventory:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStockStatus = (currentStock) => {
    if (currentStock === 0) return { variant: 'danger', text: 'Out of Stock' };
    if (currentStock < 10) return { variant: 'warning', text: 'Low Stock' };
    if (currentStock < 50) return { variant: 'info', text: 'Normal' };
    return { variant: 'success', text: 'Well Stocked' };
  };

  return (
    <Card>
      <Card.Header>
        <Row>
          <Col>
            <h5>📦 Inventory Management</h5>
          </Col>
          <Col md="auto">
            <Form.Select 
              value={selectedStore} 
              onChange={(e) => setSelectedStore(e.target.value)}
            >
              <option value="1">Store 1 - Downtown</option>
              <option value="2">Store 2 - Mall</option>
              <option value="3">Store 3 - Airport</option>
            </Form.Select>
          </Col>
        </Row>
      </Card.Header>
      
      <Card.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        
        {loading ? (
          <div className="text-center p-4">
            <Spinner animation="border" />
            <p className="mt-2">Loading inventory...</p>
          </div>
        ) : (
          <>
            
            <Table striped bordered hover responsive>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Category</th>
                  <th>Current Stock</th>
                  <th>Status</th>
                  <th>Store</th>
                  <th>Last Updated</th>
                </tr>
              </thead>
              <tbody>
                {inventory.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center">
                      No inventory data available
                    </td>
                  </tr>
                ) : (
                  inventory.map((item, index) => {
                    const status = getStockStatus(item.currentStock);
                    return (
                      <tr key={index}>
                        <td><strong>{item.productName}</strong></td>
                        <td><code>{item.productSku}</code></td>
                        <td>{item.productCategory}</td>
                        <td>
                          <span className={`badge bg-${status.variant}`}>
                            {item.currentStock}
                          </span>
                        </td>
                        <td>
                          <span className={`badge bg-${status.variant}`}>
                            {status.text}
                          </span>
                        </td>
                        <td>{item.storeName}</td>
                        <td>{new Date(item.updatedAt).toLocaleDateString()}</td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </Table>
          </>
        )}
      </Card.Body>
    </Card>
  );
});

export default InventoryList;
