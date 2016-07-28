// BASE SETUP
// =============================================================================
var express = require('express');
var bodyParser = require('body-parser');
var request = require('request');
var rabbitMQ = require('./rabbit.js');

var app = express();
app.use(bodyParser.urlencoded({extended: true}));
app.use(bodyParser.json());

var port = process.env.PORT || 9200;

// ROUTES FOR API
// =============================================================================
var router = express.Router();

//Expose API operations at http://localhost:9200/api
router.get('/', function (req, res) {
  res.json({message: 'Initialize with /api/start'});
});

router.get('/start', function (req, res) {
  rabbitMQ.startRabbitMQ();
  res.json({message: 'RabbitMQ initialized'});
});

router.route('/url')
    .post(function (req, res) {
      var prodURL = req.body.PARM;
      var jsonToMQ = {productID: '', url: ''};
      request(prodURL, function (error, response, html) {
        if (error || response.statusCode != 200) {
          res.json({message: 'Cannot get specified URL'});
          return res;
        }
        //Product ID is last segment of URL (Ex: http://www.amazon.com/gp/product/B00VVOCSOU)
        jsonToMQ.productID = prodURL.substr(prodURL.lastIndexOf('/') + 1);
        jsonToMQ.url = prodURL;
        console.log("Received request for URL:", jsonToMQ.url, ",ProdID:", jsonToMQ.productID );
        rabbitMQ.publish("", "AMZNProds", new Buffer(JSON.stringify(jsonToMQ)));
      });
      res.json({message: 'URL is pushed to stream!'});
    })

    .get(function (req, res) {
      res.json({message: 'Use POST to input URL'});
    });

//Register routes
app.use('/api', router);

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Server started at port ' + port);


