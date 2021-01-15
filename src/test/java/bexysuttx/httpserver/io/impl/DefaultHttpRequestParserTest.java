package bexysuttx.httpserver.io.impl;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import bexysuttx.httpserver.io.impl.DefaultHttpRequestParser;
import bexysuttx.httpserver.io.Constants;
import bexysuttx.httpserver.io.HttpRequest;

import bexysuttx.httpserver.io.exception.HttpVersionNotSupportedException;
import bexysuttx.httpserver.io.exception.MethodNotAllowedException;

public class DefaultHttpRequestParserTest {
	private DefaultHttpRequestParser defaultHttpRequestParser;

	@Before
	public void before() {
		defaultHttpRequestParser = new DefaultHttpRequestParser();
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private InputStream getClassPathResourceStream(String resourceName) {
		return getClass().getClassLoader().getResourceAsStream(resourceName);
	}

	@Test
	public void testGetSimple() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-simple.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals("localhost", request.getRemoteAddress());

			assertEquals("GET /index.html HTTP/1.1", request.getStartingLine());

			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("Mozilla/5.0 (X11; U; Linux i686; ru; rv:1.9b5) Gecko/2008050509 Firefox/3.0b5",
					request.getHeaders().get("User-Agent"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("close", request.getHeaders().get("Connection"));

			assertTrue(request.getParameters().isEmpty());
		}
	}

	@Test
	public void testGetHeadersCaseUnsensitive() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-headers-case-unsensitive.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals("localhost", request.getRemoteAddress());

			assertEquals("GET /index.html HTTP/1.1", request.getStartingLine());

			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("Mozilla/5.0 (X11; U; Linux i686; ru; rv:1.9b5) Gecko/2008050509 Firefox/3.0b5",
					request.getHeaders().get("User-Agent"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("close", request.getHeaders().get("Connection"));

			assertTrue(request.getParameters().isEmpty());

		}
	}

	@Test
	public void testGetHeadersNewline() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-headers-new-line.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");

			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals("localhost", request.getRemoteAddress());

			assertEquals("GET /index.html HTTP/1.1", request.getStartingLine());

			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("text/html;charset=windows-1251", request.getHeaders().get("Content-Type"));

			assertTrue(request.getParameters().isEmpty());
		}
	}

	@Test
	public void testGetInvalidHttpVersion() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-invalid-http-version.txt")) {
			thrown.expect(HttpVersionNotSupportedException.class);
			thrown.expectMessage(new IsEqual<String>("Current server support only HTTP/1.1 protocol"));
			defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");
		}
	}

	@Test
	public void testGetWithDecodedParams() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-with-decoded-params.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");

			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertFalse(request.getParameters().isEmpty());
			assertEquals(6, request.getParameters().size());
			assertEquals("welcome@wiki.net", request.getParameters().get("email"));
			assertEquals("", request.getParameters().get("password"));
			assertEquals("5", request.getParameters().get("number"));
			assertEquals("Simple Text", request.getParameters().get("text"));
			assertEquals("http://wiki.net", request.getParameters().get("url"));
			assertEquals("test&qwerty?ty=u", request.getParameters().get("p"));
		}
	}

	@Test
	public void testGetDublicateParams() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-with-duplicate-params.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals(2, request.getParameters().size());
			assertEquals("value1,value2,value1", request.getParameters().get("param1"));
			assertEquals("true", request.getParameters().get("param2"));
		}
	}

	@Test
	public void testGetSimpleParams() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("get-with-simple-params.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "");

			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals(2, request.getParameters().size());
			assertEquals("value1", request.getParameters().get("param1"));
			assertEquals("true", request.getParameters().get("param2"));
		}
	}

	@Test
	public void testHeadSimple() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("head-simple.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "");

			assertEquals("HEAD", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());
		}
	}

		
	@Test
	public void testMethodNotAllowed() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("method-not-allowed.txt")) {
			thrown.expect(MethodNotAllowedException.class);
			thrown.expectMessage(
					new IsEqual<String>("Only " + Constants.ALLOWED_METHOD + " are supported. Current method is PUT"));
			defaultHttpRequestParser.parserHttpRequest(httpMessage, "");
		}
	}

	@Test
	public void testPostSimple() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("post-simple.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");
			assertEquals("POST", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals("localhost", request.getRemoteAddress());

			assertEquals("POST /index.html HTTP/1.1", request.getStartingLine());

			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("Mozilla/5.0 (X11; U; Linux i686; ru; rv:1.9b5) Gecko/2008050509 Firefox/3.0b5",
					request.getHeaders().get("User-Agent"));
			assertEquals("close", request.getHeaders().get("Connection"));
			assertEquals("84", request.getHeaders().get("Content-Length"));

			assertEquals(5, request.getParameters().size());
			assertEquals("welcome@dev.net", request.getParameters().get("email"));
			assertEquals("", request.getParameters().get("password"));
			assertEquals("5", request.getParameters().get("number"));
			assertEquals("Simple Text", request.getParameters().get("text"));
			assertEquals("http://dev.net", request.getParameters().get("url"));
		}
	}

	@Test
	public void testPostWithEmptyBody() throws IOException {
		try (InputStream httpMessage = getClassPathResourceStream("post-with-empty-body.txt")) {
			HttpRequest request = defaultHttpRequestParser.parserHttpRequest(httpMessage, "localhost");
			assertEquals("POST", request.getMethod());
			assertEquals("/index.html", request.getUri());
			assertEquals("HTTP/1.1", request.getHttpVersion());

			assertEquals("localhost", request.getRemoteAddress());

			assertEquals("POST /index.html HTTP/1.1", request.getStartingLine());

			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("Mozilla/5.0 (X11; U; Linux i686; ru; rv:1.9b5) Gecko/2008050509 Firefox/3.0b5",
					request.getHeaders().get("User-Agent"));
			assertEquals("close", request.getHeaders().get("Connection"));
			assertEquals("0", request.getHeaders().get("Content-Length"));

			assertEquals(0, request.getParameters().size());

		}
	}
}
