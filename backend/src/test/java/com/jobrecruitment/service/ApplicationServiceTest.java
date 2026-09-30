package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.ApplicationRequest;
import com.jobrecruitment.dto.request.StatusUpdateRequest;
import com.jobrecruitment.dto.response.ApplicationResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.BusinessRuleException;
import com.jobrecruitment.exception.DuplicateResourceException;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.repository.ApplicationRepository;
import com.jobrecruitment.repository.JobRepository;
import com.jobrecruitment.repository.UserRepository;
import com.jobrecruitment.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ApplicationServiceImpl applicationService;

    private User candidate;
    private User recruiter;
    private Job job;

    @BeforeEach
    void setUp() {
        candidate = new User("John", "Doe", "john@example.com", "encoded", Role.CANDIDATE);
        candidate.setId(1L);
        candidate.setActive(true);

        recruiter = new User("Jane", "Smith", "jane@example.com", "encoded", Role.RECRUITER);
        recruiter.setId(2L);

        job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setDescription("Test description");
        job.setLocation("Hyderabad");
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setRecruiter(recruiter);
        job.setCompanyName("TechCorp");
    }

    @Test
    @DisplayName("Should apply for job successfully")
    void applyForJob_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 1L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        ApplicationResponse response = applicationService.applyForJob(1L, new ApplicationRequest(), "john@example.com");

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED.name(), response.getStatus());
        verify(applicationRepository).save(any());
    }

    @Test
    @DisplayName("Should prevent duplicate applications")
    void applyForJob_Duplicate() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> applicationService.applyForJob(1L, new ApplicationRequest(), "john@example.com"));
    }

    @Test
    @DisplayName("Should reject application to closed job")
    void applyForJob_ClosedJob() {
        job.setStatus(JobStatus.CLOSED);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(BusinessRuleException.class,
                () -> applicationService.applyForJob(1L, new ApplicationRequest(), "john@example.com"));
    }

    @Test
    @DisplayName("Should throw when job not found")
    void applyForJob_JobNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.applyForJob(99L, new ApplicationRequest(), "john@example.com"));
    }

    @Test
    @DisplayName("Should update application status through valid transition")
    void updateStatus_ValidTransition() {
        Application app = new Application();
        app.setId(1L);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);

        StatusUpdateRequest statusRequest = new StatusUpdateRequest();
        statusRequest.setStatus("UNDER_REVIEW");

        when(applicationRepository.findByIdWithJob(1L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);

        ApplicationResponse response = applicationService.updateApplicationStatus(1L, statusRequest, "jane@example.com");

        assertNotNull(response);
    }

    @Test
    @DisplayName("Should reject invalid status transition")
    void updateStatus_InvalidTransition() {
        Application app = new Application();
        app.setId(1L);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);

        StatusUpdateRequest statusRequest = new StatusUpdateRequest();
        statusRequest.setStatus("SELECTED");

        when(applicationRepository.findByIdWithJob(1L)).thenReturn(Optional.of(app));

        assertThrows(BusinessRuleException.class,
                () -> applicationService.updateApplicationStatus(1L, statusRequest, "jane@example.com"));
    }

    @Test
    @DisplayName("Should reject status change by non-owner recruiter")
    void updateStatus_NotOwner() {
        Application app = new Application();
        app.setId(1L);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);

        StatusUpdateRequest statusRequest = new StatusUpdateRequest();
        statusRequest.setStatus("UNDER_REVIEW");

        when(applicationRepository.findByIdWithJob(1L)).thenReturn(Optional.of(app));

        assertThrows(BusinessRuleException.class,
                () -> applicationService.updateApplicationStatus(1L, statusRequest, "other@example.com"));
    }

    @Test
    @DisplayName("Should withdraw application at APPLIED status")
    void withdrawApplication_Success() {
        Application app = new Application();
        app.setId(1L);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertDoesNotThrow(() -> applicationService.withdrawApplication(1L, "john@example.com"));
        verify(applicationRepository).delete(app);
    }

    @Test
    @DisplayName("Should not withdraw application at INTERVIEW stage")
    void withdrawApplication_AtInterview() {
        Application app = new Application();
        app.setId(1L);
        app.setCandidate(candidate);
        app.setJob(job);
        app.setStatus(ApplicationStatus.INTERVIEW);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThrows(BusinessRuleException.class,
                () -> applicationService.withdrawApplication(1L, "john@example.com"));
    }
}
