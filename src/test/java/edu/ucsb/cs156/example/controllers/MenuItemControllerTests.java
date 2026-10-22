package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewsController.class)
@Import(TestConfig.class)
public class MenuItemControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreviews/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreviews/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreviews/post")
                .param("itemId", "5")
                .param("reviewerEmail", "kyleli@ucsb.edu")
                .param("stars", "1")
                .param("comments", "this sucks")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreviews/post")
                .param("itemId", "5")
                .param("reviewerEmail", "kyleli@ucsb.edu")
                .param("stars", "1")
                .param("comments", "this sucks")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_reviews() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview menuItemReview1 =
        MenuItemReview.builder()
            .itemId(5)
            .reviewerEmail("kyleli@ucsb.edu")
            .stars(1)
            .comments("this sucks")
            .dateReviewed(ldt1)
            .build();

    ArrayList<MenuItemReview> expectedMenuItemReviews = new ArrayList<>();
    expectedMenuItemReviews.add(menuItemReview1);

    when(menuItemReviewRepository.findAll()).thenReturn(expectedMenuItemReviews);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreviews/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItemReviews);

    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_review() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(5)
            .reviewerEmail("kyleli@ucsb.edu")
            .stars(1)
            .comments("this sucks")
            .dateReviewed(ldt1)
            .build();

    when(menuItemReviewRepository.save(eq(review1))).thenReturn(review1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreviews/post")
                    .param("itemId", "5")
                    .param("reviewerEmail", "kyleli@ucsb.edu")
                    .param("stars", "1")
                    .param("comments", "this sucks")
                    .param("dateReviewed", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).save(eq(review1));
    String expectedJson = mapper.writeValueAsString(review1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreviews").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreviews").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(5)
            .reviewerEmail("kyleli@ucsb.edu")
            .stars(1)
            .comments("this sucks")
            .dateReviewed(ldt)
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(review1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreviews").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(review1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menuitemreview() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

    MenuItemReview review1 =
        MenuItemReview.builder()
            .itemId(5)
            .reviewerEmail("kyleli@ucsb.edu")
            .stars(1)
            .comments("this sucks")
            .dateReviewed(ldt1)
            .build();

    MenuItemReview review1Edited =
        MenuItemReview.builder()
            .itemId(3)
            .reviewerEmail("kylelii@ucsb.edu")
            .stars(2)
            .comments("this ok")
            .dateReviewed(ldt2)
            .build();

    String requestBody = mapper.writeValueAsString(review1Edited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(review1));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreviews")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    verify(menuItemReviewRepository, times(1))
        .save(review1Edited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview review1Edited =
        MenuItemReview.builder()
            .itemId(3)
            .reviewerEmail("kylelii@ucsb.edu")
            .stars(2)
            .comments("this ok")
            .dateReviewed(ldt1)
            .build();

    String requestBody = mapper.writeValueAsString(review1Edited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreviews")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_review() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview review =
        MenuItemReview.builder()
            .itemId(3)
            .reviewerEmail("kylelii@ucsb.edu")
            .stars(2)
            .comments("this ok")
            .dateReviewed(ldt1)
            .build();

    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.of(review));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreviews").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(15L);
    verify(menuItemReviewRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_menuitemreview_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(menuItemReviewRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/menuitemreviews").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 15 not found", json.get("message"));
  }
}
