package uk.gov.ons.ctp.response.casesvc.service;

public interface RollbackService {

  /**
   * To rollback actions taken prior to the publish of CaseNotification using NotificationPublisher. This is used when
   * a message does not reach the queue because the exchange is incorrectly set up, the queue has been deleted or?
   *
   * @param correlationDataId a string made up of method name where NotificationPublisher was called,caseId,previousState
   */
  void caseNotificationPublish(String correlationDataId);
}
