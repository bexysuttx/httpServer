package bexysuttx.httpserver.io.exception;

public class HttpServerException extends RuntimeException {

	
	private static final long serialVersionUID = -4796087254716131740L;
	private int statusCode = 500;
	
	 
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public HttpServerException(String message) {
		super(message);

	}

	public HttpServerException(Throwable cause) {
		super(cause);

	}

	public HttpServerException(String message, Throwable cause) {
		super(message, cause);

	}

}
