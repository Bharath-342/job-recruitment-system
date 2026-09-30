import { useState, useEffect } from 'react';
import { Container, Card, Form, Button, Badge, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { candidateService } from '../services/services';

export default function CandidateProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [newSkill, setNewSkill] = useState('');
  const [form, setForm] = useState({});

  useEffect(() => { fetchProfile(); }, []);

  const fetchProfile = async () => {
    try {
      const { data } = await candidateService.getProfile();
      setProfile(data);
      setForm({
        headline: data.headline || '', summary: data.summary || '', phone: data.phone || '',
        location: data.location || '', experienceYears: data.experienceYears || '',
        linkedinUrl: data.linkedinUrl || '', githubUrl: data.githubUrl || ''
      });
    } catch (err) { setError('Failed to load profile'); }
    finally { setLoading(false); }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError(''); setSuccess('');
    try {
      const { data } = await candidateService.updateProfile(form);
      setProfile(data);
      setSuccess('Profile updated successfully!');
    } catch (err) { setError(err.response?.data?.message || 'Failed to update'); }
    finally { setSaving(false); }
  };

  const handleAddSkill = async () => {
    if (!newSkill.trim()) return;
    try {
      const { data } = await candidateService.addSkills([newSkill.trim()]);
      setProfile(data);
      setNewSkill('');
    } catch (err) { setError(err.response?.data?.message || 'Failed to add skill'); }
  };

  const handleRemoveSkill = async (name) => {
    try {
      const { data } = await candidateService.removeSkill(name);
      setProfile(data);
    } catch (err) { setError('Failed to remove skill'); }
  };

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <Container className="py-4" style={{ maxWidth: 800 }}>
      <h2 className="fw-bold mb-4">My Profile</h2>
      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}>{success}</Alert>}

      <Card className="shadow-sm border-0 mb-4">
        <Card.Header className="bg-white fw-bold">Profile Information</Card.Header>
        <Card.Body>
          <Form onSubmit={handleSave}>
            <Form.Group className="mb-3">
              <Form.Label>Headline</Form.Label>
              <Form.Control value={form.headline} onChange={update('headline')}
                placeholder="e.g. Full Stack Java Developer" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Summary</Form.Label>
              <Form.Control as="textarea" rows={3} value={form.summary} onChange={update('summary')}
                placeholder="Brief professional summary..." />
            </Form.Group>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Phone</Form.Label>
                  <Form.Control value={form.phone} onChange={update('phone')} placeholder="Phone number" />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Location</Form.Label>
                  <Form.Control value={form.location} onChange={update('location')} placeholder="City, State" />
                </Form.Group>
              </Col>
            </Row>
            <Form.Group className="mb-3">
              <Form.Label>Years of Experience</Form.Label>
              <Form.Control type="number" value={form.experienceYears} onChange={update('experienceYears')} min={0} />
            </Form.Group>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>LinkedIn URL</Form.Label>
                  <Form.Control value={form.linkedinUrl} onChange={update('linkedinUrl')} placeholder="https://linkedin.com/in/..." />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>GitHub URL</Form.Label>
                  <Form.Control value={form.githubUrl} onChange={update('githubUrl')} placeholder="https://github.com/..." />
                </Form.Group>
              </Col>
            </Row>
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Profile'}
            </Button>
          </Form>
        </Card.Body>
      </Card>

      {/* Skills Section */}
      <Card className="shadow-sm border-0">
        <Card.Header className="bg-white fw-bold">Skills</Card.Header>
        <Card.Body>
          <div className="mb-3">
            {profile?.skills && [...profile.skills].map((skill, i) => (
              <Badge key={i} bg="primary" className="me-2 mb-2 px-3 py-2" style={{ cursor: 'pointer' }}
                onClick={() => handleRemoveSkill(skill)}>
                {skill} ✕
              </Badge>
            ))}
            {(!profile?.skills || profile.skills.length === 0) && (
              <p className="text-muted">No skills added yet.</p>
            )}
          </div>
          <div className="d-flex gap-2">
            <Form.Control value={newSkill} onChange={(e) => setNewSkill(e.target.value)}
              placeholder="Add a skill (e.g. Java, React, SQL)" style={{ maxWidth: 300 }}
              onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddSkill())} />
            <Button variant="outline-primary" onClick={handleAddSkill}>Add</Button>
          </div>
        </Card.Body>
      </Card>

      {/* Resume Section */}
      <Card className="shadow-sm border-0 mt-4">
        <Card.Header className="bg-white fw-bold">Resume</Card.Header>
        <Card.Body>
          {profile?.resumeFileName ? (
            <div className="d-flex align-items-center justify-content-between p-3 bg-light rounded mb-3">
              <div>
                <strong>📄 Current Resume:</strong> {profile.resumeFileName}
              </div>
              <Button variant="outline-secondary" size="sm" onClick={async () => {
                try {
                  const res = await candidateService.downloadResume();
                  const url = window.URL.createObjectURL(new Blob([res.data]));
                  const link = document.createElement('a');
                  link.href = url;
                  link.setAttribute('download', profile.resumeFileName || 'resume.pdf');
                  document.body.appendChild(link);
                  link.click();
                  link.remove();
                } catch (err) { setError('Failed to download resume'); }
              }}>
                Download
              </Button>
            </div>
          ) : (
            <p className="text-muted mb-3">No resume uploaded yet.</p>
          )}

          <Form.Group>
            <Form.Label>Upload / Replace Resume (PDF, DOCX, up to 5MB)</Form.Label>
            <Form.Control type="file" accept=".pdf,.doc,.docx" onChange={async (e) => {
              const file = e.target.files?.[0];
              if (!file) return;
              const fd = new FormData();
              fd.append('file', file);
              setSaving(true);
              setError('');
              try {
                const { data } = await candidateService.uploadResume(fd);
                setProfile(data);
                setSuccess('Resume uploaded successfully!');
              } catch (err) {
                setError(err.response?.data?.message || 'Failed to upload resume');
              } finally {
                setSaving(false);
              }
            }} />
          </Form.Group>
        </Card.Body>
      </Card>
    </Container>
  );
}
