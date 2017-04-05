package ru.dakenviy.minez.zombie;

import net.minecraft.server.v1_10_R1.EntityCreature;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import org.bukkit.Location;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.stages.Visibility;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameManager;

import java.lang.reflect.Field;
@Deprecated
public class PathfinderGoalMineZZombie extends PathfinderGoalNearestAttackableTarget {
    public PathfinderGoalMineZZombie(EntityCreature entryCreature, Class<EntityHuman> entityHumanClass, boolean flag) {
        super(entryCreature, entityHumanClass, flag);
    }

    public boolean a() {
        Boolean ret = super.a();
        try {
            Field e = PathfinderGoalNearestAttackableTarget.class.getDeclaredField("d");
            e.setAccessible(true);
            EntityLiving currentTarget = (EntityLiving) e.get(this);
            if (ret && currentTarget instanceof EntityHuman) {
                EntityHuman h = (EntityHuman) currentTarget;
                Location hLoc = new Location(h.getWorld().getWorld(), h.locX, h.locY, h.locZ);
                Location zLoc = new Location(h.getWorld().getWorld(), this.e.locX, this.e.locY, this.e.locZ);
                Double dist = hLoc.distanceSquared(zLoc);
                MiniGame game = MiniGameManager.getInstance().getMiniGame(h.getWorld().getWorld().getName());
                Visibility vis = ((MineZPlayer) game.getPlayer(h.getName())).getCurrentVisibility();

                if (vis == Visibility.SHIFT && dist > vis.getDistance() * vis.getDistance() ||
                        vis == Visibility.WALK && dist > vis.getDistance() * vis.getDistance() ||
                        vis == Visibility.WALK && dist > vis.getDistance() * vis.getDistance()) {
                    e.set(this, null);
                    return false;
                }
                return true;
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
        return ret;
    }
}

