package uk.gov.ons.ctp.response.casesvc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.ons.ctp.response.casesvc.domain.model.Case;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseRepository;
import uk.gov.ons.ctp.response.casesvc.representation.CaseState;
import uk.gov.ons.ctp.response.casesvc.service.RollbackService;

import java.util.UUID;

import static uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils.COMMA;
import static uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils.METHOD_SUPPORT_SERVICE_REPLAY;
import static uk.gov.ons.ctp.response.casesvc.scheduled.distribution.CaseDistributor.METHOD_CASE_DISTRIBUTOR_PROCESS_CASE;
import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.METHOD_CASE_SERVICE_CREATE_CASE_EVENT;

@Slf4j
@Service
public class RollbackServiceImpl implements RollbackService {

  public static final String UNEXPECTED_SITUATION_ERRRO_MSG =
      "Unexpected situation. Replay was triggered manually but RabbitMQ queue has been deleted.";

  @Autowired
  private CaseRepository caseRepo;

  @Autowired
  private CaseNotificationRepository caseNotificationRepository;

  @Override
  public void caseNotificationPublish(String correlationDataId, boolean msgReturned) {
    log.info("entering caseNotificationPublish with correlationDataId {}", correlationDataId);

    // Pause below is required to prevent exception 'Row was updated or deleted by another transaction'
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }

    String[] data = correlationDataId.split(COMMA);

    String methodName = data[0];
    log.info("methodName is {}", methodName);

    switch (methodName) {
      case METHOD_CASE_DISTRIBUTOR_PROCESS_CASE:
        String caseId = data[1];
        log.info("cazeId is {}", caseId);
        String caseStateToRevertTo = data[2];
        log.info("caseStateToRevertTo is {}", caseStateToRevertTo);

        Case caze = null;
        if (!StringUtils.isEmpty(caseId)) {
          caze = caseRepo.findById(UUID.fromString(caseId));
        }

        if (caze != null) {
          caze.setState(CaseState.valueOf(caseStateToRevertTo));
          caze.setIac(null);
          caseRepo.saveAndFlush(caze);
          log.info("caze now rolledback in db");
        } else {
          log.error("Unexpected situation. No case retrieved for id {}", caseId);
        }
        break;

      case METHOD_CASE_SERVICE_CREATE_CASE_EVENT:
        caseId = data[1];
        String actionPlanId = data[2];
        String notificationType = data[3];
        if (!StringUtils.isEmpty(caseId)
            && !StringUtils.isEmpty(actionPlanId)
            && !StringUtils.isEmpty(notificationType)) {
          CaseNotification caseNotification = CaseNotification.builder()
              .caseId(UUID.fromString(caseId))
              .actionPlanId(UUID.fromString(actionPlanId))
              .notificationType(notificationType)
              .build();
          caseNotificationRepository.saveAndFlush(caseNotification);
          log.info("case notification now stored in db - ready for replayCaseNotification");
        } else {
          log.error("Unexpected situation. caseId {} - actionPlanId {} - notificationType {}", caseId, actionPlanId,
              notificationType);
        }
        break;

      case METHOD_SUPPORT_SERVICE_REPLAY:
        if (msgReturned) {
          /**
           * scenario where the queue has been deleted. This should never happen because, at this stage, there has
           * already been a manual intervention to solve the RabbitMQ config and it is considered correct for a replayCaseNotification.
           *
           * We throw a RuntimeException here to ensure that CaseNotificationConfirmCallback.confirm is NOT played as
           * ack would be at true because the message reached the Exchange and we would remove the message from the DB.
           * This would be incorrect as the message has NOT reached the RabbitMQ queue.
           */
          log.error(UNEXPECTED_SITUATION_ERRRO_MSG);
          throw new RuntimeException(UNEXPECTED_SITUATION_ERRRO_MSG);
        } else {
          // we do nothing because we are trying to replayCaseNotification a message which is already stored in DB.
        }
        break;
    }
  }
}
