package bexysuttx.httpserver.io.impl;

import java.util.Collections;
import java.util.Map;

import bexysuttx.httpserver.io.HttpRequest;

class DefaultHttpRequest implements HttpRequest {

	private final String method;
	private final String uri;
	private final String httpVersion;
	private final String remoteAddress;
	private final Map<String, String> headers;
	private final Map<String, String> parameters;

	DefaultHttpRequest(String method, String uri, String httpVersion, String remoteAddress, Map<String, String> headers,
			Map<String, String> parameters) {
		super();
		this.method = method;
		this.uri = uri;
		this.httpVersion = httpVersion;
		this.remoteAddress = remoteAddress;
		this.headers = Collections.unmodifiableMap(headers);
		this.parameters = Collections.unmodifiableMap(parameters);
	}

	@Override
	public String getStartingLine() {
		return String.format("%s %s %s", getMethod(), getUri(), getHttpVersion());
	}

	public String getMethod() {
		return method;
	}

	public String getUri() {
		return uri;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

}
