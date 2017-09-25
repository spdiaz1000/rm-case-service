package uk.gov.ons.ctp.response.casesvc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.ons.ctp.response.casesvc.domain.model.Case;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseRepository;
import uk.gov.ons.ctp.response.casesvc.representation.CaseDTO;
import uk.gov.ons.ctp.response.casesvc.service.RollbackService;

import java.util.UUID;

import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.METHOD_CASE_DISTRIBUTOR_PROCESS_CASE;
import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.METHOD_CASE_SERVICE_TEST_TRANSACTIONAL_BEHAVIOUR;

@Slf4j
@Service
public class RollbackServiceImpl implements RollbackService {

  @Autowired
  private CaseRepository caseRepo;

  @Override
  public void caseNotificationPublish(String correlationDataId) {
    // Pause below is required to prevent exception 'Row was updated or deleted by another transaction'
    log.info("entering rollbackForNotificationPublisher with correlationDataId {}", correlationDataId);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }

    String[] data = correlationDataId.split(",");
    String methodName = data[0];
    log.info("methodName is {}", methodName);
    String caseId = data[1];
    log.info("caseId is {}", caseId);
    String caseStateToRevertTo = data[2];
    log.info("caseStateToRevertTo is {}", caseStateToRevertTo);

    Case caze = null;
    if (!StringUtils.isEmpty(caseId)) {
      caze = caseRepo.findById(UUID.fromString(caseId));
    }

    if (caze != null) {
      switch (methodName) {
      case METHOD_CASE_SERVICE_TEST_TRANSACTIONAL_BEHAVIOUR:
        caze.setState(CaseDTO.CaseState.valueOf(caseStateToRevertTo));
        caseRepo.saveAndFlush(caze);
        log.info("case now rolledback in db");
        break;
      case METHOD_CASE_DISTRIBUTOR_PROCESS_CASE:
        caze.setState(CaseDTO.CaseState.valueOf(caseStateToRevertTo));
        caze.setIac(null);
        caseRepo.saveAndFlush(caze);
        break;
      }
    } else {
      log.error("Unexpected situation. No case retrieved for id {}", caseId);
    }
  }
}
