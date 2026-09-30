import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Spinner, Badge, Button, Pagination, Form } from 'react-bootstrap';
import { adminService } from '../services/services';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [roleFilter, setRoleFilter] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminService.getStatistics().then(res => setStats(res.data.stats)).catch(console.error);
    fetchUsers();
  }, []);

  useEffect(() => { fetchUsers(); }, [page, roleFilter]);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10 };
      if (roleFilter) params.role = roleFilter;
      const { data } = await adminService.getUsers(params);
      setUsers(data.content);
      setTotalPages(data.totalPages);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleToggleActive = async (userId) => {
    try { await adminService.toggleUserActive(userId); fetchUsers(); }
    catch (err) { alert('Failed to update user'); }
  };

  const statCards = stats ? [
    { label: 'Total Users', value: stats.totalUsers, bg: 'primary' },
    { label: 'Candidates', value: stats.totalCandidates, bg: 'info' },
    { label: 'Recruiters', value: stats.totalRecruiters, bg: 'warning' },
    { label: 'Open Jobs', value: stats.openJobs, bg: 'success' },
    { label: 'Applications', value: stats.totalApplications, bg: 'secondary' },
  ] : [];

  return (
    <Container className="py-4">
      <h2 className="fw-bold mb-4">Admin Dashboard</h2>

      {stats && (
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
      )}

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-white d-flex justify-content-between align-items-center">
          <h5 className="fw-bold mb-0">User Management</h5>
          <Form.Select style={{ maxWidth: 200 }} value={roleFilter}
            onChange={(e) => { setRoleFilter(e.target.value); setPage(0); }}>
            <option value="">All Roles</option>
            <option value="CANDIDATE">Candidates</option>
            <option value="RECRUITER">Recruiters</option>
            <option value="ADMIN">Admins</option>
          </Form.Select>
        </Card.Header>
        <Card.Body>
          {loading ? <div className="text-center"><Spinner animation="border" /></div> : (
            <>
              <Table responsive hover>
                <thead className="table-light">
                  <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th>Action</th></tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td className="fw-semibold">{u.firstName} {u.lastName}</td>
                      <td>{u.email}</td>
                      <td><Badge bg="dark">{u.role}</Badge></td>
                      <td><Badge bg={u.active ? 'success' : 'danger'}>{u.active ? 'Active' : 'Inactive'}</Badge></td>
                      <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                      <td>
                        <Button variant={u.active ? 'outline-danger' : 'outline-success'} size="sm"
                          onClick={() => handleToggleActive(u.id)}>
                          {u.active ? 'Deactivate' : 'Activate'}
                        </Button>
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
        </Card.Body>
      </Card>
    </Container>
  );
}
