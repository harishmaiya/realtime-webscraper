var amqp = require('amqplib/callback_api');

// Use amqplib for connection and cloudamqp to maintain RabbitMQ
var amqpConn = null;
var pubChannel = null;
var offlinePubQueue = [];

module.exports =
{
  startRabbitMQ: function () {
    amqp.connect("amqp://wbeczfqc:DG86frxWObe6OTz8-iGl0YAyhaRZtpkr@hyena.rmq.cloudamqp.com/wbeczfqc" + "?heartbeat=60", function (err, conn) {
      if (err) {
        console.error("[AMQP] startRabbitMQ:", err.message);
        return err;
      }
      conn.on("error", function (err) {
        if (err.message !== "Connection closing") {
          console.error("[AMQP] startRabbitMQ: conn error", err.message);
        }
      });
      conn.on("close", function () {
        console.error("[AMQP] startRabbitMQ: closing");
      });

      console.log("[AMQP] startRabbitMQ: connected");
      amqpConn = conn;

      startPublisher();
      return null;
    });
  },

// method to publish a message, will queue messages internally if the connection is down and resend later
  publish: function (exchange, routingKey, content) {
    try {
      pubChannel.publish(exchange, routingKey, content, {persistent: true},
          function (err, ok) {
            if (err) {
              console.error("[AMQP] publish err: ", err);
              offlinePubQueue.push([exchange, routingKey, content]);
              pubChannel.connection.close();
            }
          });
    }
    catch (e) {
      console.error("[AMQP] publish exception: ", e.message);
      offlinePubQueue.push([exchange, routingKey, content]);
    }
  }
};

var pubChannel = null;
var offlinePubQueue = [];
function startPublisher() {
  amqpConn.createConfirmChannel(function (err, ch) {
    if (closeOnErr(err)) return;
    ch.on("error", function (err) {
      console.error("[AMQP] channel error", err.message);
    });
    ch.on("close", function () {
      console.log("[AMQP] channel closed");
    });

    pubChannel = ch;
    while (true) {
      var m = offlinePubQueue.shift();
      if (!m) break;
      publish(m[0], m[1], m[2]);
    }
  });
}

/* -- Test receiver -- */
/*
// Receiver that acks messages only if processed succesfully
function startReceiver() {
  amqpConn.createChannel(function (err, ch) {
    if (closeOnErr(err)) return;
    ch.on("error", function (err) {
      console.error("[AMQP] channel error", err.message);
    });
    ch.on("close", function () {
      console.log("[AMQP] channel closed");
    });
    ch.prefetch(10);
    ch.assertQueue("jobs", {durable: true}, function (err, _ok) {
      if (closeOnErr(err)) return;
      ch.consume("jobs", processMsg, {noAck: false});
    });

    function processMsg(msg) {
      work(msg, function (ok) {
        try {
          if (ok)
            ch.ack(msg);
          else
            ch.reject(msg, true);
        }
        catch (e) {
          closeOnErr(e);
        }
      });
    }
  });
}

function work(msg, cb) {
  console.log("[AMQP] worker: Message received ", msg.content.toString());
  cb(true);
}
*/

function closeOnErr(err) {
  if (!err) return false;
  console.error("[AMQP] error", err);
  amqpConn.close();
  return true;
}

