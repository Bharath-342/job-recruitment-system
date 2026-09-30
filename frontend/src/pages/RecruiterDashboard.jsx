import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Spinner, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { recruiterService } from '../services/services';
import { useAuth } from '../context/AuthContext';

export default function RecruiterDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    recruiterService.getDashboard().then(res => setStats(res.data.stats))
      .catch(console.error).finally(() => setLoading(false));
  }, []);

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  const statCards = [
    { label: 'Active Jobs', value: stats?.activeJobs || 0, bg: 'primary' },
    { label: 'Total Applications', value: stats?.totalApplications || 0, bg: 'info' },
    { label: 'Shortlisted', value: stats?.shortlisted || 0, bg: 'warning' },
    { label: 'Interviews', value: stats?.interviews || 0, bg: 'secondary' },
    { label: 'Selected', value: stats?.selected || 0, bg: 'success' },
  ];

  return (
    <Container className="py-4">
      <h2 className="fw-bold mb-4">Recruiter Dashboard</h2>
      <Row className="g-3 mb-4">
        {statCards.map((s, i) => (
          <Col sm={6} md key={i}>
            <Card className={`text-white bg-${s.bg} border-0 shadow-sm`}>
              <Card.Body className="text-center">
                <h3 className="fw-bold">{s.value}</h3>
                <small>{s.label}</small>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
      <Row className="g-3">
        <Col md={4}>
          <Card className="shadow-sm border-0 h-100">
            <Card.Body className="d-grid gap-2">
              <h5 className="fw-bold">Quick Actions</h5>
              <Button as={Link} to="/recruiter/jobs/create" variant="primary">Post New Job</Button>
              <Button as={Link} to="/recruiter/jobs" variant="outline-primary">Manage Jobs</Button>
              <Button as={Link} to="/recruiter/applications" variant="outline-secondary">View Applications</Button>
            </Card.Body>
          </Card>
        </Col>
        <Col md={8}>
          <Card className="shadow-sm border-0 h-100">
            <Card.Body>
              <h5 className="fw-bold mb-3">Overview</h5>
              <p>Welcome back, {user.firstName}! You have <strong>{stats?.activeJobs || 0}</strong> active job postings
                with <strong>{stats?.totalApplications || 0}</strong> total applications.</p>
              <p className="text-muted">
                {stats?.closedJobs > 0 && <>{stats.closedJobs} jobs closed. </>}
                {stats?.rejected > 0 && <>{stats.rejected} candidates rejected. </>}
              </p>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}
