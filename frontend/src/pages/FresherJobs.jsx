import { useState, useEffect, useCallback, useMemo } from 'react';
import { Container, Row, Col, Card, Form, Button, Badge, Spinner, Alert, Pagination, Nav } from 'react-bootstrap';
import { fresherJobService } from '../services/services';

const MNC_KEYWORDS = [
  'tcs', 'infosys', 'wipro', 'hcl', 'cognizant', 'accenture', 'capgemini', 'tech mahindra',
  'ltimindtree', 'mphasis', 'hexaware', 'coforge', 'persistent', 'ntt', 'dxc',
  'cgi', 'sapient', 'thoughtworks', 'epam', 'zensar', 'birlasoft', 'sonata',
  'kpit', 'tata', 'microsoft', 'amazon', 'google', 'ibm', 'oracle', 'sap',
  'dell', 'hp', 'cisco', 'intel', 'qualcomm', 'adobe', 'vmware', 'broadcom',
  'paypal', 'servicenow', 'salesforce', 'jpmorgan', 'goldman', 'walmart',
  'uber', 'expedia', 'visa', 'mastercard', 'american express', 'wells fargo',
  'rubrik', 'tower research', 'point72', 'imc', 'worldquant', 'zscaler', 'pure storage',
  'netskope', 'sonicwall', 'commvault', 'druva', 'bitgo'
];

