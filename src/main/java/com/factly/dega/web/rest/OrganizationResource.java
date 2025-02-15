package com.factly.dega.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.factly.dega.service.DegaUserService;
import com.factly.dega.service.OrganizationService;
import com.factly.dega.service.dto.DegaUserDTO;
import com.factly.dega.service.dto.OrganizationDTO;
import com.factly.dega.service.dto.RoleMappingDTO;
import com.factly.dega.web.rest.errors.BadRequestAlertException;
import com.factly.dega.web.rest.util.CommonUtil;
import com.factly.dega.web.rest.util.HeaderUtil;
import com.factly.dega.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for managing Organization.
 */
@RestController
@RequestMapping("/api")
public class OrganizationResource {

    private final Logger log = LoggerFactory.getLogger(OrganizationResource.class);

    private static final String ENTITY_NAME = "coreOrganization";

    private final OrganizationService organizationService;
    private final DegaUserService degaUserService;

    public OrganizationResource(OrganizationService organizationService, DegaUserService degaUserService) {
        this.organizationService = organizationService;
        this.degaUserService = degaUserService;
    }

    /**
     * POST  /organizations : Create a new organization.
     *
     * @param organizationDTO the organizationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new organizationDTO, or with status 400 (Bad Request) if the organization has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/organizations")
    @Timed
    public ResponseEntity<OrganizationDTO> createOrganization(@Valid @RequestBody OrganizationDTO organizationDTO, HttpServletRequest request) throws URISyntaxException {
        log.debug("REST request to save Organization : {}", organizationDTO);
        if (organizationDTO.getId() != null) {
            throw new BadRequestAlertException("A new organization cannot already have an ID", ENTITY_NAME, "id exists");
        }
        organizationDTO.setSlug(getSlug(CommonUtil.removeSpecialCharsFromString(organizationDTO.getName())));
        organizationDTO.setClientId(getSlug(CommonUtil.removeSpecialCharsFromString(organizationDTO.getName())));
        organizationDTO.setCreatedDate(ZonedDateTime.now());
        organizationDTO.setLastUpdatedDate(ZonedDateTime.now());
        OrganizationDTO result = organizationService.save(organizationDTO);
        return ResponseEntity.created(new URI("/api/organizations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /organizations : Updates an existing organization.
     *
     * @param organizationDTO the organizationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated organizationDTO,
     * or with status 400 (Bad Request) if the organizationDTO is not valid,
     * or with status 500 (Internal Server Error) if the organizationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/organizations")
    @Timed
    public ResponseEntity<OrganizationDTO> updateOrganization(@Valid @RequestBody OrganizationDTO organizationDTO) throws URISyntaxException {
        log.debug("REST request to update Organization : {}", organizationDTO);
        if (organizationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        organizationDTO.setLastUpdatedDate(ZonedDateTime.now());
        OrganizationDTO result = organizationService.save(organizationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, organizationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /organizations : get all the organizations.
     *
     * @param keycloakUserId the keycloakId of the user
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of organizations in body
     */
    @GetMapping("/organizations")
    @Timed
    public ResponseEntity<List<OrganizationDTO>> getOrganizations(@RequestParam(value = "keycloakUserId", required = false) String keycloakUserId,
                                                                  Pageable pageable) {
        log.debug("REST request to get a page of Organizations: query {}", keycloakUserId);
            if(StringUtils.isEmpty(keycloakUserId)){
                Page<OrganizationDTO> page = organizationService.findAll(pageable);
                HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/organizations");
                return ResponseEntity.ok().headers(headers).body(page.getContent());
            }
            return ResponseEntity.ok().body(organizationService.getOrganizations(keycloakUserId, pageable));

    }

    /**
     * GET  /organizations/:id : get the "id" organization.
     *
     * @param id the id of the organizationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the organizationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{id}")
    @Timed
    public ResponseEntity<OrganizationDTO> getOrganization(@PathVariable String id) {
        log.debug("REST request to get Organization : {}", id);
        Optional<OrganizationDTO> organizationDTO = organizationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(organizationDTO);
    }

    /**
     * DELETE  /organizations/:id : delete the "id" organization.
     *
     * @param id the id of the organizationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/organizations/{id}")
    @Timed
    public ResponseEntity<Void> deleteOrganization(@PathVariable String id) {
        log.debug("REST request to delete Organization : {}", id);
        organizationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * SEARCH  /_search/organizations?query=:query : search for the organization corresponding
     * to the query.
     *
     * @param query the query of the organization search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/organizations")
    @Timed
    public ResponseEntity<List<OrganizationDTO>> searchOrganizations(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Organizations for query {}", query);
        Page<OrganizationDTO> page = organizationService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/organizations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /organizationbyslug/:slug : get the organization.
     *
     * @param slug the slug of the OrganizationDTO
     * @return Optional<OrganizationDTO> organization by clientId and slug
     */
    @GetMapping("/organizationbyslug/{slug}")
    @Timed
    public Optional<OrganizationDTO> getOrganizationBySlug(@PathVariable String slug) {
        log.debug("REST request to get Organization by slug : {}", slug);
        Optional<OrganizationDTO> organizationDTO = organizationService.findBySlug(slug);
        return organizationDTO;
    }

    public String getSlug(String name){
        if(name != null){
            int slugExtention = 0;
            return createSlug(name, name, slugExtention);
        }
        return null;
    }

    public String createSlug(String slug, String tempSlug, int slugExtention){
        Optional<OrganizationDTO> postDTO = organizationService.findBySlug(slug);
        if(postDTO.isPresent()){
            slugExtention += 1;
            slug = tempSlug + slugExtention;
            return createSlug(slug, tempSlug, slugExtention);
        }
        return slug;
    }
}
