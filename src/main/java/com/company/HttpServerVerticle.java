package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
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
    private final static String SEND_ADDRESS = "clientVerticle";
    private final static String RECEIVE_ADDRESS = "serverVerticle";
    private final static ObjectMapper MAPPER = new ObjectMapper();

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
        router.errorHandler(404, this::resourceNotFoundHandler);
        this.server.requestHandler(this.router).listen(80);
    }

    private void handleUrlTesterRoute(RoutingContext context) {
        RequestParamsModel parameters = this.buildRequestModel(context.queryParams());
        try {
            String serializedParameters = MAPPER.writeValueAsString(parameters);
            final EventBus eventBus = vertx.eventBus();
            MessageConsumer<String> responses = eventBus.send(SEND_ADDRESS, serializedParameters).consumer(RECEIVE_ADDRESS);
            vertx.deployVerticle(HttpClientVerticle.class.getName(), new DeploymentOptions().setInstances(parameters.getRequests()));
            responses.handler(response -> context.response().end(response.body()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void validationFailuresHandler(RoutingContext context) {
        Throwable failure = context.failure();
        if (failure instanceof ValidationException) {
            context.response()
                    .setStatusCode(400)
                    .end(String.format("the parameter: %s failed in validation because of %s", ((ValidationException) failure).parameterName(), failure.getMessage()));
            System.out.println("ValidationException");
        } else {
            // TODO set to handle exceptions from the handlers functions
        }
    }

    private void resourceNotFoundHandler(RoutingContext context) {
        context.response().setStatusCode(400).end("resource not found, bad request");
        System.out.println("resource not found, bad request");
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