package org.diehl.spatium.controller;


import org.diehl.spatium.model.Post;
import org.diehl.spatium.service.PostService;

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


@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostController {

    @Inject
    PostService service;

    @GET
    public CompletionStage<List<Post>> getAll() {
        return service.findAll();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Post> getSingle(@PathParam("id") String id) {
        return service.getById(id);
    }

    @POST
    public CompletionStage<List<Post>> add(Post post) {
        service.add(post);
        return getAll();
    }

}
