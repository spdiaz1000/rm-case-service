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
import uk.gov.ons.ctp.response.casesvc.representation.CaseState;

import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.ons.ctp.response.casesvc.scheduled.distribution.CaseDistributor.METHOD_CASE_DISTRIBUTOR_PROCESS_CASE;
import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.COMMA;
import static uk.gov.ons.ctp.response.casesvc.service.impl.RollbackServiceImpl.UNEXPECTED_SITUATION_ERRRO_MSG;
import static uk.gov.ons.ctp.response.casesvc.service.impl.SupportServiceImpl.METHOD_SUPPORT_SERVICE_REPLAY;

@RunWith(MockitoJUnitRunner.class)
public class RollbackServiceImplTest {

  private static final String CASE_ID_1 ="551308fb-2d5a-4477-92c3-649d915834c1";

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
    StringBuffer correlationDataId = new StringBuffer(METHOD_SUPPORT_SERVICE_REPLAY);
    correlationDataId.append(COMMA);
    correlationDataId.append("1");
    rollbackService.caseNotificationPublish(correlationDataId.toString(), false);

    verify(caseRepo, never()).findById(any(UUID.class));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodSupportServiceReplayMsgReturned() {
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

  @Test
  public void testCaseNotificationPublishMethodCaseDistributorProcessCaseNoCaseFound() {
    StringBuffer correlationDataId = new StringBuffer(METHOD_CASE_DISTRIBUTOR_PROCESS_CASE);
    correlationDataId.append(COMMA);
    correlationDataId.append(CASE_ID_1);
    correlationDataId.append(COMMA);
    correlationDataId.append(CaseState.ACTIONABLE.name());
    rollbackService.caseNotificationPublish(correlationDataId.toString(), false);

    verify(caseRepo, times(1)).findById(eq(UUID.fromString(CASE_ID_1)));
    verify(caseRepo, never()).saveAndFlush(any(Case.class));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }

  @Test
  public void testCaseNotificationPublishMethodCaseDistributorProcessCas() {
    UUID caseId = UUID.fromString(CASE_ID_1);
    Case caze = Case.builder().state(CaseState.ACTIONABLE).iac("ABCD EFGH IJKL MNOP").build();
    when(caseRepo.findById(caseId)).thenReturn(caze);

    StringBuffer correlationDataId = new StringBuffer(METHOD_CASE_DISTRIBUTOR_PROCESS_CASE);
    correlationDataId.append(COMMA);
    correlationDataId.append(CASE_ID_1);
    correlationDataId.append(COMMA);
    correlationDataId.append(CaseState.ACTIONABLE.name());
    rollbackService.caseNotificationPublish(correlationDataId.toString(), false);

    verify(caseRepo, times(1)).findById(eq(caseId));
    caze.setIac(null);
    caze.setState(CaseState.ACTIONABLE);
    verify(caseRepo, times(1)).saveAndFlush(eq(caze));
    verify(caseNotificationRepository, never()).saveAndFlush(any(CaseNotification.class));
  }
}
