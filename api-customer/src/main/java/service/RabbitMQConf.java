package service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import model.Customer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


public class RabbitMQConf {

    public void createMessage(Customer customer,String queryType) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        ObjectMapper mapper = new ObjectMapper();
        customer.setQueryType(queryType);
        String json = mapper.writeValueAsString(customer);

        channel.basicPublish("", "customer", null, json.getBytes("UTF-8"));

        channel.close();
        connection.close();
    }
}
