package org.ta.dani.mwpl.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class MwplUtils {
	
	public static String localDateToString(LocalDate date, String format) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		return date.format(dateTimeFormatter);
	}
	

	public static LocalDate stringToLocalDate(String date, String format) {
		return LocalDate.parse(
				date, 
				new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(format).toFormatter()
		);
	}

}
