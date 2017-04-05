package ru.dakenviy.minez.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.material.Attachable;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.dakenviy.minez.event.player.*;
import ru.dakenviy.minez.items.WaterItem;
import ru.dakenviy.minez.minez.MineZMiniGame;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.minez.Order;
import ru.dakenviy.minez.stages.BleedingStage;
import ru.dakenviy.minez.stages.FractureStage;
import ru.dakenviy.minez.stages.Visibility;
import ru.dakenviy.minez.utils.NMS;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.minigamelib.MiniGamePlayer;
import ua.deamonish.modulesystem.modules.items.CustomItems;
import ua.deamonish.modulesystem.modules.messages.Messages;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MineZPlayerEventListener implements Listener {

    private ModuleMineZ moduleMineZ = ModuleMineZ.getInstance(ModuleMineZ.class);
    private MiniGameManager miniGameManager = MiniGameManager.getInstance();
    private Random random = new Random();

    @EventHandler
    public void onMoveEvent(PlayerMoveEvent event) {
        MineZPlayer player = (MineZPlayer) MiniGameManager.getInstance().getMineGamePlayer(event.getPlayer());
        if (player != null && player.isPlaying()) {
            Player p = player.getPlayer();
            if (p.isSprinting()) {
                player.setCurrentVisibility(Visibility.RUN);
            } else if (p.isSneaking()) {
                player.setCurrentVisibility(Visibility.SHIFT);
            } else {
                player.setCurrentVisibility(Visibility.WALK);
            }
            List<Entity> entities = player.getMetadata("target");
            if (entities != null) {
                Iterator<Entity> iterator = entities.iterator();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    double distance = entity.getLocation().distance(event.getTo());
                    if (distance >= ModuleMineZ.getDistanceExitCombat()) {
                        ((Zombie) entity).setTarget(null);
                        moduleMineZ.printDebug("Зомби " + entity.getUniqueId() + " отстал от игрока " + p.getName());
                        iterator.remove();
                    }
                }
            }
            player.getPlayer().setExp(player.getCurrentVisibility().getExpValue());
        }
    }

    @EventHandler
    public void changeExp(PlayerExpChangeEvent event) {
        event.getPlayer().setExp(0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.removeMetadata("grenade", moduleMineZ.getPlugin());
        player.removeMetadata("lure", moduleMineZ.getPlugin());
        MineZPlayer mineZPlayer = new MineZPlayer(player);
        if (mineZPlayer.getMiniGame() == null) {
            player.teleport(moduleMineZ.getLobbySpawnPoint());
        } else {
            miniGameManager.addPlayerToMiniGame(mineZPlayer, mineZPlayer.getMiniGame());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawnEvent(PlayerRespawnEvent event) {
        event.setRespawnLocation(moduleMineZ.getLobbySpawnPoint());
        event.getPlayer().teleport(moduleMineZ.getLobbySpawnPoint());
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MineZPlayer mineZPlayer = (MineZPlayer) miniGameManager.getMineGamePlayer(player);
        mineZPlayer.saveData();
        if (mineZPlayer.hasMiniGame())
            miniGameManager.removePlayerOnMiniGame(mineZPlayer, mineZPlayer.getMiniGame());
    }

    @EventHandler(ignoreCancelled = true)
    public void damageEvent(EntityDamageEvent event) {

        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) event.getEntity();
            MineZPlayer player = miniGameManager.getMiniGamePlayer(p);
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && !player.isFracture()) {
                int result = random.nextInt(100);
                if (result <= ModuleMineZ.getFractureChance()) {
                    setUpFracture(p);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void damageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) event.getEntity();
            MineZPlayer player = miniGameManager.getMiniGamePlayer(p);
            if (!p.getWorld().equals(event.getDamager().getWorld())) {
                event.setCancelled(true);
                if (event.getDamager().getType() == EntityType.ZOMBIE) {
                    ((Zombie) event.getDamager()).setTarget(null);
                }
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !player.isBleeding()) {
                int resultBleeding = random.nextInt(100);
                if (resultBleeding <= ModuleMineZ.getBleedingChance()) {
                    setUpBleeding(p);
                }
                if (!ModuleMineZ.isFractureOnlyFall() && !player.isFracture()) {
                    int resultFracture = random.nextInt(100);
                    if (resultFracture <= ModuleMineZ.getFractureChance()) {
                        setUpFracture(p);
                    }
                }
            }
            if (event.getDamager().getType() == EntityType.PLAYER) {
                if (((Player) event.getEntity()).getHealth() - event.getDamage() <= 0) {
                    MineZPlayer player1 = miniGameManager.getMiniGamePlayer(((Player) event.getEntity()).getPlayer());
                    Order order = player1.getPlayerOrder();
                    if(order != null){
                        ModuleMineZ.getEconomy().depositPlayer(player1.getPlayer(), order.getCost());
                        event.getDamager().sendMessage("§aПоздравляем, вы убили жертву. Вы получили §b" + order.getCost() + " §aмонет");
                        player1.getMiniGame().broadcastMessage("§6Игрок §a" + player1.getPlayer().getDisplayName() + " §6 выполнил заказ на убийство игрока §b" + player.getPlayer().getDisplayName());
                    }
                }
            }
            if (p.getHealth() - event.getDamage() <= 0 && event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
                String messageToSend = Messages.getMessage("cмерть_" + event.getCause());
                if (event.getDamager().getType() == EntityType.ZOMBIE) {
                    if (event.getDamager().hasMetadata("giant")) {
                        messageToSend = Messages.getMessage("смерть_" + event.getCause(), "%player%", p.getDisplayName());
                    } else {
                        messageToSend = Messages.getMessage("смерть_zombie", "%player%", p.getDisplayName());
                    }
                }
                player.getMiniGame().broadcastMessage(messageToSend);
            }
        }
    }


    private void setUpFracture(Player player) {
        MineZPlayer mineZPlayer = miniGameManager.getMiniGamePlayer(player);
        mineZPlayer.setFracture(true);
        NMS.sendTitle(player, Messages.getMessage("fracture_title"), Messages.getMessage("fracture_subtitle"));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 3));
        moduleMineZ.printDebug("У игрока " + player.getName() + " перелом.");
    }

    private void setUpBleeding(Player player) {
        MineZPlayer mineZPlayer = miniGameManager.getMiniGamePlayer(player);
        mineZPlayer.setBleeding(true);
        NMS.sendTitle(player, Messages.getMessage("bleeding_title"), Messages.getMessage("bleeding_subtitle"));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 3));
        moduleMineZ.printDebug("У игрока " + player.getName() + " началось кровотечение.");
    }

    @EventHandler
    public void onDeathEvent(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void consumeEvent(PlayerItemConsumeEvent event) {
        if (WaterItem.isContains(event.getItem())) {
            MineZPlayer player = miniGameManager.getMiniGamePlayer(event.getPlayer());
            WaterItem item = WaterItem.getWaterItem(event.getItem());
            if (item != null) {
                player.updateThirst(item.getThirst(), true);
            }
        }
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        if (event.getItem() != null) {

            if (event.getItem().equals(CustomItems.getItem("grenade"))) {
                boolean shooted = true;
                if (event.getClickedBlock() != null) {
                    if (event.getClickedBlock().getState().getData() instanceof DirectionalContainer || event.getClickedBlock().getState().getData() instanceof Attachable) {
                        shooted = false;
                    }
                }
                if (shooted)
                    event.getPlayer().setMetadata("grenade", new FixedMetadataValue(moduleMineZ.getPlugin(), null));
            }
            if (event.getItem().equals(CustomItems.getItem("lure"))) {
                boolean shooted = true;
                if (event.getClickedBlock() != null) {
                    if (event.getClickedBlock().getState().getData() instanceof DirectionalContainer || event.getClickedBlock().getState().getData() instanceof Attachable) {
                        shooted = false;
                    }
                }
                if (shooted)
                    event.getPlayer().setMetadata("lure", new FixedMetadataValue(moduleMineZ.getPlugin(), null));
            }
        }

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().equals(CustomItems.getItem("bandage"))) {
                MineZPlayer p = miniGameManager.getMiniGamePlayer(player);
                if (p.isBleeding()) {
                    p.setBleeding(false);
                    p.setCurrentBleedingStage(null);
                    p.setBleedingTimer(0);
                    player.sendMessage(Messages.getMessage("bleeding_end"));
                } else {
                    player.sendMessage(Messages.getMessage("no_bleeding"));
                }
            }
        }
        if (clickedBlock != null && clickedBlock.getType().equals(Material.WALL_SIGN)) {
            for (Sign sign : moduleMineZ.getAvailableSign().keySet()) {
                if (sign.getLocation().equals(clickedBlock.getLocation())) {
                    MineZPlayer miniGamePlayer = (MineZPlayer) miniGameManager.getMineGamePlayer(player);
                    MiniGame previouslyGame = miniGamePlayer.getMiniGame();

                    if (previouslyGame != null) { // Удаляем из старой игры.
                        miniGameManager.removePlayerOnMiniGame(miniGamePlayer, previouslyGame);
                        if (ModuleMineZ.debug()) {
                            Bukkit.getLogger()
                                    .info("Удалили игрока " + miniGamePlayer.getPlayer()
                                            .getName() + " из миниигры " + previouslyGame);
                        }
                    }
                    MineZMiniGame miniGame = (MineZMiniGame) moduleMineZ.getAvailableSign().get(sign);

                    if (miniGame.getSpawnPoints().isEmpty()) {
                        Bukkit.getLogger()
                                .severe("Для миниигры " + miniGame.getMiniGameName() + " не указаны спавн поинты.");
                        player.sendMessage("§cДля этой миниигры не указаны точки спавна.");
                    }
                    miniGameManager.addPlayerToMiniGame(miniGamePlayer, miniGame);
                    miniGame.teleportPlayerToSpawn(miniGamePlayer);
                    miniGamePlayer.setDefaultSetting(true);
                    miniGamePlayer.giveStartItems();
                    break;
                }
            }
        }
    }

    // Конечно место, где обрабатываются методы.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bleedingDamage(PlayerBleedingDamageEvent event) {
        if (!event.getPlayer().getPlayer().isDead()) {
            checkBleedingDie(event.getPlayer(), event.getDamage());
            event.getPlayer().getPlayer().damage(event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bleedingChange(PlayerBleedingChangeEvent event) {
        MineZPlayer player = event.getPlayer();
        BleedingStage stage = event.getNewStage();

        player.setCurrentBleedingStage(stage);
        Bukkit.getPluginManager().callEvent(new PlayerBleedingDamageEvent(player, stage.getDamage()));
        player.sendMessage(stage.getMessage());
    }

    private void checkBleedingDie(MiniGamePlayer player, double damage){
        if(player.getPlayer().getHealth() - damage <= 0){
            player.getMiniGame().broadcastMessage(Messages.getMessage("death_bleeding").replace("%player%", player.getPlayer().getDisplayName()));
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void thirstChange(PlayerThirstChangeEvent event) {
        MineZPlayer player = event.getPlayer();
        if (event.isUpdate()) {
            player.setThirsts(event.getPlayer().getThirsts() + event.getValue());
        } else {
            if (player.getThirsts() - event.getValue() >= 0) {
                player.setThirsts(player.getThirsts() - event.getValue());
            } else {
                player.setThirsts(0);
                return;
            }
        }
        player.getPlayer().setLevel(player.getThirsts());
        String message = moduleMineZ.getThirstMessages().get(player.getThirsts());
        if (message != null) {
            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fractureChange(PlayerFractureChangeEvent event) {
        FractureStage stages = event.getNewStage();
        MineZPlayer player = event.getPlayer();

        player.setCurrentFractureStage(stages);
        Bukkit.getPluginManager().callEvent(new PlayerFractureDamageEvent(player, stages.getDamage()));
        player.getPlayer().setWalkSpeed(stages.getSpeed());
        player.getPlayer().sendMessage(stages.getMessage());
    }

    @EventHandler
    public void enderPerlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true); // Что бы граната нас не телепортировала.
        }
    }

    @EventHandler
    public void changeEvent(EntityChangeBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void interactFire(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null) {
            Location loc = block.getLocation();
            loc.add(0, 1, 0);
            if (loc.getBlock().getType() == Material.FIRE) {
                if (event.getPlayer().hasPermission("deamonish.fire.stew"))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void interactFrame(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        // отменяем повороты рамок.
        if (entity != null && entity.getType().equals(EntityType.ITEM_FRAME)) {
            if (!event.getPlayer().hasPermission("deamonish.frame.manipulate")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void thirstDamage(PlayerThirstDamageEvent event){
        if(event.getPlayer().getPlayer().getHealth() - event.getDamage() <= 0){
            event.getPlayer().getMiniGame().broadcastMessage(Messages.getMessage("cмерть_thirst").replace("%player%", event.getPlayer().getPlayer().getDisplayName()));
        }
        event.getPlayer().getPlayer().damage(event.getDamage());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fractureDamage(PlayerFractureDamageEvent event) {
        if(event.getPlayer().getPlayer().getHealth() - event.getDamage() <= 0){
            event.getPlayer().getMiniGame().broadcastMessage(Messages.getMessage("cмерть_fracture").replace("%player%", event.getPlayer().getPlayer().getDisplayName()));
        }
        event.getPlayer().getPlayer().damage(event.getDamage());
    }

}
