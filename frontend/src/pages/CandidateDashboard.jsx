import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Spinner, Table, Badge, Button, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { candidateService, applicationService } from '../services/services';
import { useAuth } from '../context/AuthContext';

export default function CandidateDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [dashRes, appRes] = await Promise.all([
          candidateService.getDashboard(),
          applicationService.getMyApplications({ page: 0, size: 5 })
        ]);
        setStats(dashRes.data.stats);
        setApplications(appRes.data.content);
      } catch (err) { console.error(err); }
      finally { setLoading(false); }
    };
    fetchData();
  }, []);

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  const statCards = [
    { label: 'Total Applications', value: stats?.totalApplications || 0, bg: 'primary' },
    { label: 'Under Review', value: stats?.underReview || 0, bg: 'warning' },
    { label: 'Interviews', value: stats?.interviews || 0, bg: 'info' },
    { label: 'Selected', value: stats?.selected || 0, bg: 'success' },
    { label: 'Rejected', value: stats?.rejected || 0, bg: 'danger' },
  ];

  const statusBg = { APPLIED: 'secondary', UNDER_REVIEW: 'warning', SHORTLISTED: 'info',
                     INTERVIEW: 'primary', SELECTED: 'success', REJECTED: 'danger' };

  return (
    <Container className="py-4">
      <h2 className="fw-bold mb-4">Welcome, {user.firstName}! 👋</h2>

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

      <Row className="g-4">
        <Col md={8}>
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-white fw-bold">Recent Applications</Card.Header>
            <Card.Body>
              {applications.length === 0 ? (
                <Alert variant="info">
                  No applications yet. <Link to="/jobs">Browse jobs</Link> to get started!
                </Alert>
              ) : (
                <Table responsive hover>
                  <thead>
                    <tr><th>Job</th><th>Company</th><th>Status</th><th>Date</th></tr>
                  </thead>
                  <tbody>
                    {applications.map((app) => (
                      <tr key={app.id}>
                        <td><Link to={`/jobs/${app.jobId}`}>{app.jobTitle}</Link></td>
                        <td>{app.companyName}</td>
                        <td><Badge bg={statusBg[app.status]}>{app.status?.replace('_', ' ')}</Badge></td>
                        <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
              <Link to="/candidate/applications">View All Applications →</Link>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-white fw-bold">Quick Actions</Card.Header>
            <Card.Body className="d-grid gap-2">
              <Button as={Link} to="/jobs" variant="primary">Browse Jobs</Button>
              <Button as={Link} to="/candidate/profile" variant="outline-primary">Update Profile</Button>
              <Button as={Link} to="/candidate/applications" variant="outline-secondary">My Applications</Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}
