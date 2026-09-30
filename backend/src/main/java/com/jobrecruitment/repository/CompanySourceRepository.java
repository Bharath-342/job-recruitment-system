package com.jobrecruitment.repository;

import com.jobrecruitment.entity.CompanySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanySourceRepository extends JpaRepository<CompanySource, Long> {

    List<CompanySource> findByEnabledTrue();

    Optional<CompanySource> findByProviderAndProviderIdentifier(String provider, String providerIdentifier);

    boolean existsByProviderAndProviderIdentifier(String provider, String providerIdentifier);
}
