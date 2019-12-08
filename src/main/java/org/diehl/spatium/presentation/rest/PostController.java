package org.diehl.spatium.presentation.rest;

import org.diehl.spatium.application.SpatiumAPI;
import org.diehl.spatium.application.dto.DetailedPost;
import org.diehl.spatium.domain.model.Post;
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


@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
public class PostController {

    @Inject
    SpatiumAPI spatiumAPI;
    @Inject
    private Validator validator;

    @GET
    public CompletionStage<List<DetailedPost>> getPublicPosts() {
        return spatiumAPI.findPublicDetailedPosts();
    }

    @GET
    @Path("{id}")
    public CompletionStage<Post> getSingle(@PathParam("id") String id) {
        return spatiumAPI.findPostById(id);
    }

    @GET
    @Path("/organization/{org}")
    public CompletionStage<List<DetailedPost>> getByOrganization(@PathParam("org") String org) {
        return spatiumAPI.findOrganizationDetailedPosts(org);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Post> add(@MultipartForm Post post) {
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        if (!violations.isEmpty()) {
            String errors = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
            throw new RuntimeException(errors);
        }
        return spatiumAPI.addPost(post);
    }
}
