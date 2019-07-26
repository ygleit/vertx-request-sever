package com.company;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;


public class HttpServerVerticle extends AbstractVerticle {

    private HttpServer server;
    private Router router;
    private WebClient client;

    @Override
    public void start(Promise<Void> promise) {
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
        this.client = WebClient.create(vertx);
        var validationHandler = HTTPRequestValidationHandler.create()
                .addQueryParam("url", ParameterType.HOSTNAME, true)
                .addQueryParam("requests", ParameterType.INT, true)
                .addQueryParam("timeout", ParameterType.INT, true);
        router.route("/url_tester")
                .handler(validationHandler)
                .handler(this::handleUrlTesterRoute)
                .failureHandler(this::validationFailuresHandler);
        router.errorHandler(400 ,this::errorHandler);
        this.server.requestHandler(this.router).listen(80);
    }

    private void handleUrlTesterRoute(RoutingContext context) {
        RequestParamsModel parameters = this.buildRequestModel(context.queryParams());
        this.client.get(parameters.getUrl()).send(request -> {
            if (request.succeeded()) {
                HttpResponse<Buffer> response = request.result();
                if (response.statusCode() == 200) {
                    context.response().end("status code: 200, content length: " + response.bodyAsString().length() + ", timeout: none, time took: ");
                } else {
                    context.response().end("status code: " + response.statusCode() + ", content length:  none, timeout: yes, time took: " + parameters.getTimeout());
                }
            }
        });
    }

    private void validationFailuresHandler(RoutingContext context) {
        Throwable failure = context.failure();
        if (failure instanceof  ValidationException) {
            var validationErrorMessage = failure.getMessage();
        }
    }

    private void errorHandler(RoutingContext context){


    }

    @Override
    public void stop() {
        System.out.println("stopping");
    }

    private RequestParamsModel buildRequestModel(MultiMap queryParams) {
        return RequestParamsModel.builder()
                .url(queryParams.get("url"))
                .requests(Integer.parseInt(queryParams.get("requests")))
                .timeout(Integer.parseInt(queryParams.get("timeout")))
                .build();
    }
}