package bexysuttx.httpserver.io;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Constants {
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String HEAD = "HEAD";
	
	public static final String HTTP_VERSION = "HTTP/1.1";

	public static final List<String> ALLOWED_METHOD = Collections
			.unmodifiableList(Arrays.asList(new String[] { GET, POST, HEAD }));

	public Constants() {

	}

}
