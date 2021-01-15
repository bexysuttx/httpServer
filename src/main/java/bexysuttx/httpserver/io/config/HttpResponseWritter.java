package bexysuttx.httpserver.io.config;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpResponseWritter {

	void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException;
	
	
}
