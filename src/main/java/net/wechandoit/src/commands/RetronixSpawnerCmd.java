package net.wechandoit.src.commands;

import net.wechandoit.src.Main;
import net.wechandoit.src.gui.GUIManager;
import net.wechandoit.src.utils.ItemBuilder;
import net.wechandoit.src.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class RetronixSpawnerCmd implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try {
            if (args[0].equalsIgnoreCase("shop")) {
                if (sender instanceof Player)
                    ((Player) sender).openInventory(Main.getShopInventory());
            }
            if (sender.isOp()) {

                if (args.length == 1) {
                    if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
                        Main.getStackableSpawnerManager().setStackableSpawnerList(new ArrayList<>());
                        sender.sendMessage(MessageUtils.chat("&cSpawner Data was cleared!"));
                    } else if (args.length == 1 && args[0].equalsIgnoreCase("size"))
                    {
                        sender.sendMessage(MessageUtils.chat(Main.getStackableSpawnerManager().getStackableSpawnerList().size() + " spawners"));
                    } else if (args.length == 1 && args[0].equalsIgnoreCase("debug"))
                    {
                        if (sender instanceof Player)
                            ((Player) sender).openInventory(GUIManager.getDebugSpawnerMenu().get(0));
                    }
                }

                else if (args.length == 2 && args[0].equalsIgnoreCase("convert")) {
                    String pluginName = args[1];
                    Main.getStackableSpawnerManager().convertSpawners(pluginName);
                    sender.sendMessage(MessageUtils.chat("&cDone Converting!"));
                }

                else if (args.length == 5 && args[0].equalsIgnoreCase("give") && args[2].equalsIgnoreCase("spawner")) {
                    Player target = Bukkit.getPlayer(args[1]);
                    EntityType entityType = EntityType.valueOf(args[3].toUpperCase());
                    int amount = Integer.parseInt(args[4]);
                    target.getInventory().addItem(ItemBuilder.getSpawnerItem(entityType, amount));
                }
            }
        } catch (CommandException ignored){}
        return true;
    }
}
