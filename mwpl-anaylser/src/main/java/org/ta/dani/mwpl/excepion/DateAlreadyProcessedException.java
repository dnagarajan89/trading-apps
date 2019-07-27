package org.ta.dani.mwpl.excepion;

public class DateAlreadyProcessedException extends Exception {

	private static final long serialVersionUID = -1478566036936280790L;

	public DateAlreadyProcessedException(String errorMessage) {
		super(errorMessage);
	}

}
