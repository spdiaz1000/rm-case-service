package uk.gov.ons.ctp.response.casesvc.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static uk.gov.ons.ctp.response.casesvc.utility.BespokeConfirmCallback.DELIVERY_FAILURE_MSG;

@Slf4j
@Component
public class BespokeReturnCallback implements RabbitTemplate.ReturnCallback {

  @Override
  public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
    log.info("returning message with replyCode {} - replyText {} - exchange {} - routingKey {}", replyCode, replyText,
        exchange, routingKey);
    String errorMsg = String.format(DELIVERY_FAILURE_MSG, replyText);
    log.error(errorMsg);

    // TODO Rollback what has been done prior to publishing to queue or throw exception?
  }
}