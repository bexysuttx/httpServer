package bexysuttx.httpserver.io.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class HttpUtils {

	public static String normilizeHeaderName(String name) {
		StringBuilder headerName = new StringBuilder(name.trim());
		for (int i = 0; i < headerName.length(); i++) {
			char ch = headerName.charAt(i);
			if (i == 0) {
				toUpper(ch, i, headerName);
			} else if (ch == '-' && i < headerName.length() - 1) {
				toUpper(headerName.charAt(i + 1), i + 1, headerName);
				i++;
			} else {
				if (Character.isUpperCase(ch)) {
					headerName.setCharAt(i, Character.toLowerCase(ch));
				}
			}
		}

		return headerName.toString();
	}

	private static void toUpper(char ch, int i, StringBuilder headerName) {
		if (Character.isLowerCase(ch)) {
			headerName.setCharAt(i, Character.toUpperCase(ch));
		}

	}

	public static String readStartingLineAndHeaders(InputStream in) throws IOException {
		ByteArray byteArray = new ByteArray();
		while (true) {
			int read = in.read();
			if (read == -1) {
				throw new EOFException("InputStream closed");

			}
			byteArray.add((byte) read);
			if (byteArray.isEmptyLine()) {
				break;
			}
		}
		return new String(byteArray.toArray(), StandardCharsets.UTF_8);
	}

	private static class ByteArray {

		private byte[] array = new byte[1024];
		private int size;

		private void add(byte value) {
			if (size == array.length) {
				byte[] temp = array;
				array = new byte[array.length * 2];
				System.arraycopy(temp, 0, array, 0, size);
			}
			array[size++] = value;
		}

		private byte[] toArray() {
			if (size > 4) {
				return Arrays.copyOf(array, size - 4);

			} else {
				throw new IllegalStateException(
						"Byte array has invalid size " + Arrays.toString(Arrays.copyOf(array, size)));
			}
		}

		private boolean isEmptyLine() {
			if (size >= 4) {
				return array[size - 1] == '\n' && array[size - 2] == '\r' && array[size - 3] == '\n'
						&& array[size - 4] == '\r';
			} else {
				return false;
			}

		}

	}

	public static int getContentLengthIndex(String startingLineAndHeaders) {
		return startingLineAndHeaders.toLowerCase().indexOf(CONTENT_LENGTH);
	}

	private static final String CONTENT_LENGTH = "content-length: ";

	public static int getContentLengthValue(String startingLineAndHeaders, int contentLengthIndex) {
		int startContentLengthIndex = contentLengthIndex + CONTENT_LENGTH.length();
		int endContentLengthIndex = startingLineAndHeaders.indexOf("\r\n", startContentLengthIndex);
		if (endContentLengthIndex == -1) {
			endContentLengthIndex = startingLineAndHeaders.length();
		}
		return Integer
				.parseInt(startingLineAndHeaders.substring(startContentLengthIndex, endContentLengthIndex).trim());
	}

	public static String readMessageBody(InputStream in, int contentLength) throws IOException {
		StringBuilder messageBody = new StringBuilder();
		while (contentLength > 0) {
			byte[] messageByte = new byte[contentLength];
			int read = in.read(messageByte);
			messageBody.append(new String(messageByte, 0, read, StandardCharsets.UTF_8));
			contentLength = contentLength - read;
		}

		return messageBody.toString();
	}

	private HttpUtils() {

	}
}
