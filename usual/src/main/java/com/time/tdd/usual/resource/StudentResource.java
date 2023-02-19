package com.time.tdd.usual.resource;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.time.tdd.usual.model.Student;
import com.time.tdd.usual.model.StudentRepository;

/**
 * @author XuJian
 * @date 2023-02-18 00:04
 **/

@Path("/students")
public class StudentResource {

    private StudentRepository repository;

    @Inject
    public StudentResource(StudentRepository repository) {
        this.repository = repository;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Student> all() {
        return repository.all();
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") long id) {
        return repository.findById(id).map(Response::ok).orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }
}

