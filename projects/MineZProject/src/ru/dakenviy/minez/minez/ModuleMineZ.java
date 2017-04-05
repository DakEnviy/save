package ru.dakenviy.minez.minez;

import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.dakenviy.minez.containers.*;
import ru.dakenviy.minez.stages.Visibility;
import ru.dakenviy.minez.antirelog.AntirelogListener;
import ru.dakenviy.minez.items.WaterItem;
import ru.dakenviy.minez.mysql.MySQL;
import ru.dakenviy.minez.player.MineZPlayerEventListener;
import ru.dakenviy.minez.stages.BleedingStage;
import ru.dakenviy.minez.stages.FractureStage;
import ru.dakenviy.minez.utils.NMS;
import ru.dakenviy.minez.zombie.MineZMobListener;
import ua.deamonish.minigamelib.MiniGame;
import ua.deamonish.minigamelib.MiniGameListener;
import ua.deamonish.minigamelib.MiniGameManager;
import ua.deamonish.modulesystem.module.Module;
import ua.deamonish.modulesystem.modules.items.CustomItems;
import ua.deamonish.modulesystem.modules.messages.Messages;
import ua.deamonish.modulesystem.util.Config;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;


/**
 * Основной модуль. Инициализирующий все значения, и создающее миниигру.
 */
public class ModuleMineZ extends Module {

    public ModuleMineZ(Plugin plugin) {
        super("minez", 0, plugin, new Config(plugin, "config.yml"));
    }

    // Связка табличка = миниигра. Нажав по этой табличке, закинет в связаную игру.
    private HashMap<Sign, MiniGame> availableSign = new HashMap<>();
    private List<Location> defaultSpawnPoint = new ArrayList<>();
    private static long BLEEDING_PERIOD, CHANGE_THIRST_TIME, LURE_TIME;
    private static float ZOMBIE_SPEED, ZOMBIE_FULL_MOON_SPEED;
    private static int ZOMBIE_HEALTH, ZOMBIE_FULL_MOON_HEALTH, MAX_PLAYER_ON_MINI_GAME, DISTANCE_EXIT_COMBAT,
            FULL_MOON_PERIOD, FULL_MOON_TIME, BLEEDING_CHANCE, FRACTURE_CHANCE, THIRST_DAMAGE_PERIOD, THIRST_DAMAGE, GRENADE_DISTANCE, GRENADE_DAMAGE, LURE_DISTANCE;
    private static double ZOMBIE_DAMAGE, ZOMBIE_FULL_MOON_DAMAGE;
    private static boolean HALF_LOOT_DROP, FULL_MOON, FRACTURE_ONLY_FALL;
    private HashMap<Integer, String> thirstMessages = new HashMap<>();
    private MiniGameManager manager;
    // Мир лобби, откуда игроки будут расходится по мирам(минииграм), он же и спавн.
    private World LOBBY;
    private Location lobbySpawnPoint;
    private static Economy economy = null;

    public static Economy getEconomy() {
        return economy;
    }

    private int scheduleTaskId = 0;
    // Отвечает за нумерацию миров.
    private int iterableWorld = 1;
    private int chestsNumerable = 0;
    // Отдельный поток-таймер, в котором будут считает таймеры и изменения состояний игроков.
    private Config config, signs, stages, chests, stands, items, death;
    private static boolean debug;
    private static ModuleMineZ instance;

