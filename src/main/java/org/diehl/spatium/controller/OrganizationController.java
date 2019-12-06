package org.diehl.spatium.controller;

import org.diehl.spatium.model.Organization;
import org.diehl.spatium.service.OrganizationService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
public class OrganizationController {

    @Inject
    OrganizationService service;

    @GET
    public CompletionStage<List<Organization>> getAll() {
        return service.findAll();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Organization> getSingle(@PathParam("id") String id) {
        return service.getByKeySchema(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Organization> add(@MultipartForm Organization organization) {
        return service.add(organization);
    }
}
