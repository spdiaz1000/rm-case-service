package uk.gov.ons.ctp.response.casesvc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.message.CaseNotificationPublisher;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.service.SupportService;

import java.util.List;

import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.COMMA;

@Slf4j
@Service
public class SupportServiceImpl implements SupportService {

  public static final String METHOD_SUPPORT_SERVICE_REPLAY = "replay";

  @Autowired
  private CaseNotificationRepository caseNotificationRepository;

  @Autowired
  private CaseNotificationPublisher caseNotificationPublisher;

  // TODO We assume below that it is a CaseNotification type. Enough for now as this is the only tye of messages
  // TODO published by the CaseSvc. Make it generic in due course (msgType to be added to correlationDataId).
  @Override
  public void replay(String msgType) {
    List<CaseNotification> caseNotifications = caseNotificationRepository.findAll();

    StringBuffer correlationDataId = null;
    for (CaseNotification caseNotification : caseNotifications) {
      correlationDataId = new StringBuffer(METHOD_SUPPORT_SERVICE_REPLAY);
      correlationDataId.append(COMMA);
      correlationDataId.append(caseNotification.getCaseNotificationPK());

      caseNotificationPublisher.sendNotification(prepareCaseNotification(caseNotification),
          correlationDataId.toString());
    }
  }

  // TODO We assume below that it is a CaseNotification type. Enough for now as this is the only tye of messages
  // TODO published by the CaseSvc. Make it generic in due course (msgType to be added to correlationDataId).
  @Override
  public void removeFromDatabase(String correlationDataId) {
    String[] data = correlationDataId.split(COMMA);
    String methodName = data[0];
    log.info("methodName is {}", methodName);
    String primaryKey = data[2];
    log.info("primaryKey is {}", primaryKey);

    int primaryKeyValue = new Integer(primaryKey);
    CaseNotification caseNotification = caseNotificationRepository.findOne(primaryKeyValue);
    if (caseNotification != null) {
      caseNotificationRepository.delete(primaryKeyValue);
      log.info("caseNotification with primary key {} now deleted", primaryKeyValue);
    } else {
      log.error("unexpected situation. No caseNotification found with primary key {}", primaryKeyValue);
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
