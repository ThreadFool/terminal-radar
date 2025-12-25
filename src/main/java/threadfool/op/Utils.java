package threadfool.op;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils
{
	public static String toHms(Instant instant) {
		if (instant == null) {
			return "";
		}

		return DateTimeFormatter.ofPattern("HH:mm:ss")
				.withZone(ZoneId.systemDefault())
				.format(instant);
	}
}
