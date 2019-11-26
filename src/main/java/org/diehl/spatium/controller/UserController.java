package org.diehl.spatium.controller;

import org.diehl.spatium.model.User;
import org.diehl.spatium.service.UserService;
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


@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<List<User>> getAll() {
        return service.findAll();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<User> getSingle(@PathParam("id") String id) {
        return service.getById(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<List<User>> add(@MultipartForm User user) {
        service.add(user);
        return getAll();
    }
}
