package uk.gov.ons.ctp.response.casesvc.service;

/**
 * The service dedicated to Support tasks.
 */
public interface SupportService {

  /**
   * To replay messages of the given type that are currently stored in the database.
   *
   * @param msgType the type of Messages to replay
   */
  void replay(String msgType);

  /**
   * To remove from the database messages that have been successfully published on a RabbitMQ queue
   *
   * @param correlationDataId the data required to identify successful messages
   */
  void removeFromDatabase(String correlationDataId);
}
