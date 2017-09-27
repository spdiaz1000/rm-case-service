package uk.gov.ons.ctp.response.casesvc.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseGroup;

import java.util.UUID;

/**
 * JPA Data Repository.
 */
@Repository
public interface CaseGroupRepository extends JpaRepository<CaseGroup, Integer> {
    /**
     * To find CaseGroup by UUID
     * @param id the UUID of the CaseGroup
     * @return the matching CaseGroup
     */
    CaseGroup findById(UUID id);
}
