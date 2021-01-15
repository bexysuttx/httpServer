package bexysuttx.httpserver.io.exception;

public class BadRequestException extends AbstractRequestParseFailedException {

	private static final long serialVersionUID = -13062211763344182L;

	public BadRequestException(String message, Throwable cause, String parseLine) {
		super(message, cause, parseLine);
		setStatusCode(400);
	}

}
