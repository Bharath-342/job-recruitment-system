import { useState } from 'react';
import { useNavigate, Link, useParams } from 'react-router-dom';
import { Container, Form, Button, Card, Alert } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/services';

export default function Register() {
  const { role } = useParams(); // 'candidate' or 'recruiter'
  const isRecruiter = role === 'recruiter';
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', companyName: ''
  });
  const [error, setError] = useState('');
  const [errors, setErrors] = useState([]);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setErrors([]);
    setLoading(true);
    try {
      const service = isRecruiter ? authService.registerRecruiter : authService.registerCandidate;
      const { data } = await service(form);
      login({ userId: data.userId, email: data.email, firstName: data.firstName,
              lastName: data.lastName, role: data.role }, data.token);
      navigate(isRecruiter ? '/recruiter/dashboard' : '/candidate/dashboard');
    } catch (err) {
      const resp = err.response?.data;
      if (resp?.details) setErrors(resp.details);
      else setError(resp?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <Container className="py-5" style={{ maxWidth: 520 }}>
      <Card className="shadow border-0">
        <Card.Body className="p-4">
          <h2 className="text-center mb-4 fw-bold">
            Register as {isRecruiter ? 'Recruiter' : 'Candidate'}
          </h2>
          {error && <Alert variant="danger">{error}</Alert>}
          {errors.length > 0 && (
            <Alert variant="danger">
              <ul className="mb-0">{errors.map((e, i) => <li key={i}>{e}</li>)}</ul>
            </Alert>
          )}
          <Form onSubmit={handleSubmit}>
            <div className="d-flex gap-3">
              <Form.Group className="mb-3 flex-fill">
                <Form.Label>First Name</Form.Label>
                <Form.Control required value={form.firstName} onChange={update('firstName')}
                  placeholder="First name" minLength={2} />
              </Form.Group>
              <Form.Group className="mb-3 flex-fill">
                <Form.Label>Last Name</Form.Label>
                <Form.Control required value={form.lastName} onChange={update('lastName')}
                  placeholder="Last name" minLength={2} />
              </Form.Group>
            </div>
            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control type="email" required value={form.email} onChange={update('email')}
                placeholder="Enter your email" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Password</Form.Label>
              <Form.Control type="password" required value={form.password} onChange={update('password')}
                placeholder="Min 8 chars, uppercase, lowercase, digit" minLength={8} />
              <Form.Text className="text-muted">
                Must contain uppercase, lowercase, and a digit
              </Form.Text>
            </Form.Group>
            {isRecruiter && (
              <Form.Group className="mb-3">
                <Form.Label>Company Name</Form.Label>
                <Form.Control required value={form.companyName} onChange={update('companyName')}
                  placeholder="Enter company name" />
              </Form.Group>
            )}
            <Button type="submit" variant="primary" className="w-100 mb-3" disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </Button>
          </Form>
          <div className="text-center text-muted">
            Already have an account? <Link to="/login">Sign In</Link>
            {isRecruiter
              ? <> | <Link to="/register/candidate">Register as Candidate</Link></>
              : <> | <Link to="/register/recruiter">Register as Recruiter</Link></>
            }
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
}
