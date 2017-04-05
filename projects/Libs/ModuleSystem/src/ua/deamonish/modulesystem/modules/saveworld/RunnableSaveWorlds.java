package ua.deamonish.modulesystem.modules.saveworld;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class RunnableSaveWorlds extends BukkitRunnable {

	private ModuleSaveWorlds moduleSaveWorlds = ModuleSaveWorlds.getInstance();

	@Override
	public void run() {
		this.moduleSaveWorlds.getLogger().info("Сохранение миров.");
		for (World world : Bukkit.getWorlds()) {
			world.save();
			new RunnableSaveChunks(world).start();
		}
		this.moduleSaveWorlds.getLogger().info("Сохранение мира завершено.");
	}

	public RunnableSaveWorlds start() {
		this.runTaskTimer(this.moduleSaveWorlds.getPlugin(), this.moduleSaveWorlds.delay * 20L, this.moduleSaveWorlds.delay * 20L);
		return this;
	}
}
