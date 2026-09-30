import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Card, Badge, Button, Alert, Spinner, Row, Col, Form } from 'react-bootstrap';
import { jobService, applicationService } from '../services/services';
import { useAuth } from '../context/AuthContext';
import { FaMapMarkerAlt, FaBriefcase, FaMoneyBillWave, FaClock, FaBuilding } from 'react-icons/fa';

export default function JobDetails() {
  const { id } = useParams();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);
  const [applied, setApplied] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [coverLetter, setCoverLetter] = useState('');
  const [showApplyForm, setShowApplyForm] = useState(false);

  useEffect(() => { fetchJob(); }, [id]);

  const fetchJob = async () => {
    try {
      const { data } = await jobService.getJob(id);
      setJob(data);
    } catch (err) {
      setError('Job not found');
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async () => {
    if (!isAuthenticated()) { navigate('/login'); return; }
    setApplying(true);
    setError('');
    try {
      await applicationService.applyForJob(id, { coverLetter });
      setApplied(true);
      setSuccess('Application submitted successfully!');
      setShowApplyForm(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to apply');
    } finally {
      setApplying(false);
    }
  };

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;
  if (!job) return <Container className="py-5"><Alert variant="danger">{error || 'Job not found'}</Alert></Container>;

  const formatSalary = (min, max) => {
    if (!min && !max) return 'Not disclosed';
    const fmt = (n) => `₹${Number(n).toLocaleString()}`;
    if (min && max) return `${fmt(min)} - ${fmt(max)}`;
    return min ? `From ${fmt(min)}` : `Up to ${fmt(max)}`;
  };

  return (
    <Container className="py-4" style={{ maxWidth: 800 }}>
      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}>{success}</Alert>}

      <Card className="shadow-sm border-0">
        <Card.Body className="p-4">
          <div className="d-flex justify-content-between align-items-start flex-wrap mb-3">
            <div>
              <h2 className="fw-bold mb-1">{job.title}</h2>
              <h5 className="text-primary"><FaBuilding className="me-2" />{job.companyName}</h5>
            </div>
            <Badge bg={job.status === 'OPEN' ? 'success' : 'danger'} className="fs-6">
              {job.status}
            </Badge>
          </div>

          <Row className="g-3 mb-4">
            <Col sm={6} md={3}>
              <div className="text-muted"><FaMapMarkerAlt className="me-1" /> Location</div>
              <div className="fw-semibold">{job.location}</div>
            </Col>
            <Col sm={6} md={3}>
              <div className="text-muted"><FaBriefcase className="me-1" /> Type</div>
              <div className="fw-semibold">{job.employmentType?.replace('_', ' ')}</div>
            </Col>
            <Col sm={6} md={3}>
              <div className="text-muted"><FaClock className="me-1" /> Experience</div>
              <div className="fw-semibold">
                {job.experienceMin != null ? `${job.experienceMin}-${job.experienceMax || '+'} years` : 'Not specified'}
              </div>
            </Col>
            <Col sm={6} md={3}>
              <div className="text-muted"><FaMoneyBillWave className="me-1" /> Salary</div>
              <div className="fw-semibold">{formatSalary(job.salaryMin, job.salaryMax)}</div>
            </Col>
          </Row>

          <h5 className="fw-bold">Job Description</h5>
          <p className="mb-4" style={{ whiteSpace: 'pre-wrap' }}>{job.description}</p>

          {job.requiredSkills?.length > 0 && (
            <div className="mb-4">
              <h5 className="fw-bold">Required Skills</h5>
              {[...job.requiredSkills].map((s, i) => (
                <Badge key={i} bg="primary" className="me-2 mb-1 px-3 py-2">{s}</Badge>
              ))}
            </div>
          )}

          <div className="text-muted small mb-3">
            Posted by {job.recruiterName} • {new Date(job.createdAt).toLocaleDateString()}
            {job.deadline && <> • Deadline: {new Date(job.deadline).toLocaleDateString()}</>}
          </div>

          {user?.role === 'CANDIDATE' && job.status === 'OPEN' && !applied && (
            <div className="mt-3">
              {!showApplyForm ? (
                <Button variant="primary" size="lg" onClick={() => setShowApplyForm(true)}>
                  Apply Now
                </Button>
              ) : (
                <Card className="border">
                  <Card.Body>
                    <h5>Apply for this position</h5>
                    <Form.Group className="mb-3">
                      <Form.Label>Cover Letter (optional)</Form.Label>
                      <Form.Control as="textarea" rows={4} value={coverLetter}
                        onChange={(e) => setCoverLetter(e.target.value)}
                        placeholder="Tell the recruiter why you're a good fit..." />
                    </Form.Group>
                    <div className="d-flex gap-2">
                      <Button variant="primary" onClick={handleApply} disabled={applying}>
                        {applying ? 'Submitting...' : 'Submit Application'}
                      </Button>
                      <Button variant="outline-secondary" onClick={() => setShowApplyForm(false)}>Cancel</Button>
                    </div>
                  </Card.Body>
                </Card>
              )}
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
}
