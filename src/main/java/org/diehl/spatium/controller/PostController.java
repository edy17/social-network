package org.diehl.spatium.controller;


import org.diehl.spatium.model.Post;
import org.diehl.spatium.service.PostService;
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


@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
public class PostController {

    @Inject
    PostService service;

    @GET
    public CompletionStage<List<Post>> getPublicPosts() {
        return service.findByPublic();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Post> getSingle(@PathParam("id") String id) {
        return service.getByKeySchema(id);
    }

    @GET
    @Path("/organization/{org}")
    public CompletionStage<List<Post>> getByOrganization(@PathParam("org") String org) {
        return service.findByOrganization(org);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Post> add(@MultipartForm Post post) {
        return service.addPublicPost(post);
    }
}