export default function FresherJobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [stats, setStats] = useState(null);
  const [companies, setCompanies] = useState([]);
  const [syncing, setSyncing] = useState(false);
  const [syncResult, setSyncResult] = useState(null);

  // Search & Filter state
  const [keyword, setKeyword] = useState('');
  const [location, setLocation] = useState('');
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedCompany, setSelectedCompany] = useState('');
  const [remoteOnly, setRemoteOnly] = useState(false);
  const [experienceLevel, setExperienceLevel] = useState('');
  const [activeSection, setActiveSection] = useState('all'); // 'all', 'mnc', 'startup', 'recent'
  const [sortBy, setSortBy] = useState('relevance');
  const [sortDir, setSortDir] = useState('desc');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 9;

  const roleCategories = [
    { id: '', label: 'All Categories' },
    { id: 'JAVA_FULL_STACK', label: '⚡ Java Full Stack' },
    { id: 'JAVA_BACKEND', label: '☕ Java Backend' },
    { id: 'JAVA_DEVELOPMENT', label: '☕ Java Developer' },
    { id: 'SOFTWARE_ENGINEERING', label: '💻 Software Engineer' },
    { id: 'QA_AUTOMATION', label: '🧪 QA & Automation' },
    { id: 'IT_SUPPORT', label: '🛠️ IT Support' }
  ];

  const rolePresets = [
    'Java Full Stack',
    'Java Backend',
    'Java Fresher',
    'Associate Software Engineer',
    'Graduate Software Engineer',
    'Spring Boot Trainee',
    'Automation Test Engineer'
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
    'Coimbatore',
    'Visakhapatnam',
    'Remote - India'
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

  // Trigger manual synchronization (Section 13, 36)
  const handleSyncNow = async () => {
    setSyncing(true);
    setSyncResult(null);
    try {
      const res = await fresherJobService.triggerSync();
      setSyncResult(res.data);
      await fetchStatistics();
      await fetchCompanies();
      await fetchJobs(0);
    } catch (err) {
      console.error('Failed to trigger synchronization:', err);
      setError('Unable to trigger synchronization. Please try again.');
    } finally {
      setSyncing(false);
    }
  };

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    const kw = searchParams.get('keyword');
    const comp = searchParams.get('company');
    const loc = searchParams.get('location');
    const cat = searchParams.get('category');
    if (kw) setKeyword(kw);
    if (comp) setSelectedCompany(comp);
    if (loc) setLocation(loc);
    if (cat) setSelectedCategory(cat);
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
      if (selectedCategory) params.roleCategory = selectedCategory;
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
  }, [keyword, location, selectedRole, selectedCategory, selectedCompany, remoteOnly, experienceLevel, sortBy, sortDir]);

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

  const handleCategoryClick = (catId) => {
    setSelectedCategory(catId);
    setCurrentPage(0);
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
    setSelectedCategory('');
    setSelectedCompany('');
    setRemoteOnly(false);
    setExperienceLevel('');
    setActiveSection('all');
    setSortBy('relevance');
    setSortDir('desc');
  };

  // Format relative time
  const formatTimeAgo = (dateStr) => {
    if (!dateStr) return 'Recently';
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
      return 'Recently';
    }
  };

  // Filter jobs by section tab (MNC vs Startup vs Recent 24h)
  const displayedJobs = useMemo(() => {
    if (activeSection === 'mnc') {
      return jobs.filter(j => {
        const c = (j.companyName || '').toLowerCase();
        return MNC_KEYWORDS.some(k => c.includes(k));
      });
    }
    if (activeSection === 'startup') {
      return jobs.filter(j => {
        const c = (j.companyName || '').toLowerCase();
        return !MNC_KEYWORDS.some(k => c.includes(k));
      });
    }
    if (activeSection === 'recent') {
      const dayAgo = Date.now() - 24 * 60 * 60 * 1000;
      return jobs.filter(j => {
        if (!j.postedAt && !j.createdAt) return true;
        const d = new Date(j.postedAt || j.createdAt).getTime();
        return d >= dayAgo;
      });
    }
    return jobs;
  }, [jobs, activeSection]);

  const getCategoryBadgeColor = (cat) => {
    switch (cat) {
      case 'JAVA_FULL_STACK':
        return 'success';
      case 'JAVA_BACKEND':
        return 'primary';
      case 'JAVA_DEVELOPMENT':
        return 'info';
      case 'SOFTWARE_ENGINEERING':
        return 'dark';
      case 'QA_AUTOMATION':
        return 'warning';
      case 'IT_SUPPORT':
        return 'secondary';
      default:
        return 'secondary';
    }
  };

  return (
    <div className="py-4 bg-light">
      <Container>
        {/* Hero Section */}
        <div
          className="p-4 p-md-5 mb-4 text-white rounded-4 shadow-sm"
          style={{
            background: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #4338ca 100%)'
          }}
        >
          <Row className="align-items-center">
            <Col lg={8}>
              <div className="d-flex flex-wrap gap-2 mb-3">
                <Badge bg="success" text="white" className="px-3 py-2 fs-6 fw-bold">
                  ✓ INDIA IT FRESHERS ONLY
                </Badge>
                <Badge bg="warning" text="dark" className="px-3 py-2 fs-6 fw-bold">
                  ☕ JAVA FULL STACK FOCUS
                </Badge>
              </div>
              <h1 className="display-5 fw-bold mb-3">
                India Java & IT Fresher Job Discovery
              </h1>
              <p className="lead mb-4 text-white-50">
                Discover verified Java Full Stack, Backend, and Software Engineering openings for freshers across India. Zero experience required. Continuously updated from legitimate career feeds.
              </p>
            </Col>
            <Col lg={4}>
              <Card className="border-0 shadow bg-white text-dark rounded-4">
                <Card.Body className="p-3 text-center">
                  <h6 className="text-muted fw-bold text-uppercase mb-2">Live Verified Status</h6>
                  <div className="d-flex justify-content-around my-2">
                    <div>
                      <div className="h3 fw-bold text-primary mb-0">{stats ? stats.totalActiveFresherJobs : '...'}</div>
                      <small className="text-muted">Indian Fresher Jobs</small>
                    </div>
                    <div className="border-start ps-3">
                      <div className="h3 fw-bold text-success mb-0">{stats ? stats.uniqueCompaniesHiring : '...'}</div>
                      <small className="text-muted">Hiring Companies</small>
                    </div>
                  </div>
                  <hr className="my-2" />
                  <div className="d-flex justify-content-between text-muted small px-2">
                    <span>Cities: <strong>{stats ? stats.locations : '...'}</strong></span>
                    <span>Remote India: <strong>{stats ? stats.remoteJobs : '...'}</strong></span>
                  </div>
                  {stats?.lastSyncedAt && (
                    <div className="mt-2 text-muted" style={{ fontSize: '0.75rem' }}>
                      Last synchronized: <strong>{formatTimeAgo(stats.lastSyncedAt)}</strong>
                      <span className="ms-1 text-secondary">(Next sync in ~20m)</span>
                    </div>
                  )}

                  <Button
                    variant="primary"
                    size="sm"
                    className="w-100 mt-2 fw-semibold rounded-pill"
                    onClick={handleSyncNow}
                    disabled={syncing}
                  >
                    {syncing ? (
                      <>
                        <Spinner animation="border" size="sm" className="me-2" />
                        Discovering Openings...
                      </>
                    ) : (
                      '🔄 Discover & Sync Jobs Now'
                    )}
                  </Button>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </div>

        {/* Sync Summary Alert */}
        {syncResult && (
          <Alert variant="info" dismissible onClose={() => setSyncResult(null)} className="rounded-4 shadow-sm mb-4 border-info bg-white">
            <div className="d-flex align-items-center gap-2 mb-1 text-primary">
              <span className="fs-5">🔄</span>
              <strong className="fs-6">Live Synchronization Report</strong>
            </div>
            <div className="small text-muted">
              Scanned <strong>{syncResult.totalJobsDiscovered}</strong> positions across <strong>{syncResult.sourcesProcessed}</strong> company boards.
              Discovered <strong>{syncResult.jobsFromIndia}</strong> in India | Added <strong>{syncResult.jobsInserted}</strong> new 0-year fresher jobs | Updated <strong>{syncResult.jobsUpdated}</strong> | Deactivated <strong>{syncResult.jobsDeactivated}</strong> expired | Excluded <strong>{syncResult.jobsRejectedForeign}</strong> foreign & <strong>{syncResult.jobsRejectedExperienceGreaterThanZero}</strong> experienced.
            </div>
          </Alert>
        )}

        {/* Primary Role Category Navigation (Section 3 & 19) */}
        <div className="mb-4">
          <div className="d-flex align-items-center flex-wrap gap-2">
            <span className="text-muted fw-bold me-1">Specialization:</span>
            {roleCategories.map((c) => (
              <Button
                key={c.id}
                size="sm"
                variant={selectedCategory === c.id ? 'primary' : 'outline-dark'}
                className="rounded-pill px-3 fw-semibold"
                onClick={() => handleCategoryClick(c.id)}
              >
                {c.label}
              </Button>
            ))}
          </div>
        </div>

        {/* Search & Multi-Filter Form */}
        <Card className="border-0 shadow-sm rounded-4 mb-4">
          <Card.Body className="p-4">
            <Form onSubmit={handleSearchSubmit}>
              <Row className="g-3">
                <Col md={4}>
                  <Form.Group controlId="searchKeyword">
                    <Form.Label className="fw-semibold">Technologies / Skills</Form.Label>
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
                    <Form.Label className="fw-semibold">Indian Location / City</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder="e.g. Hyderabad, Bengaluru, Pune..."
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                    />
                  </Form.Group>
                </Col>

                <Col md={3}>
                  <Form.Group controlId="searchCompany">
                    <Form.Label className="fw-semibold">Hiring Company</Form.Label>
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

              {/* Quick Preset Badges */}
              <Row className="mt-3 pt-3 border-top align-items-center">
                <Col md={6}>
                  <div className="d-flex flex-wrap gap-1 align-items-center">
                    <small className="text-muted me-2">Indian Tech Hubs:</small>
                    {locationPresets.map((loc) => (
                      <Badge
                        key={loc}
                        bg={location === loc || (loc === 'All Locations' && !location) ? 'dark' : 'light'}
                        text={location === loc || (loc === 'All Locations' && !location) ? 'white' : 'dark'}
                        className="px-2 py-1 border"
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
                    label="Remote India"
                    checked={remoteOnly}
                    onChange={(e) => setRemoteOnly(e.target.checked)}
                    className="fw-semibold"
                  />

                  <Form.Select
                    size="sm"
                    style={{ width: '190px' }}
                    value={`${sortBy}_${sortDir}`}
                    onChange={(e) => {
                      const [s, d] = e.target.value.split('_');
                      setSortBy(s);
                      setSortDir(d);
                    }}
                  >
                    <option value="relevance_desc">⚡ Java Relevance (Default)</option>
                    <option value="postedAt_desc">Newest First</option>
                    <option value="postedAt_asc">Oldest First</option>
                    <option value="companyName_asc">Company (A-Z)</option>
                  </Form.Select>
                </Col>
              </Row>
            </Form>
          </Card.Body>
        </Card>

        {/* Section Navigation Tabs (Section 24, 25, 26) */}
        <Nav variant="tabs" className="mb-4 fw-semibold border-bottom">
          <Nav.Item>
            <Nav.Link
              active={activeSection === 'all'}
              onClick={() => setActiveSection('all')}
              className="text-dark cursor-pointer"
            >
              All Fresher Openings ({totalElements})
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link
              active={activeSection === 'mnc'}
              onClick={() => setActiveSection('mnc')}
              className="text-dark cursor-pointer"
            >
              🏢 Top MNC Fresher Openings
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link
              active={activeSection === 'startup'}
              onClick={() => setActiveSection('startup')}
              className="text-dark cursor-pointer"
            >
              🚀 Startup & Product Openings
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link
              active={activeSection === 'recent'}
              onClick={() => setActiveSection('recent')}
              className="text-dark cursor-pointer"
            >
              🌟 Newly Discovered (Last 24h)
            </Nav.Link>
          </Nav.Item>
        </Nav>

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
            <p className="mt-3 text-muted fw-semibold">Querying verified Indian IT fresher feeds...</p>
          </div>
        )}

        {/* Job Cards Grid */}
        {!loading && displayedJobs.length > 0 && (
          <>
            <Row className="g-4">
              {displayedJobs.map((job) => {
                const locationText = [job.city, job.state].filter(Boolean).join(', ');
                const displayLocation = locationText ? `${locationText}, India` : (job.location && job.location.toLowerCase().includes('india') ? job.location : `${job.location || 'India'}, India`);

                return (
                  <Col key={job.id} lg={4} md={6}>
                    <Card className="h-100 border-0 shadow-sm rounded-4 position-relative hover-shadow transition-all bg-white">
                      <Card.Body className="d-flex flex-column p-4">
                        {/* Top Row: Company & Match Badge */}
                        <div className="d-flex justify-content-between align-items-start mb-2">
                          <span className="fw-bold text-primary fs-5">{job.companyName}</span>
                          {job.relevanceScore > 0 && (
                            <Badge bg="success" className="px-2 py-1 fw-bold">
                              {job.relevanceScore >= 90 ? '⚡ 95% Match' : `${job.relevanceScore}% Match`}
                            </Badge>
                          )}
                        </div>

                        {/* Job Title */}
                        <h5 className="fw-bold text-dark mb-2" style={{ lineHeight: '1.4' }}>
                          {job.title}
                        </h5>

                        {/* Role Category Badge */}
                        <div className="mb-2">
                          <Badge bg={getCategoryBadgeColor(job.roleCategory)} className="px-2 py-1">
                            {job.roleCategoryName || job.roleCategory || 'Software Engineering'}
                          </Badge>
                          <Badge bg="light" text="dark" className="border ms-2">
                            {job.sourceProvider || 'ATS Verified'}
                          </Badge>
                        </div>

                        {/* Location: City, State, India */}
                        <div className="d-flex align-items-center flex-wrap gap-2 text-muted small mb-2">
                          <span className="fw-semibold text-dark">
                            📍 {displayLocation}
                          </span>
                          <Badge bg="primary" className="px-2 py-1">
                            🇮🇳 INDIA
                          </Badge>
                          {job.remote && (
                            <Badge bg="info" text="dark" className="rounded-pill">Remote - India</Badge>
                          )}
                        </div>

                        {/* Experience: 0 years & Status: Verified */}
                        <div className="d-flex align-items-center flex-wrap gap-2 mb-3">
                          <Badge bg="success" className="px-2 py-1">
                            Experience: 0 years
                          </Badge>
                          <Badge bg="success" className="px-2 py-1">
                            Status: Verified
                          </Badge>
                          {job.employmentType && (
                            <Badge bg="secondary" className="px-2 py-1">
                              {job.employmentType}
                            </Badge>
                          )}
                        </div>

                        {/* Technology Match Pills */}
                        {job.technologyMatch && (
                          <div className="d-flex flex-wrap gap-1 mb-3">
                            {job.technologyMatch.split(',').map((tech, idx) => (
                              <span
                                key={idx}
                                className="badge bg-light text-primary border border-primary-subtle fw-semibold"
                                style={{ fontSize: '0.75rem' }}
                              >
                                {tech.trim()}
                              </span>
                            ))}
                          </div>
                        )}

                        {/* Card Footer: Posted Date, Verified Date & Direct Apply Button */}
                        <div className="mt-auto pt-3 border-top">
                          <div className="d-flex justify-content-between text-muted small mb-2">
                            <span>Posted: <strong>{job.postedAt ? new Date(job.postedAt).toLocaleDateString() : 'Recent'}</strong></span>
                            <span>Verified: <strong>{formatTimeAgo(job.lastVerifiedAt || job.lastSeenAt)}</strong></span>
                          </div>
                          <a
                            href={job.applicationUrl || job.sourceUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn btn-primary btn-sm w-100 fw-bold rounded-pill"
                          >
                            Apply Directly ↗
                          </a>
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                );
              })}
            </Row>

            {/* No Foreign Fallback Message */}
            <div className="text-center mt-4 mb-2">
              <div className="alert alert-light border text-muted d-inline-block px-4 py-2 rounded-pill small shadow-sm">
                Showing exclusively verified Indian IT fresher jobs. Foreign & experienced postings are strictly rejected.
              </div>
            </div>
          </>
        )}

        {/* Empty State */}
        {!loading && displayedJobs.length === 0 && (
          <Card className="border-0 shadow-sm rounded-4 text-center p-5 my-4">
            <Card.Body>
              <div style={{ fontSize: '3.5rem' }}>🔍</div>
              <h4 className="fw-bold mt-3">No verified fresher jobs are currently available in this section.</h4>
              <p className="text-muted">
                Try switching section tabs, choosing another technology, or resetting filters.
              </p>
              <div className="d-flex justify-content-center gap-2 flex-wrap mt-3">
                <Button variant="outline-primary" size="sm" onClick={handleResetFilters} className="rounded-pill px-3">
                  Reset All Filters
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setSelectedCategory('JAVA_FULL_STACK'); }} className="rounded-pill px-3">
                  Java Full Stack
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setLocation('Hyderabad'); }} className="rounded-pill px-3">
                  Hyderabad
                </Button>
                <Button variant="outline-secondary" size="sm" onClick={() => { setLocation('Bengaluru'); }} className="rounded-pill px-3">
                  Bengaluru
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
