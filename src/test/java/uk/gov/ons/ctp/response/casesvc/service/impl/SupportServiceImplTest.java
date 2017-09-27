package uk.gov.ons.ctp.response.casesvc.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.message.CaseNotificationPublisher;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.ons.ctp.response.casesvc.service.impl.CaseServiceImpl.COMMA;
import static uk.gov.ons.ctp.response.casesvc.service.impl.SupportServiceImpl.METHOD_SUPPORT_SERVICE_REPLAY;

@RunWith(MockitoJUnitRunner.class)
public class SupportServiceImplTest {

  private static final String CASE_NOTIFICATION = "CaseNotification";
  private static final String DISABLED = "DISABLED";

  private static final UUID CASE_ID_1 = UUID.fromString("551308fb-2d5a-4477-92c3-649d915834c1");
  private static final UUID CASE_ID_2 = UUID.fromString("551308fb-2d5a-4477-92c3-649d915834c2");
  private static final UUID ACTIONPLAN_ID_1 = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a1");
  private static final UUID ACTIONPLAN_ID_2 = UUID.fromString("e71002ac-3575-47eb-b87f-cd9db92bf9a2");

  @Mock
  private CaseNotificationRepository caseNotificationRepository;

  @Mock
  private CaseNotificationPublisher caseNotificationPublisher;

  @InjectMocks
  private SupportServiceImpl supportService;

  @Test
  public void testReplayWhenNoMsgFound() {
    List<CaseNotification> emptyList = new ArrayList<>();
    when(caseNotificationRepository.findAll()).thenReturn(emptyList);

    supportService.replay(CASE_NOTIFICATION);

    verify(caseNotificationPublisher, never()).sendNotification(any(uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification.class), any(String.class));
  }

  @Test
  public void testReplayWhenMsgsFound() {
    List<CaseNotification> msgList = new ArrayList<>();
    msgList.add(CaseNotification.builder().caseNotificationPK(1).caseId(CASE_ID_1).actionPlanId(ACTIONPLAN_ID_1).notificationType(DISABLED).build());
    msgList.add(CaseNotification.builder().caseNotificationPK(2).caseId(CASE_ID_2).actionPlanId(ACTIONPLAN_ID_2).notificationType(DISABLED).build());
    when(caseNotificationRepository.findAll()).thenReturn(msgList);

    supportService.replay(CASE_NOTIFICATION);

    uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification caseNotificationMsg1 = new uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification(
        CASE_ID_1.toString(),
        ACTIONPLAN_ID_1.toString(),
        NotificationType.valueOf(DISABLED));
    StringBuffer correlationDataId = new StringBuffer(METHOD_SUPPORT_SERVICE_REPLAY);
    correlationDataId.append(COMMA);
    correlationDataId.append("1");

    ArgumentCaptor<uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification> caseNotificationArgument = ArgumentCaptor.forClass(uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification.class);
    ArgumentCaptor<String> correlationDataIdArgument = ArgumentCaptor.forClass(String.class);
    verify(caseNotificationPublisher, times(2)).sendNotification(caseNotificationArgument.capture(), correlationDataIdArgument.capture());

    // TODO assertions on what has been captured
  }
}
