package org.diehl.spatium.presentation.rest;

import org.diehl.spatium.application.SpatiumAPI;
import org.diehl.spatium.domain.model.Comment;
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

@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
public class CommentController {

    @Inject
    SpatiumAPI spatiumAPI;
    @Inject
    private Validator validator;

    @GET
    @Path("{id}")
    public CompletionStage<List<Comment>> getByPostId(@PathParam("id") String id) {
        return spatiumAPI.findCommentsByPostId(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Comment> add(@MultipartForm Comment comment) {
        Set<ConstraintViolation<Comment>> violations = validator.validate(comment);
        if (!violations.isEmpty()) {
            String errors = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
            throw new IllegalStateException(errors);
        }
        return spatiumAPI.addComment(comment);
    }
}
