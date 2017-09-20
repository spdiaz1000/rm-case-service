package uk.gov.ons.ctp.response.casesvc.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BespokeConfirmCallback implements RabbitTemplate.ConfirmCallback {

  public final static String DELIVERY_FAILURE_MSG = "Delivery of message to queue failed. Cause is %s.";

  @Override
  public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    log.info("confirming message with ack {} for id {}", ack, correlationData.getId());
    if (!ack) {
      String errorMsg = String.format(DELIVERY_FAILURE_MSG, cause);
      log.error(errorMsg);

      // TODO Rollback what has been done prior to publishing to queue
    }
  }
}
