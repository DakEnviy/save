package ru.dakenviy.minez.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.dakenviy.minez.minez.ModuleMineZ;
import ru.dakenviy.minez.stages.Visibility;
import ru.dakenviy.minez.event.player.PlayerThirstChangeEvent;
import ru.dakenviy.minez.items.WaterItem;
import ru.dakenviy.minez.minez.MineZMiniGame;
import ru.dakenviy.minez.minez.Order;
import ru.dakenviy.minez.mysql.MySQL;
import ru.dakenviy.minez.stages.BleedingStage;
import ru.dakenviy.minez.stages.FractureStage;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.minigamelib.MiniGamePlayer;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.antirelog.CombatHandle;
import ua.deamonish.modulesystem.modules.items.CustomItems;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс представляет игрока этого режима.
 * В нем описано состояние игрока, в данном режиме.
 */
public class MineZPlayer extends MiniGamePlayer {

    private boolean bleeding = false;
    private boolean fracture = false;
    private long bleedingTimer = 0;
    private long fractureTimer = 0;
    // Время последнего изменения состояния энумов.
    private long lastChangeThirsts = 0;
    private int damageThirstTimer = 0;
    private ModuleMineZ mineZ = Module.getInstance(ModuleMineZ.class);

    private PlayerGameStats stats = new PlayerGameStats();
    private BleedingStage currentBleedingStage = null;
    private Visibility currentVisibility;
    private FractureStage currentFractureStage = null;
    private CombatHandle combatHandle = new CombatHandle(player);
    private int thirsts;

    private boolean playing;

    public MineZPlayer(Player player) {
        super(player);
        loadData();
    }

    public MineZPlayer(Player player, boolean playing) {
        super(player);
        this.playing = playing;
        loadData();
    }

    public void death(PlayerDeathEvent event){
        this.setPlaying(false);
        this.removeMetadata("target");
        this.getCombatHandle().endCombat();
        if(hasMiniGame()){
            MiniGameManager.getInstance().removePlayerOnMiniGame(this, getMiniGame());
        }

    }

    @Override
    public MineZMiniGame getMiniGame() {
        return (MineZMiniGame) miniGame;
    }

    public boolean isPlayerOrder(){
        for(Order order : getMiniGame().getOrders()){
            if(order.getVictim().equals(this)){
                return true;
            }
        }
        return false;
    }

    public Order getPlayerOrder(){
        for(Order order : this.getMiniGame().getOrders()){
            if(order.getVictim().equals(this)){
                return order;
            }
        }
        return null;
    }

    /**
     * Загружает дату игрока.
     */
    private void loadData() {
        String fullData = "";
        try {
            PreparedStatement statement = MySQL.getConnection().prepareStatement("SELECT `data` FROM `" + MySQL.MAINEZ_PLAYER_TABLE + "` WHERE `player_name`=?");
            statement.setString(1, getPlayer().getName());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                fullData = set.getString("data");
                set.close();
                statement.close();
            } else {
                set.close();
                statement.close();
                if (ModuleMineZ.debug()) {
                    Bukkit.getLogger().info("Даты игрока " + player.getName() + " не найдено, создаем новую.");
                }
                setDefaultSetting(false);
                fullData = createData();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (ModuleMineZ.debug()) {
            Bukkit.getLogger().info("Загрузили дату для игрока " + player.getName() + " его дата имеет вид \"" + fullData + "\"");
        }
        String[] data = fullData.split("/");
        if (data.length == 11) {
            if(data[0].equals("null")){
                player.teleport(ModuleMineZ.getInstance(ModuleMineZ.class).getLobbySpawnPoint());
                setDefaultSetting(false);
            } else {
                setMiniGame(MiniGameManager.getInstance().getMiniGame(data[0]));
                setBleedingTimer(Long.parseLong(data[1]));
                if(data[2].equals("null")){
                    currentBleedingStage = null;
                } else {
                    setCurrentBleedingStage(BleedingStage.valueOf(data[2]));
                }
                setThirsts(Integer.parseInt(data[3]));
                setCurrentVisibility(Visibility.valueOf(data[4]));
                setPlaying(Boolean.parseBoolean(data[5]));
                if(data[6].equals("null")){
                    currentFractureStage = null;
                } else {
                    setCurrentFractureStage(FractureStage.valueOf(data[6]));
                }
                setBleeding(Boolean.parseBoolean(data[7]));
                setFracture(Boolean.parseBoolean(data[8]));
                setLastChangeThirsts(Long.parseLong(data[9]));
                setFractureTimer(Long.parseLong(data[10]));

            }

        }
    }

    /**
     * Устанавливает дефолтные значения, для игроков.
     * Устанавливается для впервые зашедших игроков с аргументом false;
     * И устанавливается для тех, кто зашел в новую миниигру, с аргументом true;
     * В остальных случаях, данные загружается с БД.
     *
     * @param playing - играет ли игрок, после установки значений.
     */
    void setDefaultSetting(boolean playing) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setAllowFlight(false);
        player.setWalkSpeed(0.2f);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.setFlying(false);
        fractureTimer = 0;
        bleedingTimer = 0;
        fracture = false;
        bleeding = false;
        currentBleedingStage = null;
        thirsts = 30;
        player.setLevel(30);
        currentVisibility = Visibility.SHIFT;
        this.playing = playing;
        player.removeMetadata("grenade", mineZ.getPlugin());
        player.removeMetadata("lure", mineZ.getPlugin());
        if (ModuleMineZ.debug())
            Bukkit.getLogger().info("Игроку " + player.getName() + " установили значения по умолчанию. Флаг playing: " + playing);
    }

