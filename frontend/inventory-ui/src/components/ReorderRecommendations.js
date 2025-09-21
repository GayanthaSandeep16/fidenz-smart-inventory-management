import React, { useState } from 'react';
import { Card, Button, Alert, Table, Spinner, Form, Row, Col, Badge } from 'react-bootstrap';
import axios from 'axios';

const ReorderRecommendations = ({ token, axiosConfig }) => {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [storeId, setStoreId] = useState('1');

  const fetchRecommendations = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.get(
        `/api/algorithms/reorder-recommendations/${storeId}`,
        axiosConfig
      );
      setRecommendations(response.data);
    } catch (err) {
      setError('Failed to fetch reorder recommendations');
      console.error('Error fetching recommendations:', err);
    } finally {
      setLoading(false);
    }
  };

  const getPriorityBadge = (recommendedQuantity, currentStock) => {
    const urgency = recommendedQuantity / Math.max(currentStock, 1);
    
    if (urgency >= 3) {
      return <Badge bg="danger">🚨 URGENT</Badge>;
    } else if (urgency >= 2) {
      return <Badge bg="warning">⚠️ HIGH</Badge>;
    } else if (urgency >= 1.5) {
      return <Badge bg="info">📋 MEDIUM</Badge>;
    } else {
      return <Badge bg="success">✅ LOW</Badge>;
    }
  };

  const getTotalReorderValue = () => {
    return recommendations.reduce((total, item) => {
      // Estimate unit price based on category (this would come from your product data)
      const estimatedPrice = getEstimatedPrice(item.productCategory);
      return total + (item.recommendedQuantity * estimatedPrice);
    }, 0);
  };

  const getEstimatedPrice = (category) => {
    // Mock prices - in real app, this would come from product data
    const prices = {
      'Electronics': 299.99,
      'Accessories': 49.99,
      'Clothing': 79.99,
      'Books': 19.99,
      'Default': 99.99
    };
    return prices[category] || prices['Default'];
  };

  return (
    <Card>
      <Card.Header>
        <h5>🔄 Smart Reorder Recommendations</h5>
        <p className="mb-0 text-muted">
          AI-powered recommendations based on sales patterns and lead times
        </p>
      </Card.Header>
      
      <Card.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        
        <Row className="mb-3">
          <Col md={6}>
            <Form.Group>
              <Form.Label>Store</Form.Label>
              <Form.Select value={storeId} onChange={(e) => setStoreId(e.target.value)}>
                <option value="1">Store 1 - Downtown</option>
                <option value="2">Store 2 - Mall</option>
                <option value="3">Store 3 - Airport</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={6} className="d-flex align-items-end">
            <Button
              variant="primary"
              onClick={fetchRecommendations}
              disabled={loading}
              className="w-100"
            >
              {loading ? 'Analyzing...' : '🔄 Get Recommendations'}
            </Button>
          </Col>
        </Row>

        {recommendations.length > 0 && (
          <>
            {/* Summary Stats */}
            <Row className="mb-4">
              <Col md={4}>
                <Card className="text-center bg-info text-white">
                  <Card.Body>
                    <h3>{recommendations.length}</h3>
                    <p>Items Need Reorder</p>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-center bg-warning text-dark">
                  <Card.Body>
                    <h3>{recommendations.filter(r => r.recommendedQuantity >= r.currentStock * 2).length}</h3>
                    <p>Urgent Items</p>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-center bg-success text-white">
                  <Card.Body>
                    <h3>${getTotalReorderValue().toFixed(0)}</h3>
                    <p>Estimated Order Value</p>
                  </Card.Body>
                </Card>
              </Col>
            </Row>

            {/* Recommendations Table */}
            <Table striped bordered hover responsive>
              <thead>
                <tr>
                  <th>Priority</th>
                  <th>Product</th>
                  <th>Current Stock</th>
                  <th>Reorder Point</th>
                  <th>Recommended Qty</th>
                  <th>Avg Daily Sales</th>
                  <th>Lead Time</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {recommendations.map((item, index) => (
                  <tr key={index}>
                    <td>{getPriorityBadge(item.recommendedQuantity, item.currentStock)}</td>
                    <td>
                      <strong>{item.productName}</strong>
                      <br />
                      <small className="text-muted">{item.productCategory}</small>
                    </td>
                    <td>
                      <span className={`badge ${item.currentStock < 10 ? 'bg-danger' : 'bg-primary'}`}>
                        {item.currentStock}
                      </span>
                    </td>
                    <td>{item.reorderPoint}</td>
                    <td>
                      <strong className="text-success">{item.recommendedQuantity}</strong>
                    </td>
                    <td>{item.averageDailySales.toFixed(1)}</td>
                    <td>{item.leadTime} days</td>
                    <td>
                      <Button 
                        size="sm" 
                        variant="success"
                        onClick={() => alert(`Order ${item.recommendedQuantity} units of ${item.productName}`)}
                      >
                        📦 Order Now
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </>
        )}

        {loading && (
          <div className="text-center p-4">
            <Spinner animation="border" />
            <p className="mt-2">Analyzing sales patterns and generating recommendations...</p>
          </div>
        )}

        {recommendations.length === 0 && !loading && (
          <Alert variant="info">
            <h6>🤖 About Smart Reordering</h6>
            <ul className="mb-0">
              <li><strong>AI Analysis:</strong> Analyzes 30 days of sales data</li>
              <li><strong>Seasonality:</strong> Accounts for weekday vs weekend patterns</li>
              <li><strong>Lead Times:</strong> Considers supplier delivery times</li>
              <li><strong>Safety Stock:</strong> Includes buffer for unexpected demand</li>
              <li><strong>Smart Alerts:</strong> Prioritizes based on urgency</li>
            </ul>
          </Alert>
        )}
      </Card.Body>
    </Card>
  );
};

export default ReorderRecommendations;
