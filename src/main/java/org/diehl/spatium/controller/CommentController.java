package org.diehl.spatium.controller;

import org.diehl.spatium.model.Comment;
import org.diehl.spatium.service.CommentService;
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

@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
public class CommentController {

    @Inject
    CommentService service;

    @GET
    @Path("{id}")
    public CompletionStage<List<Comment>> getPublicCommentByPostId(@PathParam("id") String id) {
        return service.findByPublicPost(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public CompletionStage<Comment> addPublicComment(@MultipartForm Comment comment) {
        return service.addPublicComment(comment);
    }
}
