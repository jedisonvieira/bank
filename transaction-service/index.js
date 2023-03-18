const { Client } = require("pg");
const express = require("express");
const amqplib = require("amqplib/callback_api");

const port = 3000;
const app = express();
const host = "localhost";
const amqp = `amqp://${host}:5672`;
const transactionQueue = "transaction";
const customerQueue = "customer";
const transactionDb = {
  user: "postgres",
  host: host,
  database: "bank",
  password: "postgres",
  port: 5433,
};

app.use(express.json());

app.listen(port, () => {
  amqplib.connect(amqp, (err, conn) => {
    if (err) throw err;
    conn.createChannel((err, channel) => {
      if (err) throw err;
      channel.assertQueue(customerQueue);
      console.log(`transaction-service connected to queue ${customerQueue}!`);
      channel.consume(customerQueue, (msg) => {
        const client = new Client(transactionDb);
        const customer = JSON.parse(msg.content);

        console.log("Customer incoming...", customer);
        client.connect();
        if (customer.queryType == "insert") {
          client.query(
            "INSERT INTO customer (name, cpf) VALUES ($1, $2)",
            [customer.name, customer.cpf],
            (err) => {
              console.log("Erro:" + err);
              if (err) throw err;
              client.end();
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

app.post("/transaction", (req, res) => {
  const client = new Client(transactionDb);
  client.connect();
  client.query(
    "INSERT INTO transaction (customer_id, description, type, value) VALUES ($1, $2, $3, $4)",
    [req.body.customerId, req.body.description, req.body.type, req.body.value],
    (err) => {
      if (err) throw err;
      client.end();
    }
  );

  amqplib.connect(amqp, (err, conn) => {
    if (err) throw err;
    conn.createChannel((err, ch) => {
      if (err) throw err;
      const msg = JSON.stringify(req.body);
      ch.assertQueue(transactionQueue, {
        durable: false,
        autoDelete: true,
      }).sendToQueue(transactionQueue, Buffer.from(msg));
      console.log(`Trasaction sent ${msg}`);
    });
    setTimeout(() => {
      conn.close();
    }, 500);
  });
  res.status(200).send({ Message: "Transaction inserted!" });
});
