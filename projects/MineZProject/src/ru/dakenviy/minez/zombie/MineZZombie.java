package ru.dakenviy.minez.zombie;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import ru.dakenviy.minez.minez.ModuleMineZ;

import java.lang.reflect.Field;
import java.util.List;

@Deprecated
public class MineZZombie extends EntityZombie implements IMonster {

    private final float bw;

    public static Zombie spawn(Location location) {
        WorldServer mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        MineZZombie customEntity = new MineZZombie(mcWorld);
        customEntity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld) location.getWorld()).addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (Zombie) customEntity.getBukkitEntity();
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(100.0);
    }

    public void setOnFire(final int i) {
    }

    public int getExpReward() {
        return 0;
    }

    public void die(DamageSource d) {
        super.die(d);
        this.dropSpecialLoot();
    }

    public MineZZombie(World world) {
        super(world);

        this.bw = ModuleMineZ.getZombieSpeed() / 100.0f;
        Zombie z = (Zombie) this.getBukkitEntity();
        z.setBaby(false);
        z.setCanPickupItems(false);
        z.getEquipment().clear();
        z.setMaxHealth((double) ModuleMineZ.getZombieHealth());
        z.setHealth((double) ModuleMineZ.getZombieHealth());
        try {

            final Field e = PathfinderGoalSelector.class.getDeclaredField("b");
            e.setAccessible(true);
            e.set(this.goalSelector, Sets.newLinkedHashSet());
            e.set(this.targetSelector, Sets.newLinkedHashSet());
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException var4) {
            var4.printStackTrace();
        }
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, (double) this.bw, false));
        this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, (double) this.bw));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0f));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(2, new PathfinderGoalMineZZombie(this, EntityHuman.class, true));
    }


    /**
     * Метод, что отвечает за выпадение лута из зомби. Который появился, в следствии смерти игрока, от рук зомби.
     */
    private void dropSpecialLoot() {
        final Zombie z = (Zombie) this.getBukkitEntity();
        final List deadPlayerMeta = z.getMetadata("dead_player");
        if (deadPlayerMeta != null && deadPlayerMeta.size() >= 1) {
            final List<ItemStack> inv = (List<ItemStack>) ((FixedMetadataValue) deadPlayerMeta.get(0)).value();
            inv.stream().filter(i -> !ModuleMineZ.isHalfLootDrop() || Math.random() >= 0.5)
                    .forEach(i -> z.getWorld().dropItem(z.getLocation().add(Math.random() * 3.0 - 1.5, Math.random() * 2.0 - 1.5, Math.random() * 2.0 - 1.5), i));
        }
    }
}


