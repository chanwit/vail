load('vertx.js');

var webServerConf = {
	port: 8082,
	host: 'localhost',
	ssl: false,
	bridge: true
}

vertx.deployVerticle('web-server', webServerConf);