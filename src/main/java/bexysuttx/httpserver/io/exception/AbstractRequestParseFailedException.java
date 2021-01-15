package bexysuttx.httpserver.io.exception;

public abstract class AbstractRequestParseFailedException extends HttpServerException {

	private static final long serialVersionUID = -7237086471848619034L;
	private final String parseLine;

	public AbstractRequestParseFailedException(String message, Throwable cause, String parseLine) {
		super(message, cause);
		this.parseLine = parseLine;
	}

	public AbstractRequestParseFailedException(Throwable cause, String parseLine) {
		super(cause);
		this.parseLine = parseLine;
	}

	public AbstractRequestParseFailedException(String message, String parseLine) {
		super(message);
		this.parseLine = parseLine;
	}

	public String getParseLine() {
		return parseLine;
	}
}
