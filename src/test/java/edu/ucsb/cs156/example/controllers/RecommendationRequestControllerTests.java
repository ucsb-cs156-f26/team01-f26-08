package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/RECOMMENDATIONREQUESTS/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/RECOMMENDATIONREQUESTS/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/RECOMMENDATIONREQUESTS/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RECOMMENDATIONREQUESTS/post")
                .param("requesterEmail", "student1@ucsb.edu")
                .param("professorEmail", "prof1@ucsb.edu")
                .param("explanation", "grad school application")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-03-11T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // Functionality tests

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendation_requests() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("grad school application")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .professorEmail("prof2@ucsb.edu")
            .explanation("job application")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RECOMMENDATIONREQUESTS/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("grad school application")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(request))).thenReturn(request);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/RECOMMENDATIONREQUESTS/post")
                    .param("requesterEmail", "student1@ucsb.edu")
                    .param("professorEmail", "prof1@ucsb.edu")
                    .param("explanation", "grad school application")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2022-03-11T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(request);
    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_recommendation_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T12:30:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2026-11-21T12:30:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .id(15L)
            .requesterEmail("student@ucsb.edu")
            .professorEmail("professor@ucsb.edu")
            .explanation("Graduate school recommendation")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(request));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/RecommendationRequest").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, times(1)).delete(request);

    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_delete_nonexistent_recommendation_request() throws Exception {
    // arrange
    when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/RecommendationRequest").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(15L);
    verify(recommendationRequestRepository, never()).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete_recommendation_requests() throws Exception {

    mockMvc
        .perform(delete("/api/RecommendationRequest").param("id", "15").with(csrf()))
        .andExpect(status().isForbidden());

    verify(recommendationRequestRepository, never()).delete(any());
  }

  @Test
  public void logged_out_users_cannot_delete_recommendation_requests() throws Exception {

    mockMvc
        .perform(delete("/api/RecommendationRequest").param("id", "15").with(csrf()))
        .andExpect(status().isForbidden());

    verify(recommendationRequestRepository, never()).delete(any());
  }
}
