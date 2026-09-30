import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Container, Card, Form, Button, Row, Col, Alert } from 'react-bootstrap';
import { jobService } from '../services/services';

export default function CreateJob() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '', description: '', location: '', employmentType: 'FULL_TIME',
    experienceMin: '', experienceMax: '', salaryMin: '', salaryMax: '',
    requiredSkills: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const payload = {
        ...form,
        experienceMin: form.experienceMin ? parseInt(form.experienceMin) : null,
        experienceMax: form.experienceMax ? parseInt(form.experienceMax) : null,
        salaryMin: form.salaryMin ? parseFloat(form.salaryMin) : null,
        salaryMax: form.salaryMax ? parseFloat(form.salaryMax) : null,
        requiredSkills: form.requiredSkills ? form.requiredSkills.split(',').map(s => s.trim()).filter(Boolean) : []
      };
      await jobService.createJob(payload);
      navigate('/recruiter/jobs');
    } catch (err) {
      const resp = err.response?.data;
      setError(resp?.details?.join(', ') || resp?.message || 'Failed to create job');
    } finally { setLoading(false); }
  };

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <Container className="py-4" style={{ maxWidth: 800 }}>
      <h2 className="fw-bold mb-4">Post a New Job</h2>
      {error && <Alert variant="danger">{error}</Alert>}
      <Card className="shadow-sm border-0">
        <Card.Body className="p-4">
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Job Title *</Form.Label>
              <Form.Control required value={form.title} onChange={update('title')}
                placeholder="e.g. Senior Java Developer" minLength={3} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Description *</Form.Label>
              <Form.Control as="textarea" rows={5} required value={form.description}
                onChange={update('description')} placeholder="Job description, responsibilities, requirements..."
                minLength={20} />
            </Form.Group>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Location *</Form.Label>
                  <Form.Control required value={form.location} onChange={update('location')}
                    placeholder="e.g. Hyderabad, India" />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Employment Type *</Form.Label>
                  <Form.Select value={form.employmentType} onChange={update('employmentType')}>
                    <option value="FULL_TIME">Full Time</option>
                    <option value="PART_TIME">Part Time</option>
                    <option value="CONTRACT">Contract</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="REMOTE">Remote</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Min Experience (years)</Form.Label>
                  <Form.Control type="number" value={form.experienceMin}
                    onChange={update('experienceMin')} min={0} />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Max Experience (years)</Form.Label>
                  <Form.Control type="number" value={form.experienceMax}
                    onChange={update('experienceMax')} min={0} />
                </Form.Group>
              </Col>
            </Row>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Min Salary (₹)</Form.Label>
                  <Form.Control type="number" value={form.salaryMin}
                    onChange={update('salaryMin')} min={0} />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Max Salary (₹)</Form.Label>
                  <Form.Control type="number" value={form.salaryMax}
                    onChange={update('salaryMax')} min={0} />
                </Form.Group>
              </Col>
            </Row>
            <Form.Group className="mb-3">
              <Form.Label>Required Skills (comma separated)</Form.Label>
              <Form.Control value={form.requiredSkills} onChange={update('requiredSkills')}
                placeholder="e.g. Java, Spring Boot, React, SQL" />
            </Form.Group>
            <div className="d-flex gap-2">
              <Button type="submit" variant="primary" disabled={loading}>
                {loading ? 'Posting...' : 'Post Job'}
              </Button>
              <Button variant="outline-secondary" onClick={() => navigate(-1)}>Cancel</Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
}
