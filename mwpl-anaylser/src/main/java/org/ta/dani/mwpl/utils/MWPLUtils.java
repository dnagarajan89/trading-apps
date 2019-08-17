package org.ta.dani.mwpl.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;

public class MWPLUtils {
	
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

	public static LocalDate dervieMwplDate(LocalDate date) {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		int weekendOffset = 0;
		if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
			weekendOffset = 2;
		} else if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
			weekendOffset = 1;
		}
		if (weekendOffset > 0) {
			yesterday = yesterday.minusDays(weekendOffset);
		}
		return yesterday;
	}

	private static final String appendStrings(String... strings) {
		final StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(strings).forEach(str -> stringBuilder.append(str));
		return stringBuilder.toString();
	}

}
