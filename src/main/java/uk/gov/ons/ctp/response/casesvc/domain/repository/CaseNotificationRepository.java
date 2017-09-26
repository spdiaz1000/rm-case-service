package uk.gov.ons.ctp.response.casesvc.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;

/**
 * JPA Data Repository.
 */
@Repository
@Transactional(readOnly = true)
public interface CaseNotificationRepository extends JpaRepository<CaseNotification, Integer> {
}

