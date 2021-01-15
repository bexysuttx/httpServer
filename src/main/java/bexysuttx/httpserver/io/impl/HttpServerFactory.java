package bexysuttx.httpserver.io.impl;

import java.util.Properties;

import bexysuttx.httpserver.io.HandlerConfig;
import bexysuttx.httpserver.io.HttpServer;
import bexysuttx.httpserver.io.config.HttpServerConfig;

public class HttpServerFactory {

	protected HttpServerFactory() {

	}

	public static HttpServerFactory create() {
		return new HttpServerFactory();
	}

	public  HttpServer createHttpServer(HandlerConfig handlerConfig,Properties overrideServerProperties) throws Exception {
		HttpServerConfig httpServerConfig = new DefaultHttpServerConfig(handlerConfig, overrideServerProperties);
		return new DefaultHttpServer(httpServerConfig);
	}
}
