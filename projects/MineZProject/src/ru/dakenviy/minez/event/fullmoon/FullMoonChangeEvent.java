package ru.dakenviy.minez.event.fullmoon;

import org.bukkit.event.*;


public class FullMoonChangeEvent extends Event {

    private static HandlerList handlerList = new HandlerList();
    private boolean startFullMoon;

    public FullMoonChangeEvent(boolean startFullMoon) {
        super(false);
        this.startFullMoon = startFullMoon;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }


    public boolean isStartFullMoon() {
        return startFullMoon;
    }

    public void setStartFullMoon(boolean endFullMoon) {
        this.startFullMoon = endFullMoon;
    }
}
