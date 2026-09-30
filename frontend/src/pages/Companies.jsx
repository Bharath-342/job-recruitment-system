import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, InputGroup, Button, Badge, Spinner, Pagination } from 'react-bootstrap';
import { fresherJobService } from '../services/services';
import { FaBuilding, FaSearch, FaCheckCircle, FaClock, FaBriefcase } from 'react-icons/fa';

export default function Companies() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;

  useEffect(() => {
    fetchCompanies(page);
  }, [page]);

  const fetchCompanies = async (pageNumber) => {
    setLoading(true);
    try {
      const res = await fresherJobService.getCompaniesDirectory({ page: pageNumber, size: pageSize });
      setCompanies(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      console.error('Failed to fetch companies directory', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredCompanies = companies.filter(c => 
    c.companyName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatTime = (ts) => {
    if (!ts) return 'Recently';
    try {
      const dt = new Date(ts);
      return dt.toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return 'Recently';
    }
  };

  return (
    <Container className="py-5">
      {/* Header */}
      <div className="mb-4">
        <Badge bg="success" className="mb-2 px-3 py-1">VERIFIED EMPLOYERS</Badge>
        <h1 className="fw-bold mb-1">Companies Hiring Freshers</h1>
        <p className="text-muted">
          Showing companies with at least one active, verified 0-year eligible position. No artificial limits.
        </p>
      </div>

      {/* Search & Stats Bar */}
      <Card className="shadow-sm border-0 mb-4 p-3 rounded-3 bg-white">
        <Row className="align-items-center g-3">
          <Col md={8}>
            <InputGroup>
              <InputGroup.Text className="bg-light border-end-0">
                <FaSearch className="text-muted" />
              </InputGroup.Text>
              <Form.Control
                placeholder="Search company name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="border-start-0 bg-light"
              />
            </InputGroup>
          </Col>
          <Col md={4} className="text-md-end text-muted small">
            {!loading && (
              <span>
                Showing <strong>{filteredCompanies.length > 0 ? page * pageSize + 1 : 0} - {Math.min((page + 1) * pageSize, totalElements)}</strong> of <strong>{totalElements}</strong> companies
              </span>
            )}
          </Col>
        </Row>
      </Card>

      {/* Directory Content */}
      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-2 text-muted">Loading verified companies...</p>
        </div>
      ) : filteredCompanies.length === 0 ? (
        <Card className="border-0 shadow-sm p-5 text-center my-4">
          <div className="text-muted mb-3"><FaBuilding size={48} /></div>
          <h5>No verified companies found</h5>
          <p className="text-muted small">
            {searchTerm ? `No companies matched "${searchTerm}".` : 'No companies currently have active 0-year jobs in the database.'}
          </p>
          {searchTerm && (
            <div>
              <Button variant="outline-primary" size="sm" onClick={() => setSearchTerm('')}>
                Clear Search
              </Button>
            </div>
          )}
        </Card>
      ) : (
        <>
          <Row className="g-4">
            {filteredCompanies.map((c, idx) => (
              <Col md={6} lg={4} key={idx}>
                <Card className="h-100 border-0 shadow-sm rounded-4 p-3 hover-card" style={{ background: '#ffffff', transition: 'all 0.2s' }}>
                  <Card.Body className="d-flex flex-column justify-content-between">
                    <div>
                      <div className="d-flex justify-content-between align-items-start mb-3">
                        <div className="p-2 rounded-3 bg-light text-primary border">
                          <FaBuilding size={24} />
                        </div>
                        <Badge bg="success" pill className="d-flex align-items-center gap-1 px-2 py-1">
                          <FaBriefcase size={10} />
                          <span>{c.activeFresherJobsCount} {c.activeFresherJobsCount === 1 ? 'Job' : 'Jobs'}</span>
                        </Badge>
                      </div>

                      <h5 className="fw-bold mb-2 text-dark">{c.companyName}</h5>
                      <div className="text-muted small mb-3 d-flex align-items-center gap-1">
                        <FaClock size={12} className="text-secondary" />
                        <span>Verified: {formatTime(c.lastVerifiedAt)}</span>
                      </div>
                    </div>

                    <div className="pt-3 border-top d-flex justify-content-between align-items-center">
                      <span className="small text-success d-flex align-items-center gap-1">
                        <FaCheckCircle size={12} /> 0-Yr Eligible
                      </span>
                      <Button
                        as={Link}
                        to={`/fresher-jobs?company=${encodeURIComponent(c.companyName)}`}
                        variant="outline-primary"
                        size="sm"
                        className="fw-semibold px-3 rounded-pill"
                      >
                        View Jobs →
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-center mt-5">
              <Pagination>
                <Pagination.Prev
                  disabled={page === 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                />
                {[...Array(totalPages)].map((_, i) => (
                  <Pagination.Item
                    key={i}
                    active={i === page}
                    onClick={() => setPage(i)}
                  >
                    {i + 1}
                  </Pagination.Item>
                ))}
                <Pagination.Next
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                />
              </Pagination>
            </div>
          )}
        </>
      )}
    </Container>
  );
}
