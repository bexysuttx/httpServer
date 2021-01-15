package bexysuttx.httpserver.io.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bexysuttx.httpserver.io.Constants;
import bexysuttx.httpserver.io.HttpRequest;
import bexysuttx.httpserver.io.HttpServerContext;
import bexysuttx.httpserver.io.config.HttpClientSocketHandler;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.config.ReadableHttpResponse;
import bexysuttx.httpserver.io.exception.AbstractRequestParseFailedException;
import bexysuttx.httpserver.io.exception.HttpServerException;
import bexysuttx.httpserver.io.exception.MethodNotAllowedException;

class DefaultHttpClientSocketHandler implements HttpClientSocketHandler {
	private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("ACCESS_LOG");
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpClientSocketHandler.class);
	private final Socket clientSocket;
	private final String remoteAddress;
	private final HttpServerConfig httpServerConfig;

	public DefaultHttpClientSocketHandler(Socket clientSocket, HttpServerConfig httpServerConfig) {
		super();
		this.clientSocket = clientSocket;
		this.remoteAddress = clientSocket.getRemoteSocketAddress().toString();
		this.httpServerConfig = httpServerConfig;
	}

	@Override
	public void run() {
		try {
			execute();
		} catch (Exception e) {
			LOGGER.error("Client request failed: " + e.getMessage(), e);
		}

	}

	private void execute() throws Exception {
		try (Socket s = clientSocket) {
			s.setKeepAlive(false);
			try (InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
				processRequest(remoteAddress, in, out);
			}
		}
	}

	private void processRequest(String remoteAddress, InputStream in, OutputStream out) throws Exception, IOException {
		ReadableHttpResponse response = httpServerConfig.getHttpResponseBilder().bildNewHttpResponse();
		String startingLine = null;
		try {
			HttpRequest request = httpServerConfig.getHttpRequestParser().parserHttpRequest(in, remoteAddress);
			startingLine = request.getStartingLine();
			processRequest(request, response);
		} catch (AbstractRequestParseFailedException e) {
			startingLine = e.getParseLine();
			handleException(e, response);
		} catch (EOFException e) {
			LOGGER.warn("Client socket closed connection");
			return;
		}
		httpServerConfig.getHttpResponseBilder().prepareHttpResponse(response, startingLine.startsWith(Constants.HEAD));
		ACCESS_LOGGER.info("Request: {} - \"{}\", Response: {} ({} bytes)", remoteAddress, startingLine, response.getStatus(), response.getBodyLength());
		httpServerConfig.getHttpResponseWritter().writeHttpResponse(out, response);

	}

	private void handleException(Exception ex, ReadableHttpResponse response) {
		LOGGER.error("Exception during request: " + ex.getMessage(), ex);
		if (ex instanceof HttpServerException) {
			HttpServerException e = (HttpServerException) ex;
			response.setStatus(e.getStatusCode());
			if (e instanceof MethodNotAllowedException) {
				response.setHeader("Allow", StringUtils.join(Constants.ALLOWED_METHOD, ", "));
			}
		} else {
			response.setStatus(500);
		}
	}

	private void processRequest(HttpRequest request, ReadableHttpResponse response) {
		HttpServerContext context = httpServerConfig.getHttpServerContext();
		try {
			httpServerConfig.getHttpServerDispatcher().handle(context, request, response);
		} catch (Exception e) {
			handleException(e, response);
		}

	}

}
