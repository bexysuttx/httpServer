package bexysuttx.httpserver.io.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bexysuttx.httpserver.io.Constants;
import bexysuttx.httpserver.io.HttpRequest;
import bexysuttx.httpserver.io.config.HttpRequestParser;
import bexysuttx.httpserver.io.exception.BadRequestException;
import bexysuttx.httpserver.io.exception.HttpServerConfigException;
import bexysuttx.httpserver.io.exception.HttpServerException;
import bexysuttx.httpserver.io.exception.HttpVersionNotSupportedException;
import bexysuttx.httpserver.io.exception.MethodNotAllowedException;
import bexysuttx.httpserver.io.utils.DataUtils;
import bexysuttx.httpserver.io.utils.HttpUtils;

class DefaultHttpRequestParser implements HttpRequestParser {

	@Override
	public HttpRequest parserHttpRequest(InputStream in, String remoteAddress)
			throws IOException, HttpServerConfigException {
		String startingLine = null;

		try {
			ParseRequest request = parseInputStream(in);
			return convertParsedRequestToHttpRequest(request, remoteAddress);
		} catch (RuntimeException e) {
			if (e instanceof HttpServerException) {
				throw e;
			} else {
				throw new BadRequestException("Can't parse http request: " + e.getMessage(), e, startingLine);
			}
		}
	}

	private HttpRequest convertParsedRequestToHttpRequest(ParseRequest request, String remoteAddress) throws IOException {
		String[] startingLineData = request.startingLine.split(" ");
		String method = startingLineData[0];
		String uri = startingLineData[1];
		String httpVersion = startingLineData[2];
		validateHttpVersion(request.startingLine, httpVersion);
		Map<String, String> headers = parseHeaders(request.headersLine);

		ProcessedUri processedUri = extractParametersIfPresent(method, uri, httpVersion, request.messageBody);

		return new DefaultHttpRequest(method, processedUri.uri, httpVersion, remoteAddress, headers,
				processedUri.parameters);
	}

	private ProcessedUri extractParametersIfPresent(String method, String uri, String httpVersion, String messageBody) throws IOException {
		Map<String, String> map = Collections.emptyMap();
		if (Constants.GET.equalsIgnoreCase(method) || Constants.HEAD.equalsIgnoreCase(method)) {
			int indexOfDelim = uri.indexOf('?');
			if (indexOfDelim != -1) {
				return extractParametersFromUri(uri, indexOfDelim);
			}
		} else if (Constants.POST.equalsIgnoreCase(method)) {
			if (messageBody != null && !"".equals(messageBody)) {
				map = getParameters(messageBody);

			}
		} else {
			throw new MethodNotAllowedException(method, String.format("%s $s $s", method, uri, httpVersion));
		}
		return new ProcessedUri(uri, map);
	}

	private ProcessedUri extractParametersFromUri(String uri, int indexOfDelim) throws UnsupportedEncodingException {
		String paramString = uri.substring(indexOfDelim + 1);
		Map<String, String> parameters = getParameters(paramString);
		uri = uri.substring(0, indexOfDelim);
		return new ProcessedUri(uri, parameters);
	}

	private Map<String, String> getParameters(String paramString) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<>();
		String[] param = paramString.split("&");
		for (String items : param) {
			String[] item = items.split("=");
			if (item.length == 1) {
				item = new String[] { item[0], "" };
			}
			String name = item[0];
			String value = map.get(name);
			if (value != null) {
				value = value + "," + URLDecoder.decode(item[1], StandardCharsets.UTF_8);
			} else {
				value = URLDecoder.decode(item[1], StandardCharsets.UTF_8);
			}
			map.put(name, value);
		}
		return map;
	}

	private Map<String, String> parseHeaders(List<String> headersLine) {
		Map<String, String> map = new LinkedHashMap<>();
		String prevName = null;
		for (String headersItem : headersLine) {
			prevName = putHeader(prevName, map, headersItem);
		}
		return map;
	}

	private String putHeader(String prevName, Map<String, String> map, String headersItem) {
		if (headersItem.charAt(0) == ' ') {
			String value = map.get(prevName) + headersItem.trim();
			map.put(prevName, value);
			return prevName;
		} else {
			int index = headersItem.indexOf(':');
			String name = HttpUtils.normilizeHeaderName(headersItem.substring(0, index));
			String value = headersItem.substring(index + 1).trim();
			map.put(name, value);
			return name;
		}
	}

	private void validateHttpVersion(String startingLine, String httpVersion) {
		if (!Constants.HTTP_VERSION.equals(httpVersion)) {
			throw new HttpVersionNotSupportedException(
					"Current server support only " + Constants.HTTP_VERSION + " protocol", startingLine);
		}

	}

	protected ParseRequest parseInputStream(InputStream in) throws IOException {
		String startingLineAndHeaders = HttpUtils.readStartingLineAndHeaders(in);
		int contentLengthIndex = HttpUtils.getContentLengthIndex(startingLineAndHeaders);
		if (contentLengthIndex != -1) {
			int contentLength = HttpUtils.getContentLengthValue(startingLineAndHeaders, contentLengthIndex);
			String messageBody = HttpUtils.readMessageBody(in, contentLength);
			return new ParseRequest(startingLineAndHeaders, messageBody);
		}
		return new ParseRequest(startingLineAndHeaders, null);
	}

	private static class ParseRequest {
		private final String startingLine;
		private final List<String> headersLine;
		private final String messageBody;

		public ParseRequest(String startingLineAndHeaders, String messageBody) {
			super();
			List<String> list = DataUtils.convertToLineList(startingLineAndHeaders);
			this.startingLine = list.remove(0);
			if (list.isEmpty()) {
				this.headersLine = Collections.emptyList();
			} else {
				this.headersLine = Collections.unmodifiableList(list);
			}
			this.messageBody = messageBody;
		}

	}

	private static class ProcessedUri {
		final String uri;
		final Map<String, String> parameters;

		public ProcessedUri(String uri, Map<String, String> parameters) {
			super();
			this.uri = uri;
			this.parameters = parameters;
		}

	}

}
