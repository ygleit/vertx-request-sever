package com.company;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(HttpServerVerticle.class.getName());
    }
}
