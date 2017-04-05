package ua.deamonish.modulesystem.modules.saveworld;

import org.bukkit.plugin.Plugin;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.module.ModuleManager;
import ua.deamonish.modulesystem.util.Config;


public class ModuleSaveWorlds extends Module {

	private static ModuleSaveWorlds instance;
	protected int delay;

	public static ModuleSaveWorlds getInstance() {
		return instance;
	}

	private RunnableSaveWorlds runnable;

	public ModuleSaveWorlds(Plugin plugin) {
		super("save_world", ModuleManager.PRIORITY_HIGH, plugin, new Config(plugin, "saveInConfig-worlds.yml"));
		instance = this;
	}

	@Override
	public void onEnable() {
		this.getConfig().setIfNotExist("delay-saveInConfig-worlds-second", 900);

		delay = this.getConfig().getInt("delay-saveInConfig-worlds-second");
		if (delay > 0) {
			this.getLogger().info("Миры будут сохраняться каждые " + delay + " секунд.");
			if (runnable != null)
				runnable.cancel();
			runnable = new RunnableSaveWorlds().start();
		}
	}

	@Override
	public void onDisable() {
		if (runnable != null)
			runnable.cancel();
	}

	@Override
	public void onReload() {
		this.standartReload();
	}

	public RunnableSaveWorlds getRunnable() {
		return runnable;
	}
}
