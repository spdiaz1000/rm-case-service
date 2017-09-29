package uk.gov.ons.ctp.response.casesvc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.message.CaseNotificationPublisher;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils;
import uk.gov.ons.ctp.response.casesvc.service.SupportService;

import java.util.List;

import static uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils.COMMA;
import static uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils.METHOD_SUPPORT_SERVICE_REPLAY;

@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

  @Autowired
  private CaseNotificationRepository caseNotificationRepository;

  @Autowired
  private CaseNotificationPublisher caseNotificationPublisher;

  @Override
  public void replayCaseNotification() {
    List<CaseNotification> caseNotifications = caseNotificationRepository.findAll();

    for (CaseNotification caseNotification : caseNotifications) {
      caseNotificationPublisher.sendNotification(prepareCaseNotification(caseNotification),
          CorrelationDataIdUtils.providerForSupportService(caseNotification.getCaseNotificationPK()));
    }
  }

  @Override
  public void removeCaseNotificationFromDatabase(String correlationDataId) {
    String[] data = correlationDataId.split(COMMA);
    String methodName = data[0];
    log.info("methodName is {}", methodName);
    String primaryKey = data[1];
    log.info("primaryKey is {}", primaryKey);

    if (methodName.equalsIgnoreCase(METHOD_SUPPORT_SERVICE_REPLAY)) {
      int primaryKeyValue = new Integer(primaryKey);
      CaseNotification caseNotification = caseNotificationRepository.findOne(primaryKeyValue);
      if (caseNotification != null) {
        caseNotificationRepository.delete(primaryKeyValue);
        log.info("caseNotification with primary key {} now deleted", primaryKeyValue);
      } else {
        log.error("unexpected situation. No caseNotification found with primary key {}", primaryKeyValue);
      }
    }
  }

  /**
   * To go from the domain object to the message object for a CaseNotification
   *
   * @param caseNotification the domain object sourced from database
   * @return the message object
   */
  private uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification prepareCaseNotification(
      CaseNotification caseNotification) {
    return new uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification(
        caseNotification.getCaseId().toString(),
        caseNotification.getActionPlanId().toString(),
        NotificationType.valueOf(caseNotification.getNotificationType()));
  }
}
