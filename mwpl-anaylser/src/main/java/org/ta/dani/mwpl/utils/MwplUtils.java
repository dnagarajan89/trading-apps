package org.ta.dani.mwpl.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MwplUtils {
	
	public static String localDateToString(LocalDate date, String format) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return date.format(dateTimeFormatter);
	}
	

	public static LocalDate stirngToLocalDate(String date, String format) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return LocalDate.parse(date, dateTimeFormatter);
	}

}
