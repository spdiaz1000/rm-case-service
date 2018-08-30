package uk.gov.ons.ctp.response.casesvc.message.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;

@MessageEndpoint
public class EventPublisherImpl {
  private static final Logger log = LoggerFactory.getLogger(EventPublisherImpl.class);

  @Qualifier("amqpTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  public void publishEvent(String event) {
    log.with("event", event).debug("Publish Event");
    rabbitTemplate.convertAndSend(event);
  }
}
