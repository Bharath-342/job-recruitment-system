import { Link } from 'react-router-dom';
import { Container, Row, Col, Button, Card } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import { FaBriefcase, FaUserTie, FaSearch, FaRocket } from 'react-icons/fa';

export default function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <div>
      {/* Hero Section */}
      <div className="hero-section text-white text-center py-5" style={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        minHeight: '60vh',
        display: 'flex',
        alignItems: 'center'
      }}>
        <Container>
          <h1 className="display-3 fw-bold mb-3">Find Your Dream Job</h1>
          <p className="lead mb-4 opacity-75">
            Connect with top employers and discover opportunities that match your skills
          </p>
          <div className="d-flex gap-3 justify-content-center flex-wrap">
            <Button as={Link} to="/jobs" variant="light" size="lg" className="px-4 fw-semibold">
              <FaSearch className="me-2" />Browse Jobs
            </Button>
            {!isAuthenticated() && (
              <Button as={Link} to="/register/candidate" variant="outline-light" size="lg" className="px-4">
                Get Started
              </Button>
            )}
          </div>
        </Container>
      </div>

      {/* Features */}
      <Container className="py-5">
        <h2 className="text-center mb-5 fw-bold">Why Choose JobRecruit?</h2>
        <Row className="g-4">
          {[
            { icon: <FaBriefcase size={40} />, title: 'Thousands of Jobs', desc: 'Browse through curated job listings from top companies across industries.' },
            { icon: <FaUserTie size={40} />, title: 'Smart Matching', desc: 'Get matched with jobs that fit your skills, experience, and preferences.' },
            { icon: <FaSearch size={40} />, title: 'Advanced Search', desc: 'Filter jobs by location, salary, experience, and employment type.' },
            { icon: <FaRocket size={40} />, title: 'Track Applications', desc: 'Monitor your application status in real-time from applied to selected.' },
          ].map((f, i) => (
            <Col md={6} lg={3} key={i}>
              <Card className="h-100 border-0 shadow-sm text-center p-4 hover-card">
                <Card.Body>
                  <div className="text-primary mb-3">{f.icon}</div>
                  <Card.Title className="fw-bold">{f.title}</Card.Title>
                  <Card.Text className="text-muted">{f.desc}</Card.Text>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      </Container>

      {/* CTA */}
      <div className="bg-dark text-white text-center py-5">
        <Container>
          <h3 className="mb-3">Ready to start your journey?</h3>
          <div className="d-flex gap-3 justify-content-center flex-wrap">
            <Button as={Link} to="/register/candidate" variant="primary" size="lg">
              I'm a Candidate
            </Button>
            <Button as={Link} to="/register/recruiter" variant="outline-light" size="lg">
              I'm a Recruiter
            </Button>
          </div>
        </Container>
      </div>
    </div>
  );
}
