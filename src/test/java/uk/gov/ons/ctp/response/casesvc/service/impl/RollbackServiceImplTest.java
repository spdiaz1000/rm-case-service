package uk.gov.ons.ctp.response.casesvc.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.casesvc.domain.model.Case;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseRepository;

import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.COMMA;
import static uk.gov.ons.ctp.response.casesvc.service.impl.RollbackServiceImpl.UNEXPECTED_SITUATION_ERRRO_MSG;
import static uk.gov.ons.ctp.response.casesvc.service.impl.SupportServiceImpl.METHOD_SUPPORT_SERVICE_REPLAY;

@RunWith(MockitoJUnitRunner.class)
public class RollbackServiceImplTest {

  @Mock
  private CaseRepository caseRepo;

  @Mock
  private CaseNotificationRepository caseNotificationRepository;

  @InjectMocks
  private RollbackServiceImpl rollbackService;

  @Test
  public void testCaseNotificationPublishInvalidCorrelationDataId() {
    rollbackService.caseNotificationPublish("someRandom string - ", false);

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodReplayMsgNotReturned() {
    StringBuffer correlationDataId = new StringBuffer(METHOD_SUPPORT_SERVICE_REPLAY);
    correlationDataId.append(COMMA);
    correlationDataId.append("1");
    rollbackService.caseNotificationPublish(correlationDataId.toString(), false);

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodReplayMsgReturned() {
    StringBuffer correlationDataId = new StringBuffer(METHOD_SUPPORT_SERVICE_REPLAY);
    correlationDataId.append(COMMA);
    correlationDataId.append("1");
    try {
      rollbackService.caseNotificationPublish(correlationDataId.toString(), true);
      fail();
    } catch (RuntimeException e) {
      assertEquals(UNEXPECTED_SITUATION_ERRRO_MSG, e.getMessage());
    }

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }
}
