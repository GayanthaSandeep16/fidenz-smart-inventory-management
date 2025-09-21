import React, { useState } from 'react';
import { Card, Button, Alert, Table, Spinner, Form, Row, Col, Badge } from 'react-bootstrap';
import axios from 'axios';

const ABCAnalysis = ({ token, axiosConfig }) => {
  const [analysis, setAnalysis] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [storeId, setStoreId] = useState('1');
  const [days, setDays] = useState('30');

  const runAnalysis = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.get(
        `/api/algorithms/abc-analysis/${storeId}?days=${days}`,
        axiosConfig
      );
      setAnalysis(response.data);
    } catch (err) {
      setError('Failed to run ABC analysis');
      console.error('Error running ABC analysis:', err);
    } finally {
      setLoading(false);
    }
  };

  const getCategoryBadge = (category) => {
    switch (category) {
      case 'A':
        return <Badge bg="success">A - High Value</Badge>;
      case 'B':
        return <Badge bg="warning">B - Medium Value</Badge>;
      case 'C':
        return <Badge bg="secondary">C - Low Value</Badge>;
      default:
        return <Badge bg="light">{category}</Badge>;
    }
  };

  const getCategoryStats = () => {
    const stats = { A: 0, B: 0, C: 0 };
    analysis.forEach(item => {
      stats[item.category] = (stats[item.category] || 0) + 1;
    });
    return stats;
  };

  const stats = getCategoryStats();

  return (
    <Card>
      <Card.Header>
        <h5>📊 ABC Analysis - Product Value Classification</h5>
        <p className="mb-0 text-muted">
          Identify your most valuable products based on revenue contribution
        </p>
      </Card.Header>
      
      <Card.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        
        <Row className="mb-3">
          <Col md={4}>
            <Form.Group>
              <Form.Label>Store</Form.Label>
              <Form.Select value={storeId} onChange={(e) => setStoreId(e.target.value)}>
                <option value="1">Store 1 - Downtown</option>
                <option value="2">Store 2 - Mall</option>
                <option value="3">Store 3 - Airport</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group>
              <Form.Label>Analysis Period (Days)</Form.Label>
              <Form.Select value={days} onChange={(e) => setDays(e.target.value)}>
                <option value="7">Last 7 days</option>
                <option value="30">Last 30 days</option>
                <option value="90">Last 90 days</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={4} className="d-flex align-items-end">
            <Button
              variant="primary"
              onClick={runAnalysis}
              disabled={loading}
              className="w-100"
            >
              {loading ? 'Analyzing...' : '📊 Run Analysis'}
            </Button>
          </Col>
        </Row>

        {analysis.length > 0 && (
          <>
            {/* Category Summary */}
            <Row className="mb-4">
              <Col md={4}>
                <Card className="text-center bg-success text-white">
                  <Card.Body>
                    <h3>{stats.A}</h3>
                    <p>Category A Products</p>
                    <small>High Value - Focus Here!</small>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-center bg-warning text-dark">
                  <Card.Body>
                    <h3>{stats.B}</h3>
                    <p>Category B Products</p>
                    <small>Medium Value - Monitor</small>
                  </Card.Body>
                </Card>
              </Col>
              <Col md={4}>
                <Card className="text-center bg-secondary text-white">
                  <Card.Body>
                    <h3>{stats.C}</h3>
                    <p>Category C Products</p>
                    <small>Low Value - Minimize</small>
                  </Card.Body>
                </Card>
              </Col>
            </Row>

            {/* Analysis Results Table */}
            <Table striped bordered hover responsive>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Category</th>
                  <th>SKU</th>
                  <th>Total Revenue</th>
                  <th>% of Total</th>
                  <th>Cumulative %</th>
                  <th>Recommendation</th>
                </tr>
              </thead>
              <tbody>
                {analysis.map((item, index) => (
                  <tr key={index}>
                    <td><strong>{item.product.name}</strong></td>
                    <td>{getCategoryBadge(item.category)}</td>
                    <td><code>{item.product.sku}</code></td>
                    <td><strong>${item.totalRevenue.toFixed(2)}</strong></td>
                    <td>{item.percentageOfTotal.toFixed(2)}%</td>
                    <td>{item.cumulativePercentage.toFixed(2)}%</td>
                    <td>
                      {item.category === 'A' && (
                        <span className="text-success">
                          🎯 High Priority - Never run out!
                        </span>
                      )}
                      {item.category === 'B' && (
                        <span className="text-warning">
                          ⚖️ Balanced approach
                        </span>
                      )}
                      {item.category === 'C' && (
                        <span className="text-muted">
                          💤 Minimize inventory
                        </span>
                      )}
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
            <p className="mt-2">Running ABC analysis...</p>
          </div>
        )}

        {analysis.length === 0 && !loading && (
          <Alert variant="info">
            <h6>📊 About ABC Analysis</h6>
            <ul className="mb-0">
              <li><strong>Category A:</strong> Top 20% products generating 80% revenue - Keep well stocked!</li>
              <li><strong>Category B:</strong> Next 30% products generating 15% revenue - Monitor regularly</li>
              <li><strong>Category C:</strong> Bottom 50% products generating 5% revenue - Minimize investment</li>
            </ul>
          </Alert>
        )}
      </Card.Body>
    </Card>
  );
};

export default ABCAnalysis;
