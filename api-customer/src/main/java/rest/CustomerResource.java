package rest;

import dao.CustomerDAO;
import model.Customer;
import service.RabbitMQConf;

import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private static final CustomerDAO dao = new CustomerDAO();
    private RabbitMQConf rabbitMQConf = new RabbitMQConf();


    @GET
    @Path("/{id}")
    public Response getCustomerById(@PathParam("id") Integer id) {
        Customer customer = dao.selectById(id);
        if (isNull(customer)) {
            return Response.status(404).build();
        }
        return Response.ok(customer).build();
    }


    @POST
    public Response insertCustomer(@NotNull Customer customer) throws IOException, TimeoutException {
        dao.insert(customer);
        rabbitMQConf.createMessage(customer,"insert");
        return Response.created(URI.create("customer")).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCustomer(@PathParam("id") @NotNull Integer id, @NotNull Customer customer) throws IOException, TimeoutException {
        if(!id.equals(customer.getId())){
         return Response.status(Response.Status.BAD_REQUEST).build();
        }
        dao.update(customer);
        rabbitMQConf.createMessage(customer,"update");
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") @NotNull Integer id) throws IOException, TimeoutException {

        Customer customer = dao.selectById(id);
        if (isNull(customer)) {
            return Response.status(404).build();
        }
        dao.delete(id);
        rabbitMQConf.createMessage(customer,"delete");
        return Response.noContent().build();
    }
}
