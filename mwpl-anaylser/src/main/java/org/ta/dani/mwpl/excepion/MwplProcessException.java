package org.ta.dani.mwpl.excepion;

public class MwplProcessException extends RuntimeException {

	private static final long serialVersionUID = -3869626903124767611L;

	public MwplProcessException(String errorMessage) {
		super(errorMessage);
	}

	public MwplProcessException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}
}
