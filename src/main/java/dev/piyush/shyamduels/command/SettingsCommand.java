package dev.piyush.shyamduels.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.gui.SettingsGui;
import org.bukkit.entity.Player;

@CommandAlias("settings|preferences|prefs")
@Description("Open player settings menu")
public class SettingsCommand extends BaseCommand {
    
    private final ShyamDuels plugin;
    
    public SettingsCommand(ShyamDuels plugin) {
        this.plugin = plugin;
    }
    
    @Default
    public void onSettings(Player player) {
        new SettingsGui(plugin, player).open(player);
    }
}
