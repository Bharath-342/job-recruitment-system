import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert, Badge, Spinner } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { authService, fresherJobService } from '../services/services';
import { 
  FaUserGraduate, FaShieldAlt, FaBuilding, FaExternalLinkAlt, 
  FaFilter, FaMapMarkerAlt, FaCode, FaCheckCircle, FaSearch, FaArrowRight 
} from 'react-icons/fa';

export default function Home() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [stats, setStats] = useState({
    totalActiveFresherJobs: 0,
    uniqueCompaniesHiring: 0,
    locations: 0,
    remoteJobs: 0,
    lastVerifiedAt: null
  });
  const [statsLoading, setStatsLoading] = useState(true);

  useEffect(() => {
    fresherJobService.getStatistics()
      .then(res => setStats(res.data))
      .catch(err => console.error('Failed to load stats', err))
      .finally(() => setStatsLoading(false));
  }, []);

  const handleChange = (field) => (e) => {
    setForm({ ...form, [field]: e.target.value });
    if (error) setError('');
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { data } = await authService.registerCandidate({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      });

      // Log in and immediately redirect candidate to Fresher Job Dashboard
      login({
        userId: data.userId,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        role: data.role
      }, data.token);

      navigate('/fresher-jobs');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.details?.[0] || 'Registration failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="landing-experience">
      {/* Hero & Registration Section */}
      <section className="py-5" style={{
        background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f766e 100%)',
        color: '#ffffff',
        minHeight: '85vh',
        display: 'flex',
        alignItems: 'center'
      }}>
        <Container>
          <Row className="align-items-center g-5">
            {/* Left Column: Platform Identity & Headline */}
            <Col lg={7}>
              <div className="d-inline-flex align-items-center gap-2 px-3 py-1 rounded-pill mb-3" style={{ background: 'rgba(20, 184, 166, 0.2)', border: '1px solid #14b8a6' }}>
                <span className="badge bg-success rounded-pill px-2 py-1">STRICT 0-YEAR VERIFIED</span>
                <span className="small text-light">Official ATS Aggregation • No Exp Jobs Allowed</span>
              </div>

              <h1 className="display-4 fw-black mb-3 text-white" style={{ letterSpacing: '-0.5px' }}>
                Find Jobs Built for <span style={{ color: '#2dd4bf' }}>Freshers</span>
              </h1>

              <p className="lead text-light mb-4" style={{ fontSize: '1.25rem', lineHeight: 1.6, opacity: 0.9 }}>
                Search verified current job openings specifically designed for candidates with <strong>zero professional experience</strong>.
              </p>

              <div className="p-3 rounded-3 mb-4" style={{ background: 'rgba(255, 255, 255, 0.07)', borderLeft: '4px solid #2dd4bf' }}>
                <div className="fw-bold text-white mb-1 d-flex align-items-center gap-2">
                  <FaShieldAlt className="text-warning" /> Zero-Tolerance Experience Filter
                </div>
                <small className="text-light opacity-90">
                  Every job is checked against the original employer ATS requirements. Positions mandating 1+, 2+, or senior experience—or with missing/ambiguous experience—are strictly excluded.
                </small>
              </div>

              {/* Action Buttons */}
              <div className="d-flex flex-wrap gap-3 mb-4">
                <Button as={Link} to="/fresher-jobs" variant="warning" size="lg" className="fw-bold px-4 shadow text-dark d-flex align-items-center gap-2">
                  <FaSearch /> Browse Fresher Jobs
                </Button>
                <Button as={Link} to="/companies" variant="outline-light" size="lg" className="px-4">
                  <FaBuilding className="me-2" /> Verified Companies
                </Button>
              </div>

              {/* Quick DB Stats Bar */}
              <div className="pt-3 border-top border-secondary d-flex flex-wrap gap-4 text-light">
                <div>
                  <div className="fs-4 fw-bold text-warning">
                    {statsLoading ? <Spinner size="sm" animation="border" /> : stats.totalActiveFresherJobs}
                  </div>
                  <small className="text-muted text-uppercase" style={{ fontSize: '0.75rem' }}>Active 0-Yr Jobs</small>
                </div>
                <div>
                  <div className="fs-4 fw-bold text-info">
                    {statsLoading ? <Spinner size="sm" animation="border" /> : stats.uniqueCompaniesHiring}
                  </div>
                  <small className="text-muted text-uppercase" style={{ fontSize: '0.75rem' }}>Companies Hiring</small>
                </div>
                <div>
                  <div className="fs-4 fw-bold text-success">
                    {statsLoading ? <Spinner size="sm" animation="border" /> : stats.locations}
                  </div>
                  <small className="text-muted text-uppercase" style={{ fontSize: '0.75rem' }}>Cities / Remote</small>
                </div>
                <div>
                  <div className="fs-4 fw-bold text-light">
                    {statsLoading ? <Spinner size="sm" animation="border" /> : stats.remoteJobs}
                  </div>
                  <small className="text-muted text-uppercase" style={{ fontSize: '0.75rem' }}>Remote Roles</small>
                </div>
              </div>
            </Col>

            {/* Right Column: First-Experience Candidate Registration Card */}
            <Col lg={5}>
              <Card className="shadow-lg border-0 rounded-4" style={{ background: '#ffffff', color: '#1e293b' }}>
                <Card.Body className="p-4 p-md-5">
                  <div className="text-center mb-4">
                    <Badge bg="success" className="mb-2 px-3 py-1 text-uppercase">Candidate Sign Up</Badge>
                    <h3 className="fw-bold mb-1">Create Your Account</h3>
                    <p className="text-muted small">Start applying to verified 0-year software roles</p>
                  </div>

                  {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}

                  <Form onSubmit={handleRegister}>
                    <Row className="g-2 mb-3">
                      <Col sm={6}>
                        <Form.Label className="small fw-semibold mb-1">First Name</Form.Label>
                        <Form.Control
                          size="sm"
                          required
                          value={form.firstName}
                          onChange={handleChange('firstName')}
                          placeholder="e.g. Rahul"
                        />
                      </Col>
                      <Col sm={6}>
                        <Form.Label className="small fw-semibold mb-1">Last Name</Form.Label>
                        <Form.Control
                          size="sm"
                          required
                          value={form.lastName}
                          onChange={handleChange('lastName')}
                          placeholder="e.g. Sharma"
                        />
                      </Col>
                    </Row>

                    <Form.Group className="mb-3">
                      <Form.Label className="small fw-semibold mb-1">Email Address</Form.Label>
                      <Form.Control
                        size="sm"
                        type="email"
                        required
                        value={form.email}
                        onChange={handleChange('email')}
                        placeholder="you@domain.com"
                      />
                    </Form.Group>

                    <Row className="g-2 mb-3">
                      <Col sm={6}>
                        <Form.Label className="small fw-semibold mb-1">Password</Form.Label>
                        <Form.Control
                          size="sm"
                          type="password"
                          required
                          value={form.password}
                          onChange={handleChange('password')}
                          placeholder="Min 6 characters"
                        />
                      </Col>
                      <Col sm={6}>
                        <Form.Label className="small fw-semibold mb-1">Confirm Password</Form.Label>
                        <Form.Control
                          size="sm"
                          type="password"
                          required
                          value={form.confirmPassword}
                          onChange={handleChange('confirmPassword')}
                          placeholder="Re-enter password"
                        />
                      </Col>
                    </Row>

                    <div className="d-flex align-items-center justify-content-between mb-3 bg-light p-2 rounded">
                      <span className="small text-muted">Role:</span>
                      <Badge bg="primary">CANDIDATE (0-Year Job Seeker)</Badge>
                    </div>

                    <Button
                      variant="primary"
                      type="submit"
                      disabled={loading}
                      className="w-100 py-2 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-2"
                    >
                      {loading ? (
                        <>
                          <Spinner size="sm" animation="border" /> Registering...
                        </>
                      ) : (
                        <>
                          Register & Search Jobs <FaArrowRight />
                        </>
                      )}
                    </Button>
                  </Form>

                  <div className="text-center mt-3 pt-3 border-top">
                    <p className="small text-muted mb-2">
                      Already have an account?{' '}
                      <Link to="/login" className="fw-semibold text-primary text-decoration-none">
                        Login here
                      </Link>
                    </p>
                    <Link to="/fresher-jobs" className="small text-secondary text-decoration-none">
                      Skip for now & browse fresher jobs →
                    </Link>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Container>
      </section>

      {/* Key Benefits Grid (Section 3 Requirement) */}
      <section className="py-5 bg-white">
        <Container>
          <div className="text-center mb-5">
            <h2 className="fw-bold mb-2">Why FreshStart Jobs?</h2>
            <p className="text-muted">Built specifically for freshers and candidates with 0 prior experience.</p>
          </div>

          <Row className="g-4">
            {[
              {
                icon: <FaShieldAlt className="text-success" size={32} />,
                title: 'Strict 0-Year Experience Filter',
                desc: 'Only jobs whose original description explicitly verifies zero experience or fresh graduates are allowed.'
              },
              {
                icon: <FaBuilding className="text-primary" size={32} />,
                title: 'Multi-Company Aggregation',
                desc: 'No artificial 7-company caps. Continuous feeds from Greenhouse, Lever, and Ashby across dozens of tech leaders.'
              },
              {
                icon: <FaExternalLinkAlt className="text-warning" size={32} />,
                title: 'Direct Official Applications',
                desc: 'Apply directly on the employer\'s official career portal or ATS page. No fake or intercepted forms.'
              },
              {
                icon: <FaFilter className="text-info" size={32} />,
                title: 'Ambiguity & Conflict Elimination',
                desc: 'Missing experience or conflicting requirements are strictly excluded to avoid wasted time on experienced jobs.'
              },
              {
                icon: <FaMapMarkerAlt className="text-danger" size={32} />,
                title: 'Location & Remote Filters',
                desc: 'Easily filter by Indian tech hubs (Bengaluru, Hyderabad, Pune, Chennai, Noida) or 100% remote roles.'
              },
              {
                icon: <FaCode className="text-indigo" size={32} />,
                title: 'Java & Full Stack Priority',
                desc: 'Targeted focus on Java, Spring Boot, REST APIs, SQL, React, and Core Software Engineering openings.'
              },
              {
                icon: <FaCheckCircle className="text-success" size={32} />,
                title: 'Regular ATS Verification',
                desc: 'Continuous synchronization updates active status and flags closed roles with exact timestamps.'
              },
              {
                icon: <FaUserGraduate className="text-primary" size={32} />,
                title: 'Fresh Graduate Focus',
                desc: 'Trainees, campus graduates, and entry-level aspirants find real jobs without competing against seniors.'
              }
            ].map((benefit, idx) => (
              <Col md={6} lg={3} key={idx}>
                <Card className="h-100 border-0 shadow-sm p-3 rounded-4 hover-card" style={{ background: '#f8fafc' }}>
                  <Card.Body>
                    <div className="mb-3">{benefit.icon}</div>
                    <h6 className="fw-bold mb-2">{benefit.title}</h6>
                    <p className="text-muted small mb-0" style={{ lineHeight: 1.5 }}>
                      {benefit.desc}
                    </p>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* Popular Fresher Searches */}
      <section className="py-4 bg-light border-top border-bottom">
        <Container>
          <div className="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <span className="fw-bold text-secondary small text-uppercase">Popular Fresher Searches:</span>
            <div className="d-flex flex-wrap gap-2">
              {[
                'Java Fresher', 'Java Backend', 'Java Full Stack',
                'Software Engineer', 'Associate Software Engineer',
                'Graduate Software Engineer', 'Trainee Software Engineer'
              ].map((query, idx) => (
                <Button
                  key={idx}
                  as={Link}
                  to={`/fresher-jobs?keyword=${encodeURIComponent(query)}`}
                  variant="outline-secondary"
                  size="sm"
                  className="rounded-pill px-3 py-1 bg-white"
                >
                  {query}
                </Button>
              ))}
            </div>
          </div>
        </Container>
      </section>
    </div>
  );
}
