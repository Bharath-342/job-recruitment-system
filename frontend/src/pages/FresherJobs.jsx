import { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Form, Button, Badge, Spinner, Alert, Pagination } from 'react-bootstrap';
import { fresherJobService } from '../services/services';

export default function FresherJobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [stats, setStats] = useState(null);
  const [companies, setCompanies] = useState([]);

  // Search & Filter state
  const [keyword, setKeyword] = useState('');
  const [location, setLocation] = useState('');
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedCompany, setSelectedCompany] = useState('');
  const [remoteOnly, setRemoteOnly] = useState(false);
  const [experienceLevel, setExperienceLevel] = useState('');
  const [sortBy, setSortBy] = useState('postedAt');
  const [sortDir, setSortDir] = useState('desc');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 9;

  const rolePresets = [
    'Java Fresher',
    'Java Backend',
    'Java Full Stack',
    'Software Engineer',
    'Associate Software Engineer',
    'Graduate Software Engineer',
    'Trainee Software Engineer'
  ];

  const locationPresets = [
    'All Locations',
    'Hyderabad',
    'Bengaluru',
    'Chennai',
    'Pune',
    'Mumbai',
    'Delhi NCR',
    'Noida',
    'Gurgaon/Gurugram',
    'Kolkata',
    'Ahmedabad',
    'Kochi',
    'Visakhapatnam',
    'Remote - India',
    'India'
  ];

  // Fetch verified statistics
  const fetchStatistics = async () => {
    try {
      const res = await fresherJobService.getStatistics();
      setStats(res.data);
    } catch (err) {
      console.error('Failed to load job statistics:', err);
    }
  };

  // Fetch unique companies
  const fetchCompanies = async () => {
    try {
      const res = await fresherJobService.getCompanies();
      setCompanies(res.data || []);
    } catch (err) {
      console.error('Failed to load companies:', err);
    }
  };

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    const kw = searchParams.get('keyword');
    const comp = searchParams.get('company');
    const loc = searchParams.get('location');
    if (kw) setKeyword(kw);
    if (comp) setSelectedCompany(comp);
    if (loc) setLocation(loc);
  }, []);

  // Fetch jobs
  const fetchJobs = useCallback(async (page = 0) => {
    setLoading(true);
    setError('');
    try {
      const params = {
        page,
        size: pageSize,
        sort: sortBy,
        direction: sortDir,
      };

      if (keyword.trim()) params.keyword = keyword.trim();
      if (location.trim() && location !== 'All Locations') params.location = location.trim();
      if (selectedRole) params.role = selectedRole;
      if (selectedCompany) params.company = selectedCompany;
      if (remoteOnly) params.remote = true;
      if (experienceLevel) params.experienceLevel = experienceLevel;

      const res = await fresherJobService.getFresherJobs(params);
      setJobs(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
      setCurrentPage(page);
    } catch (err) {
      console.error('Failed to fetch fresher jobs:', err);
      setError('Unable to load fresher jobs right now. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [keyword, location, selectedRole, selectedCompany, remoteOnly, experienceLevel, sortBy, sortDir]);

  useEffect(() => {
    fetchStatistics();
    fetchCompanies();
  }, []);

  useEffect(() => {
    fetchJobs(0);
  }, [fetchJobs]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchJobs(0);
  };

  const handleRoleClick = (role) => {
    if (selectedRole === role) {
      setSelectedRole('');
    } else {
      setSelectedRole(role);
    }
  };

  const handleLocationPresetClick = (loc) => {
    if (loc === 'All Locations') {
      setLocation('');
    } else {
      setLocation(loc);
    }
  };

  const handleResetFilters = () => {
    setKeyword('');
    setLocation('');
    setSelectedRole('');
    setSelectedCompany('');
    setRemoteOnly(false);
    setExperienceLevel('');
    setSortBy('postedAt');
    setSortDir('desc');
  };

  // Format relative time
  const formatTimeAgo = (dateStr) => {
    if (!dateStr) return 'Recently listed';
    try {
      const date = new Date(dateStr);
      const diffMs = new Date() - date;
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
      const diffDays = Math.floor(diffHours / 24);

      if (diffDays > 0) return `${diffDays}d ago`;
      if (diffHours > 0) return `${diffHours}h ago`;
      const diffMinutes = Math.floor(diffMs / (1000 * 60));
      if (diffMinutes > 0) return `${diffMinutes}m ago`;
      return 'Just now';
    } catch {
      return 'Recently listed';
    }
  };

  return (
    <div className="py-4 bg-light">
      <Container>
        {/* Hero Section */}
        <div
          className="p-4 p-md-5 mb-4 text-white rounded-4 shadow-sm"
          style={{
            background: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #4a00e0 100%)'
          }}
        >
          <Row className="align-items-center">
            <Col lg={8}>
              <Badge bg="success" text="white" className="px-3 py-2 fs-6 mb-3 fw-bold">
                ✓ STRICT 0-YEAR VERIFIED
              </Badge>
              <h1 className="display-5 fw-bold mb-3">
                Find Your First Job
              </h1>
              <p className="lead mb-4 text-white-50">
                Search current opportunities that accept candidates with zero professional experience.
              </p>
            </Col>
            <Col lg={4}>
              <Card className="border-0 shadow bg-white text-dark rounded-4">
                <Card.Body className="p-3 text-center">
                  <h6 className="text-muted fw-bold text-uppercase mb-2">Live Verified Status</h6>
                  <div className="d-flex justify-content-around my-2">
                    <div>
                      <div className="h3 fw-bold text-primary mb-0">{stats ? stats.totalActiveFresherJobs : '...'}</div>
                      <small className="text-muted">Fresher Jobs</small>
                    </div>
                    <div className="border-start ps-3">
                      <div className="h3 fw-bold text-success mb-0">{stats ? stats.uniqueCompaniesHiring : '...'}</div>
                      <small className="text-muted">Companies Hiring</small>
                    </div>
                  </div>
                  <hr className="my-2" />
                  <div className="d-flex justify-content-between text-muted small px-2">
                    <span>Locations: <strong>{stats ? stats.locations : '...'}</strong></span>
                    <span>Remote: <strong>{stats ? stats.remoteJobs : '...'}</strong></span>
                  </div>
                  {stats?.lastSyncedAt && (
                    <div className="mt-2 text-muted" style={{ fontSize: '0.75rem' }}>
                      Last Verified: <strong>{formatTimeAgo(stats.lastSyncedAt)}</strong>
                    </div>
                  )}
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </div>

        {/* Quick Role Filters */}
        <div className="mb-4">
          <div className="d-flex align-items-center flex-wrap gap-2">
            <span className="text-muted fw-semibold me-1">Target Roles:</span>
            {rolePresets.map((r) => (
              <Button
                key={r}
                size="sm"
                variant={selectedRole === r ? 'primary' : 'outline-secondary'}
                className="rounded-pill px-3"
                onClick={() => handleRoleClick(r)}
              >
                {r}
              </Button>
            ))}
            {selectedRole && (
              <Button
                size="sm"
                variant="link"
                className="text-danger p-0 ms-2 text-decoration-none"
                onClick={() => setSelectedRole('')}
              >
                ✕ Clear Role
              </Button>
            )}
          </div>
        </div>

        {/* Search & Multi-Filter Form */}
        <Card className="border-0 shadow-sm rounded-4 mb-4">
          <Card.Body className="p-4">
            <Form onSubmit={handleSearchSubmit}>
              <Row className="g-3">
                <Col md={4}>
                  <Form.Group controlId="searchKeyword">
                    <Form.Label className="fw-semibold">Keywords / Skills</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder="e.g. Java, Spring Boot, React, SQL..."
                      value={keyword}
                      onChange={(e) => setKeyword(e.target.value)}
                    />
                  </Form.Group>
                </Col>

                <Col md={3}>
                  <Form.Group controlId="searchLocation">
                    <Form.Label className="fw-semibold">Location / City</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder="e.g. Hyderabad, Bengaluru, Remote..."
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                    />
                  </Form.Group>
                </Col>

                <Col md={3}>
                  <Form.Group controlId="searchCompany">
                    <Form.Label className="fw-semibold">Company</Form.Label>
                    <Form.Select
                      value={selectedCompany}
                      onChange={(e) => setSelectedCompany(e.target.value)}
                    >
                      <option value="">All Companies</option>
                      {companies.map((c) => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>

                <Col md={2} className="d-flex align-items-end">
                  <div className="w-100 d-flex gap-2">
                    <Button type="submit" variant="primary" className="w-100 fw-bold">
                      Search
                    </Button>
                    <Button variant="outline-secondary" onClick={handleResetFilters} title="Reset all filters">
                      ↺
                    </Button>
                  </div>
                </Col>
              </Row>

              <Row className="mt-3 pt-3 border-top align-items-center">
                <Col md={6}>
                  <div className="d-flex flex-wrap gap-1 align-items-center">
                    <small className="text-muted me-2">Popular Cities:</small>
                    {locationPresets.map((loc) => (
                      <Badge
                        key={loc}
                        bg={location === loc || (loc === 'All Locations' && !location) ? 'dark' : 'light'}
                        text={location === loc || (loc === 'All Locations' && !location) ? 'white' : 'dark'}
                        className="px-2 py-1 cursor-pointer border"
                        style={{ cursor: 'pointer' }}
                        onClick={() => handleLocationPresetClick(loc)}
                      >
                        {loc}
                      </Badge>
                    ))}
                  </div>
                </Col>

                <Col md={6} className="d-flex justify-content-md-end align-items-center gap-3 mt-2 mt-md-0">
                  <Form.Check
                    type="switch"
                    id="remote-switch"
                    label="Remote Only"
                    checked={remoteOnly}
                    onChange={(e) => setRemoteOnly(e.target.checked)}
                    className="fw-semibold"
                  />

                  <Form.Select
                    size="sm"
                    style={{ width: '160px' }}
                    value={`${sortBy}_${sortDir}`}
                    onChange={(e) => {
                      const [s, d] = e.target.value.split('_');
                      setSortBy(s);
                      setSortDir(d);
                    }}
                  >
                    <option value="postedAt_desc">Newest First</option>
                    <option value="postedAt_asc">Oldest First</option>
                    <option value="companyName_asc">Company (A-Z)</option>
                  </Form.Select>
                </Col>
              </Row>
            </Form>
          </Card.Body>
        </Card>

        {/* Results Header */}
        <div className="d-flex justify-content-between align-items-center mb-3">
          <div>
            <h5 className="fw-bold mb-0 text-dark">
              {loading ? 'Searching job feeds...' : `${totalElements} Fresher Positions Found`}
            </h5>
            {stats && (
              <small className="text-muted">
                Showing current verified postings across <strong>{stats.uniqueCompaniesHiring}</strong> hiring companies
              </small>
            )}
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <Alert variant="danger" dismissible onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        {/* Loading Spinner */}
        {loading && (
          <div className="text-center py-5">
            <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
            <p className="mt-3 text-muted fw-semibold">Querying verified company feeds...</p>
          </div>
        )}

        {/* Job Cards Grid */}
        {!loading && jobs.length > 0 && (
          <Row className="g-4">
            {jobs.map((job) => (
              <Col key={job.id} lg={4} md={6}>
                <Card className="h-100 border-0 shadow-sm rounded-4 position-relative hover-shadow transition-all">
                  <Card.Body className="d-flex flex-column p-4">
                    {/* Top Row: Company & Source Badge */}
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <span className="fw-bold text-primary fs-5">{job.companyName}</span>
                      <Badge bg="light" text="dark" className="border">
                        {job.sourceProvider || 'ATS Verified'}
                      </Badge>
                    </div>

                    {/* Job Title */}
                    <h5 className="fw-bold text-dark mb-2" style={{ lineHeight: '1.4' }}>
                      {job.title}
                    </h5>

                    {/* Location & Remote */}
                    <div className="d-flex align-items-center flex-wrap gap-2 text-muted small mb-3">
                      <span>📍 {job.location || 'India'}</span>
                      {job.remote && (
                        <Badge bg="success" className="rounded-pill">Remote</Badge>
                      )}
                      {job.department && (
                        <span className="text-secondary">• {job.department}</span>
                      )}
                    </div>

                    {/* Fresher & Experience Tag - Strictly 0-Year Only */}
                    <div className="mb-3">
                      <Badge bg="success" className="me-2 px-2 py-1">
                        🎓 0 Years / Fresher Eligible
                      </Badge>
                      {job.employmentType && (
                        <Badge bg="secondary" className="px-2 py-1">
                          {job.employmentType}
                        </Badge>
                      )}
                    </div>

                    {/* Skills pills */}
                    {job.skills && (
                      <div className="d-flex flex-wrap gap-1 mb-4">
                        {job.skills.split(',').slice(0, 4).map((s, idx) => (
                          <span
                            key={idx}
                            className="badge bg-light text-secondary border fw-normal"
                            style={{ fontSize: '0.75rem' }}
                          >
                            {s.trim()}
                          </span>
                        ))}
                      </div>
                    )}

                    {/* Card Footer: Timestamp & Direct Apply Button */}
                    <div className="mt-auto pt-3 border-top d-flex justify-content-between align-items-center">
                      <small className="text-muted" title={job.postedAt}>
                        🕒 {formatTimeAgo(job.postedAt || job.lastVerifiedAt)}
                      </small>

                      <a
                        href={job.applicationUrl || job.sourceUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn btn-primary btn-sm px-3 fw-bold rounded-pill"
                      >
                        Apply on Official Website ↗
                      </a>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        )}

        {/* Empty State - Section 40 Requirement */}
        {!loading && jobs.length === 0 && (
          <Card className="border-0 shadow-sm rounded-4 text-center p-5 my-4">
            <Card.Body>
              <div style={{ fontSize: '3.5rem' }}>🔍</div>
              <h4 className="fw-bold mt-3">No verified 0-year jobs found for this search.</h4>
              <p className="text-muted">
                Try another location, try another keyword, or try another role.
              </p>
              <div className="d-flex justify-content-center gap-2 flex-wrap mt-3">
                <Button variant="outline-primary" size="sm" onClick={handleResetFilters} className="rounded-pill px-3">
                  Reset All Filters
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setLocation('Bengaluru'); }} className="rounded-pill px-3">
                  Try Bengaluru
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setLocation('Hyderabad'); }} className="rounded-pill px-3">
                  Try Hyderabad
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setKeyword('Java'); }} className="rounded-pill px-3">
                  Try Java
                </Button>
              </div>
            </Card.Body>
          </Card>
        )}

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="d-flex justify-content-center mt-5">
            <Pagination>
              <Pagination.First
                disabled={currentPage === 0}
                onClick={() => fetchJobs(0)}
              />
              <Pagination.Prev
                disabled={currentPage === 0}
                onClick={() => fetchJobs(currentPage - 1)}
              />
              {[...Array(totalPages)].map((_, i) => {
                if (
                  i === 0 ||
                  i === totalPages - 1 ||
                  (i >= currentPage - 2 && i <= currentPage + 2)
                ) {
                  return (
                    <Pagination.Item
                      key={i}
                      active={i === currentPage}
                      onClick={() => fetchJobs(i)}
                    >
                      {i + 1}
                    </Pagination.Item>
                  );
                } else if (i === currentPage - 3 || i === currentPage + 3) {
                  return <Pagination.Ellipsis key={i} disabled />;
                }
                return null;
              })}
              <Pagination.Next
                disabled={currentPage === totalPages - 1}
                onClick={() => fetchJobs(currentPage + 1)}
              />
              <Pagination.Last
                disabled={currentPage === totalPages - 1}
                onClick={() => fetchJobs(totalPages - 1)}
              />
            </Pagination>
          </div>
        )}
      </Container>
    </div>
  );
}
