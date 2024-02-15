package com.marcosisocram.handle;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BaseHandle {

    protected void handleResponse(HttpExchange httpExchange, int code) throws IOException {

        try (OutputStream outputStream = httpExchange.getResponseBody()) {

            httpExchange.sendResponseHeaders(code, 0);
            outputStream.flush();
        }
    }

    protected void handleResponse(String json, HttpExchange httpExchange, int code) throws IOException {

        try (OutputStream outputStream = httpExchange.getResponseBody()) {

            httpExchange.sendResponseHeaders(code, json.length());
            outputStream.write(json.getBytes());
            outputStream.flush();
        }
    }
}
