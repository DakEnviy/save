package ru.dakenviy.minez.mysql;

import ua.deamonish.modulesystem.modules.mysql.ModuleMySQL;

import java.sql.Connection;

public enum MySQL {

	MAINEZ_PLAYER_TABLE("minez_players",
			"CREATE TABLE IF NOT EXISTS `minez_players` (\n" +
			"  `player_name` varchar(17) NOT NULL,\n" +
			"  `data` text NOT NULL,\n" +
			"  PRIMARY KEY (`player_name`)\n" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"),
	;

	public void setTable(String table) {
		this.table = table;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	private String table;
	private String create;

	MySQL(String table, String create) {
		this.table = table;
		this.create = create;

	}

	public String getTable() {
		return table;
	}

	/**
	 * Получить код создания таблицы
	 * @return код создания таблицы
	 */
	public String getCreateQuery() {
		return create;
	}

	@Override
	public String toString() {
		return table;
	}

	public static ModuleMySQL moduleMySQL = ModuleMySQL.getInstance();

	/**
	 * Выполнить запрос (лучше так не делать)
	 * @param query
	 */
	public static void execute(String query) {
		moduleMySQL.execute(query);
	}

	/**
	 * Получить mysql-соединение
	 *
	 * @return mysql-соединение
	 */
	public static Connection getConnection() {
		return moduleMySQL.getConnection();
	}
}
