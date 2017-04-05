package ru.dakenviy.minez.zombie;

import net.minecraft.server.v1_10_R1.EntityZombie;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftZombie;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.dakenviy.minez.event.fullmoon.FullMoonChangeEvent;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.player.MineZPlayer;
import ru.dakenviy.minez.stages.Visibility;
import ru.dakenviy.minez.utils.NMS;
import ua.deamonish.modulesystem.modules.messages.Messages;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.minigamelib.MiniGamePlayer;
import ua.deamonish.modulesystem.module.Module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MineZMobListener implements Listener {

    private ModuleMineZ mineZ = Module.getInstance(ModuleMineZ.class);
    private MiniGameManager manager = MiniGameManager.getInstance();

    @EventHandler
    public void spawn(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld().equals(mineZ.getLobbyWorld())) {
            event.setCancelled(true);
            return;
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (event.getEntityType() == EntityType.ZOMBIE) {
                Zombie entity = (Zombie) event.getEntity();
                if (ModuleMineZ.isFullMoon()) {
                    setUpZombie(entity);
                } else {
                    setUpZombieFullMoon(entity);
                }
                return;
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void targetingEvent(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() != null && event.getTarget() != null) {
            if (event.getEntity().getType() == EntityType.ZOMBIE && event.getTarget().getType() == EntityType.PLAYER) {
                Player p = (Player) event.getTarget();
                MineZPlayer player = manager.getMiniGamePlayer(p);
                List<Entity> value = player.getMetadata("target");
                if (value != null) {
                    if (value.contains(event.getEntity())) {
                        return;
                    }
                    if (!zombieAggro(event.getEntity(), player)) {
                        event.setCancelled(true);
                    } else {
                        value.add(event.getEntity());
                    }
                } else {
                    List<Entity> entities = new ArrayList<>();
                    entities.add(event.getEntity());
                    player.setMetadata("target", entities);
                }
                if (!event.isCancelled()) {
                    mineZ.printDebug("Зомби " + event.getEntity().getUniqueId() + " сагрился на игрока " + p
                            .getName() + " на дистанции " + p.getLocation().distance(event.getEntity().getLocation()));
                }
            }

        }
    }

    @EventHandler
    public void entityCombustEvent(EntityCombustEvent event) {
        if (event.getEntityType() == EntityType.ZOMBIE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMoonChange(FullMoonChangeEvent event) {
        ModuleMineZ.setFullMoon(event.isStartFullMoon());
        if(event.isStartFullMoon()){
            Bukkit.getLogger().info("Началось полнолуние.");
        } else {
            Bukkit.getLogger().info("Полнолуние закончилось.");
        }

        for (MiniGame game : manager.getMiniGame()) {
            Collection<Zombie> zombies = game.getWorld().getEntitiesByClass(Zombie.class);
            for (Zombie zombie : zombies) {
                if (!event.isStartFullMoon()) {
                    setUpZombie(zombie);
                } else {
                    setUpZombieFullMoon(zombie);
                }
            }
            if (event.isStartFullMoon()) {
                NMS.sendTitle(game.getWorld().getPlayers(), Messages.getMessage("full_moon_start_title"), Messages
                        .getMessage("full_moon_start_subtitle"));
            } else {
                NMS.sendTitle(game.getWorld().getPlayers(), Messages.getMessage("full_moon_end_title"), Messages
                        .getMessage("full_moon_end_subtitle"));
            }
        }
    }


    private boolean zombieAggro(Entity zombie, MineZPlayer player) {
        Visibility currentVisibility = player.getCurrentVisibility();
        double distance = zombie.getLocation().distance(player.getPlayer().getLocation());
        return currentVisibility.getDistance() >= distance && distance <= ModuleMineZ.getDistanceExitCombat();

    }

    private void setUpZombie(Zombie zombie) {
        EntityZombie zombie1 = ((CraftZombie) zombie).getHandle();
        zombie1.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(ModuleMineZ.getZombieSpeed());
        zombie1.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(30f);
        zombie1.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(ModuleMineZ.getZombieDamage());
        zombie.setCanPickupItems(false);
        zombie.setMaxHealth(ModuleMineZ.getZombieHealth());
        zombie.setHealth(ModuleMineZ.getZombieHealth());
        zombie.getEquipment().clear();
    }

    private void setUpZombieFullMoon(Zombie zombie) {
        EntityZombie zombie1 = ((CraftZombie) zombie).getHandle();
        zombie1.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(ModuleMineZ.getZombieFullMoonSpeed());
        zombie1.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40f);
        zombie1.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(ModuleMineZ.getZombieFullMoonDamage());
        zombie.setCanPickupItems(false);
        zombie.setMaxHealth(ModuleMineZ.getZombieFullMoonHealth());
        zombie.setHealth(ModuleMineZ.getZombieFullMoonHealth());
        zombie.getEquipment().clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void projectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player p = (Player) event.getEntity().getShooter();
            if (p.hasMetadata("lure")) {
                event.getEntity().setMetadata("lure", new FixedMetadataValue(mineZ.getPlugin(), null));
                p.removeMetadata("lure", mineZ.getPlugin());
                mineZ.printDebug("Приманка запущенна игроком " + p.getName());
            }

            if (p.hasMetadata("grenade")) {
                event.getEntity().setMetadata("grenade", new FixedMetadataValue(mineZ.getPlugin(), null));
                p.removeMetadata("grenade", mineZ.getPlugin());
                mineZ.printDebug("Граната запущенна игроком " + p.getName());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void projectileHit(ProjectileHitEvent event) {
        // СПАСИБО БАКИТУ ЗА КОСТЫЛИ!
        if (event.getEntity() instanceof Snowball || event.getEntity() instanceof EnderPearl) {
            Location loc = event.getEntity().getLocation();
            Collection<LivingEntity> result = loc.getWorld().getNearbyEntities(loc, 3, 3, 3).stream()
                    .filter(e -> e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.PLAYER)
                    .map(entity -> ((LivingEntity) entity))
                    .collect(Collectors.toList());
            if (event.getEntity().hasMetadata("lure")) hitLure(event, result);
            if (event.getEntity().hasMetadata("grenade")) hitGrenade(event);
        }
    }

    private void hitGrenade(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player p = (Player) event.getEntity().getShooter();
            MineZPlayer player = manager.getMiniGamePlayer(p);
            Location loc = event.getEntity().getLocation();

            Collection<LivingEntity> entities = loc.getWorld().getNearbyEntities(loc, ModuleMineZ.getGrenadeDistance(), ModuleMineZ
                    .getGrenadeDistance(), ModuleMineZ
                    .getGrenadeDistance()).stream().filter(e -> e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.PLAYER).map(entity -> ((LivingEntity) entity)).collect(Collectors
                    .toList());

            spawnEffectGrenade(loc);
            for (LivingEntity entity : entities) {
                if (entities instanceof Zombie) {
                    if (((Zombie) entities).getHealth() - ModuleMineZ.getGrenadeDamage() <= 0) {
                        player.getStats().incriminateKillingZombie();
                    }
                    ((Zombie) entity).setTarget(p);
                }
                if (entities instanceof Player) {
                    if (((Player) entities).getHealth() - ModuleMineZ.getGrenadeDamage() <= 0) {
                        player.getStats().incriminateKillingPlayer();
                    }
                }
                entity.damage(ModuleMineZ.getGrenadeDamage());
            }
            mineZ.printDebug("Граната взорвалась в локации: "+loc.getX() +"."+loc.getY()+"."+loc.getZ());
        }
    }

    private void spawnEffectGrenade(Location loc) {
        int x0 = loc.getBlockX();
        int z0 = loc.getBlockZ();
        MiniGame game = MiniGameManager.getInstance().getMiniGame(loc.getWorld());
        for (MiniGamePlayer player : game.getPlayers().values()) {
            Player p = player.getPlayer();
            p.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 100f, 0f);
            p.playEffect(loc, Effect.LARGE_SMOKE, Effect.LARGE_SMOKE.getData());
            int r = ModuleMineZ.getGrenadeDistance();
            int y = loc.getBlockY();
            for(;r > 0; r=r-2){
                loc.setY(y);
                for(int a = 0; a<=180; a = a+10) {
                    double x = x0 + r * Math.sin(a);
                    double z = z0 - r * Math.cos(a);
                    loc.setZ(z);
                    loc.setX(x);
                    p.playEffect(loc, Effect.EXPLOSION_LARGE, Effect.EXPLOSION.getData());
                }
                y++;
            }


        }
    }

    private void hitLure(ProjectileHitEvent event, Collection<LivingEntity> hit) {
        Location loc = event.getEntity().getLocation();
        boolean isEntity = false;
        Collection<LivingEntity> aggredEntities = loc.getWorld().getNearbyEntities(loc, ModuleMineZ.getLureDistance(), ModuleMineZ
                .getLureDistance(), ModuleMineZ
                .getLureDistance()).stream().filter(e -> e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.PLAYER).map(entity -> ((LivingEntity) entity)).collect(Collectors
                .toList());
        for (LivingEntity entity : hit) {
            if (entity.hasMetadata("hited_lure")) { // Попал в энтити.
                entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (ModuleMineZ.getLureTime() * 20), 1));
                mineZ.printDebug("Приманка попала в энтити. Локация энтити: " + entity.getLocation().getX() + "." + entity.getLocation().getY() + "." + entity.getLocation().getZ());
                isEntity = true;
                for (LivingEntity zombie : aggredEntities) {
                    if (zombie != entity && zombie instanceof Zombie) {
                        ((Zombie) zombie).setTarget(entity); // Зомби бегут на цель.
                    }
                }
            }
        }

        if (!isEntity) { // Попал в блок.
            mineZ.printDebug("Приманка попала в блок. Локация блока: " + loc.getX() + "."+loc.getY()+"."+loc.getZ());
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setInvulnerable(true);
            stand.setVisible(false);
            stand.setMarker(true);
            for (LivingEntity entity : aggredEntities) {
                if (entity instanceof Zombie)
                    ((Zombie) entity).setTarget(stand); // Зомби бегут на стенд.
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(mineZ.getPlugin(), stand::remove, ModuleMineZ.getLureTime() * 20);
        }
    }

    @EventHandler
    public void entityDamageProjectile(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                if (projectile.hasMetadata("lure")) {
                    event.getEntity().setMetadata("hited_lure", new FixedMetadataValue(mineZ.getPlugin(), null));
                }
                if (projectile.hasMetadata("grenade")) {
                    event.getEntity()
                            .setMetadata("hited_greande", new FixedMetadataValue(mineZ.getPlugin(), null));
                }
            }

        }
    }
}
