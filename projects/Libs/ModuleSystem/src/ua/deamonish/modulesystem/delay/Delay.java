package ua.deamonish.modulesystem.delay;

public abstract class Delay {

	private int min;
	private int tick = 0;

	/**
	 * Будет выполняться время от времени
	 * @param min кол-во минут, через которое run() будет запускаться
	 */
	public Delay(int min) {
		this.min = min;
	}

	public abstract void run();

	protected void tick() {
		if (tick++ == min) {
			tick = 0;
			this.run();
		}
	}

	public void setMin(int min) {
		this.min = min;
		this.tick = 0;
	}
}
