package org.talend.camel.designer.migration;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;

import org.talend.commons.exception.PersistenceException;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class DeprecatedSimpleLanguageSyntaxMigrationTask extends AbstractRouteItemComponentMigrationTask {

	private static String WRAPPER_START = "${";
	private static String WRAPPER_END = "}";

	private String valueExpression = null;
	private String valueLanguage = null;

	@Override
	public String getComponentNameRegex() {
		return "cAggregate|cIdempotentConsumer|cLoadBalancer|cMessageFilter|cMessageRouter|cRecipientList|cSetBody|cSetHeader|cSetProperty|cSplitter|cWireTap";
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2021, 6, 14, 13, 0, 0);
		return gc.getTime();
	}

	@Override
	protected boolean execute(NodeType node) throws Exception {
		return wrapSpecialCommands(node);
	}

	private boolean wrapSpecialCommands(NodeType currentNode) throws PersistenceException {

		boolean save = false;
		boolean isOldFormatExpression = false;

		ElementParameterType paramLanguages = UtilTool.findParameterType(currentNode, "LANGUAGES");
		ElementParameterType paramExpression = UtilTool.findParameterType(currentNode, "EXPRESSION");

		if (paramLanguages == null || paramExpression == null) {
			return false;
		}

		valueLanguage = paramLanguages.getValue();
		valueExpression = paramExpression.getValue();

		if (valueLanguage == null || valueExpression == null) {
			return false;
		}

		valueExpression = valueExpression.replaceAll("\"", "");

		if (valueLanguage.equalsIgnoreCase("SIMPLE")) {

			isOldFormatExpression = isOldFormatExpression(valueExpression, false);

			if (isOldFormatExpression) {
				save = true;
			}

			String wrappedBodyAndHeaderExpressions = wrapBodyAndHeaderExpressions(valueExpression);
			if (!valueExpression.equalsIgnoreCase(wrappedBodyAndHeaderExpressions)) {
				valueExpression = wrappedBodyAndHeaderExpressions;
				save = true;
			}

			if (save) {
				for (Object e : currentNode.getElementParameter()) {
					ElementParameterType p = (ElementParameterType) e;
					if ("EXPRESSION".equals(p.getName())) {
						if (isOldFormatExpression) {
							p.setValue("\"" + WRAPPER_START + valueExpression + WRAPPER_END + "\"");
						} else {
							p.setValue("\"" + valueExpression + "\"");
						}
						break;
					}
				}
			}

		}

		return save;

	}

	private String wrapBodyAndHeaderExpressions(String valueExpression) {
		String findRegExp = "(?<!\\$\\{)((body|in.body|header|headers|header|in.header|in.headers)\\[\\d*\\])(?!\\})";
		String replaceRegExp = "\\$\\{$1\\}";
		return valueExpression.replaceAll(findRegExp, replaceRegExp);
	}

	private static boolean isOldFormatExpression(String basicExpression, boolean strict) {

		if (isEmpty(basicExpression) || basicExpression.contains(WRAPPER_START)
				|| basicExpression.contains(WRAPPER_END)) {
			return false;

		}

		boolean result = createSimpleExpressionDirectly(basicExpression);
		if (result) {
			return true;
		}

		// body and headers first
		result = createSimpleExpressionBodyOrHeader(basicExpression, strict);
		if (result) {
			return true;
		}

		// camelContext OGNL
		String remainder = ifStartsWithReturnRemainder("camelContext", basicExpression);
		if (remainder != null) {
			boolean invalid = isInvalidValidOgnlExpression(remainder);
			if (invalid) {
				return false;
			}
			return true;
		}

		// Exception OGNL
		remainder = ifStartsWithReturnRemainder("exception", basicExpression);
		if (remainder != null) {
			boolean invalid = isInvalidValidOgnlExpression(remainder);
			if (invalid) {
				return false;
			}
			return true;
		}

		// property
		remainder = ifStartsWithReturnRemainder("property", basicExpression);
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("exchangeProperty", basicExpression);
		}
		if (remainder != null) {
			// remove leading character (dot or ?)
			if (remainder.startsWith(".") || remainder.startsWith("?")) {
				remainder = remainder.substring(1);
			}
			// remove starting and ending brackets
			if (remainder.startsWith("[") && remainder.endsWith("]")) {
				remainder = remainder.substring(1, remainder.length() - 1);
			}

			// validate syntax
			boolean invalid = isInvalidValidOgnlExpression(remainder);
			if (invalid) {
				return false;
			}

			if (isValidOgnlExpression(remainder)) {
				// ognl based property
				return true;
			} else {
				// regular property
				return true;
			}
		}

		// system property
		remainder = ifStartsWithReturnRemainder("sys.", basicExpression);
		if (remainder != null) {
			return true;
		}
		remainder = ifStartsWithReturnRemainder("sysenv.", basicExpression);
		if (remainder != null) {
			return true;
		}

		// exchange OGNL
		remainder = ifStartsWithReturnRemainder("exchange", basicExpression);
		if (remainder != null) {
			boolean invalid = isInvalidValidOgnlExpression(remainder);
			if (invalid) {
				return false;
			}
			return true;
		}

		// file: prefix
		remainder = ifStartsWithReturnRemainder("file:", basicExpression);
		if (remainder != null) {
			boolean fileExpression = createSimpleFileExpression(remainder, strict);
			if (fileExpression) {
				return true;
			}
		}

		// date: prefix
		remainder = ifStartsWithReturnRemainder("date:", basicExpression);
		if (remainder != null) {
			String[] parts = remainder.split(":", 2);
			if (parts.length == 1) {
				return true;
			} else if (parts.length == 2) {
				return true;
			}
		}

		// date-with-timezone: prefix
		remainder = ifStartsWithReturnRemainder("date-with-timezone:", basicExpression);
		if (remainder != null) {
			String[] parts = remainder.split(":", 3);
			if (parts.length < 3) {
				return false;
			}
			return true;
		}

		// bean: prefix
		remainder = ifStartsWithReturnRemainder("bean:", basicExpression);
		if (remainder != null) {
			return true;
		}

		// properties: prefix
		remainder = ifStartsWithReturnRemainder("properties:", basicExpression);
		if (remainder != null) {
			String[] parts = remainder.split(":");
			if (parts.length > 2) {
				return false;
			}
			return true;
		}

		// properties-location: prefix
		remainder = ifStartsWithReturnRemainder("properties-location:", basicExpression);
		if (remainder != null) {
			String[] parts = remainder.split(":");
			if (parts.length > 3) {
				return false;
			}

			String locations = null;
			String key = remainder;
			if (parts.length >= 2) {
				locations = before(remainder, ":");
				key = after(remainder, ":");
			}
			return true;
		}

		// ref: prefix
		remainder = ifStartsWithReturnRemainder("ref:", basicExpression);
		if (remainder != null) {
			return true;
		}

		// const: prefix
		remainder = ifStartsWithReturnRemainder("type:", basicExpression);
		if (remainder != null) {
			// we want to cache this expression so we wont re-evaluate it as the
			// type/constant wont change
			return true;
		}

		// miscellaneous basicExpressions
		boolean misc = createSimpleExpressionMisc(basicExpression);
		if (misc) {
			return true;
		}

		if (strict) {
			return false;
		} else {
			return false;
		}
	}

	private static boolean createSimpleExpressionDirectly(String expression) {
		if (isEqualToAny(expression, "body", "in.body")) {
			return true;
		} else if (equal(expression, "out.body")) {
			return true;
		} else if (equal(expression, "id")) {
			return true;
		} else if (equal(expression, "exchangeId")) {
			return true;
		} else if (equal(expression, "exchange")) {
			return true;
		} else if (equal(expression, "exception")) {
			return true;
		} else if (equal(expression, "exception.message")) {
			return true;
		} else if (equal(expression, "exception.stacktrace")) {
			return true;
		} else if (equal(expression, "threadName")) {
			return true;
		} else if (equal(expression, "camelId")) {
			return true;
		} else if (equal(expression, "routeId")) {
			return true;
		} else if (equal(expression, "null")) {
			return true;
		}

		return false;
	}

	private static boolean createSimpleExpressionBodyOrHeader(String basicExpression, boolean strict) {
		// bodyAs
		String remainder = ifStartsWithReturnRemainder("bodyAs(", basicExpression);
		if (remainder != null) {
			String type = before(remainder, ")");
			if (type == null) {
				return false;
			}
			type = removeQuotes(type);
			remainder = after(remainder, ")");
			if (isNotEmpty(remainder)) {
				boolean invalid = isInvalidValidOgnlExpression(remainder);
				if (invalid) {
					return false;
				}
				return true;
			} else {
				return true;
			}

		}
		// mandatoryBodyAs
		remainder = ifStartsWithReturnRemainder("mandatoryBodyAs(", basicExpression);
		if (remainder != null) {
			String type = before(remainder, ")");
			if (type == null) {
				return false;
			}
			type = removeQuotes(type);
			remainder = after(remainder, ")");
			if (isNotEmpty(remainder)) {
				boolean invalid = isInvalidValidOgnlExpression(remainder);
				if (invalid) {
					return false;
				}
				return true;
			} else {
				return true;
			}
		}

		// body OGNL
		remainder = ifStartsWithReturnRemainder("body", basicExpression);
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("in.body", basicExpression);
		}
		if (remainder != null) {
			// OGNL must start with a . ? or [
			boolean ognlStart = remainder.startsWith(".") || remainder.startsWith("?") || remainder.startsWith("[");
			boolean invalid = !ognlStart || isInvalidValidOgnlExpression(remainder);
			if (invalid) {
				return false;
			}
			return true;
		}

		// headerAs
		remainder = ifStartsWithReturnRemainder("headerAs(", basicExpression);
		if (remainder != null) {
			String keyAndType = before(remainder, ")");
			if (keyAndType == null) {
				return false;
			}

			String key = before(keyAndType, ",");
			String type = after(keyAndType, ",");
			remainder = after(remainder, ")");
			if (isEmpty(key) || isEmpty(type) || isNotEmpty(remainder)) {
				return false;
			}
			key = removeQuotes(key);
			type = removeQuotes(type);
			return true;
		}

		// headers basicExpression
		if ("in.headers".equals(basicExpression) || "headers".equals(basicExpression)) {
			return true;
		}

		// in header basicExpression
		remainder = ifStartsWithReturnRemainder("in.headers", basicExpression);
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("in.header", basicExpression);
		}
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("headers", basicExpression);
		}
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("header", basicExpression);
		}
		if (remainder != null) {
			// remove leading character (dot or ?)
			if (remainder.startsWith(".") || remainder.startsWith("?")) {
				remainder = remainder.substring(1);
			}
			// remove starting and ending brackets
			if (remainder.startsWith("[") && remainder.endsWith("]")) {
				remainder = remainder.substring(1, remainder.length() - 1);
			}
			// remove quotes from key
			String key = removeLeadingAndEndingQuotes(remainder);

			// validate syntax
			boolean invalid = isInvalidValidOgnlExpression(key);
			if (invalid) {
				return false;
			}

			if (isValidOgnlExpression(key)) {
				// ognl based header
				return true;
			} else {
				// regular header
				return true;
			}
		}

		// out header basicExpression
		remainder = ifStartsWithReturnRemainder("out.header.", basicExpression);
		if (remainder == null) {
			remainder = ifStartsWithReturnRemainder("out.headers.", basicExpression);
		}
		if (remainder != null) {
			return true;
		}

		return false;
	}

	private static boolean createSimpleFileExpression(String remainder, boolean strict) {
		if (equal(remainder, "name")) {
			return true;
		} else if (equal(remainder, "name.noext")) {
			return true;
		} else if (equal(remainder, "name.noext.single")) {
			return true;
		} else if (equal(remainder, "name.ext") || equal(remainder, "ext")) {
			return true;
		} else if (equal(remainder, "name.ext.single")) {
			return true;
		} else if (equal(remainder, "onlyname")) {
			return true;
		} else if (equal(remainder, "onlyname.noext")) {
			return true;
		} else if (equal(remainder, "onlyname.noext.single")) {
			return true;
		} else if (equal(remainder, "parent")) {
			return true;
		} else if (equal(remainder, "path")) {
			return true;
		} else if (equal(remainder, "absolute")) {
			return true;
		} else if (equal(remainder, "absolute.path")) {
			return true;
		} else if (equal(remainder, "length") || equal(remainder, "size")) {
			return true;
		} else if (equal(remainder, "modified")) {
			return true;
		}
		if (strict) {
			return false;
		}
		return false;
	}

	private static boolean createSimpleExpressionMisc(String basicExpression) {
		String remainder;

		// random basicExpression
		remainder = ifStartsWithReturnRemainder("random(", basicExpression);
		if (remainder != null) {
			String values = before(remainder, ")");
			if (values == null || isEmpty(values)) {
				return false;
			}
			if (values.contains(",")) {
				String[] tokens = values.split(",", -1);
				if (tokens.length > 2) {
					return false;
				}
				return true;
			} else {
				return true;
			}
		}

		// skip basicExpression
		remainder = ifStartsWithReturnRemainder("skip(", basicExpression);
		if (remainder != null) {
			String values = before(remainder, ")");
			if (values == null || isEmpty(values)) {
				return false;
			}
			return true;
		}

		// collate basicExpression
		remainder = ifStartsWithReturnRemainder("collate(", basicExpression);
		if (remainder != null) {
			String values = before(remainder, ")");
			if (values == null || isEmpty(values)) {
				return false;
			}
			return true;
		}

		// messageHistory basicExpression
		remainder = ifStartsWithReturnRemainder("messageHistory", basicExpression);
		if (remainder != null) {
			return true;
		} else if (equal(basicExpression, "messageHistory")) {
			return true;
		}
		return false;
	}

	private static String ifStartsWithReturnRemainder(String prefix, String text) {
		if (text.startsWith(prefix)) {
			String remainder = text.substring(prefix.length());
			if (remainder.length() > 0) {
				return remainder;
			}
		}
		return null;
	}

	/**
	 * A helper method for comparing objects for equality while handling nulls
	 */
	public static boolean equal(Object a, Object b) {
		return equal(a, b, false);
	}

	/**
	 * A helper method for comparing objects for equality while handling nulls
	 */
	public static boolean equal(final Object a, final Object b, final boolean ignoreCase) {
		if (a == b) {
			return true;
		}

		if (a == null || b == null) {
			return false;
		}

		if (ignoreCase) {
			if (a instanceof String && b instanceof String) {
				return ((String) a).equalsIgnoreCase((String) b);
			}
		}

		if (a.getClass().isArray() && b.getClass().isArray()) {
			// uses array based equals
			return Objects.deepEquals(a, b);
		} else {
			// use regular equals
			return a.equals(b);
		}
	}

	/**
	 * Tests whether the value is <b>not</b> <tt>null</tt>, an empty string or an
	 * empty collection/map.
	 *
	 * @param value the value, if its a String it will be tested for text length as
	 *              well
	 * @return true if <b>not</b> empty
	 */
	public static boolean isNotEmpty(Object value) {
		if (value == null) {
			return false;
		} else if (value instanceof String) {
			String text = (String) value;
			return text.trim().length() > 0;
		} else if (value instanceof Collection) {
			return !((Collection<?>) value).isEmpty();
		} else if (value instanceof Map) {
			return !((Map<?, ?>) value).isEmpty();
		} else {
			return true;
		}
	}

	public static boolean isEqualToAny(Object object, Object... values) {
		for (Object value : values) {
			if (equal(object, value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests whether the value is <tt>null</tt> or an empty string.
	 *
	 * @param value the value, if its a String it will be tested for text length as
	 *              well
	 * @return true if empty
	 */
	public static boolean isEmpty(Object value) {
		return !isNotEmpty(value);
	}

	/**
	 * Counts the number of times the given char is in the string
	 *
	 * @param s  the string
	 * @param ch the char
	 * @return number of times char is located in the string
	 */
	public static int countChar(String s, char ch) {
		if (isEmpty(s)) {
			return 0;
		}

		int matches = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (ch == c) {
				matches++;
			}
		}

		return matches;
	}

	/**
	 * Returns the string before the given token
	 *
	 * @param text   the text
	 * @param before the token
	 * @return the text before the token, or <tt>null</tt> if text does not contain
	 *         the token
	 */
	public static String before(String text, String before) {
		if (!text.contains(before)) {
			return null;
		}
		return text.substring(0, text.indexOf(before));
	}

	/**
	 * Returns the string after the given token
	 *
	 * @param text  the text
	 * @param after the token
	 * @return the text after the token, or <tt>null</tt> if text does not contain
	 *         the token
	 */
	public static String after(String text, String after) {
		if (!text.contains(after)) {
			return null;
		}
		return text.substring(text.indexOf(after) + after.length());
	}

	/**
	 * Removes all quotes (single and double) from the string
	 *
	 * @param s the string
	 * @return the string without quotes (single and double)
	 */
	public static String removeQuotes(String s) {
		if (isEmpty(s)) {
			return s;
		}

		s = replaceAll(s, "'", "");
		s = replaceAll(s, "\"", "");
		return s;
	}

	/**
	 * Replaces all the from tokens in the given input string.
	 * <p/>
	 * This implementation is not recursive, not does it check for tokens in the
	 * replacement string.
	 *
	 * @param input the input string
	 * @param from  the from string, must <b>not</b> be <tt>null</tt> or empty
	 * @param to    the replacement string, must <b>not</b> be empty
	 * @return the replaced string, or the input string if no replacement was needed
	 * @throws IllegalArgumentException if the input arguments is invalid
	 */
	public static String replaceAll(String input, String from, String to) {
		if (isEmpty(input)) {
			return input;
		}
		if (from == null) {
			throw new IllegalArgumentException("from cannot be null");
		}
		if (to == null) {
			// to can be empty, so only check for null
			throw new IllegalArgumentException("to cannot be null");
		}

		// fast check if there is any from at all
		if (!input.contains(from)) {
			return input;
		}

		final int len = from.length();
		final int max = input.length();
		StringBuilder sb = new StringBuilder(max);
		for (int i = 0; i < max;) {
			if (i + len <= max) {
				String token = input.substring(i, i + len);
				if (from.equals(token)) {
					sb.append(to);
					// fast forward
					i = i + len;
					continue;
				}
			}

			// append single char
			sb.append(input.charAt(i));
			// forward to next
			i++;
		}
		return sb.toString();
	}

	/**
	 * Removes all leading and ending quotes (single and double) from the string
	 *
	 * @param s the string
	 * @return the string without leading and ending quotes (single and double)
	 */
	public static String removeLeadingAndEndingQuotes(String s) {
		if (isEmpty(s)) {
			return s;
		}

		String copy = s.trim();
		if (copy.startsWith("'") && copy.endsWith("'")) {
			return copy.substring(1, copy.length() - 1);
		}
		if (copy.startsWith("\"") && copy.endsWith("\"")) {
			return copy.substring(1, copy.length() - 1);
		}

		// no quotes, so return as-is
		return s;
	}

	public static boolean isInvalidValidOgnlExpression(String expression) {
		if (isEmpty(expression)) {
			return false;
		}

		if (!expression.contains(".") && !expression.contains("[") && !expression.contains("]")) {
			return false;
		}

		// the brackets should come in pair
		int bracketBegin = countChar(expression, '[');
		int bracketEnd = countChar(expression, ']');
		if (bracketBegin > 0 || bracketEnd > 0) {
			return bracketBegin != bracketEnd;
		}

		// check for double dots
		if (expression.contains("..")) {
			return true;
		}

		return false;
	}

	/**
	 * Tests whether or not the given String is a Camel OGNL expression.
	 * <p/>
	 * An expression is considered an OGNL expression when it contains either one of
	 * the following chars: . or [
	 *
	 * @param expression the String
	 * @return <tt>true</tt> if a Camel OGNL expression, otherwise <tt>false</tt>.
	 */
	public static boolean isValidOgnlExpression(String expression) {
		if (isEmpty(expression)) {
			return false;
		}

		// the brackets should come in a pair
		int bracketBegin = countChar(expression, '[');
		int bracketEnd = countChar(expression, ']');
		if (bracketBegin > 0 && bracketEnd > 0) {
			return bracketBegin == bracketEnd;
		}

		return expression.contains(".");
	}

}
