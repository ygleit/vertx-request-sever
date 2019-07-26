package com.company;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class HttpClientVerticle extends AbstractVerticle {
    private WebClient client;
    private final int timeout;
    private final String url;

    public HttpClientVerticle(RequestParamsModel requestParameters){
        this.client = WebClient.create(vertx);
        this.timeout = requestParameters.getTimeout();
        this.url = requestParameters.getUrl();
    }

    @Override
    public void start(){
        this.client.get(this.url, "").timeout(this.timeout).send(request -> {
            if (request.succeeded()) {
                HttpResponse<Buffer> response = request.result();
                if (response.statusCode() == 200) {
                    context.response().end("status code: 200, content length: " + response.bodyAsString().length() + ", timeout: none, time took: ");
                } else {
                    context.response().end("status code: none, content length:  none, timeout: yes, time took: " + this.timeout());
                }
            }
        });
    }
}
