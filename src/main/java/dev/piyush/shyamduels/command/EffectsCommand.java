package dev.piyush.shyamduels.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.piyush.shyamduels.ShyamDuels;
import dev.piyush.shyamduels.gui.KillEffectsGui;
import org.bukkit.entity.Player;

@CommandAlias("effects|killeffects|ke")
@Description("Open kill effects menu")
public class EffectsCommand extends BaseCommand {
    
    private final ShyamDuels plugin;
    
    public EffectsCommand(ShyamDuels plugin) {
        this.plugin = plugin;
    }
    
    @Default
    public void onEffects(Player player) {
        new KillEffectsGui(plugin, player).open(player);
    }
}