    /**
     * Сериализирует дату игрока, и сохраняет в БД.
     */
    void saveData() {
        String data = serializeData();
        try {
            PreparedStatement update = MySQL.getConnection().prepareStatement("UPDATE `" + MySQL.MAINEZ_PLAYER_TABLE + "` SET `data`=? WHERE `player_name`=?");
            update.setString(1, data);
            update.setString(2, player.getName());
            update.execute();
            update.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (ModuleMineZ.debug()) {
            Bukkit.getLogger().info("Сохранили дату игрока " + player.getName() + " его дата имеет вид \""+data+"\"");
        }
    }

    private String createData() {
        String data = serializeData();
        try {
            PreparedStatement insert = MySQL.getConnection().prepareStatement("INSERT INTO `" + MySQL.MAINEZ_PLAYER_TABLE + "` VALUES (?,?)");
            insert.setString(1, player.getName());
            insert.setString(2, data);
            insert.execute();
            insert.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (ModuleMineZ.debug()) {
            Bukkit.getLogger().info("Создали и сохранили дату игрока " + player.getName());
        }
        return data;
    }

    private String serializeData() {
        String data = "null";
        if (getMiniGame() != null) {
            data = getMiniGame().getMiniGameName();
        }
        data += "/" + bleedingTimer;
        data += "/" + currentBleedingStage;
        data += "/" + thirsts;
        data += "/" + currentVisibility;
        data += "/" + playing;
        data += "/" + currentFractureStage;
        data += "/" + bleeding;
        data += "/" + fracture;
        data += "/" + lastChangeThirsts;
        data += "/" + fractureTimer;
        return data;
    }

    public BleedingStage getCurrentBleedingStage() {
        return currentBleedingStage;
    }

    public Visibility getCurrentVisibility() {
        return currentVisibility;
    }

    void giveStartItems(){
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.addItem(new ItemStack(Material.WOOD_SWORD));
        inventory.addItem(WaterItem.getWaterItem("water").getItemStack());
        inventory.addItem(CustomItems.getItem("bandage"));
        inventory.addItem(CustomItems.getItem("radio"));
        inventory.addItem(CustomItems.getItem("grenade"));
        inventory.addItem(CustomItems.getItem("lure"));
    }

    void setCurrentBleedingStage(BleedingStage currentBleedingStage) {
        this.currentBleedingStage = currentBleedingStage;
    }

    void setThirsts(int thirsts) {
        this.thirsts = thirsts;
    }

    void updateThirst(int thirsts, boolean add){
        Bukkit.getPluginManager().callEvent(new PlayerThirstChangeEvent(this, thirsts, add));
    }

    void setCurrentVisibility(Visibility currentVisibility) {
        this.currentVisibility = currentVisibility;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public long getLastChangeThirsts() {
        return lastChangeThirsts;
    }

    public int getThirsts() {
        return thirsts;
    }

    public void setLastChangeThirsts(long lastChangeThirsts) {
        this.lastChangeThirsts = lastChangeThirsts;
    }

    public FractureStage getCurrentFractureStage() {
        return currentFractureStage;
    }

    void setCurrentFractureStage(FractureStage currentFractureStage) {
        this.currentFractureStage = currentFractureStage;
    }

    boolean hasMiniGame(){
        return miniGame != null;
    }

    public boolean isBleeding() {
        return bleeding;
    }

    void setBleeding(boolean bleeding) {
        this.bleeding = bleeding;
    }

    public boolean isFracture() {
        return fracture;
    }

    void setFracture(boolean fracture) {
        this.fracture = fracture;
    }

    public long getBleedingTimer() {
        return bleedingTimer;
    }

    public void setBleedingTimer(long bleedingTimer) {
        this.bleedingTimer = bleedingTimer;
    }

    public long getFractureTimer() {
        return fractureTimer;
    }

    public void setFractureTimer(long fractureTimer) {
        this.fractureTimer = fractureTimer;
    }

    public int getDamageThirstTimer() {
        return damageThirstTimer;
    }

    public void setDamageThirstTimer(int damageThirstTimer) {
        this.damageThirstTimer = damageThirstTimer;
    }

    public PlayerGameStats getStats() {
        return stats;
    }

    public void setStats(PlayerGameStats stats) {
        this.stats = stats;
    }

    public CombatHandle getCombatHandle() {
        return combatHandle;
    }

    public void setCombatHandle(CombatHandle combatHandle) {
        this.combatHandle = combatHandle;
    }
}
