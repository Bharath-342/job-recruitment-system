import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Table, Badge, Button, Spinner, Form, Alert, Pagination, Modal } from 'react-bootstrap';
import { recruiterService } from '../services/services';

export default function RecruiterApplications() {
  const { jobId } = useParams();
  const [applications, setApplications] = useState([]);
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => { fetchApplications(); }, [page, jobId]);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10 };
      const { data } = jobId
        ? await recruiterService.getJobApplications(jobId, params)
        : await recruiterService.getApplications(params);
      setApplications(data.content);
      setTotalPages(data.totalPages);
    } catch (err) { setError('Failed to load applications'); }
    finally { setLoading(false); }
  };

  const handleStatusChange = async (appId, newStatus) => {
    try {
      await recruiterService.updateApplicationStatus(appId, { status: newStatus });
      fetchApplications();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  const statusBg = { APPLIED: 'secondary', UNDER_REVIEW: 'warning', SHORTLISTED: 'info',
                     INTERVIEW: 'primary', SELECTED: 'success', REJECTED: 'danger' };

  const nextStatuses = {
    APPLIED: ['UNDER_REVIEW', 'REJECTED'],
    UNDER_REVIEW: ['SHORTLISTED', 'REJECTED'],
    SHORTLISTED: ['INTERVIEW', 'REJECTED'],
    INTERVIEW: ['SELECTED', 'REJECTED'],
  };

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  return (
    <Container className="py-4">
      <h2 className="fw-bold mb-4">{jobId ? 'Job Applications' : 'All Applications'}</h2>
      {error && <Alert variant="danger">{error}</Alert>}
      {applications.length === 0 ? (
        <Alert variant="info">No applications found.</Alert>
      ) : (
        <>
          <Table responsive hover className="shadow-sm">
            <thead className="table-dark">
              <tr><th>Candidate</th><th>Email</th><th>Job</th><th>Status</th><th>Applied</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {applications.map((app) => (
                <tr key={app.id}>
                  <td className="fw-semibold">{app.candidateName}</td>
                  <td>{app.candidateEmail}</td>
                  <td>{app.jobTitle}</td>
                  <td><Badge bg={statusBg[app.status]}>{app.status?.replace('_', ' ')}</Badge></td>
                  <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
                    <td>
                      <div className="d-flex gap-1 flex-wrap align-items-center">
                        <Button size="sm" variant="outline-info" onClick={async () => {
                          try {
                            const res = await recruiterService.getCandidateProfile(app.candidateId);
                            setSelectedCandidate({ ...res.data, coverLetter: app.coverLetter, appId: app.id });
                          } catch (e) {
                            setSelectedCandidate({ firstName: app.candidateName, email: app.candidateEmail, coverLetter: app.coverLetter, userId: app.candidateId });
                          }
                        }}>
                          View Profile
                        </Button>
                        <Button size="sm" variant="outline-secondary" onClick={async () => {
                          try {
                            const res = await recruiterService.downloadResume(app.candidateId);
                            const url = window.URL.createObjectURL(new Blob([res.data]));
                            const link = document.createElement('a');
                            link.href = url;
                            link.setAttribute('download', `${app.candidateName.replace(/\s+/g, '_')}_Resume.pdf`);
                            document.body.appendChild(link);
                            link.click();
                            link.remove();
                          } catch (err) {
                            alert('Resume not available or could not be downloaded.');
                          }
                        }}>
                          Resume 📄
                        </Button>
                        {nextStatuses[app.status] && nextStatuses[app.status].map((status) => (
                          <Button key={status} size="sm"
                            variant={status === 'REJECTED' ? 'outline-danger' : 'outline-success'}
                            onClick={() => handleStatusChange(app.id, status)}>
                            {status.replace('_', ' ')}
                          </Button>
                        ))}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          {totalPages > 1 && (
            <div className="d-flex justify-content-center">
              <Pagination>
                <Pagination.Prev disabled={page === 0} onClick={() => setPage(page - 1)} />
                {[...Array(totalPages)].map((_, i) => (
                  <Pagination.Item key={i} active={i === page} onClick={() => setPage(i)}>{i + 1}</Pagination.Item>
                ))}
                <Pagination.Next disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} />
              </Pagination>
            </div>
          )}
        </>
      )}

      {/* Candidate Profile Modal */}
      <Modal show={!!selectedCandidate} onHide={() => setSelectedCandidate(null)} centered size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Candidate Details</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedCandidate && (
            <div>
              <h4 className="fw-bold">{selectedCandidate.firstName} {selectedCandidate.lastName}</h4>
              <p className="text-muted mb-2">✉ {selectedCandidate.email} {selectedCandidate.phone && `| 📞 ${selectedCandidate.phone}`}</p>
              {selectedCandidate.headline && <p className="fw-semibold text-primary">{selectedCandidate.headline}</p>}
              {selectedCandidate.summary && (
                <div className="mb-3">
                  <h6>Summary</h6>
                  <p className="bg-light p-3 rounded">{selectedCandidate.summary}</p>
                </div>
              )}
              {selectedCandidate.coverLetter && (
                <div className="mb-3">
                  <h6>Cover Letter / Application Note</h6>
                  <p className="bg-light p-3 rounded">{selectedCandidate.coverLetter}</p>
                </div>
              )}
              {selectedCandidate.skills && selectedCandidate.skills.length > 0 && (
                <div className="mb-3">
                  <h6>Skills</h6>
                  <div>
                    {selectedCandidate.skills.map((s, i) => (
                      <Badge key={i} bg="primary" className="me-2 mb-2 p-2">{s}</Badge>
                    ))}
                  </div>
                </div>
              )}
              {selectedCandidate.experienceYears !== undefined && (
                <p><strong>Experience:</strong> {selectedCandidate.experienceYears} years</p>
              )}
              {selectedCandidate.location && (
                <p><strong>Location:</strong> {selectedCandidate.location}</p>
              )}
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setSelectedCandidate(null)}>Close</Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}
