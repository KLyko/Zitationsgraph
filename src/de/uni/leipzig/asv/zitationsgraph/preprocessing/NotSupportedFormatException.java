package de.uni.leipzig.asv.zitationsgraph.preprocessing;
/**
 * Exception to notify that the format of an input file is not supported.
 * @author Klaus Lyko
 *
 */
public class NotSupportedFormatException extends Exception {
	public NotSupportedFormatException() {
		super("Input Format of the file is not spported");
	}
	public NotSupportedFormatException(String s) {
		super(s);
	}
}
