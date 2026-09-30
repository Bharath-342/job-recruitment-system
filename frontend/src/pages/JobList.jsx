import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Badge, Pagination, Spinner, Alert } from 'react-bootstrap';
import { jobService } from '../services/services';
import { FaMapMarkerAlt, FaBriefcase, FaClock } from 'react-icons/fa';

export default function JobList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [jobs, setJobs] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    keyword: searchParams.get('keyword') || '',
    location: searchParams.get('location') || '',
    employmentType: searchParams.get('employmentType') || '',
    experience: searchParams.get('experience') || '',
  });
  const page = parseInt(searchParams.get('page') || '0');

  useEffect(() => {
    fetchJobs();
  }, [searchParams]);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      const params = { page, size: 9 };
      if (filters.keyword) params.keyword = filters.keyword;
      if (filters.location) params.location = filters.location;
      if (filters.employmentType) params.employmentType = filters.employmentType;
      if (filters.experience) params.experience = filters.experience;

      const { data } = await jobService.searchJobs(params);
      setJobs(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      console.error('Failed to fetch jobs', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => { if (v) params.set(k, v); });
    params.set('page', '0');
    setSearchParams(params);
  };

  const goToPage = (p) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', p.toString());
    setSearchParams(params);
  };

  const formatSalary = (min, max) => {
    if (!min && !max) return null;
    const fmt = (n) => n >= 100000 ? `₹${(n/100000).toFixed(1)}L` : `₹${n.toLocaleString()}`;
    if (min && max) return `${fmt(min)} - ${fmt(max)}`;
    if (min) return `From ${fmt(min)}`;
    return `Up to ${fmt(max)}`;
  };

  return (
    <Container className="py-4">
      {/* Search / Filter Bar */}
      <Card className="mb-4 shadow-sm border-0">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-2 align-items-end">
              <Col md={3}>
                <Form.Control placeholder="Job title, keyword..." value={filters.keyword}
                  onChange={(e) => setFilters({ ...filters, keyword: e.target.value })} />
              </Col>
              <Col md={3}>
                <Form.Control placeholder="Location..." value={filters.location}
                  onChange={(e) => setFilters({ ...filters, location: e.target.value })} />
              </Col>
              <Col md={2}>
                <Form.Select value={filters.employmentType}
                  onChange={(e) => setFilters({ ...filters, employmentType: e.target.value })}>
                  <option value="">All Types</option>
                  <option value="FULL_TIME">Full Time</option>
                  <option value="PART_TIME">Part Time</option>
                  <option value="CONTRACT">Contract</option>
                  <option value="INTERNSHIP">Internship</option>
                  <option value="REMOTE">Remote</option>
                </Form.Select>
              </Col>
              <Col md={2}>
                <Form.Control type="number" placeholder="Max Experience" value={filters.experience}
                  onChange={(e) => setFilters({ ...filters, experience: e.target.value })} min={0} />
              </Col>
              <Col md={2}>
                <Button type="submit" variant="primary" className="w-100">Search</Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {/* Job Cards */}
      {loading ? (
        <div className="text-center py-5"><Spinner animation="border" variant="primary" /></div>
      ) : jobs.length === 0 ? (
        <Alert variant="info" className="text-center">
          No jobs found matching your criteria. Try adjusting your filters.
        </Alert>
      ) : (
        <>
          <Row className="g-3">
            {jobs.map((job) => (
              <Col md={6} lg={4} key={job.id}>
                <Card className="h-100 shadow-sm border-0 hover-card">
                  <Card.Body className="d-flex flex-column">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <Badge bg={job.employmentType === 'FULL_TIME' ? 'primary' :
                                 job.employmentType === 'REMOTE' ? 'success' : 'secondary'}>
                        {job.employmentType?.replace('_', ' ')}
                      </Badge>
                      <small className="text-muted">
                        {new Date(job.createdAt).toLocaleDateString()}
                      </small>
                    </div>
                    <Card.Title className="fw-bold">{job.title}</Card.Title>
                    <Card.Subtitle className="mb-2 text-primary">{job.companyName}</Card.Subtitle>
                    <div className="text-muted small mb-2">
                      <div><FaMapMarkerAlt className="me-1" />{job.location}</div>
                      {job.experienceMin != null && (
                        <div><FaBriefcase className="me-1" />
                          {job.experienceMin}-{job.experienceMax || '+'} yrs experience
                        </div>
                      )}
                      {formatSalary(job.salaryMin, job.salaryMax) && (
                        <div><FaClock className="me-1" />{formatSalary(job.salaryMin, job.salaryMax)}</div>
                      )}
                    </div>
                    <Card.Text className="text-muted small flex-grow-1">
                      {job.description?.substring(0, 120)}...
                    </Card.Text>
                    {job.requiredSkills?.length > 0 && (
                      <div className="mb-2">
                        {[...job.requiredSkills].slice(0, 4).map((s, i) => (
                          <Badge key={i} bg="light" text="dark" className="me-1 mb-1">{s}</Badge>
                        ))}
                      </div>
                    )}
                    <Button as={Link} to={`/jobs/${job.id}`} variant="outline-primary" size="sm">
                      View Details
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-center mt-4">
              <Pagination>
                <Pagination.Prev disabled={page === 0} onClick={() => goToPage(page - 1)} />
                {[...Array(Math.min(totalPages, 5))].map((_, i) => {
                  const p = page < 3 ? i : page - 2 + i;
                  if (p >= totalPages) return null;
                  return (
                    <Pagination.Item key={p} active={p === page} onClick={() => goToPage(p)}>
                      {p + 1}
                    </Pagination.Item>
                  );
                })}
                <Pagination.Next disabled={page >= totalPages - 1} onClick={() => goToPage(page + 1)} />
              </Pagination>
            </div>
          )}
        </>
      )}
    </Container>
  );
}
