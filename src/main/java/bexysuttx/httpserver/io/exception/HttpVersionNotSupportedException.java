package bexysuttx.httpserver.io.exception;

public class HttpVersionNotSupportedException extends AbstractRequestParseFailedException {
	private static final long serialVersionUID = 8835366511566959208L;

	public HttpVersionNotSupportedException(String message, String parseLine) {
		super(message, parseLine);
		setStatusCode(505);
	}

}
