package uk.gov.ons.ctp.response.casesvc.service;

/**
 * The service dedicated to actions required when a published message fails to reach a RabbitMQ queue. This
 * occurs when an exchange or a queue is badly configured/deleted.
 */
public interface RollbackService {

  /**
   * To rollback actions taken prior to the publish of CaseNotification using NotificationPublisher
   * or
   * To store failed messages to DB
   *
   * This is used when a message does not reach the queue because the exchange is incorrectly set up, the queue has been
   * deleted or?
   *
   * @param correlationDataId the data required to rollback or store to DB
   * @param msgReturned true if the message was returned (ie case where the queue was deleted)
   */
  void caseNotificationPublish(String correlationDataId, boolean msgReturned);
}
