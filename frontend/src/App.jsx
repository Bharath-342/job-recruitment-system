import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import JobList from './pages/JobList';
import JobDetails from './pages/JobDetails';
import CandidateDashboard from './pages/CandidateDashboard';
import CandidateProfile from './pages/CandidateProfile';
import MyApplications from './pages/MyApplications';
import RecruiterDashboard from './pages/RecruiterDashboard';
import CreateJob from './pages/CreateJob';
import ManageJobs from './pages/ManageJobs';
import RecruiterApplications from './pages/RecruiterApplications';
import AdminDashboard from './pages/AdminDashboard';
import FresherJobs from './pages/FresherJobs';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="d-flex flex-column min-vh-100">
          <Navbar />
          <main className="flex-grow-1 bg-light">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register/:role" element={<Register />} />
              <Route path="/jobs" element={<JobList />} />
              <Route path="/jobs/:id" element={<JobDetails />} />
              <Route path="/fresher-jobs" element={<FresherJobs />} />

              {/* Candidate Routes */}
              <Route path="/candidate/dashboard" element={
                <ProtectedRoute roles={['CANDIDATE']}><CandidateDashboard /></ProtectedRoute>
              } />
              <Route path="/candidate/profile" element={
                <ProtectedRoute roles={['CANDIDATE']}><CandidateProfile /></ProtectedRoute>
              } />
              <Route path="/candidate/applications" element={
                <ProtectedRoute roles={['CANDIDATE']}><MyApplications /></ProtectedRoute>
              } />

              {/* Recruiter Routes */}
              <Route path="/recruiter/dashboard" element={
                <ProtectedRoute roles={['RECRUITER']}><RecruiterDashboard /></ProtectedRoute>
              } />
              <Route path="/recruiter/jobs/create" element={
                <ProtectedRoute roles={['RECRUITER']}><CreateJob /></ProtectedRoute>
              } />
              <Route path="/recruiter/jobs" element={
                <ProtectedRoute roles={['RECRUITER']}><ManageJobs /></ProtectedRoute>
              } />
              <Route path="/recruiter/applications" element={
                <ProtectedRoute roles={['RECRUITER']}><RecruiterApplications /></ProtectedRoute>
              } />
              <Route path="/recruiter/jobs/:jobId/applications" element={
                <ProtectedRoute roles={['RECRUITER']}><RecruiterApplications /></ProtectedRoute>
              } />

              {/* Admin Routes */}
              <Route path="/admin/dashboard" element={
                <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
              } />
              <Route path="/admin/users" element={
                <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
              } />
              <Route path="/admin/jobs" element={
                <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
              } />
            </Routes>
          </main>
          <footer className="bg-dark text-white text-center py-3">
            <small>© 2024 JobRecruit - Job & Recruitment Management System</small>
          </footer>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
