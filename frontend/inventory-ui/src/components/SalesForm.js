import React, { useState, useEffect } from 'react';
import { Card, Form, Button, Alert, Row, Col } from 'react-bootstrap';
import axios from 'axios';

const SalesForm = ({ token, axiosConfig }) => {
  const [formData, setFormData] = useState({
    productId: '',
    storeId: '1',
    quantity: '',
    unitPrice: ''
  });
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    setLoadingProducts(true);
    try {
      const response = await axios.get('/api/products', axiosConfig);
      setProducts(response.data);
    } catch (err) {
      setError('Failed to load products');
      console.error('Error fetching products:', err);
    } finally {
      setLoadingProducts(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const saleData = {
        productId: parseInt(formData.productId),
        storeId: parseInt(formData.storeId),
        quantity: parseInt(formData.quantity),
        unitPrice: parseFloat(formData.unitPrice)
      };

      const response = await axios.post(
        '/api/sales/transaction',
        saleData,
        axiosConfig
      );

      setSuccess(`Sale recorded successfully! Transaction ID: ${response.data.id}`);
      setFormData({
        productId: '',
        storeId: '1',
        quantity: '',
        unitPrice: ''
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record sale');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <Card.Header>
        <h5>💰 Record New Sale</h5>
      </Card.Header>
      
      <Card.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        {success && <Alert variant="success">{success}</Alert>}
        {loadingProducts && <Alert variant="info">Loading products...</Alert>}
        
        <Form onSubmit={handleSubmit}>
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Product</Form.Label>
                <Form.Select
                  name="productId"
                  value={formData.productId}
                  onChange={handleChange}
                  disabled={loadingProducts}
                  required
                >
                  <option value="">Select a product...</option>
                  {products.map((product) => (
                    <option key={product.id} value={product.id}>
                      {product.name} - {product.category}
                    </option>
                  ))}
                </Form.Select>
                <Form.Text className="text-muted">
                  Choose a product from the dropdown
                </Form.Text>
              </Form.Group>
            </Col>
            
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Store</Form.Label>
                <Form.Select
                  name="storeId"
                  value={formData.storeId}
                  onChange={handleChange}
                  required
                >
                  <option value="1">Store 1 - Downtown</option>
                  <option value="2">Store 2 - Mall</option>
                  <option value="3">Store 3 - Airport</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>
          
          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Quantity</Form.Label>
                <Form.Control
                  type="number"
                  name="quantity"
                  value={formData.quantity}
                  onChange={handleChange}
                  placeholder="Enter quantity"
                  min="1"
                  required
                />
              </Form.Group>
            </Col>
            
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Unit Price ($)</Form.Label>
                <Form.Control
                  type="number"
                  step="0.01"
                  name="unitPrice"
                  value={formData.unitPrice}
                  onChange={handleChange}
                  placeholder="0.00"
                  min="0"
                  required
                />
              </Form.Group>
            </Col>
          </Row>
          
          {formData.quantity && formData.unitPrice && (
            <Alert variant="info">
              <strong>Total Amount: ${(formData.quantity * formData.unitPrice).toFixed(2)}</strong>
            </Alert>
          )}
          
          <Button
            variant="success"
            type="submit"
            disabled={loading}
            className="w-100"
          >
            {loading ? 'Recording Sale...' : '💰 Record Sale'}
          </Button>
        </Form>
      </Card.Body>
    </Card>
  );
};

export default SalesForm;
