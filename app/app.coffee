load 'vertx.js'

webServerConf =
	port: 8080
	host: 'localhost'
	ssl: false
	bridge: true

vertx.deployVerticle('web-server', webServerConf)