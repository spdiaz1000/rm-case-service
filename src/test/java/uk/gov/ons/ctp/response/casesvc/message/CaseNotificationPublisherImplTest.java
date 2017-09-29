package uk.gov.ons.ctp.response.casesvc.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import uk.gov.ons.ctp.response.casesvc.message.impl.CaseNotificationPublisherImpl;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils;
import uk.gov.ons.ctp.response.casesvc.representation.CaseState;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static uk.gov.ons.ctp.response.casesvc.message.impl.CaseNotificationPublisherImpl.LIFECYCLE_EVENTS_ROUTING_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseNotificationPublisherImplTest {

  private static final String CASE_ID_1 = "551308fb-2d5a-4477-92c3-649d915834c1";
  private static final String ACTION_PLAN_ID_1 = "551308fb-2d5a-4477-92c3-649d915834c2";

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private CaseNotificationPublisherImpl caseNotificationPublisher;

  @Test
  public void testSendNotification() {
    CaseNotification caseNotification = CaseNotification.builder()
        .withCaseId(CASE_ID_1)
        .withActionPlanId(ACTION_PLAN_ID_1)
        .withNotificationType(NotificationType.DISABLED)
        .build();

    String correlationDataId = CorrelationDataIdUtils.providerForCaseDistributor(UUID.fromString(CASE_ID_1), CaseState.ACTIONABLE);
    caseNotificationPublisher.sendNotification(caseNotification, correlationDataId);

    ArgumentCaptor<Object> messageArgument = ArgumentCaptor.forClass(Object.class);
    ArgumentCaptor<CorrelationData> correlationDataIdArgument = ArgumentCaptor.forClass(CorrelationData.class);
    Mockito.verify(rabbitTemplate, times(1)).convertAndSend(eq(LIFECYCLE_EVENTS_ROUTING_KEY),
        messageArgument.capture(), any(MessagePostProcessor.class), correlationDataIdArgument.capture());

    CorrelationData correlationData = correlationDataIdArgument.getValue();
    assertEquals(correlationDataId.toString(), correlationData.getId());

    CaseNotification caseNotificationSent = (CaseNotification)messageArgument.getValue();
    assertEquals(caseNotificationSent.getCaseId(), caseNotification.getCaseId());
    assertEquals(caseNotificationSent.getActionPlanId(), caseNotification.getActionPlanId());
    assertEquals(caseNotificationSent.getNotificationType(), caseNotification.getNotificationType());
  }
}
