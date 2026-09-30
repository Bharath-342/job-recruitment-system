package com.jobrecruitment.provider;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;

import java.util.List;

public interface JobSourceProvider {

    boolean supports(String providerName);

    List<RawJobDto> fetchJobs(CompanySource source) throws Exception;

    AggregatedJob normalize(RawJobDto rawJob, CompanySource source);
}
