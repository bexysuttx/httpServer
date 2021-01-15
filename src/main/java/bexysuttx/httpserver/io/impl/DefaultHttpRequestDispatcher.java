package bexysuttx.httpserver.io.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import bexysuttx.httpserver.io.HttpHandler;
import bexysuttx.httpserver.io.HttpRequest;
import bexysuttx.httpserver.io.HttpResponse;
import bexysuttx.httpserver.io.HttpServerContext;
import bexysuttx.httpserver.io.config.HttpServerDispatcher;
import bexysuttx.httpserver.io.exception.HttpServerException;

public class DefaultHttpRequestDispatcher implements HttpServerDispatcher {
	private final HttpHandler defaultHttpHandler;
	private final Map<String, HttpHandler> httpHandler;

	DefaultHttpRequestDispatcher(HttpHandler defaultHttpHandler, Map<String, HttpHandler> httpHandler) {
		super();
		Objects.requireNonNull(defaultHttpHandler);
		Objects.requireNonNull(httpHandler);
		this.defaultHttpHandler = defaultHttpHandler;
		this.httpHandler = httpHandler;
	}

	@Override
	public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
		try {
			HttpHandler handler = getHttpHandler(request);
			handler.handle(context, request, response);
		} catch (RuntimeException e) {
			if (e instanceof HttpServerException) {
				throw e;
			} else {
				throw new HttpServerException("Handle request: " + request.getUri() + " failed: ", e);
			}
		}

	}

	private HttpHandler getHttpHandler(HttpRequest request) {
		HttpHandler handler = httpHandler.get(request.getUri());
		if (handler == null) {
			handler = defaultHttpHandler;
		}
		return handler;
	}

}
