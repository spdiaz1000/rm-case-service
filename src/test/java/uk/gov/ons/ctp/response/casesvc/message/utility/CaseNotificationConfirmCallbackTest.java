package uk.gov.ons.ctp.response.casesvc.message.utility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.support.CorrelationData;
import uk.gov.ons.ctp.response.casesvc.service.RollbackService;
import uk.gov.ons.ctp.response.casesvc.service.SupportService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class CaseNotificationConfirmCallbackTest {

  @Mock
  private RollbackService rollbackService;

  @Mock
  private SupportService supportService;

  @InjectMocks
  private CaseNotificationConfirmCallback caseNotificationConfirmCallback;

  @Test
  public void testConfirmAcktrue() {
    CorrelationData correlationData = new CorrelationData();
    String correlationDataId = "unitTest";
    correlationData.setId(correlationDataId);
    caseNotificationConfirmCallback.confirm(correlationData, true, null);

    Mockito.verify(rollbackService, Mockito.never()).caseNotificationPublish(eq(correlationDataId), eq(false));
    Mockito.verify(supportService, times(1)).removeFromDatabase(eq(correlationDataId));
  }

  @Test
  public void testConfirmAckFalse() {
    CorrelationData correlationData = new CorrelationData();
    String correlationDataId = "unitTest";
    correlationData.setId(correlationDataId);
    caseNotificationConfirmCallback.confirm(correlationData, false, null);

    Mockito.verify(rollbackService, times(1)).caseNotificationPublish(eq(correlationDataId), eq(false));
    Mockito.verify(supportService, Mockito.never()).removeFromDatabase(eq(correlationDataId));
  }
}
