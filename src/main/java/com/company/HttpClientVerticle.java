package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;


public class HttpClientVerticle extends AbstractVerticle {
    private WebClient client;
    private final static String RECEIVE_ADDRESS = "clientVerticle";
    private final static String SEND_ADDRESS = "serverVerticle";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public HttpClientVerticle() {
    }

    @Override
    public void start() {
        StopWatch stopWatch = new StopWatch();
        this.client = WebClient.create(vertx);
        final EventBus eventBus = vertx.eventBus();
        MessageConsumer<String> consumer = eventBus.consumer(RECEIVE_ADDRESS);
        consumer.handler(
                parameters -> {
                    try {
                        RequestParamsModel deserializedParameters = MAPPER.readValue(parameters.body(), RequestParamsModel.class);
                        stopWatch.start();
                        this.client.get(deserializedParameters.getUrl(), "").timeout(deserializedParameters.getTimeout()).send(request -> {
                            stopWatch.stop();
                            if (request.succeeded()) {
                                HttpResponse<Buffer> response = request.result();
                                if (response.statusCode() == 200) {
                                    eventBus.send(SEND_ADDRESS, String.format("status code: 200, content length: %d, timeout: none, time took: %dms", response.bodyAsString().length(), stopWatch.getTime()));
                                } else {
                                    eventBus.send(SEND_ADDRESS, String.format("status code: none, content length: none, timeout: none, time took: %dms", deserializedParameters.getTimeout()));
                                }
                            }
                            stopWatch.reset();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).exceptionHandler(error -> System.out.println(error.getMessage()));

    }
}
