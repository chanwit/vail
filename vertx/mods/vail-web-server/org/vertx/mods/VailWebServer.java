/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.mods;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import java.io.File;

/**
 * A simple web server module that can serve static files, and also can
 * bridge event bus messages to/from client side JavaScript and the server side
 * event bus.
 *
 * Please see the modules manual for full description of what configuration
 * parameters it takes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author Chanwit Kaewkasi
 */
public class VailWebServer extends BusModBase implements Handler<HttpServerRequest> {

  private String indexPage;
  private String[] webRoots;

  public void start() {
    super.start();

    HttpServer server = vertx.createHttpServer();

    if (getOptionalBooleanConfig("ssl", false)) {
      server.setSSL(true).setKeyStorePassword(getOptionalStringConfig("key_store_password", "wibble"))
                         .setKeyStorePath(getOptionalStringConfig("key_store_path", "server-keystore.jks"));
    }

    if (getOptionalBooleanConfig("static_files", true)) {
      server.requestHandler(this);
    }

    boolean bridge = getOptionalBooleanConfig("bridge", false);
    if (bridge) {
      SockJSServer sjsServer = vertx.createSockJSServer(server);
      JsonArray permitted = getOptionalArrayConfig("permitted", new JsonArray());

      sjsServer.bridge(getOptionalObjectConfig("sjs_config", new JsonObject().putString("prefix", "/eventbus")), permitted,
                       getOptionalLongConfig("auth_timeout", 5 * 60 * 1000),
                       getOptionalStringConfig("auth_address", "vertx.basicauthmanager.authorise"));
    }

    webRoots = (getOptionalStringConfig("web_root", "web") + ",../app/client").split(",");
    for(int i = 0; i < webRoots.length; i++) {
      webRoots[i] = webRoots[i] + File.separator;
      System.out.println("webroot " + i + " : " + webRoots[i]);
    }

    indexPage = getOptionalStringConfig("index_page", "index.html");

    server.listen(getOptionalIntConfig("port", 80), getOptionalStringConfig("host", "0.0.0.0"));
  }

  private void sendFile(HttpServerRequest req, String path) {
    System.out.println("send File");
    for(String root: webRoots) {
      try {
        if(vertx.fileSystem().existsSync(root + path)) {
          req.response.sendFile(root + path);
          return;
        }
      } catch(Throwable e) {}
    }
    req.response.statusCode = 404;
    req.response.end();
  }

  public void handle(HttpServerRequest req) {
    if (req.path.equals("/")) {
      sendFile(req, indexPage);
    } else if (!req.path.contains("..")) {
      sendFile(req, req.path);
    } else {
      req.response.statusCode = 404;
      req.response.end();
    }
  }

}
