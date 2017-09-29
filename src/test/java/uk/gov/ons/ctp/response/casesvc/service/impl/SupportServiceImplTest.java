package uk.gov.ons.ctp.response.casesvc.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.domain.repository.CaseNotificationRepository;
import uk.gov.ons.ctp.response.casesvc.message.CaseNotificationPublisher;
import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.ons.ctp.response.casesvc.message.utility.CorrelationDataIdUtils.COMMA;

@RunWith(MockitoJUnitRunner.class)
public class SupportServiceImplTest {

  private static final String CASE_NOTIFICATION = "CaseNotification";
  private static final String DISABLED = "DISABLED";
  public static final String UNEXPECTED_METHOD_NAME = "someMethod";

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

    ArgumentCaptor<uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification> caseNotificationArgument =
        ArgumentCaptor.forClass(uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification.class);
    ArgumentCaptor<String> correlationDataIdArgument = ArgumentCaptor.forClass(String.class);
    verify(caseNotificationPublisher, times(2)).sendNotification(caseNotificationArgument.capture(), correlationDataIdArgument.capture());


    List<uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification> caseNotificationList =  caseNotificationArgument.getAllValues();
    assertEquals(2, caseNotificationList.size());

    uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification caseNotificationMsg1 = caseNotificationList.get(0);
    assertEquals(CASE_ID_1.toString(), caseNotificationMsg1.getCaseId());
    assertEquals(ACTIONPLAN_ID_1.toString(), caseNotificationMsg1.getActionPlanId());
    assertEquals(NotificationType.valueOf(DISABLED), caseNotificationMsg1.getNotificationType());
    uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification caseNotificationMsg2 = caseNotificationList.get(1);
    assertEquals(CASE_ID_2.toString(), caseNotificationMsg2.getCaseId());
    assertEquals(ACTIONPLAN_ID_2.toString(), caseNotificationMsg2.getActionPlanId());
    assertEquals(NotificationType.valueOf(DISABLED), caseNotificationMsg2.getNotificationType());

    List<String> correlationdataIdList = correlationDataIdArgument.getAllValues();
    assertEquals(2, correlationdataIdList.size());

    String correlationDataId1 = CorrelationDataIdUtils.providerForSupportService(1);
    String correlationDataId2 = CorrelationDataIdUtils.providerForSupportService(2);
    assertThat(correlationdataIdList, containsInAnyOrder(correlationDataId1, correlationDataId2));
  }

  @Test
  public void removeFromDatabaseUnexpectedMethodName() {
    StringBuffer correlationDataId1 = new StringBuffer(UNEXPECTED_METHOD_NAME);
    correlationDataId1.append(COMMA);
    correlationDataId1.append("1");
    supportService.removeFromDatabase(correlationDataId1.toString());

    verify(caseNotificationRepository, never()).findOne(any(Integer.class));
    verify(caseNotificationRepository, never()).delete(any(Integer.class));
  }

  @Test
  public void removeFromDatabaseExpectedMethodNameButNoCaseNotificationFound() {
    when(caseNotificationRepository.findOne(any(Integer.class))).thenReturn(null);

    String correlationDataId = CorrelationDataIdUtils.providerForSupportService(1);
    supportService.removeFromDatabase(correlationDataId);

    verify(caseNotificationRepository, times(1)).findOne(any(Integer.class));
    verify(caseNotificationRepository, never()).delete(any(Integer.class));
  }

  @Test
  public void removeFromDatabaseExpectedMethodNameAndCaseNotificationFound() {
    CaseNotification caseNotification = CaseNotification.builder()
        .caseId(CASE_ID_1)
        .actionPlanId(ACTIONPLAN_ID_1)
        .notificationType(DISABLED)
        .build();
    when(caseNotificationRepository.findOne(any(Integer.class))).thenReturn(caseNotification);

    String correlationDataId = CorrelationDataIdUtils.providerForSupportService(1);
    supportService.removeFromDatabase(correlationDataId);

    verify(caseNotificationRepository, times(1)).findOne(any(Integer.class));
    verify(caseNotificationRepository, times(1)).delete(eq(1));
  }
}
