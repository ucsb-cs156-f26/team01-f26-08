package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestsControllerTests extends ControllerTestCase {

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
}
