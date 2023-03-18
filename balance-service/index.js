const { Client } = require("pg");
const express = require("express");
const amqplib = require("amqplib/callback_api");

const customerPort = 3000;
const transactionPort = 3001;
const app = express();
const host = "localhost";
const amqp = `amqp://${host}:5672`;
const customerQueue = "customer";
const transactionQueue = "transaction";
const balanceDb = {
  user: "postgres",
  host: host,
  database: "bank",
  password: "postgres",
  port: 5434,
};

app.use(express.json());

app.listen(customerPort, () => {
  amqplib.connect(amqp, (err, conn) => {
    if (err) throw err;
    conn.createChannel((err, channel) => {
      if (err) throw err;
      channel.assertQueue(customerQueue, { durable: false, autoDelete: true });
      channel = channel;
      console.log(`balance-service connected to queue ${customerQueue}!`);
      channel.consume(customerQueue, (msg) => {
        const client = new Client(balanceDb);
        const customer = JSON.parse(msg.content);
        console.log("Customer incoming...", customer);
        client.connect();
        if (customer.queryType == "insert") {
          client.query(
            "INSERT INTO customer (name, cpf) VALUES ($1, $2)",
            [customer.name, customer.cpf],
            (err) => {
              if (err) throw err;
            }
          );
        } else if (customer.queryType == "delete") {
          client.query(
            "DELETE FROM customer WHERE id = $1",
            [customer.id],
            (err) => {
              if (err) throw err;
            }
          );
        } else {
          client.query(
            "UPDATE customer SET name = $2 WHERE id = $1",
            [customer.id, customer.name],
            (err) => {
              if (err) throw err;
            }
          );
        }
      });
    });
  });
});

app.listen(transactionPort, () => {
  amqplib.connect(amqp, (err, conn) => {
    if (err) throw err;
    conn.createChannel((err, channel) => {
      if (err) throw err;
      channel.assertQueue(transactionQueue, {
        durable: false,
        autoDelete: true,
      });
      console.log(`balance-service connected to queue ${transactionQueue}!`);
      channel.consume(transactionQueue, (msg) => {
        const client = new Client(balanceDb);
        const transaction = JSON.parse(msg.content);
        client.connect();
        console.log("Transaction incoming...", msg.content.toString());
        client.query(
          "SELECT value FROM balance WHERE customer_id = $1",
          [transaction.customerId],
          (err, res) => {
            let currentValue;
            if (err) throw err;
            if (transaction.type == "INCOME") {
              currentValue = res.rows[0].value
                ? res.rows[0].value
                : 0 + transaction.value;
            } else {
              currentValue = res.rows[0].value
                ? res.rows[0].value
                : 0 - transaction.value;
            }
            client.query(
              "UPDATE balance SET value = $2 WHERE customer_id = $1",
              [transaction.customerId, currentValue],
              (err) => {
                if (err) throw err;
              }
            );
          }
        );
      });
    });
  });
});
