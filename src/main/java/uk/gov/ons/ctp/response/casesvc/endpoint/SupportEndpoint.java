package uk.gov.ons.ctp.response.casesvc.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.ons.ctp.common.endpoint.CTPEndpoint;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.casesvc.domain.model.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.service.SupportService;

/**
 * The REST endpoint controller for CaseSvc Support tasks
 */
@RestController
@RequestMapping(value = "/support", produces = "application/json")
@Slf4j
public class SupportEndpoint implements CTPEndpoint {

  private static final String UNEXPECTED_MSG_TYPE = "Unexpected message type: %s";

  @Autowired
  private SupportService supportService;

  /**
   * To replay messages that are stored in the database because they failed reaching a RabbitMQ queue when initially
   * published.
   *
   * @param msgType the type of message to replay, ie CaseNotification or ?
   * @return the case found
   * @throws CTPException something went wrong
   */
  @RequestMapping(value = "/replay/{msgType}", method = RequestMethod.POST)
  public ResponseEntity<?> replayMessages(@PathVariable("msgType") final String msgType) throws CTPException {
    log.info("Entering replayMessages with msgType {}", msgType);

    // TODO We assume below that it is a CaseNotification type. Enough for now as this is the only tye of messages
    // TODO published by the CaseSvc. Make it generic in due course.
    if (msgType.equalsIgnoreCase(CaseNotification.class.getSimpleName())) {
      supportService.replay(msgType);
    } else {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, String.format(UNEXPECTED_MSG_TYPE, msgType));
    }

    return ResponseEntity.noContent().build();
  }
}
