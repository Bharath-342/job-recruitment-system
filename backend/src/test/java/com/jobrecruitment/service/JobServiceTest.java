package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.JobRequest;
import com.jobrecruitment.dto.response.JobResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.BusinessRuleException;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.repository.*;
import com.jobrecruitment.service.impl.JobServiceImpl;
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
class JobServiceTest {

    @Mock private JobRepository jobRepository;
    @Mock private UserRepository userRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private RecruiterProfileRepository recruiterProfileRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks private JobServiceImpl jobService;

    private User recruiter;
    private RecruiterProfile recruiterProfile;
    private JobRequest jobRequest;
    private Job job;

    @BeforeEach
    void setUp() {
        recruiter = new User("Jane", "Smith", "jane@example.com", "encoded", Role.RECRUITER);
        recruiter.setId(2L);

        recruiterProfile = new RecruiterProfile();
        recruiterProfile.setUser(recruiter);
        recruiterProfile.setCompanyName("TechCorp");

        jobRequest = new JobRequest();
        jobRequest.setTitle("Java Developer");
        jobRequest.setDescription("This is a test job description for Java Developer position");
        jobRequest.setLocation("Hyderabad");
        jobRequest.setEmploymentType("FULL_TIME");

        job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setDescription("Test");
        job.setLocation("Hyderabad");
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(JobStatus.OPEN);
        job.setRecruiter(recruiter);
        job.setCompanyName("TechCorp");
    }

    @Test
    @DisplayName("Should create job successfully")
    void createJob_Success() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(recruiter));
        when(recruiterProfileRepository.findByUserId(2L)).thenReturn(Optional.of(recruiterProfile));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(1L);
            return j;
        });

        JobResponse response = jobService.createJob(jobRequest, "jane@example.com");

        assertNotNull(response);
        assertEquals("Java Developer", response.getTitle());
        assertEquals("TechCorp", response.getCompanyName());
        verify(jobRepository).save(any());
    }

    @Test
    @DisplayName("Should reject update of another recruiter's job")
    void updateJob_NotOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(BusinessRuleException.class,
                () -> jobService.updateJob(1L, jobRequest, "other@example.com"));
    }

    @Test
    @DisplayName("Should reject update of closed job")
    void updateJob_ClosedJob() {
        job.setStatus(JobStatus.CLOSED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(BusinessRuleException.class,
                () -> jobService.updateJob(1L, jobRequest, "jane@example.com"));
    }

    @Test
    @DisplayName("Should close job successfully")
    void closeJob_Success() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenReturn(job);

        assertDoesNotThrow(() -> jobService.closeJob(1L, "jane@example.com"));
        assertEquals(JobStatus.CLOSED, job.getStatus());
    }

    @Test
    @DisplayName("Should throw when job not found")
    void getJob_NotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobService.getJobById(99L));
    }

    @Test
    @DisplayName("Should reject delete by non-owner non-admin")
    void deleteJob_NotOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(BusinessRuleException.class,
                () -> jobService.deleteJob(1L, "other@example.com", false));
    }
}
