package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** This is a REST controller for UCSBOrganizations */
@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationsController extends ApiController {

  @Autowired UCSBOrganizationsRepository ucsbOrganizationsRepository;

  /**
   * List all UCSB organizations
   *
   * @return an iterable of UCSBOrganizations
   */
  @Operation(summary = "List all UCSB organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganizations> allUCSBOrganizations() {
    Iterable<UCSBOrganizations> organizations = ucsbOrganizationsRepository.findAll();
    return organizations;
  }

  /**
   * Get a UCSB organization by ID
   *
   * @param id the ID of the organization
   * @return the UCSB organization
   */
  @Operation(summary = "Get a UCSB organization by ID")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganizations getUCSBOrganizationById(
      @Parameter(name = "id") @RequestParam String id) {

    UCSBOrganizations organization =
        ucsbOrganizationsRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "UCSB Organization Not Found"));

    return organization;
  }

  /**
   * Create a new UCSB organization
   *
   * @param orgCode the organization code
   * @param orgTranslationShort the short organization name
   * @param orgTranslation the full organization name
   * @param inactive whether the organization is inactive
   * @return the saved UCSB organization
   */
  @Operation(summary = "Create a new UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganizations postUCSBOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive)
      throws JsonProcessingException {

    UCSBOrganizations organization = new UCSBOrganizations();
    organization.setOrgCode(orgCode);
    organization.setOrgTranslationShort(orgTranslationShort);
    organization.setOrgTranslation(orgTranslation);
    organization.setInactive(inactive);

    UCSBOrganizations savedOrganization = ucsbOrganizationsRepository.save(organization);

    return savedOrganization;
  }
}
