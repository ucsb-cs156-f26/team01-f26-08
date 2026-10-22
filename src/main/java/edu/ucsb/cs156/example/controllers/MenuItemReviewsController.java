package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for MenuItemReviewsController */
@Tag(name = "MenuItemReviews")
@RequestMapping("/api/menuitemreviews")
@RestController
@Slf4j
public class MenuItemReviewsController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all MenuItemReviews
   *
   * @return an iterable of MenuItemReview
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> menuItemReviews = menuItemReviewRepository.findAll();
    return menuItemReviews;
  }

  /**
   * Create a new date
   *
   * @param itemId id of the item
   * @param reviewerEmail email of the reviewer
   * @param stars the rating in stars
   * @param comments any commments left by the reviewer
   * @param dateReviewed the date of the review
   * @return the saved MenuItemReview
   */
  @Operation(summary = "Create a new MenuItemReview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "comments") @RequestParam String comments,
      @Parameter(name = "dateReviewed") @RequestParam LocalDateTime dateReviewed)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("localDateTime={}", dateReviewed);

    MenuItemReview review = new MenuItemReview();
    review.setItemId(itemId);
    review.setReviewerEmail(reviewerEmail);
    review.setStars(stars);
    review.setComments(comments);
    review.setDateReviewed(dateReviewed);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(review);

    return savedMenuItemReview;
  }

  /**
   * Update a single review
   *
   * @param id id of the review to update
   * @param incoming the new review
   * @return the updated review object
   */
  @Operation(summary = "Update a single MenuItemReview")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReview updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReview incoming) {

    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    menuItemReview.setItemId(incoming.getItemId());
    menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
    menuItemReview.setStars(incoming.getStars());
    menuItemReview.setComments(incoming.getComments());
    menuItemReview.setDateReviewed(incoming.getDateReviewed());

    menuItemReviewRepository.save(menuItemReview);

    return menuItemReview;
  }
}

  // private Long id;

  // private Long itemId;
  // private String reviewerEmail;
  // private int stars;
  // private String comments;
  // private LocalDateTime dateReviewed;
