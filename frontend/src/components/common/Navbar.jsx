import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Navbar as BSNavbar, Nav, Container, Button, Badge } from 'react-bootstrap';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardLink = () => {
    if (!user) return '/';
    switch (user.role) {
      case 'CANDIDATE': return '/candidate/dashboard';
      case 'RECRUITER': return '/recruiter/dashboard';
      case 'ADMIN': return '/admin/dashboard';
      default: return '/';
    }
  };

  return (
    <BSNavbar bg="dark" variant="dark" expand="lg" sticky="top" className="shadow-sm">
      <Container>
        <BSNavbar.Brand as={Link} to="/" className="fw-bold">
          <span style={{ color: '#6c5ce7' }}>Job</span>Recruit
        </BSNavbar.Brand>
        <BSNavbar.Toggle />
        <BSNavbar.Collapse>
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/jobs">Browse Jobs</Nav.Link>
            {isAuthenticated() && (
              <Nav.Link as={Link} to={getDashboardLink()}>Dashboard</Nav.Link>
            )}
            {user?.role === 'CANDIDATE' && (
              <>
                <Nav.Link as={Link} to="/candidate/applications">My Applications</Nav.Link>
                <Nav.Link as={Link} to="/candidate/profile">Profile</Nav.Link>
              </>
            )}
            {user?.role === 'RECRUITER' && (
              <>
                <Nav.Link as={Link} to="/recruiter/jobs">My Jobs</Nav.Link>
                <Nav.Link as={Link} to="/recruiter/applications">Applications</Nav.Link>
                <Nav.Link as={Link} to="/recruiter/jobs/create">Post Job</Nav.Link>
              </>
            )}
            {user?.role === 'ADMIN' && (
              <>
                <Nav.Link as={Link} to="/admin/users">Users</Nav.Link>
                <Nav.Link as={Link} to="/admin/jobs">Jobs</Nav.Link>
              </>
            )}
          </Nav>
          <Nav>
            {isAuthenticated() ? (
              <>
                <Nav.Item className="d-flex align-items-center me-3">
                  <span className="text-light me-2">{user.firstName}</span>
                  <Badge bg="info">{user.role}</Badge>
                </Nav.Item>
                <Button variant="outline-light" size="sm" onClick={handleLogout}>Logout</Button>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/login">Login</Nav.Link>
                <Nav.Link as={Link} to="/register/candidate">Register</Nav.Link>
              </>
            )}
          </Nav>
        </BSNavbar.Collapse>
      </Container>
    </BSNavbar>
  );
}
