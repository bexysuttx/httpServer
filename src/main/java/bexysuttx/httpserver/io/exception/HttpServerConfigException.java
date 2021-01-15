package bexysuttx.httpserver.io.exception;

public class HttpServerConfigException extends HttpServerException {

	private static final long serialVersionUID = 1897442973602651735L;

	public HttpServerConfigException(String message) {
		super(message);

	}

	public HttpServerConfigException(Throwable cause) {
		super(cause);
	}

	public HttpServerConfigException(String message, Throwable cause) {
		super(message, cause);
	}

}
