package bexysuttx.httpserver.io.config;

import java.io.IOException;
import java.io.InputStream;

import bexysuttx.httpserver.io.HttpRequest;
import bexysuttx.httpserver.io.exception.HttpServerConfigException;

public interface HttpRequestParser {
	
	HttpRequest parserHttpRequest(InputStream in, String remoteAddress) throws IOException, HttpServerConfigException;
	

}
