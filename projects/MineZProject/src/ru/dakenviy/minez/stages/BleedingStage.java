package ru.dakenviy.minez.stages;

/**
 * Перечисление, представляющее стадии кровотечения.
 * Хранит в себе значения по кол-ву урона, наносимого персонажу стадией.
 * А так же время, через которое, наступает стадия.
 */
public enum BleedingStage {

    // Сейчас указаны дефолтные значения. Они настраиваются в конфиге.
    LOW(60000, 1, "Я истекаю кровью..."),
    MEDIUM(180000, 2, "Я потерял много крови..."),
    HIGH(300000, 3, "Кажется я умираю, я потерял слишком много крови...");

    private long time; // Время в милисикундах.
    private double damage;
    private String message; // Сообщение которое пишется, при смене стадии.

    BleedingStage(long time, double damage, String message) {
        this.time = time;
        this.damage = damage;
        this.message = message;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public BleedingStage getNextStage() {
        int ordinal = this.ordinal()+1;
        if (ordinal < BleedingStage.values().length) {
            for (BleedingStage stage : BleedingStage.values()) {
                if (stage.ordinal() == ordinal) {
                    return stage;
                }
            }
        }
        return this;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long minutes) {
        this.time = minutes;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return name();
    }
}
