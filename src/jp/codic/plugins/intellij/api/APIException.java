package jp.codic.plugins.intellij.api;

/**
 * Exception that throws when API acccess faild.
 */
public class APIException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private int code;

	public APIException(String message) {
		super(message);
	}

	public APIException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}
