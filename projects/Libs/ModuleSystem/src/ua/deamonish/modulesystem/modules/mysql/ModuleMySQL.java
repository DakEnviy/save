package ua.deamonish.modulesystem.modules.mysql;

import ua.deamonish.modulesystem.modules.delay.Delay;
import ua.deamonish.modulesystem.modules.delay.ModuleDelay;
import ua.deamonish.modulesystem.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.util.Config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class ModuleMySQL extends Module {

	private static ModuleMySQL instance;
	public static ModuleMySQL getInstance() {
		return instance;
	}

	private Connection connection;

	public ModuleMySQL(Plugin plugin) {
		super("mysql", -1000, plugin, new Config(plugin, "mysql.yml"));
		instance = this;
	}

	private String URL;
	private String USER;
	private String PASS;
	private String encoding;


	private ArrayList<PreparedStatement> executesLater = new ArrayList<>();
	private HashMap<Player, PreparedStatement> executesPlayerLater = new HashMap<>();

	private boolean first = true;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		ModuleDelay moduleDelay = ModuleDelay.getInstance();
		if (first) {
			first = false;
			moduleDelay.add(new Delay(10) {
				@Override
				public void run() {
					executesLater.forEach(ModuleMySQL.this::execute);
					executesLater.clear();
				}
			});
			moduleDelay.add(new Delay(1) {
				@Override
				public void run() {
					try {
						if (ModuleMySQL.this.connection.isClosed()) {
							ModuleMySQL.this.connectMySQL();
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			});
		}

		this.registerListenersThis();

		Config config = this.getConfig();
		config.setIfNotExist("url", "jdbc:mysql://localhost:3306/test");
		config.setIfNotExist("username", "root");
		config.setIfNotExist("password", "");
		config.setIfNotExist("encoding", "cp1251");

		//this.getLogger().info("Подключаемся к бд.");
		URL = config.getString("url");
		USER = config.getString("username");
		PASS = config.getString("password");
		encoding = config.getString("encoding");

		this.connectMySQL();
	}

	private void connectMySQL() {
		try {
			this.getLogger().info("Загрузка MySQL.");
			Properties properties = new Properties();
			properties.put("user", USER);
			properties.put("password", PASS);
			properties.put("characterEncoding", encoding);
			properties.put("useUnicode", "true");
			connection = DriverManager.getConnection(URL, properties);
			if (connection.isClosed()) {
				this.getLogger().severe("Подключение с MySQL не было установлено!");
				Bukkit.shutdown();
			} else {
				this.getLogger().info("Соединение с MySQL успешно установлено!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public void onDisable() {
		this.getLogger().info("Отключение сессии с базой данных.");
		try {
			if (connection != null) {
				executesLater.forEach(this::execute);
				executesPlayerLater.values().forEach(this::execute);
				executesPlayerLater.clear();
				executesLater.clear();
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReload() {
		this.getConfig().reload();
		this.onDisable();
		this.onEnable();
	}

	@EventHandler(priority = EventPriority.MONITOR) // Выполнится в last очередь
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		this.executesPlayerLater.entrySet().stream().filter(entry -> entry.getKey().equals(player)).forEach(entry -> {
			this.executesPlayerLater.remove(entry.getKey());
			this.execute(entry.getValue());
		});
	}


	/**
	 * Получить сессию mysql
	 */
	public Connection getConnection() {
		try {
			if (connection != null && connection.isClosed()) {
				connection = DriverManager.getConnection(URL, USER, PASS);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	/**
	 * Выполнить запрос.
	 * @param sqlQuery - SQL запрос, который нужно выполнить.
	 */
	public void execute(String sqlQuery) {
		try {
			PreparedStatement e = connection.prepareStatement(sqlQuery);
			e.execute();
			e.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("MySQL.");
			e.printStackTrace();
		}
	}

	public void execute(PreparedStatement preparedStatement) {
		try {
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("MySQL.");
			e.printStackTrace();
		}
	}

	public void executeLater(PreparedStatement preparedStatement) {
		this.executesLater.add(preparedStatement);
	}

	public void executePlayerLater(Player player, PreparedStatement preparedStatement) {
		this.executesPlayerLater.put(player, preparedStatement);
	}


}