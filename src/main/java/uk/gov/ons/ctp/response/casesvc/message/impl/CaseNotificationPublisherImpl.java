package uk.gov.ons.ctp.response.casesvc.message.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.MessageEndpoint;
import uk.gov.ons.ctp.response.casesvc.message.CaseNotificationPublisher;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;

/**
 * Service implementation responsible for publishing case lifecycle events to notification channel
 */
@MessageEndpoint
public class CaseNotificationPublisherImpl implements CaseNotificationPublisher {
  private static final Logger log = LoggerFactory.getLogger(CaseNotificationPublisherImpl.class);

  @Qualifier("caseNotificationRabbitTemplate")
  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void sendNotification(CaseNotification caseNotification) {
    log.with("case_notification", caseNotification).debug("Entering sendNotification");
    rabbitTemplate.convertAndSend(caseNotification);
  }
}
