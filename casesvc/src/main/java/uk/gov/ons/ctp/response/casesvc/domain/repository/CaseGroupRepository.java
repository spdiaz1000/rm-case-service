package uk.gov.ons.ctp.response.casesvc.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.gov.ons.ctp.response.casesvc.domain.model.CaseGroup;

/**
 * JPA Data Repository.
 */
@Repository
public interface CaseGroupRepository extends JpaRepository<CaseGroup, Integer> {
    CaseGroup findById(UUID id);
}
