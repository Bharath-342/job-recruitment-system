import { useState, useEffect } from 'react';
import { Container, Table, Badge, Pagination, Spinner, Alert, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { applicationService } from '../services/services';

export default function MyApplications() {
  const [applications, setApplications] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchApplications(); }, [page]);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const { data } = await applicationService.getMyApplications({ page, size: 10 });
      setApplications(data.content);
      setTotalPages(data.totalPages);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleWithdraw = async (id) => {
    if (!window.confirm('Are you sure you want to withdraw this application?')) return;
    try {
      await applicationService.withdrawApplication(id);
      fetchApplications();
    } catch (err) { alert(err.response?.data?.message || 'Failed to withdraw'); }
  };

  const statusBg = { APPLIED: 'secondary', UNDER_REVIEW: 'warning', SHORTLISTED: 'info',
                     INTERVIEW: 'primary', SELECTED: 'success', REJECTED: 'danger' };

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  return (
    <Container className="py-4">
      <h2 className="fw-bold mb-4">My Applications</h2>
      {applications.length === 0 ? (
        <Alert variant="info">
          You haven't applied to any jobs yet. <Link to="/jobs">Browse jobs</Link> to get started!
        </Alert>
      ) : (
        <>
          <Table responsive hover className="shadow-sm">
            <thead className="table-dark">
              <tr><th>Job</th><th>Company</th><th>Location</th><th>Status</th><th>Applied</th><th>Action</th></tr>
            </thead>
            <tbody>
              {applications.map((app) => (
                <tr key={app.id}>
                  <td><Link to={`/jobs/${app.jobId}`} className="fw-semibold">{app.jobTitle}</Link></td>
                  <td>{app.companyName}</td>
                  <td>{app.location}</td>
                  <td><Badge bg={statusBg[app.status]}>{app.status?.replace('_', ' ')}</Badge></td>
                  <td>{new Date(app.appliedAt).toLocaleDateString()}</td>
                  <td>
                    {(app.status === 'APPLIED' || app.status === 'UNDER_REVIEW') && (
                      <Button variant="outline-danger" size="sm" onClick={() => handleWithdraw(app.id)}>
                        Withdraw
                      </Button>
                    )}
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
    </Container>
  );
}
