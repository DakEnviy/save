package ua.deamonish.modulesystem.command;

import ua.deamonish.modulesystem.util.ChatComponent;
import ua.deamonish.modulesystem.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("javadoc")
public abstract class Command extends org.bukkit.command.Command {

    private CommandSender sender;
    private String[] args;
    private String currentName; // то имя, которое указал игрок в данный момент написания команды
    private long delay = 0; // период использования команды (для каждого игрока), милисекунды

    private boolean isExecute = false;

    /**
     * Команда
     * @param name имя команды
     * @param perm право команды (если его нету, пишите null)<br>
     *             Если будет указано это право, то не нужно будет использовать <code>::checkPerm(String)</code>
     * @param aliases (синонимы команды)
     */
    public Command(String name, String perm, String... aliases) {
        super(name.toLowerCase());
        this.setPermission(perm);
        this.setPermissionMessage("§cУ вас нет прав.");
        this.setAliases(Arrays.asList(aliases));
    }


    /**
     * Старый конструктор куоманды
     * @param name имя команды
     * @deprecated устарел, используйте <code>::<init>init(String,String,String[])</code>
     */
    @Deprecated
    public Command(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String currentCommand, String[] args) {
        // есть ли соответствующие права
        if (!this.testPermission(sender)) {
            return false;
        }

        if (!isExecute) {
            isExecute = true;
            this.sender = sender;
            this.args = args;
            this.currentName = currentCommand;

            boolean ret;
            try {
                ret = this.execute(sender, args);
                isExecute = false;  // и раз
            } catch (CommandException e) {
                isExecute = false;  // и два
                ret = false;
            } catch (Exception e) {
                isExecute = false;  // и три
                sender.sendMessage("§cПроизошла ошибка при выполнении команды. Ошибка уже отправлена администраторам.");
                if (sender.hasPermission("mineland.libs.senderrorcommand")) {
                    ChatComponent.getErrorReport(e, "§cОшибка [Наведи].").send(sender);
                }

                e.printStackTrace();
                ret = false;
            } finally {
                isExecute = false;  // и четыре
            }
            this.sender = null;
            this.args = null;
            isExecute = false; // и пять
            return ret;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        if (!isExecute) {
            isExecute = true;
            this.sender = sender;
            this.args = args;
            this.currentName = label;
            List<String> ret;
            try {
                ret = tab(sender, args);
                isExecute = false; // и раз
            } catch (CommandException e) {
                isExecute = false; // и два
                ret = null;
            } catch (Exception e) {
                isExecute = false; // и три
                e.printStackTrace();
                ret = null;
            } finally {
                isExecute = false; // и черыте
            }

            this.sender = null;
            this.args = null;
            isExecute = false; // и пять
            return ret;
        }
        return null;
    }

    /**
     * Получить период использования команды (для каждого игрока)
     * @return период (милисекунды)
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Установить период использования команды (для каждого игрока)
     * @param delay период (милисекунды)
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    public abstract List<String> tab(CommandSender sender, String[] args);


    public abstract boolean execute(CommandSender sender, String[] args);


    /* -=-=-=-=-=-=-=-=-=-=- */
	/*     Полезные штуки    */
	/* -=-=-=-=-=-=-=-=-=-=- */

    /**
     * Добавить еще одно название команды
     * @param cmd - команда
     * @deprecated используйте первый конструктор <code>Command(String, String, String...)</code>
     */
    @Deprecated
    public void addAlias(String cmd) {
        this.getAliases().add(cmd);
    }

    /* -=-=-=-=-=-=-=-=-=-=- */
	/*  Проверки аргументов  */
	/* -=-=-=-=-=-=-=-=-=-=- */

    /**
     * Получить мир
     * @param arg по названию
     * @return - мир.
     */
    public World getWorld(String arg) throws CommandException {
        World world = Bukkit.getWorld(arg);
        if (world == null) {
            String worlds = "";
            for (World w : Bukkit.getWorlds())
                worlds += w.getName() + ' ';
            sender.sendMessage("§сМир не найден. Доступные миры: " + worlds);
            throw new CommandException();
        }
        return world;
    }

    /**
     * Если тут указан мир
     * @param arg - имя мира.
     * @return true - если такой мир есть. false - если такого мира нет.
     */
    public boolean isWorld(String arg) {
        return Bukkit.getWorld(arg) != null;
    }

    /**
     * Получить integer значение
     * @param arg - строка, с цифрами
     * @return int значение, полученое при парсинге строки.
     */
    public int getInt(String arg) throws CommandException {
        try {
            return Integer.parseInt(arg);
        } catch (Exception e) {
            this.errorBadValue(arg);
            return 0;
        }
    }

    /**
     * Преобразовать в double значение
     * @param arg строка, с цифрами
     * @return double значение, полученое при парсинге строки.
     * @throws CommandException
     */
    public double getDouble(String arg) throws CommandException {
        try {
            return Double.parseDouble(arg);
        } catch (Exception e) {
            this.errorBadValue(arg);
            return 0;
        }
    }

    /**
     * Вызвать CommandException
     * @param arg
     * @throws CommandException
     */
    public void errorNotFound(String arg) throws CommandException {
        sender.sendMessage("Значение "+arg + "не найдено.");
        throw new CommandException();
    }

    /**
     * Плохое значение
     * Вызвать CommandException
     * @throws CommandException
     */
    public void errorBadValue(String arg) throws CommandException {
        sender.sendMessage("§cНекорректное значение: " + arg);
        throw new CommandException();
    }

    public void errorArgumentNotFound() throws CommandException {
        sender.sendMessage("§cАргумент не найден.");
        throw new CommandException();
    }

    /**
     * Значениче должно быть в диапазоне от min до max, включая их
     * @param value это значение
     * @param min
     * @param max
     */
    public void checkDiapason(double value, double min, double max) throws CommandException {
        if (value < min || value > max) {
            sender.sendMessage("§сУкажите число в диапазоне от " + this.getDisplay(min) + " до" + this.getDisplay(max));
            throw new CommandException();
        }
    }

    private String getDisplay (double d) {
        return d == (int)d ? String.valueOf((int)d) : String.valueOf(d);
    }



    /**
     * Получить игрока по нику
     * @param name
     * @return
     * @throws CommandException
     */
    public Player getPlayer(String name) throws CommandException {
        Player player = Bukkit.getPlayer(name);


        if (player == null) {
            this.sender.sendMessage("Игрок с ником " + name +" не онлайн.");
            throw new CommandException();
        }
        return player;
    }

    /**
     * В отличии от оригинального метода - этот не выбивает CommandException, когда игрок NULL и не пишет сообщение "Игрок не онлайн".
     */
    public Player getPlayerWithoutException(String name){
        return Bukkit.getPlayer(name);
    }

    /**
     * Есть ли такой игрок по этому нику
     * @param name
     * @return
     */
    public boolean isPlayer(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    /**
     * Проверить наличие прав
     * @param perm
     * @return
     */
    public boolean isPerm(String perm) {
        return !(sender instanceof Player) || sender.hasPermission(perm);
    }

    /**
     * Игрок должен иметь это право
     * @param perm
     * @throws CommandException
     */
    public void checkPerm(String perm) throws CommandException {
        if (!this.isPerm(perm)) {
            sender.sendMessage("§cУ вас нет прав.");
            throw new CommandException();
        }
    }

    /**
     * тот, кто написал команду - игрок?
     * @return true, если да
     */
    public boolean isSenderPlayer() {
        return sender instanceof Player;
    }

    /**
     * Нельзя писать с консоли
     * @throws CommandException
     */
    public void checkIsConsole() throws CommandException {
        if (!this.isSenderPlayer()) {
            sender.sendMessage("Нельзя писать с консоли.");
            throw new CommandException();
        }
    }

    /**
     * Если игрок себя указал
     * @param arg
     */
    public void checkIsThisPlayer(String arg) throws CommandException {
        if (sender.getName().equals(arg)) {
            sender.sendMessage("§cНельзя указывать себя.");
            throw new CommandException();
        }
    }

    /**
     * Сколько минимум должно быть аргументов
     * @param min минимум аргументов
     * @throws CommandException
     */
    public void checkSizeArguments(int min) throws CommandException {
        if (args.length < min) {
            String message = "§cНеправильное кол-во аргументов.";
            String name = this.currentName;
            String usage = "/" + name + " ";
            for (String arg : this.args ){
                usage += arg + " ";
            }

            for (int i = args.length; i < min; i++) {
                usage += "[???] ";
            }
            message = StringUtil.replace(message, "%usage%", usage);
            message = StringUtil.replace(message, "%cmd%", "/" + name);
            sender.sendMessage(message);
            throw new CommandException();
        }
    }

    public boolean getBoolean(String value) throws CommandException {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            this.errorBadValue(value);
            return false;
        }
    }

    /**
     * Получить список ников игроков
     * @return
     */
    public List<String> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player:: getName).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Получить список названий миров
     * @return
     */
    public List<String> getWorlds() {
        ArrayList<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds())
            worlds.add(world.getName());
        return worlds;
    }

    /**
     * Отсеять список на наличие arg в строках
     * @param list
     * @param arg
     * @return
     */
    public List<String> getContains(Collection<String> list, String arg) {
        ArrayList<String> tab = new ArrayList<>();
        for (String s : list)
            if (StringUtil.containsIgnoreCase(s, arg))
                tab.add(s);
        return tab;
    }

    /**
     * Объеденить аргументы в строку через пробел
     * @param start первый аргумент
     * @return
     */
    public String getArguments(int start) {
        String s = "";
        for (int i = start; i < args.length; i++) {
            s += i == start ? args[i] : ' ' + args[i];
        }
        return s;
    }


    public void checkError(boolean b, String message) throws CommandException {
        if (b && message != null) {
            sender.sendMessage(message);
            throw new CommandException();
        }
    }

    public void argsToLowerCase0() {
        this.argsToLowerCase(0);
    }

    public void argsToLowerCase(int i) {
        args[i] = args[i].toLowerCase();
    }


    /**
     * Полчуить значение Long
     * @param arg
     * @return
     * @throws CommandException
     */
    public long getLong(String arg) throws CommandException {
        try {
            return Long.parseLong(arg);
        } catch (Exception e) {
            this.errorBadValue(arg);
            throw new CommandException();
        }
    }




}
