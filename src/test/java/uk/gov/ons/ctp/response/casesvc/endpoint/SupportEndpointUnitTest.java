package uk.gov.ons.ctp.response.casesvc.endpoint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.casesvc.service.SupportService;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.casesvc.endpoint.SupportEndpoint.UNEXPECTED_MSG_TYPE;

/**
 * Support Endpoint Unit tests
 */
public class SupportEndpointUnitTest {

  private static final String CASE_NOTIFICATION = "CaseNotification";
  private static final String NON_EXISTING_MSG_TYPE = "SomeType";
  private static final String REPLAY_MSG = "replayMessages";
  private static final String RUNTIME_ERROR_MSG = "DB connection KO";

  @InjectMocks
  private SupportEndpoint supportEndpoint;

  @Mock
  private SupportService supportService;

  private MockMvc mockMvc;

  /**
   * Set up of tests
   * @throws Exception exception thrown
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
        .standaloneSetup(supportEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();
  }

  @Test
  public void replayMsgTypeNotFound() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/support/replayCaseNotification/%s", NON_EXISTING_MSG_TYPE), ""));

    actions.andExpect(status().isNotFound());
    actions.andExpect(handler().handlerType(SupportEndpoint.class));
    actions.andExpect(handler().methodName(REPLAY_MSG));
    actions.andExpect(jsonPath("$.error.code", is(CTPException.Fault.RESOURCE_NOT_FOUND.name())));
    actions.andExpect(jsonPath("$.error.message", is(String.format(UNEXPECTED_MSG_TYPE, NON_EXISTING_MSG_TYPE))));
    actions.andExpect(jsonPath("$.error.timestamp", isA(String.class)));

    verify(supportService, never()).replayCaseNotification();
  }

  @Test
  public void replayMsgTypeFound() throws Exception {
    ResultActions actions = mockMvc.perform(postJson(String.format("/support/replayCaseNotification/%s", CASE_NOTIFICATION), ""));

    actions.andExpect(status().is2xxSuccessful());
    actions.andExpect(handler().handlerType(SupportEndpoint.class));
    actions.andExpect(handler().methodName(REPLAY_MSG));

    verify(supportService, times(1)).replayCaseNotification();
  }

  @Test
  public void replayMsgTypeFoundButServiceBlowsUp() throws Exception {
    doThrow(new RuntimeException(RUNTIME_ERROR_MSG)).when(supportService).replayCaseNotification();

    ResultActions actions = mockMvc.perform(postJson(String.format("/support/replayCaseNotification/%s", CASE_NOTIFICATION), ""));

    actions.andExpect(status().is5xxServerError());
    actions.andExpect(handler().handlerType(SupportEndpoint.class));
    actions.andExpect(handler().methodName(REPLAY_MSG));
    actions.andExpect(jsonPath("$.error.code", is(CTPException.Fault.SYSTEM_ERROR.name())));
    actions.andExpect(jsonPath("$.error.message", is(RUNTIME_ERROR_MSG)));
    actions.andExpect(jsonPath("$.error.timestamp", isA(String.class)));


    verify(supportService, times(1)).replayCaseNotification();
  }
}
