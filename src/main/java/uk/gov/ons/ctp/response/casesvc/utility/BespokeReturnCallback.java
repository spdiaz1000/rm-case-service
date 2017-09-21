package uk.gov.ons.ctp.response.casesvc.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.casesvc.message.notification.CaseNotification;
import uk.gov.ons.ctp.response.casesvc.service.CaseService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.StringReader;

import static uk.gov.ons.ctp.response.casesvc.utility.BespokeConfirmCallback.DELIVERY_FAILURE_MSG;

@Slf4j
@Component
public class BespokeReturnCallback implements RabbitTemplate.ReturnCallback {

  @Autowired
  private CaseService caseService;

  @Override
  public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
    log.info("returning message {} with replyCode {} - replyText {} - exchange {} - routingKey {}", message, replyCode,
        replyText, exchange, routingKey);
    String errorMsg = String.format(DELIVERY_FAILURE_MSG, replyText);
    log.error(errorMsg);

    String bodyMessageString = new String(message.getBody());
    log.info("bodyMessageString is {}", bodyMessageString);

    MessageProperties messageProps = message.getMessageProperties();
    String correlationIdString = messageProps.getCorrelationIdString();
    // TODO correlationIdString is null at the moment
    log.info("correlationIdString is {}", correlationIdString);

//    // TODO reuse our existing Unmarshaller
//    CaseNotification caseNotification = null;
//    try {
//      JAXBContext jaxbContext = JAXBContext.newInstance(CaseNotification.class);
//
//      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//      StringReader reader = new StringReader(bodyMessageString);
//      caseNotification = (CaseNotification) jaxbUnmarshaller.unmarshal(reader);
//    } catch (JAXBException e) {
//
//    }

    // TODO Get correlationId
    caseService.rollbackTestTransactionalBehaviour(correlationIdString);
  }
}