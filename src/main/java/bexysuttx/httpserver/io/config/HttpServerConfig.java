package bexysuttx.httpserver.io.config;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import bexysuttx.httpserver.io.HttpServerContext;
import bexysuttx.httpserver.io.ServerInfo;

public interface HttpServerConfig extends AutoCloseable {
	
	ServerInfo getServerInfo();
	
	String getStatusMessage(int statusCode);
	
	HttpResponseBilder getHttpResponseBilder();
	
	HttpResponseWritter getHttpResponseWritter();
	
	HttpRequestParser getHttpRequestParser();
	
	HttpServerDispatcher getHttpServerDispatcher();
	
	HttpServerContext getHttpServerContext();
	
	ThreadFactory getWorkerThreadFactory();
	 
	HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket);


}
