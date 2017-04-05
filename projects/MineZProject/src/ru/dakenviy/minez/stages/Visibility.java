package ru.dakenviy.minez.stages;


/**
 * Энум представляет собой селектор значений, для установки состояния полосы опыта, отображающий уровень шума игрока.
 */
public enum Visibility {
    // Сейчас указаны дефолтные значения. Они настраиваются в конфиге.
    SHIFT(0.35f, 5),
    WALK(0.65f, 10),
    RUN(0.95f, 14);

    Visibility(float expValue, int distance) {
        this.expValue = expValue;
        this.distance = distance;
    }

    private float expValue;
    private int distance;

    public float getExpValue() {
        return (float) (expValue + Math.random()*0.05);
    }

    public float getStaticExpValue(){
        return expValue;
    }

    public void setExpValue(float expValue) {
        this.expValue = expValue;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return name();
    }
}
