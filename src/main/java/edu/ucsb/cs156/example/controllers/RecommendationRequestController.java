package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequests */
@Tag(name = "RecommendationRequest")
@RequestMapping({"/api/RECOMMENDATIONREQUESTS", "/api/RecommendationRequest"})
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {
  /** Updates all editable fields while preserving the stored id. */
  @Operation(
      summary = "Update a single recommendation request",
      description =
          "Admin only. Supply all fields except id as JSON. Dates use ISO local date-time "
              + "format, e.g. 2026-10-21T12:00:00. The query id is preserved and any body id is ignored. "
              + "Returns 404 if the request does not exist.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id", description = "Id of the request to update") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest incoming) {
    RecommendationRequest request =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));
    request.setRequesterEmail(incoming.getRequesterEmail());
    request.setProfessorEmail(incoming.getProfessorEmail());
    request.setExplanation(incoming.getExplanation());
    request.setDateRequested(incoming.getDateRequested());
    request.setDateNeeded(incoming.getDateNeeded());
    request.setDone(incoming.getDone());
    recommendationRequestRepository.save(request);
    return request;
  }

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   *
   * @return all recommendation requests
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequest =
        recommendationRequestRepository.findAll();
    return recommendationRequest;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail requester's email
   * @param professorEmail professor's email
   * @param explanation explanation of request
   * @param dateRequested date of request
   * @param dateNeeded date needed
   * @param done has been fulfilled
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "dateRequested")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(name = "dateNeeded")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    log.info("dateRequested={}, dateNeeded={}", dateRequested, dateNeeded);

    RecommendationRequest request = new RecommendationRequest();
    request.setRequesterEmail(requesterEmail);
    request.setProfessorEmail(professorEmail);
    request.setExplanation(explanation);
    request.setDateRequested(dateRequested);
    request.setDateNeeded(dateNeeded);
    request.setDone(done);

    return recommendationRequestRepository.save(request);
  }

  /**
   * Get a single date by id
   *
   * @param id the id of the recommendation request
   * @return a recommendation request
   */
  @Operation(summary = "Get a single recommendation request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recommendationRequest;
  }
}
