package bexysuttx.httpserver.io.exception;

import bexysuttx.httpserver.io.Constants;

public class MethodNotAllowedException extends AbstractRequestParseFailedException {
	private static final long serialVersionUID = 8303546917153147569L;

	public MethodNotAllowedException(String method, String parseLine) {
		super("Only " + Constants.ALLOWED_METHOD + " are supported. Current method is " + method, parseLine);
		setStatusCode(405);
	}

}
