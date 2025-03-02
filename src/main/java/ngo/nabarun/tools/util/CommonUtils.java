package ngo.nabarun.tools.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {
	private final static ObjectMapper objectMapper = new ObjectMapper();
	private static final String VERSION = "VERSION";
	private static Date systemDate = null;

	/**
	 * Utility function to convert java Date to TimeZone format
	 * 
	 * @param date
	 * @param format
	 * @param timeZone
	 * @return
	 */
	public static String formatDateToString(Date date, String format, String timeZone) {
		// null check
		if (date == null)
			return null;
		// create SimpleDateFormat object with input format
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		// default system timezone if passed null or empty
		if (timeZone == null || "".equalsIgnoreCase(timeZone.trim())) {
			timeZone = Calendar.getInstance().getTimeZone().getID();
		}
		// set timezone to SimpleDateFormat
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		// return Date in required format with timezone as String
		return sdf.format(date);
	}

	public static boolean isCurrentMonth(Date givenDate) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(givenDate);
		cal2.setTime(getSystemDate());
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}

	public static List<String> getMonthsBetween(Date startDate, Date endDate, String format) {
		List<String> list = new ArrayList<String>();
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();
		beginCalendar.setTime(startDate);
		finishCalendar.setTime(endDate);
		DateFormat formaterYd = new SimpleDateFormat(format);
		while (beginCalendar.before(finishCalendar)) {
			list.add(formaterYd.format(beginCalendar.getTime()));
			beginCalendar.add(Calendar.MONTH, 1);
		}
		return list;
	}

	public static List<String> getMonthsBetween(Date startDate, Date endDate) {

		return getMonthsBetween(startDate, endDate, "MMMM yyyy");
	}

	public static String getFormattedDateString(Date date, String format) {
		if (date == null) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		return formaterYd.format(date);
	}

	public static String getFormattedDateString(Date date) {
		return getFormattedDateString(date, "MMMM yyyy");
	}

	public static Date addDaysToDate(Date date, int days) {
		return addSecondsToDate(date, days * 86400); // One day to 86400 seconds
	}

	public static Date addSecondsToDate(Date date, int seconds) {
		if (date == null) {
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}

	public static String getURLToFileName(String url) {
		try {
			return Paths.get(new URI(url).getPath()).getFileName().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void copyNonNullProperties(Object src, Object target) {
		BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
	}

	private static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null)
				emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	public static List<String> getProdProfileNames() {
		return List.of("PROD", "PRODUCTION");
	}

	public static <T> T jsonToPojo(String json, Class<T> classz) {
		try {
			return objectMapper.readValue(json, classz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date getSystemDate() {
		Date date = (systemDate == null ? new Date() : systemDate);
		// System.err.println("Get System date="+date);
		return date;
	}

	public static void setSystemDate(String date) throws ParseException {
		// System.err.println("System date="+date);
		if (date == null) {
			systemDate = null;
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			systemDate = format.parse(date);
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	public static byte[] toByteArray(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
		} catch (IOException e) {
			System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return baos.toByteArray();
	}

	public static Map<String, Object> toMap(Object object) {
		return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
		});
	}

	public static <T> T convertToType(Object object, TypeReference<T> type) {
		ObjectMapper objectMapper = new ObjectMapper();
		// String jsonString = "{\"symbol\":\"ABCD\}";
		objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		// Trade trade = objectMapper.readValue(jsonString, new TypeReference<Symbol>()
		// {});
		return objectMapper.convertValue(object, type);
	}

	public static String toJSONString(Object obj, boolean pretty) throws JsonProcessingException {
		if (pretty) {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		}
		return objectMapper.writeValueAsString(obj);
	}

	public static boolean areEqual(Object oldValue, Object newValue) {
		if (oldValue == null) {
			return newValue == null;
		} else {
			return oldValue.equals(newValue);
		}
	}

	public static String getAppVersion() {
		return System.getenv(VERSION) == null ? System.getProperty(VERSION) : System.getenv(VERSION);
	}

	public static Date getDayOfCurrentMonth(int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getSystemDate());
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	public static Date getLastDayOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getSystemDate());
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

}
