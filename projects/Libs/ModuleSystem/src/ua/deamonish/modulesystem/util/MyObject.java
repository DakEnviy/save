package ua.deamonish.modulesystem.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Удобная рефлекция объекта.
 * При создании указывайте объект:
 *
 * MyObject object = new MyObject(Object object);
 */
public class MyObject {

	private Object object;
	private Class clazz;

	/**
	 * Получаем из готового объекта
	 * @param object - объект, из которого извлекаем класс, для рефлексии.
	 */
	public MyObject (Object object) {
		this.object = object;
		this.clazz = object.getClass();
	}

	/**
	 * Получаем из класса
	 * @param clazz - класс
	 */
	public MyObject (Class clazz) {
		this.object = null;
		this.clazz = clazz;
	}

	/**
	 * Получает переменные.
	 * Не нужно беспокоиться о приватности переменной!
	 * Использование:
	 *
	 *   object.getField("temp");
	 *   где:
	 *    * "temp" - название переменной
	 *
	 * @param name название переменной
	 * @return MyObject этой переменной (ее можно получить через .getObject())
	 */
	public MyObject getField(String name) {
		try {
			Field field = null;
			Class c = this.clazz;
			do {
				try {
					field = c.getDeclaredField(name);
				} catch(NoSuchFieldException ignored) {
				} catch(Exception e) {
					e.printStackTrace();
				}
				if (field != null) {
					break;
				}
			} while((c = c.getSuperclass()) != null);

			if (field == null)
				throw new NoSuchFieldException(name);

			boolean isSetAccessible = false;
			if (!field.isAccessible()) {
				field.setAccessible(true);
				isSetAccessible = true;
			}
			Object get = field.get(this.object);
			if (isSetAccessible) {
				field.setAccessible(false);
			}
			return get == null ? null : new MyObject(get);
		} catch (Exception e) {
			Bukkit.getLogger().severe("Error in: " + this.getObject().getClass());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Установить значение переменной
	 * @param name имя переменной
	 * @param value значение
	 */
	public void setField(String name, Object value) {
		try {

			Class c = this.clazz;
			Field field = null;
			do {
				try {
					field = c.getDeclaredField(name);
				} catch(Exception ignored) {}
				if (field != null) {
					break;
				}
			} while((c = c.getSuperclass()) != null);

			boolean isSetAccessible = false;
			if (!field.isAccessible()) {
				field.setAccessible(true);
				isSetAccessible = true;
			}
			field.set(this.object, value instanceof MyObject ? ((MyObject)value).getObject() : value);
			if (isSetAccessible) {
				field.setAccessible(false);
			}
		} catch (Exception e) {
			System.out.println("Переменная '" + name + "' не найдена.");
			e.printStackTrace();
		}
	}

	/**
	 * Вызов методов.
	 * Не нужно беспокоится о приватности метода!
	 * Не нужно беспокоится о аргументах метода,
	 * моя система сама все кастит и т.п.
	 * Если этот метод функция, то вернет значение, иначе null.
	 * Использование:
	 *
	 *   object.invokeMethod("exetute", true, 10);
	 *   где:
	 *    * "exetute" - название метода
	 *    * true - любой объект (первый аргумент метода, boolean значение)
	 *    * 10 - любой объект (второй аргумент метода int)
	 *
	 * @param name название метода
	 * @param args аргументы (Можно указывать сразу MyObject, оно само достанет)
	 * @return если этот метод функция, то вернет что-то, иначе null
	 */
	public MyObject invokeMethod(String name, Object... args) {
		try {

			Method method = null;
			this.fixArgs(args);

			Class c = this.clazz;
			mainmain: do {
				main: for (Method m : c.getDeclaredMethods()) {

					if (!m.getName().equals(name))
						continue;
					if (m.getParameterCount() != args.length)
						continue;

					for (int i = 0; i < m.getParameterCount(); i++) {
						if (args[i] != null) {
							if (!m.getParameterTypes()[i].isInstance(args[i])) {
								continue main;
							}
						}
					}
					method = m;
					break mainmain;
				}
			} while((c = c.getSuperclass()) != null);

			if (method == null) {
				throw new NullPointerException("Метод не найден.");
			}
			boolean isSetAccessible = false;
			if (!method.isAccessible()) {
				method.setAccessible(true);
				isSetAccessible = true;
			}
			Object returnObject = method.invoke(this.object, args);
			if (isSetAccessible) {
				method.setAccessible(false);
			}
			return returnObject == null ? null : new MyObject(returnObject);
		} catch (Exception e) {
			Bukkit.getLogger().severe("Error in: " + this.getObject().getClass());
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Получить объект, с которым работаем
	 * @return - дженерик-объект
	 * @throws ClassCastException - если сохранять в переменную друго-го типа.
	 */
	public <T> T  getObject() {
		return (T) object;
	}

	private void fixArgs(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof MyObject) {
				args[i] = ((MyObject)args[i]).getObject();
			}
		}
	}

	public static MyObject wrap(Object player) {
		return new MyObject(player);
	}

	public static MyObject wrap(Class clazz) {
		return new MyObject(clazz);
	}
}