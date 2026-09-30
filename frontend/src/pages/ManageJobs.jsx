import { useState, useEffect } from 'react';
import { Container, Table, Badge, Button, Spinner, Pagination, Alert } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { recruiterService, jobService } from '../services/services';

export default function ManageJobs() {
  const [jobs, setJobs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchJobs(); }, [page]);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      const { data } = await recruiterService.getMyJobs({ page, size: 10 });
      setJobs(data.content);
      setTotalPages(data.totalPages);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleClose = async (id) => {
    if (!window.confirm('Close this job?')) return;
    try { await jobService.closeJob(id); fetchJobs(); }
    catch (err) { alert(err.response?.data?.message || 'Failed'); }
  };

  if (loading) return <Container className="py-5 text-center"><Spinner animation="border" /></Container>;

  return (
    <Container className="py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="fw-bold">My Jobs</h2>
        <Button as={Link} to="/recruiter/jobs/create" variant="primary">+ Post New Job</Button>
      </div>
      {jobs.length === 0 ? (
        <Alert variant="info">No jobs posted yet. <Link to="/recruiter/jobs/create">Create your first job</Link>!</Alert>
      ) : (
        <>
          <Table responsive hover className="shadow-sm">
            <thead className="table-dark">
              <tr><th>Title</th><th>Location</th><th>Type</th><th>Status</th><th>Applications</th><th>Posted</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {jobs.map((job) => (
                <tr key={job.id}>
                  <td className="fw-semibold">{job.title}</td>
                  <td>{job.location}</td>
                  <td>{job.employmentType?.replace('_', ' ')}</td>
                  <td><Badge bg={job.status === 'OPEN' ? 'success' : 'danger'}>{job.status}</Badge></td>
                  <td>
                    <Link to={`/recruiter/jobs/${job.id}/applications`}>{job.applicationCount || 0} applicants</Link>
                  </td>
                  <td>{new Date(job.createdAt).toLocaleDateString()}</td>
                  <td>
                    {job.status === 'OPEN' && (
                      <Button variant="outline-danger" size="sm" onClick={() => handleClose(job.id)}>Close</Button>
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
