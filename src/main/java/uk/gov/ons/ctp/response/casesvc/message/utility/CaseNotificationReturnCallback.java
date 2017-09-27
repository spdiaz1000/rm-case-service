package uk.gov.ons.ctp.response.casesvc.message.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.casesvc.service.RollbackService;

import static uk.gov.ons.ctp.response.casesvc.message.impl.CaseNotificationPublisherImpl.HEADER_USED_TO_ROLLBACK;
import static uk.gov.ons.ctp.response.casesvc.message.utility.CaseNotificationConfirmCallback.DELIVERY_FAILURE_MSG;

/**
 * Utility class to deal with Publisher Returns for CaseNotifications
 */
@Slf4j
@Component
public class CaseNotificationReturnCallback implements RabbitTemplate.ReturnCallback {

  @Autowired
  private RollbackService rollbackService;

  @Override
  public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
    log.info("returning message {} with replyCode {} - replyText {} - exchange {} - routingKey {}", message, replyCode,
        replyText, exchange, routingKey);

    String errorMsg = String.format(DELIVERY_FAILURE_MSG, replyText);
    log.error(errorMsg);

    MessageProperties messageProps = message.getMessageProperties();
    String correlationIdString = (String)messageProps.getHeaders().get(HEADER_USED_TO_ROLLBACK);
    log.info("correlationIdString is {}", correlationIdString);

    rollbackService.caseNotificationPublish(correlationIdString, true);
  }
}