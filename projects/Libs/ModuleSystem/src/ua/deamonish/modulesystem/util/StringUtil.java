package ua.deamonish.modulesystem.util;

public class StringUtil {

	public static String toString(long l) {
		boolean negative = l < 0;
		if (negative)
			l = -l;
		char[] number = String.valueOf(l).toCharArray();
		double countData = (double) number.length / 3;
		int count = (int) (countData % 1 != 0.0D ? countData : countData - 1);
		char[] text = new char[number.length + count];

		int pos = 0;
		for (int i = number.length - 1; i >= 0; i--) {
			text[i + count] = number[i];
			if (++pos == 3 && count > 0) {
				pos = 0;
				count--;
				text[i + count] = '.';
			}
		}

		return (negative ? "-" : "") + new String(text);
	}

	public static String toStringLong(double i) {
		return toString((long) i);
	}

	/**
	 * Проверить начилие строки в строке
	 * @param string
	 * @param region
	 * @return
	 */
	public static boolean contains(String string, String region) {
		if (string == null
				|| region == null
				|| string.length() < region.length()) {
			return false;
		}

		if (region.length() == 0)
			return true;

		char[] value = string.toCharArray();
		char[] check = region.toCharArray();

		int pos = 0;
		for (int next = 0; next < value.length; next++) {
			if (value[next] == check[pos]) {
				pos++;
				if (pos == check.length) {
					return true;
				}
			} else if (value[next] == check[0]) {
				pos = 1;
			} else {
				pos = 0;
			}
		}
		return false;
	}

	/**
	 * Проверить начилие строки в строке
	 * @param string
	 * @param region
	 * @return
	 */
	public static boolean containsIgnoreCase(String string, String region) {
		if (string == null
				|| region == null
				|| string.length() < region.length()) {
			return false;
		}

		if (region.length() == 0)
			return true;

		char[] value = string.toCharArray();
		char[] check = region.toCharArray();

		int pos = 0;
		for (int next = 0; next < value.length; next++) {
			if (value[next] == Character.toUpperCase(check[pos]) || value[next] == Character.toLowerCase(check[pos])) {
				pos++;
				if (pos == check.length) {
					return true;
				}
			} else if (value[next] == check[0]) {
				pos = 1;
			} else {
				pos = 0;
			}
		}
		return false;
	}

	/**
	 * Разделить строку, если она превышает лимит символов
	 * @param string
	 * @param limit количество символов
	 * @return
	 */
	public static String[] stack(String string, int limit) {
		if (string == null || string.length() <= limit || limit <= 0) {
			return new String[] { string };
		}

		char[] value = string.toCharArray();
		String[] result = new String[value.length / limit + 1];


		for (int pos = 0; pos < result.length; pos++) {
			int size = Math.min(limit, value.length - pos * limit);
			char[] line = new char[size];
			System.arraycopy(value, pos * limit, line, 0, size);
			result[pos] = new String(line);
		}
		return result;
	}

	/**
	 * Оптимизированая процедура замены в строке. Работает в 4 раза быстрее
	 * @param string
	 * @param r1
	 * @param r2
	 * @return
	 */
	public static String replace(String string, String r1, String r2) {
		if (string == null)
			return null;
		return replace(string, r1, r2, string.length());
	}

	/**
	 * Оптимизированая процедура замены в строке. Работает в 4 раза быстрее
	 * @param string
	 * @param r1
	 * @param r2
	 * @param limit количество замен
	 * @return
	 */
	public static String replace(String string, String r1, String r2, int limit) {
		if (limit < 1
				|| string == null
				|| r1 == null
				|| r2 == null
				|| string.length() == 0
				|| r1.length() == 0)
			return string;

		char[] rep1 = r1.toCharArray();
		char[] rep2 = r2.toCharArray();

		char[] value = string.toCharArray();

		if (rep1.length == 1 && rep2.length == 1) {
			return replace(value, rep1[0], rep2[0], limit);
		} else {
			return replace(value, rep1, rep2, limit);
		}
	}

	private static String replace(char[] value, char r1, char r2, int limit) {
		if (r1 != r2) {
			int replaced = 0;
			for (int next = 0; next < value.length; next++) {
				if (value[next] == r1) {
					value[next] = r2;
					if (++replaced == limit)
						break;
				}
			}
		}
		return new String(value);
	}

	private static String replace(char[] value, char[] rep1, char[] rep2, int limit) {
		int length1 = rep1.length;
		int length2 = rep2.length;

		int replaced = 0;
		int pos = 0;
		for (int next = 0; next < value.length; next++) {

			if (value[next] == rep1[pos]) {
				pos++;
				if (pos == length1) {

					int start = next - length1 + 1;

					char[] newValue = new char[value.length - length1 + length2];
					System.arraycopy(value, 0, newValue, 0, start);
					System.arraycopy(rep2, 0, newValue, start, length2);
					System.arraycopy(value, start + length1, newValue, start + length2, value.length - next - 1);

					next = start + length2 - 1;
					pos = 0;
					value = newValue;
					if (++replaced == limit)
						break;
				}
			} else if (value[next] == rep1[0]) {
				pos = 1;
			} else {
				pos = 0;
			}
		}

		return new String(value);
	}

	/**
	 * Разделить строку по определенному кол-ву символов, при этом не разрывая текст только по указаному символу
	 * @param line строка
	 * @param limit лимит символов
	 * @param probel этот символ будет заменяться на \n
	 * @return
	 */
	public static String stackWord(String line, int limit, char probel) {
		char[] value = line.toCharArray();
		//  i равно лимиту. Если i меньше длинны строки, то цикл идет дальше.
		// За каждый проход мы прибавляет limit символов.
		for (int i = limit; i < value.length; i += limit) {
			try {
				// если в проверяемом участке уже существует \n или ::, то пропускаем
				String verify_line = line.substring(i, i + limit - 1);
				int income_n_index = verify_line.indexOf("\n");
				int income_double_coma_index = verify_line.indexOf("::");
				if((income_n_index != -1) || (income_double_coma_index != -1)) {
					break;
				}
			} catch(StringIndexOutOfBoundsException ignored)
			{}
			// идем внутри limit цикла по каждому символу.
			for (int j = i; j < value.length; j++) {
				// если это символ-разделитель, то заменяем.
				if (value[j] == probel) {
					value[j] = '\n';
					break;
				}
			}
		}
		return new String(value);
	}

	public static String removeLast(String trace, int n) {
		if (n <= 0) {
			return trace;
		}
		char[] value = trace.toCharArray();
		if (value.length <= n) {
			return "";
		}
		char[] valueNew = new char[value.length - n];
		System.arraycopy(value, 0, valueNew, 0, valueNew.length);
		return new String(valueNew);
	}

	/**
	 * Вернуть строку, которая находится по центру между пробелами
	 */
	public static String getCentered(String part,int length) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < (length / 2 - part.length() / 2); i++)
		{
			builder.append(' ');
		}
		builder.append(part);

		for (int i = (length / 2 + part.length() / 2); i < length ; i++)
		{
			builder.append(' ');
		}

		return builder.toString();
	}
}
