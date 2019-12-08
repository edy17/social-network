package org.diehl.spatium.presentation.rest;

import org.diehl.spatium.application.SpatiumAPI;
import org.diehl.spatium.domain.model.Organization;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationController {

    @Inject
    SpatiumAPI spatiumAPI;
    @Inject
    private Validator validator;

    @GET
    public CompletionStage<List<Organization>> getAll() {
        return spatiumAPI.findAllOrganizations();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Organization> getSingle(@PathParam("id") String id) {
        return spatiumAPI.findOrganizationById(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Organization> add(@MultipartForm Organization organization) {
        Set<ConstraintViolation<Organization>> violations = validator.validate(organization);
        if (!violations.isEmpty()) {
            String errors = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
            throw new RuntimeException(errors);
        }
        return spatiumAPI.addOrganization(organization);
    }
}
