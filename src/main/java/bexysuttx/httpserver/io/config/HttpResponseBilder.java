package bexysuttx.httpserver.io.config;

public interface HttpResponseBilder {

	ReadableHttpResponse bildNewHttpResponse();

	void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody);

}
