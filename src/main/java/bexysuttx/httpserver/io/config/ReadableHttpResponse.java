package bexysuttx.httpserver.io.config;

import java.util.Map;

import bexysuttx.httpserver.io.HttpResponse;

public interface ReadableHttpResponse extends HttpResponse {

	int getStatus();

	Map<String, Object> getHeaders();
	
	byte[] getBody();
	  
	boolean isBodyEmpty();
	
	int getBodyLength(); 

}
