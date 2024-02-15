package com.marcosisocram.handle;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public abstract class CustonHttpHandler extends BaseHandle {

    abstract void handle(HttpExchange httpExchange, String requestId) throws IOException;

}
