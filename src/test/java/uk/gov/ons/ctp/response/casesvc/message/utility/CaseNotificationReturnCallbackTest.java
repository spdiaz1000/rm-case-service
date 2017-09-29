package uk.gov.ons.ctp.response.casesvc.message.utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uk.gov.ons.ctp.response.casesvc.service.RollbackService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static uk.gov.ons.ctp.response.casesvc.message.impl.CaseNotificationPublisherImpl.HEADER_USED_TO_ROLLBACK;

@RunWith(MockitoJUnitRunner.class)
public class CaseNotificationReturnCallbackTest {

  @Mock
  private RollbackService rollbackService;

  @InjectMocks
  private CaseNotificationReturnCallback caseNotificationReturnCallback;

  @Test
  public void testReturnedMessage() {
    MessageProperties messageProps = new MessageProperties();
    String correlationIdString = "unitTest";
    messageProps.setHeader(HEADER_USED_TO_ROLLBACK, correlationIdString);
    Message message = new Message(null, messageProps);
    caseNotificationReturnCallback.returnedMessage(message, 1, "replyText", "exchange", "routingKey");

    Mockito.verify(rollbackService, times(1)).caseNotificationPublish(eq(correlationIdString), eq(true));
  }
}
