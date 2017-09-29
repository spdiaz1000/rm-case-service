package uk.gov.ons.ctp.response.casesvc.message.utility;

import uk.gov.ons.ctp.response.casesvc.message.notification.NotificationType;
import uk.gov.ons.ctp.response.casesvc.representation.CaseState;

import java.util.UUID;

public class CorrelationDataIdUtils {

  public static final String COMMA = ",";
  public static final String METHOD_CASE_DISTRIBUTOR_PROCESS_CASE = "processCase";
  public static final String METHOD_CASE_SERVICE_CREATE_CASE_EVENT = "createCaseEvent";

  public static String providerForCaseDistributor(UUID caseId, CaseState initialState) {
    StringBuffer correlationDataId = new StringBuffer(METHOD_CASE_DISTRIBUTOR_PROCESS_CASE);
    correlationDataId.append(COMMA);
    correlationDataId.append(caseId.toString());
    correlationDataId.append(COMMA);
    correlationDataId.append(initialState.toString());
    return correlationDataId.toString();
  }

  public static String providerForCaseService(String caseId, String actionPlanId, NotificationType notificationType) {
    StringBuffer correlationDataId = new StringBuffer(METHOD_CASE_SERVICE_CREATE_CASE_EVENT);
    correlationDataId.append(COMMA);
    correlationDataId.append(caseId);
    correlationDataId.append(COMMA);
    correlationDataId.append(actionPlanId);
    correlationDataId.append(COMMA);
    correlationDataId.append(notificationType.name());
    return correlationDataId.toString();
  }
}









