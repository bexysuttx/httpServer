package bexysuttx.httpserver.io.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import bexysuttx.httpserver.io.Constants;
import bexysuttx.httpserver.io.config.HttpResponseWritter;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.config.ReadableHttpResponse;

class DefaultHttpResponseWritter extends AbstractHttpConfigurableComponent implements HttpResponseWritter {

	DefaultHttpResponseWritter(HttpServerConfig httpServerConfig) {
		super(httpServerConfig);
	}

	@Override
	public void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException {
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
		addStartingLine(writer, response);
		addHeaders(writer, response);
		writer.println();
		writer.flush();

		addMessageBody(out, response);
	}

	private void addMessageBody(OutputStream out, ReadableHttpResponse response) throws IOException {
		if (!response.isBodyEmpty()) {
			out.write(response.getBody());
			out.flush();

		}

	}

	private void addHeaders(PrintWriter writer, ReadableHttpResponse response) {
		for (Entry<String, Object> header : response.getHeaders().entrySet()) {
			writer.println(String.format("%s: %s", header.getKey(), header.getValue()));
		}

	}

	private void addStartingLine(PrintWriter writer, ReadableHttpResponse response) {
		String httpVersion = Constants.HTTP_VERSION;
		int statusCode = response.getStatus();
		String statusMessage = httpServerConfig.getStatusMessage(statusCode);
		writer.println(String.format("%s %s %s", httpVersion, statusCode, statusMessage));
	}

}
