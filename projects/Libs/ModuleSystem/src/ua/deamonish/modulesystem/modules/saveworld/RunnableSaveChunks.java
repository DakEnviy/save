package ua.deamonish.modulesystem.modules.saveworld;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Сохраняем чанки мира
 */
public class RunnableSaveChunks extends BukkitRunnable {

	private ModuleSaveWorlds moduleSaveWorlds = ModuleSaveWorlds.getInstance();

	private World world;
	private Chunk[] chunks;
	private int i      = 0;

	private int save = 0;

	public RunnableSaveChunks(World world) {
		this.world = world;
		this.chunks = world.getLoadedChunks();
		this.moduleSaveWorlds.getLogger().info("Сохранения чанков мира '" + this.world.getName() + "', " +
				" кол-во: " + this.chunks.length + ", примерное время: " + (this.chunks.length * 1F / 20) + " секунд.");
	}

	@Override
	public void run() {
		if (i < chunks.length) {
			try {
				if (this.chunks[i].unload(true))
					this.save++;
			} catch(Exception e) {
				System.out.println("Ошибка сохранения мира!");
				e.printStackTrace();
			}
		} else {
			this.cancel();
		}
		this.i++;
	}

	public synchronized void start() {
		this.runTaskTimer(ModuleSaveWorlds.getInstance().getPlugin(), 1L, 1L);
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
		super.cancel();
		this.moduleSaveWorlds.getLogger().info(
				"Сохранения чанков мира '" + this.world.getName() + "' завершено, " +
						this.save + "/" + this.i + " сохранено."
		);
	}
}
