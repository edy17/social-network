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
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Path("/posts")
public class PostController {

    @Inject
    SpatiumAPI spatiumAPI;
    @Inject
    private Validator validator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<List<DetailedPost>> getPublicPosts() {
        return spatiumAPI.findPublicDetailedPosts();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public CompletionStage<Post> getSingle(@PathParam("id") String id) {
        return spatiumAPI.findPostById(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organization/{org}")
    public CompletionStage<List<DetailedPost>> getByOrganization(@PathParam("org") String org) {
        return spatiumAPI.findOrganizationDetailedPosts(org);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Post> add(@MultipartForm Post post) {
        Set<ConstraintViolation<Post>> violations = validator.validate(post);
        if (!violations.isEmpty()) {
            String errors = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
            throw new IllegalStateException(errors);
        }
        return spatiumAPI.addPost(post);
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/image/{key}")
    public Response getImage(@PathParam("key") String key) {
        InputStream inputStream = spatiumAPI.getImage(key);
        return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM).build();
    }
}
