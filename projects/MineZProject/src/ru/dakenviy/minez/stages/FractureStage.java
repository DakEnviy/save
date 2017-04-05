package ru.dakenviy.minez.stages;

/**
 * Стадия перелома ноги. Со временем, игрок с поломаной ногой двигается медленее.
 */
public enum FractureStage {

    // Сейчас указаны дефолтные значения. Они настраиваются в конфиге.
    LOW(0, 1, 0.18f, "Кажется я сломал ногу..."),
    MEDIUM(180000, 2, 0.16f,  "Нога ужасно болит..."),
    HIGH(300000, 3, 0.14f, "Я не могу идти...");

    FractureStage(long time, double damage, float speed, String message) {
        this.time = time;
        this.damage = damage;
        this.speed = speed;
        this.message = message;
    }

    private long time; // Время в милисикундах между стадиями.
    private double damage; // Урон наносимый при смене стадии.
    private float speed; // Скорость хотьбы, с переломом.
    private String message; // Сообщение которое пишется, при смене стадии.

    public FractureStage getNextStage(){
        int ordinal = this.ordinal()+1;
        if(ordinal < FractureStage.values().length){
            for(FractureStage stages : FractureStage.values()){
                if(stages.ordinal() == ordinal){
                    return stages;
                }
            }

        }
        return this;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public double getDamage() {
        return damage;
    }

    public String getMessage() {
        return message;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
