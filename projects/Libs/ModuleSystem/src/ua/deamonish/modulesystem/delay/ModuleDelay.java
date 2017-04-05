package ua.deamonish.modulesystem.delay;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.module.ModuleManager;

import java.util.ArrayList;

/**
 * Устарел, лучше Bukkit.getScheduler()
 */
public class ModuleDelay extends Module {

	private static ModuleDelay instance;

	public static ModuleDelay getInstance() {
		return instance;
	}

	private ArrayList<Delay> delays = new ArrayList<>();


	public ModuleDelay(Plugin plugin) {
		super("delay", ModuleManager.PRIORITY_LOW, plugin, null);
		instance = this;
	}

	@Override
	public void onEnable() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Delay delay : delays) {
					try {
						delay.tick();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskTimer(this.getPlugin(), 20*60, 20*60);
	}

	@Override
	public void onDisable() {

	}

	@Override
	public void onReload() {

	}

	/**
	 * Устарел, лучше Bukkit.getScheduler()
	 */
	@Deprecated
	public void add(Delay delay) {
		this.delays.add(delay);
	}

	public void remove(Delay delay) {
		this.delays.remove(delay);
	}

	public ArrayList<Delay> getDelays() {
		return delays;
	}
}
