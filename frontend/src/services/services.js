import api from './api';

export const authService = {
  registerCandidate: (data) => api.post('/auth/register/candidate', data),
  registerRecruiter: (data) => api.post('/auth/register/recruiter', data),
  login: (data) => api.post('/auth/login', data),
};

export const jobService = {
  searchJobs: (params) => api.get('/jobs', { params }),
  getJob: (id) => api.get(`/jobs/${id}`),
  createJob: (data) => api.post('/jobs', data),
  updateJob: (id, data) => api.put(`/jobs/${id}`, data),
  closeJob: (id) => api.patch(`/jobs/${id}/close`),
  deleteJob: (id) => api.delete(`/jobs/${id}`),
};

export const applicationService = {
  applyForJob: (jobId, data) => api.post(`/jobs/${jobId}/applications`, data),
  getMyApplications: (params) => api.get('/applications/my', { params }),
  withdrawApplication: (id) => api.delete(`/applications/${id}/withdraw`),
};

export const candidateService = {
  getProfile: () => api.get('/candidate/profile'),
  updateProfile: (data) => api.put('/candidate/profile', data),
  addSkills: (skills) => api.post('/candidate/skills', skills),
  removeSkill: (name) => api.delete(`/candidate/skills/${name}`),
  getDashboard: () => api.get('/candidate/dashboard'),
  uploadResume: (formData) => api.post('/candidate/resume', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  downloadResume: () => api.get('/candidate/resume', { responseType: 'blob' }),
};

export const recruiterService = {
  getMyJobs: (params) => api.get('/recruiter/jobs', { params }),
  getApplications: (params) => api.get('/recruiter/applications', { params }),
  getJobApplications: (jobId, params) => api.get(`/recruiter/jobs/${jobId}/applications`, { params }),
  updateApplicationStatus: (id, data) => api.patch(`/recruiter/applications/${id}/status`, data),
  getCandidateProfile: (userId) => api.get(`/recruiter/candidates/${userId}`),
  downloadResume: (userId) => api.get(`/recruiter/candidates/${userId}/resume`, { responseType: 'blob' }),
  updateProfile: (data) => api.put('/recruiter/profile', data),
  getDashboard: () => api.get('/recruiter/dashboard'),
};

export const adminService = {
  getUsers: (params) => api.get('/admin/users', { params }),
  toggleUserActive: (id) => api.patch(`/admin/users/${id}/toggle-active`),
  removeJob: (id) => api.delete(`/admin/jobs/${id}`),
  getStatistics: () => api.get('/admin/statistics'),
};

export const fresherJobService = {
  getFresherJobs: (params) => api.get('/jobs/fresher', { params }),
  searchAllAggregated: (params) => api.get('/jobs/search', { params }),
  getFresherJob: (id) => api.get(`/jobs/fresher/${id}`),
  getStatistics: () => api.get('/jobs/statistics'),
  getCompanies: () => api.get('/jobs/companies'),
  getSources: () => api.get('/jobs/sources'),
  triggerSync: (sourceId) => api.post('/jobs/sync', null, { params: { sourceId } }),
};