    @Override
    public void onEnable() {
        instance = this;
        this.registerData(new MineZPlayerEventListener());
        this.registerData(new MineZMobListener());
        this.registerData(new MiniGameListener());
        this.registerListenersThis();
        new AntirelogListener();
        manager = MiniGameManager.getInstance();

        config = getConfig();
        signs = new Config(getPlugin(), "sign.yml");
        stages = new Config(getPlugin(), "stages.yml");
        chests = new Config(getPlugin(), "chests.yml");
        stands = new Config(getPlugin(), "stands.yml");
        items = new Config(getPlugin(), "items.yml");
        death = new Config(getPlugin(), "death.yml");

        createTables();
        setDefaultDecorSigns();
        setDefaultStagesValues();
        setDefaultConfigValues();
        setDefaultChest();
        setDefaultStand();
        setDefaultItems();
        setDefaultDeathMessages();
        try {
            debug = config.getBoolean("debug");
            BLEEDING_PERIOD = stages.getLong("bleeding_stage_period");
            ZOMBIE_SPEED = config.getFloat("zombie_speed");
            ZOMBIE_FULL_MOON_SPEED = config.getFloat("zombie_full_moon_speed");
            ZOMBIE_HEALTH = config.getInt(("zombie_health"));
            ZOMBIE_FULL_MOON_HEALTH = config.getInt("zombie_full_moon_health");
            ZOMBIE_DAMAGE = config.getDouble("zombie_damage");
            ZOMBIE_FULL_MOON_DAMAGE = config.getDouble("zombie_full_moon_damage");
            HALF_LOOT_DROP = config.getBoolean("zombie_half_loot_drop");
            DISTANCE_EXIT_COMBAT = config.getInt("distance_exit_combat");
            FULL_MOON_PERIOD = config.getInt("full_moon_period");
            FULL_MOON_TIME = config.getInt("full_moon_time");
            setupEconomy();
            loadThirstsValues();
            loadVisibilityValues();
            loadBleedingStageValues();
            loadFractureValue();
            loadMiniGameInfo();
            loadLobby();
            loadItems();
            loadChests();
            loadStands();
            loadMessage();
            int updateChestTime = chests.getInt("chest_update_time") * 1000;

            // Таймер, что в отдельном потоке, будет выполнять задачу.
            Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin(), new PlayerScheduler(), 0, 20);
            scheduleTaskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), new ContainerScheduler(), 0, updateChestTime).getTaskId();

            for (World world : Bukkit.getWorlds()) {
                if (manager.getMiniGames().keySet().contains(world.getName()) || world.getName()
                        .equals(getLobbyWorld().getName())) {
                    world.setTime(12000);
                } else {
                    Bukkit.unloadWorld(world, false);
                }
            }
        } catch (Exception ex) {
            getLogger().severe("Ошибка при загрузке данных с конфига.");
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(scheduleTaskId);
        Set<MineZMiniGame> games = manager.getMiniGame();
        for (MineZMiniGame game : games) {
            for (MineZArmorStand stand : game.getStands()) {
                stand.getStand().remove();
            }
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void setDefaultItems() {
        items.setIfNotExist("items.radio.item", "381:0:1:&eРадио:&aВзяв радио в руки вы сможете писать в глобальный чат.");
        items.setIfNotExist("items.grenade.item", "368:0:1:&eГраната &c[ПКМ]:aНажмите ПКМ, что бы кинуть гранату. \n&aГраната взорвется при попадании в моба или об землю.");
        items.setIfNotExist("items.lure.item", "332:0:1:&eПриманка &c[ПКМ]:&aНажмите ПКМ, что бы кинуть приманку.\n&aПри столкновении с игроком. Агрит на него всех мобов в радиусе 15 метров.\n&aПри столкновении с землей, все зомби в радиусе 15 метров, сбегают в точку столкновения.");
        items.setIfNotExist("items.bandage.item", "339:0:1:&eБинт:&aИспользуется для остановки кровотечений.");
        items.setIfNotExist("items.lure.time", 15);
        items.setIfNotExist("items.lure.distance", 15);
        items.setIfNotExist("items.grenade.distance", 15);
        items.setIfNotExist("items.grenade.damage", 10);

        items.setIfNotExist("waterItems.milk.item", "335:0:1:&eМолоко:&aНажмите &cПКМ&e, что бы выпить молока. \n Ввосстановит &e5 жажды.");
        items.setIfNotExist("waterItems.milk.thirst", 5);
        items.setIfNotExist("waterItems.water.item", "373:0:1:&eБутылка воды:&aНажмите &cПКМ&e, что бы выпить воды. \nВода восстанавливает 10 жажды.");
        items.setIfNotExist("waterItems.water.thirst", 10);
    }

    private void loadItems() {
        LURE_DISTANCE = items.getInt("items.lure.distance");
        GRENADE_DAMAGE = items.getInt("items.grenade.damage");
        GRENADE_DISTANCE = items.getInt("items.grenade.distance");
        LURE_TIME = items.getLong("items.lure.time");
        for (String key : items.getConfigurationSection("items").getKeys(false)) {
            ItemStack stack = Config.toItemStack(items.getStringColor("items." + key + ".item"));
            CustomItems.addItem(key, stack);
        }

        for (String key : items.getConfigurationSection("waterItems").getKeys(false)) {
            ItemStack stack = Config.toItemStack(items.getStringColor("waterItems." + key + ".item"));
            int thirst = items.getInt("waterItems." + key + ".thirst");
            WaterItem item = new WaterItem(stack, thirst);
            WaterItem.addWaterItem(key, item);
        }
    }

    private void setDefaultStagesValues() {
        stages.setIfNotExist("fracture_title", "§cУ вас перелом");
        stages.setIfNotExist("fracture_subtitle", "§eСкорость движения снижена.");

        stages.setIfNotExist("bleeding_title", "§cУ вас началось кровотечение");
        stages.setIfNotExist("bleeding_subtitle", "");

        stages.setIfNotExist("fracture_chance", 15);
        stages.setIfNotExist("bleeding_chance", 15);
        stages.setIfNotExist("fracture_only_fall", true);
        stages.setIfNotExist("thirst_message.8", "В горле пересохло...");
        stages.setIfNotExist("thirst_message.5", "Я хочу пить.");
        stages.setIfNotExist("thirst_message.3", "Я очень хочу пить.");
        stages.setIfNotExist("thirst_message.0", "Если я немедленно не найду воду, я умру.");
        stages.setIfNotExist("thirst_damage", 1);
        stages.setIfNotExist("thirst_period_damage", 10);
        stages.setIfNotExist("change_thirst_time", 60);
        // Перелом
        stages.setIfNotExist("fracture.low.message", "Кажется я сломал ногу...");
        stages.setIfNotExist("fracture.medium.message", "Нога ужасно болит...");
        stages.setIfNotExist("fracture.high.message", "Я не могу идти...");

        stages.setIfNotExist("fracture.low.damage", 1);
        stages.setIfNotExist("fracture.medium.damage", 2);
        stages.setIfNotExist("fracture.high.damage", 3);

        stages.setIfNotExist("fracture.low.speed", 0.18f);
        stages.setIfNotExist("fracture.medium.speed", 0.16f);
        stages.setIfNotExist("fracture.high.speed", 0.14f);

        stages.setIfNotExist("fracture.low.time", 0);
        stages.setIfNotExist("fracture.medium.time", 180);
        stages.setIfNotExist("fracture.high.time", 300);
        // Урон от потери крови. В баките 1 урон = 0.5 сердца.
        stages.setIfNotExist("bleeding_stage.low.damage", 1);
        stages.setIfNotExist("bleeding_stage.medium.damage", 2);
        stages.setIfNotExist("bleeding_stage.high.damage", 3);
        // Время, для наступления стадии в секундах.
        stages.setIfNotExist("bleeding_stage.low.time", 60);
        stages.setIfNotExist("bleeding_stage.medium.time", 180);
        stages.setIfNotExist("bleeding_stage.high.time", 300);
        // Сообщения при смене стадий кровопотери.
        stages.setIfNotExist("bleeding_stage.low.message", "Я истекаю кровью...");
        stages.setIfNotExist("bleeding_stage.medium.message", "Я потерял много крови...");
        stages.setIfNotExist("bleeding_stage.high.message", "Кажется я умираю, я потерял слишком много крови...");
        // Время, с какой периодичностью, будет минусовать хп. (Каждые 5 секунд)
        stages.setIfNotExist("bleeding_stage_period", 5);
        stages.setIfNotExist("bleeding_end", "§aВы упешно остановили кровотечение");
        stages.setIfNotExist("no_bleeding", "§cУ вас нет кровотечения.");

        stages.setIfNotExist("fracture_end", "§aВы залечили перелом. Скорость ввостановлена.");
        stages.setIfNotExist("no_fracture", "§cУ вас нет перелома.");
        // Видимость для игрока.
        stages.setIfNotExist("visibility.shift.experience", 0.3f);
        stages.setIfNotExist("visibility.shift.distance", 5);
        stages.setIfNotExist("visibility.walk.experience", 0.6f);
        stages.setIfNotExist("visibility.walk.distance", 10);
        stages.setIfNotExist("visibility.run.experience", 0.9f);
        stages.setIfNotExist("visibility.run.distance", 14);


    }

    private void setDefaultChest() {
        chests.setDescription("Каждый сундук, имеет свою локацию. \n" +
                "Каждый сундук, должен иметь свой итем пулл. \n" +
                "Каждый предмет итем-пулла, должен иметь предмет и шанс.\n" +
                "Обязательно для каждого сундука прописать минимальное и максимальное кол-во предметов. \n" +
                "chest_update_time - время в секундах, с которым обновляются сундуки. По умолчанию стоит 600 - 10 минут.\n" +
                "chest_force - если стоит true. Сундуки по указаным в них локациях будут НАСИЛЬНО спавнится. Если стоит false будет просто кидать экзепшен.");
        chests.setIfNotExist("chest_force", true);
        chests.setIfNotExist("chest_update_time", 600);
        chests.setDefault("chest.chest1.location", Config.toString(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)));
        chests.setDefault("chest.chest1.items.1.item", "285:20:1:none:none");
        chests.setDefault("chest.chest1.items.1.chance", 50);
        chests.setDefault("chest.chest1.items.2.item", "279:40:1:none:none");
        chests.setDefault("chest.chest1.items.2.chance", 40);
        chests.setDefault("chest.chest1.maxItem", 4);
        chests.setDefault("chest.chest1.minItem", 2);
        chests.setDefault("chest.chest2.location", Config.toString(new Location(Bukkit.getWorlds().get(0), 0, 0, 0)));
        chests.setDefault("chest.chest2.items.1.item", "285:20:1:none:none");
        chests.setDefault("chest.chest2.items.1.chance", 50);
        chests.setDefault("chest.chest2.items.2.item", "279:40:1:none:none");
        chests.setDefault("chest.chest2.items.2.chance", 40);
        chests.setDefault("chest.chest2.maxItem", 4);
        chests.setDefault("chest.chest2.minItem", 2);
    }

    private void setDefaultStand() {
        chests.setDescription("В этом конфиге. Хранятся данные об арморстендах.\n " +
                "1. У каждого арморстенда есть, своя локация.\n " +
                "2. У каждого стенда, есть пул предметов, пул описан по пути stands.name.items. \n" +
                "Каждый предмет под пул должен иметь свой шанс, свой тип (helmet, chestplate, leggings, hand, boots) и собственно сам айди предмета.\n" +
                "3. Каждый стенд, имеет список UUID. - эту графу трогать не стоит. Там все автоматизировано.\n" +
                "Соблюдайте спецификацию YML. Для каждой секции, необходимо делать 2 пробела.");
    }

    private void loadStands() {
        ConfigurationSection section = stands.getConfigurationSection("stands");
        if (section == null) {
            printDebug("На данный момент, секции отвечающей за стенды не существует.");
        } else {
            for (String standName : section.getKeys(false)) {
                String path = "stands." + standName + ".";
                Location loc = Config.toLocation(stands.getString(path + "location"));
                /*List<String> stringUUID = stands.getStringList(path + "UUID");
                List<UUID> uuids = stringUUID.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());*/

                HashSet<MineZMiniGame> games = manager.getMiniGame();
                List<ArmorItem> items = new ArrayList<>();
                // Выгружаем итем пул для этого стенда.
                for (String itemSection : stands.getConfigurationSection(path + "items").getKeys(false)) {
                    String newPath = path + ".items." + itemSection + ".";
                    ItemStack stack = stands.getItemStack(newPath + "item");
                    int chance = stands.getInt(newPath + "chance");
                    String typeEnum = stands.getString(newPath + "type").toUpperCase();

                    try {
                        ArmorItemType type = ArmorItemType.valueOf(typeEnum);
                        items.add(new ArmorItem(chance, stack, type));
                    } catch (IllegalArgumentException ex) {
                        getLogger()
                                .severe("В конфиге для предмета по пути " + newPath + "type указан некорректный тип. Подробнее в ошибке.");
                        ex.printStackTrace();
                    }
                }
                // Пробегаемся по минииграм, в поисках стендов. Если их нет создаем. По окончанию, сейвим текущие UUID.
                for (MineZMiniGame game : games) {
                    loc.setWorld(game.getWorld());
                    ArmorStand stand = NMS.spawnArmorStand(loc.clone());
                    game.addStand(new MineZArmorStand(stand, items));
                }
            }
        }
    }


    /**
     * Загружает локации и содержимое для сундуков.
     */
    private void loadChests() {
        boolean force = chests.getBoolean("chest_force");
        for (String chest : chests.getConfigurationSection("chest").getKeys(false)) {
            String key = "chest." + chest + ".items";
            Location loc = chests.getLocation("chest." + chest + ".location");

            List<ChestItem> chestItems = new ArrayList<>();
            for (String item : chests.getConfigurationSection(key).getKeys(false)) {
                ItemStack itemStack = Config.toItemStack(chests.getString(key + "." + item + ".item"));
                int chance = chests.getInt(key + "." + item + ".chance");
                if (itemStack != null)
                    chestItems.add(new ChestItem(chance, itemStack));
            }

            int maxItemSize = chests.getInt("chest." + chest + ".maxItem");
            int minItemSize = chests.getInt("chest." + chest + ".minItem");
            chestsNumerable++;
            for (MiniGame miniGame : MiniGameManager.getInstance().getMiniGames().values()) {
                MineZMiniGame mineZ = (MineZMiniGame) miniGame;
                Location clone = loc.clone();
                clone.setWorld(miniGame.getWorld());
                try {
                    BlockState state = clone.getBlock().getState();
                    if (!(state instanceof Chest)) {
                        if (force) {
                            clone.getBlock().setType(Material.CHEST);
                            state = clone.getBlock().getState();
                            printDebug("Был заспавнен сундук по локации " + clone);
                        } else {
                            throw new IllegalArgumentException("Локация сундука из конфига: " + clone + ", НЕ указывает на сундук. Тип блока по этой локации " + state
                                    .getType()
                                    .name());
                        }
                    }
                    Chest chest1 = (Chest) state;
                    mineZ.getChests().add(new MineZChest(clone, chest1, chestItems, minItemSize, maxItemSize));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void createTables() {
        try {
            PreparedStatement statement = MySQL.getConnection()
                    .prepareStatement(MySQL.MAINEZ_PLAYER_TABLE.getCreateQuery());
            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    private void setDefaultDecorSigns() {
        signs.setDescription("Тут описывается оформление таблички, на которую игрок будет кликать.");
        signs.setIfNotExist("line1", "&3Мир %number%");
        signs.setIfNotExist("line2", "&aОнлайн: [%n%/%m%]");
        signs.setIfNotExist("line3", "&cЖми, что бы начать играть.");
        signs.setIfNotExist("line4", "");
    }

    private void loadLobby() {
        try {
            String world_name = config.getString("lobby_world");
            LOBBY = Bukkit.getWorld(world_name);
            // Мира нет, генерируем.
            if (LOBBY == null) {
                LOBBY = Bukkit.createWorld(new WorldCreator(world_name).type(WorldType.FLAT)
                        .environment(World.Environment.NORMAL));
                // Все равно нет, бросаем экзепшен.
                if (LOBBY == null) {
                    throw new RuntimeException("Мир '" + world_name + "' не найден и не был создан.");
                }
            }
            this.lobbySpawnPoint = config.getLocation("lobby_spawn_point");
            this.lobbySpawnPoint.setWorld(LOBBY);
        } catch (IllegalArgumentException ex) {
            getLogger().info("Мир указанный по пути lobby_world не найден.");
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }

        loadSigns();
    }


    /**
     * Сохраняет новый арморстенд, в конфиг.
     *
     * @param loc  - условная локация(универсальная для всех миров).
     * @param path - имя, под которым сохранится данные об арморстенде в конфиге.
     */
    public void addArmorStand(Location loc, String path) {
        String completePath = "stands." + path + ".";
        stands.set(completePath + "location", Config.toString(loc));
        stands.set(completePath + "items.1.item", "298:0:0");
        stands.set(completePath + "items.1.chance", 50);
        stands.set(completePath + "items.1.type", "helmet");
        stands.set(completePath + "items.2.item", "300:0:0");
        stands.set(completePath + "items.2.chance", 50);
        stands.set(completePath + "items.2.type", "leggings");
        stands.save();
    }

    /**
     * Добавляет в конфиг новый сундук. И делает макет для заполнения его данными.
     * В качестве имени для сундука, выступает его порядковый номер.
     *
     * @param loc - локация сундука.
     */
    public void addChest(Location loc) {
        chests.set("chest." + chestsNumerable + ".items.1.item", "1:0:1:none:none");
        chests.set("chest." + chestsNumerable + ".maxItem", 2);
        chests.set("chest." + chestsNumerable + ".minItem", 1);
        chests.set("chest." + chestsNumerable + ".items.1.chance", 50);
        chests.set("chest." + chestsNumerable + ".location", Config.toString(loc));
        chests.save();
        chestsNumerable++;
    }

    /**
     * Подгружает оформление табличек, для перехода в миры миниигры.
     */
    private void loadSigns() {
        // Порядковый номер мира.
        for (String signNames : config.getConfigurationSection("signs").getKeys(false)) {
            MiniGame miniGame = manager.getMiniGame(signNames);
            Location locationSign = config.getLocation("signs." + signNames + ".sign_location");
            // Нулпоинтер НЕ ловим, в месте где он будет ловится, он вырубит сервер, что бы юзверь конфиг фикснул.
            if (miniGame == null) {
                throw new NullPointerException("При загрузке табличек, произошла ошибка. \n Для секции " + signNames + " не найдена миниигра " + signNames + ". Настройте конфиг.");
            }
            try {
                // Прописываем строчки в табличке.
                BlockState blockSign = locationSign.getBlock().getState();
                if (blockSign instanceof Sign) {
                    Sign sign = (Sign) blockSign;
                    setSign(sign, miniGame);
                    availableSign.put(sign, miniGame);
                } else {
                    manager.removeMiniGame(miniGame);
                    throw new IllegalArgumentException("Локация таблички, что указана для миниигры " + signNames + " НЕ указывает на табличку. Тип блока по этой локации " + blockSign
                            .getType()
                            .name());
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }


    }

    private void setDefaultDeathMessages() {
        for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
            String key = "смерть_" + cause.toString();
            death.setIfNotExist(key, "");
        }
        death.setIfNotExist("смерть_thirst", "&6Игрок &b%player%&6 скончался от обезвоживания.");
        death.setIfNotExist("смерть_zombie", "&6Игрок &b%player%&6 был съеден зомби.");
        death.setIfNotExist("смерть_bleeding", "&6Игрок &b%player%&6 скончался, от потери крови.");
        death.setIfNotExist("смерть_fracture", "&6Игрок &b%player%&6 погиб, от болевого шока, вызванного переломом.");
        death.setIfNotExist("смерть_giant", "&6Игрок &b%player%&6 был безжалостно раздавлен гигантом.");

    }

    private void loadMessage() {
        Messages.addMessage("fracture_title", stages.getString("fracture_title"));
        Messages.addMessage("fracture_subtitle", stages.getString("fracture_subtitle"));

        Messages.addMessage("bleeding_title", stages.getString("bleeding_title"));
        Messages.addMessage("bleeding_subtitle", stages.getString("bleeding_subtitle"));

        Messages.addMessage("full_moon_start_title", config.getString("full_moon_start_title"));
        Messages.addMessage("full_moon_start_subtitle", config.getString("full_moon_start_subtitle"));

        Messages.addMessage("full_moon_end_title", config.getString("full_moon_end_title"));
        Messages.addMessage("full_moon_end_subtitle", config.getString("full_moon_end_subtitle"));

        Messages.addMessage("bleeding_end", stages.getString("bleeding_end"));
        Messages.addMessage("no_bleeding", stages.getString("no_bleeding"));

        Messages.addMessage("fracture_end", stages.getString("fracture_end"));
        Messages.addMessage("no_fracture", stages.getString("no_fracture"));
        for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
            String key = "смерть_" + cause.toString();
            Messages.addMessage(key, death.getString(key));
        }

        Messages.addMessage("смерть_thirst", death.getString("смерть_thirst"));
        Messages.addMessage("смерть_zombie", death.getString("смерть_zombie"));
        Messages.addMessage("смерть_bleeding", death.getString("смерть_bleeding"));
        Messages.addMessage("смерть_fracture", death.getString("смерть_fracture"));
        Messages.addMessage("смерть_giant", death.getString("смерть_giant"));
    }

    /**
     * Устанавливает дефолтные значения для конфига.
     */
    private void setDefaultConfigValues() {
        config.setIfNotExist("debug", false);
        config.setIfNotExist("half_loot_drop", false);
        // Лимит игроков для миниигры.
        config.setIfNotExist("miniGamePlayerLimit", 50);
        // Имена миниигр и это же ее Мир.
        config.setIfNotExist("miniGames", Arrays.asList("game1", "game2"));

        config.setIfNotExist("zombie_speed", 0.3f);
        config.setIfNotExist("zombie_full_moon_speed", 0.35f);
        config.setIfNotExist("zombie_health", 20);
        config.setIfNotExist("zombie_full_moon_health", 26);
        config.setIfNotExist("zombie_half_loot_drop", false);
        config.setIfNotExist("zombie_damage", 3D);
        config.setIfNotExist("zombie_full_moon_damage", 4D);
        config.setIfNotExist("distance_exit_combat", 30);
        config.setIfNotExist("full_moon_period", 5);
        config.setIfNotExist("full_moon_time", 1200);
        config.setIfNotExist("full_moon_start_title", "&cНачалось полнолуние");
        config.setIfNotExist("full_moon_start_subtitle", "&eЗомби стали сильнее");
        config.setIfNotExist("full_moon_end_title", "&aПолнолуние закончилось");
        config.setIfNotExist("full_moon_end_subtitle", "&eЗомби ослабились");

        config.setIfNotExist("lobby_world", "lobby");
        // Локации храню с помощью своего парсера.
        // Секция signs имеет в себе связку ключ-значение.
        // Где ключом является имя миниигры, а значением локация таблички(к которой игрка привязана).
        config.setIfNotExist("signs.game1.sign_location", Config.toString(new Location(Bukkit.getWorlds()
                .get(0), 0, 0, 0)));
        config.setIfNotExist("signs.game2.sign_location", Config.toString(new Location(Bukkit.getWorlds()
                .get(0), 0, 0, 0)));
        // в lobby_spawn_point попадают игроки, при заходе на сервер если нет привязаной миниигры.
        config.setIfNotExist("lobby_spawn_point", Config.toString(new Location(Bukkit.getWorlds().get(0), 20, 0, 0)));
        config.setIfNotExist("spawnPoints", Arrays.asList(Config.toString(new Location(Bukkit.getWorlds()
                        .get(0), 1, 20, 1)),
                Config.toString(new Location(Bukkit
                        .getWorlds()
                        .get(0), 1, 20, 2))));
        config.setIfNotExist("minimal_order_bet", 1000);
    }

    /**
     * Загружает систему видимости.
     */
    private void loadVisibilityValues() {
        printDebug("Загружаем значения для уровней видимости.");

        for (String visibilityEnum : stages.getConfigurationSection("visibility").getKeys(false)) {
            Visibility vis = Visibility.valueOf(visibilityEnum.toUpperCase());
            // Есть ли такой энум.
            if (vis == null) {
                getLogger().severe("Значение с путем visibility." + visibilityEnum + " некоректное.");
                continue;
            }
            float value = stages.getFloat("visibility." + visibilityEnum + ".experience");
            int distance = stages.getInt("visibility." + visibilityEnum + ".distance");
            // Допустимое ли значение.
            if (value > 1 || value < 0) {
                throw new RuntimeException("Значение по пути visibility." + visibilityEnum + ".experience больше еденицы или меньше 0.");
            }

            if (distance < 0) {
                throw new RuntimeException("Недопустимые значения по пути visibility." + visibilityEnum + ".distance. Дистанция не может быть меньше 0.");
            }
            // Энум получает константу.
            vis.setExpValue(value);
            vis.setDistance(distance);

            printDebug("Для энума заметности \"" + vis
                    .name() + "\" установили значение опыта " + value + " и дистанцию " + distance);
        }
    }

    private void loadFractureValue() {
        printDebug("Загружаем значения для стадий переломов.");
        for (String stage : stages.getConfigurationSection("fracture").getKeys(false)) {
            try {
                FractureStage fractureStage = FractureStage.valueOf(stage.toUpperCase());
                FRACTURE_CHANCE = stages.getInt("fracture_chance");
                FRACTURE_ONLY_FALL = stages.getBoolean("fracture_only_fall");
                double damage = stages.getDouble("fracture." + stage + ".damage");
                float speed = stages.getFloat("fracture." + stage + ".speed");
                String message = stages.getStringColor("fracture." + stage + ".message");
                long time = stages.getLong("fracture." + stage + ".time");

                fractureStage.setDamage(damage);
                fractureStage.setSpeed(speed);
                fractureStage.setMessage(message);
                fractureStage.setTime(time);

                printDebug("Для энума " + stage +
                        " установили урон " + damage +
                        ", время " + time + " ms," +
                        " скорость " + speed +
                        " и сообщение \n\"" + message + "\"");
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().severe("Значение с путем fracture." + stage + " некоректное.");
            }

        }
    }


    /**
     * Загружает данные для системы кровопотери.
     */
    private void loadBleedingStageValues() {
        printDebug("Загружаем значения для стадий кровопотерь.");

        for (String bleedingEnum : stages.getConfigurationSection("bleeding_stage").getKeys(false)) {
            try {
                BLEEDING_CHANCE = stages.getInt("bleeding_chance");
                BleedingStage stage = BleedingStage.valueOf(bleedingEnum.toUpperCase());
                double damage = stages.getFloat("bleeding_stage." + bleedingEnum + ".damage");
                long time = stages.getLong("bleeding_stage." + bleedingEnum + ".time");
                String message = stages.getString("bleeding_stage." + bleedingEnum + ".message");
                // Энум получает константы.
                stage.setTime(time);
                stage.setDamage(damage);
                stage.setMessage(message);

                printDebug("Для энума " + stage.name() +
                        " установили урон " + damage +
                        ", время " + time +
                        " ms и сообщение \n\"" + message + "\"");


            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().severe("Значение с путем bleeding_stage." + bleedingEnum + " некоректное.");
                ex.printStackTrace();
            }


        }
    }

    private void loadThirstsValues() {
        printDebug("Выгружаем значения про жажду.");
        CHANGE_THIRST_TIME = stages.getLong("change_thirst_time");
        THIRST_DAMAGE_PERIOD = stages.getInt("thirst_period_damage");
        THIRST_DAMAGE = stages.getInt("thirst_damage");
        for (String minutes : stages.getConfigurationSection("thirst_message").getKeys(false)) {
            thirstMessages.put(Integer.valueOf(minutes), stages.getStringColor("thirst_message." + minutes));
        }
        printDebug("Сообщения для жажды: \n" + thirstMessages.values());
        printDebug("Жажда будет изменятся каждые " + CHANGE_THIRST_TIME + " секунд.");
        printDebug("Урон от жажды будет каждые " + THIRST_DAMAGE_PERIOD + " секунд.");

    }

    public void printDebug(String args) {
        if (debug) {
            getLogger().info(args);
        }
    }

    public void setSign(Sign sign, MiniGame miniGame) {
        sign.setLine(0, signs.getStringColor("line1")
                .replace("%number%", iterableWorld + "")
                .replace("%n%", miniGame.getOnline() + "")
                .replace("%m%", miniGame.getMaxPlayerSize() + ""));
        sign.setLine(1, signs.getStringColor("line2")
                .replace("%number%", iterableWorld + "")
                .replace("%n%", miniGame.getOnline() + "")
                .replace("%m%", miniGame.getMaxPlayerSize() + ""));
        sign.setLine(2, signs.getStringColor("line3")
                .replace("%number%", iterableWorld + "")
                .replace("%n%", miniGame.getOnline() + "")
                .replace("%m%", miniGame.getMaxPlayerSize() + ""));
        sign.setLine(3, signs.getStringColor("line4")
                .replace("%number%", iterableWorld + "")
                .replace("%n%", miniGame.getOnline() + "")
                .replace("%m%", miniGame.getMaxPlayerSize() + ""));
        sign.update();
        iterableWorld++;
    }

    public void updateSign() {
        iterableWorld = 1;
        for (Map.Entry<Sign, MiniGame> sign : availableSign.entrySet()) {
            this.setSign(sign.getKey(), sign.getValue());
        }
    }

    /**
     * Загружает и инициализирует миниигры.
     */
    private void loadMiniGameInfo() {
        printDebug("Загружаем данные для миниигр");

        MAX_PLAYER_ON_MINI_GAME = config.getInt("miniGamePlayerLimit");
        defaultSpawnPoint = config.getLocationList("spawnPoints");
        for (String miniGame : config.getStringList("miniGames")) {
            MineZMiniGame game = new MineZMiniGame(miniGame, MAX_PLAYER_ON_MINI_GAME, getPlugin());
            game.setSpawnPoint(defaultSpawnPoint);
            MiniGameManager.getInstance().addMiniGame(game);
        }
        getLogger().info("Загружены миниигры: " + StringUtils.join(MiniGameManager.getInstance()
                .getMiniGames()
                .keySet(), "|"));

        printDebug("Максимальное число игроков для миниигр: " + MAX_PLAYER_ON_MINI_GAME);
        printDebug("Загружено " + defaultSpawnPoint.size() + " локаций спавна.");
    }

    public void recalculateSpawnPosition() {
        for (MiniGame game : manager.getMiniGames().values()) {
            MineZMiniGame games = (MineZMiniGame) game;
            games.setSpawnPoint(defaultSpawnPoint);
        }
    }


    public static int getMaxPlayerOnMiniGame() {
        return MAX_PLAYER_ON_MINI_GAME;
    }

    public static long getBleedingPeriod() {
        return BLEEDING_PERIOD;
    }

    public static int getZombieHealth() {
        return ZOMBIE_HEALTH;
    }

    public static float getZombieSpeed() {
        return ZOMBIE_SPEED;
    }

    public static boolean isHalfLootDrop() {
        return HALF_LOOT_DROP;
    }

    public static boolean debug() {
        return debug;
    }

    public HashMap<Sign, MiniGame> getAvailableSign() {
        return availableSign;
    }

    public World getLobbyWorld() {
        return LOBBY;
    }

    public List<Location> getDefaultSpawnPoint() {
        return defaultSpawnPoint;
    }

    public void saveSpawnPoints() {
        List<String> list = defaultSpawnPoint.stream().map(Config::toString).collect(Collectors.toList());
        config.setAndSave("spawnPoints", list);
    }

    public void setLobbySpawnPoint(Location lobbySpawnPoint) {
        config.setAndSave("lobby_spawn_point", Config.toString(lobbySpawnPoint));
        this.lobbySpawnPoint = lobbySpawnPoint;
    }

    public void saveSigns() {
        for (Sign sign : availableSign.keySet()) {
            config.set("signs." + availableSign.get(sign)
                    .getMiniGameName() + ".sign_location", Config.toString(sign.getLocation()));
        }
        config.save();
    }


    public static long getChangeThirstTime() {
        return CHANGE_THIRST_TIME;
    }

    /**
     * Локация с рандомом, что бы не спавнились в одной точке.
     *
     * @return локацию для спавна в лобби.
     */
    public Location getLobbySpawnPoint() {
        Location loc = lobbySpawnPoint.clone();
        loc.add((Math.random() * 3) - 3, 0, (Math.random() * 3) - 3);
        return loc;
    }

    public HashMap<Integer, String> getThirstMessages() {
        return thirstMessages;
    }

    public int getIterableWorld() {
        return iterableWorld;
    }

    public int getChestsNumerable() {
        return chestsNumerable;
    }

    public static float getZombieFullMoonSpeed() {
        return ZOMBIE_FULL_MOON_SPEED;
    }

    public static int getZombieFullMoonHealth() {
        return ZOMBIE_FULL_MOON_HEALTH;
    }

    public static double getZombieDamage() {
        return ZOMBIE_DAMAGE;
    }

    public static double getZombieFullMoonDamage() {
        return ZOMBIE_FULL_MOON_DAMAGE;
    }

    public static int getDistanceExitCombat() {
        return DISTANCE_EXIT_COMBAT;
    }

    public static void setDistanceExitCombat(int distanceExitCombat) {
        DISTANCE_EXIT_COMBAT = distanceExitCombat;
    }

    public static boolean isFullMoon() {
        return FULL_MOON;
    }

    public static void setFullMoon(boolean fullMoon) {
        FULL_MOON = fullMoon;
    }

    public static int getFullMoonPeriod() {
        return FULL_MOON_PERIOD;
    }

    public static void setFullMoonPeriod(int fullMoonPeriod) {
        FULL_MOON_PERIOD = fullMoonPeriod;
    }

    public static int getFullMoonTime() {
        return FULL_MOON_TIME;
    }

    public static void setFullMoonTime(int fullMoonTime) {
        FULL_MOON_TIME = fullMoonTime;
    }

    public static int getBleedingChance() {
        return BLEEDING_CHANCE;
    }

    public static void setBleedingChance(int bleedingChance) {
        BLEEDING_CHANCE = bleedingChance;
    }

    public static int getFractureChance() {
        return FRACTURE_CHANCE;
    }

    public static void setFractureChance(int fractureChance) {
        FRACTURE_CHANCE = fractureChance;
    }

    public static boolean isFractureOnlyFall() {
        return FRACTURE_ONLY_FALL;
    }

    public static int getThirstDamagePeriod() {
        return THIRST_DAMAGE_PERIOD;
    }

    public static int getGrenadeDamage() {
        return GRENADE_DAMAGE;
    }

    public static int getGrenadeDistance() {
        return GRENADE_DISTANCE;
    }

    public static int getLureDistance() {
        return LURE_DISTANCE;
    }

    public static int getThirstDamage() {
        return THIRST_DAMAGE;
    }

    public static long getLureTime() {
        return LURE_TIME;
    }

    public Config getSigns() {
        return signs;
    }

    public Config getStages() {
        return stages;
    }

    public Config getChests() {
        return chests;
    }

    public Config getStands() {
        return stands;
    }

    public Config getItems() {
        return items;
    }

    public Config getDeath() {
        return death;
    }

    public static ModuleMineZ getInstance() {
        return instance;
    }
}

