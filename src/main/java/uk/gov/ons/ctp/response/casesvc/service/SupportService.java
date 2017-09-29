package uk.gov.ons.ctp.response.casesvc.service;

/**
 * The service dedicated to Support tasks.
 */
public interface SupportService {

  /**
   * To replay CaseNotification messages that are currently stored in the database.
   */
  void replayCaseNotification();

  /**
   * To remove from the database CaseNotification messages that have been successfully published on a RabbitMQ queue
   *
   * @param correlationDataId the data required to identify successful messages
   */
  void removeCaseNotificationFromDatabase(String correlationDataId);
}
