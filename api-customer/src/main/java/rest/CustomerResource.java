package rest;

import dao.CustomerDAO;
import model.Customer;
import service.RabbitMQConf;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.nonNull;


@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {
   private static final CustomerDAO dao = new CustomerDAO();


   private RabbitMQConf rabbitMQConf = new RabbitMQConf();


    @GET
    @Path("/{id}")
    public Customer getCustomerById(@PathParam("id") Integer id) {
        return dao.selectById(id);
    }
    @GET
    public List<Customer> getCustomers() {
        return dao.selectAll();
    }


    @POST
    public Response insertCustomer(@NotNull Customer customer) throws IOException, TimeoutException {
      dao.insert(customer);
      rabbitMQConf.createMessage(customer);
      return Response.created(URI.create("customer")).build();
    }

    @PUT
    public Response updateCustomer(@NotNull Customer customer){
        dao.update(customer);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") @NotNull Integer id){
        Customer customer = dao.selectById(id);
        if(nonNull(customer)) dao.delete(customer);
        return Response.ok().build();
    }
}
