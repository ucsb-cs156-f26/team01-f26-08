package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockitoBean HelpRequestRepository helpRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for GET /api/helprequests/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk());
  }

  // Authorization tests for POST /api/helprequests/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "Table 7")
                .param("requestTime", "2026-10-21T12:30:00")
                .param("explanation", "Need help with Git")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "Table 7")
                .param("requestTime", "2026-10-21T12:30:00")
                .param("explanation", "Need help with Git")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  // Tests with mocked database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {
    // arrange
    HelpRequest request1 =
        HelpRequest.builder()
            .id(1L)
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 7")
            .requestTime(LocalDateTime.parse("2026-10-21T12:30:00"))
            .explanation("Need help with Git")
            .solved(false)
            .build();

    HelpRequest request2 =
        HelpRequest.builder()
            .id(2L)
            .requesterEmail("student2@ucsb.edu")
            .teamId("team02")
            .tableOrBreakoutRoom("Room 3")
            .requestTime(LocalDateTime.parse("2026-10-21T13:00:00"))
            .explanation("Need help with Spring Boot")
            .solved(true)
            .build();

    var expectedRequests = Arrays.asList(request1, request2);
    when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_help_request() throws Exception {
    // arrange
    LocalDateTime requestTime = LocalDateTime.parse("2026-10-21T12:30:00");

    HelpRequest incomingRequest =
        HelpRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 7")
            .requestTime(requestTime)
            .explanation("Need help with Git")
            .solved(true)
            .build();

    HelpRequest savedRequest =
        HelpRequest.builder()
            .id(17L)
            .requesterEmail("student@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("Table 7")
            .requestTime(requestTime)
            .explanation("Need help with Git")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(incomingRequest))).thenReturn(savedRequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequests/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("teamId", "team01")
                    .param("tableOrBreakoutRoom", "Table 7")
                    .param("requestTime", "2026-10-21T12:30:00")
                    .param("explanation", "Need help with Git")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).save(eq(incomingRequest));
    String expectedJson = mapper.writeValueAsString(savedRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/helprequests").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

    // arrange
    LocalDateTime ldt = LocalDateTime.parse("2026-10-21T12:30:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .teamId("8")
            .tableOrBreakoutRoom("Table 1")
            .requestTime(ldt)
            .explanation("Need help with Git")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_help_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T12:30:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2026-10-22T13:30:00");

    HelpRequest helpRequestOrig =
        HelpRequest.builder()
            .id(67L)
            .requesterEmail("student1@ucsb.edu")
            .teamId("8")
            .tableOrBreakoutRoom("Table 1")
            .requestTime(ldt1)
            .explanation("Need help with Git")
            .solved(false)
            .build();

    HelpRequest helpRequestEdited =
        HelpRequest.builder()
            .id(67L)
            .requesterEmail("student2@ucsb.edu")
            .teamId("9")
            .tableOrBreakoutRoom("Table 2")
            .requestTime(ldt2)
            .explanation("Need help with Java")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(helpRequestEdited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(helpRequestOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);
    verify(helpRequestRepository, times(1)).save(helpRequestEdited);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);

    // Verify every attribute was updated
    assertEquals(67L, helpRequestOrig.getId());
    assertEquals("student2@ucsb.edu", helpRequestOrig.getRequesterEmail());
    assertEquals("9", helpRequestOrig.getTeamId());
    assertEquals("Table 2", helpRequestOrig.getTableOrBreakoutRoom());
    assertEquals(ldt2, helpRequestOrig.getRequestTime());
    assertEquals("Need help with Java", helpRequestOrig.getExplanation());
    assertEquals(true, helpRequestOrig.getSolved());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_help_request_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T12:30:00");

    HelpRequest helpRequestEdited =
        HelpRequest.builder()
            .requesterEmail("student@ucsb.edu")
            .teamId("8")
            .tableOrBreakoutRoom("Table 1")
            .requestTime(ldt1)
            .explanation("Need help with Git")
            .solved(false)
            .build();

    String requestBody = mapper.writeValueAsString(helpRequestEdited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/helprequests")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 67 not found", json.get("message"));
  }
}
