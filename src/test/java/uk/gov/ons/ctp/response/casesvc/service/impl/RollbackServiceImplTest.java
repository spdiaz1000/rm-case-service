package uk.gov.ons.ctp.response.casesvc.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.casesvc.domain.model.Case;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseRepository;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils;
import uk.gov.ons.ctp.response.casesvc.representation.CaseState;

import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.ons.ctp.response.casesvc.service.impl.RollbackServiceImpl.UNEXPECTED_SITUATION_ERRRO_MSG;

@RunWith(MockitoJUnitRunner.class)
public class RollbackServiceImplTest {

  private static final String CASE_ID_1 = "551308fb-2d5a-4477-92c3-649d915834c1";
  private static final String ACTION_PLAN_ID_1 = "551308fb-2d5a-4477-92c3-649d915834c2";

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
  public void testCaseNotificationPublishMethodSupportServiceReplayMsgNotReturned() {
    String correlationDataId = CorrelationDataIdUtils.providerForSupportService(1);
    rollbackService.caseNotificationPublish(correlationDataId, false);

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodSupportServiceReplayMsgReturned() {
    String correlationDataId = CorrelationDataIdUtils.providerForSupportService(1);
    try {
      rollbackService.caseNotificationPublish(correlationDataId, true);
      fail();
    } catch (RuntimeException e) {
      assertEquals(UNEXPECTED_SITUATION_ERRRO_MSG, e.getMessage());
    }

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodCaseDistributorProcessCaseNoCaseFound() {
    String correlationDataId = CorrelationDataIdUtils
        .providerForCaseDistributor(UUID.fromString(CASE_ID_1), CaseState.ACTIONABLE);
    rollbackService.caseNotificationPublish(correlationDataId, false);

    verify(caseRepo, times(1)).findById(eq(UUID.fromString(CASE_ID_1)));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodCaseDistributorProcessCase() {
    UUID caseId = UUID.fromString(CASE_ID_1);
    Case caze = Case.builder().state(CaseState.ACTIONABLE).iac("ABCD EFGH IJKL MNOP").build();
    when(caseRepo.findById(caseId)).thenReturn(caze);

    String correlationDataId = CorrelationDataIdUtils
        .providerForCaseDistributor(UUID.fromString(CASE_ID_1), CaseState.ACTIONABLE);
    rollbackService.caseNotificationPublish(correlationDataId, false);

    verify(caseRepo, times(1)).findById(eq(caseId));
    caze.setIac(null);
    caze.setState(CaseState.ACTIONABLE);
    verify(caseRepo, times(1)).saveAndFlush(eq(caze));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodCaseServiceCreateEvent() {
    String correlationDataId = CorrelationDataIdUtils.providerForCaseService(CASE_ID_1, ACTION_PLAN_ID_1, NotificationType.DISABLED);
    rollbackService.caseNotificationPublish(correlationDataId, false);

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));

    ArgumentCaptor<CaseNotification> caseNotificationArgument = ArgumentCaptor.forClass(CaseNotification.class);
    verify(caseNotificationRepository, times(1)).saveAndFlush(caseNotificationArgument.capture());
    CaseNotification caseNotification = caseNotificationArgument.getValue();
    assertEquals(UUID.fromString(CASE_ID_1), caseNotification.getCaseId());
    assertEquals(UUID.fromString(ACTION_PLAN_ID_1), caseNotification.getActionPlanId());
    assertEquals(NotificationType.DISABLED.name(), caseNotification.getNotificationType());
  }
}
