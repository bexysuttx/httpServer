package bexysuttx.httpserver.io.impl;

import java.util.Date;
import java.util.Map;

import bexysuttx.httpserver.io.config.HttpResponseBilder;
import bexysuttx.httpserver.io.config.HttpServerConfig;
import bexysuttx.httpserver.io.config.ReadableHttpResponse;
import bexysuttx.httpserver.io.utils.DataUtils;

class DefaultHttpResponseBuilder extends AbstractHttpConfigurableComponent implements HttpResponseBilder {

	public DefaultHttpResponseBuilder(HttpServerConfig httpServerConfig) {
		super(httpServerConfig);
	}

	protected ReadableHttpResponse createReadableHttpResponse() {
		return new DefaultReadableHttpResponse();
	}

	@Override
	public ReadableHttpResponse bildNewHttpResponse() {
		ReadableHttpResponse response = createReadableHttpResponse();
		response.setHeader("Date", new Date());
		response.setHeader("Server", httpServerConfig.getServerInfo().getName());
		response.setHeader("Content-Language", "en");
		response.setHeader("Connection", "close");
		response.setHeader("Content-Type", "text/html");
		return response;
	}

	@Override
	public void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody) {
		if (response.getStatus() >= 400 && response.isBodyEmpty()) {
			setDefaultResponseErrorBody(response);
		}
		setContentLength(response);
		if (clearBody) {
			clearBody(response);
		}
	}

	private void setDefaultResponseErrorBody(ReadableHttpResponse response) {
		Map<String, Object> args = DataUtils.buildMap(new Object[][] { { "STATUS-CODE", response.getStatus() },
				{ "STATUS-MESSAGE", httpServerConfig.getStatusMessage(response.getStatus()) } });
		String content = httpServerConfig.getHttpServerContext().getHtmlTemplateManager().processTemplate("error.html", args);
		response.setBody(content);

	}

	private void clearBody(ReadableHttpResponse response) {
		response.setBody("");

	}

	private void setContentLength(ReadableHttpResponse response) {
		response.setHeader("Content-Length", response.getBodyLength());
	}

}
