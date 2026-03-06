package dev.piyush.shyamduels.util;

import dev.piyush.shyamduels.ShyamDuels;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageUtils {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, Map.of());
    }

    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender == null)
            return;

        String rawMessage = ShyamDuels.getInstance().getConfigManager().getMessages().getString(path);
        if (rawMessage == null || rawMessage.isEmpty())
            return;

        String prefix = ShyamDuels.getInstance().getConfigManager().getMessages().getString("prefix", "");
        Component content = parse(prefix + rawMessage, placeholders);

        sender.sendMessage(content);
    }

    public static void sendRawMessage(CommandSender sender, String rawMessage, Map<String, String> placeholders) {
        if (sender == null || rawMessage == null || rawMessage.isEmpty())
            return;
        sender.sendMessage(parse(rawMessage, placeholders));
    }

    public static void sendActionBar(Player player, String path, Map<String, String> placeholders) {
        if (player == null)
            return;

        String rawMessage = ShyamDuels.getInstance().getConfigManager().getMessages().getString(path);
        if (rawMessage == null || rawMessage.isEmpty())
            return;

        String message = rawMessage;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("<" + entry.getKey() + ">", entry.getValue());
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        player.sendActionBar(parseColors(message));
    }

    public static void sendTitle(Player player, String path) {
        sendTitle(player, path, Map.of());
    }

    public static void sendTitle(Player player, String path, Map<String, String> placeholders) {
        if (player == null)
            return;

        String titleStr = ShyamDuels.getInstance().getConfigManager().getMessages().getString(path + ".title", "");
        String subtitleStr = ShyamDuels.getInstance().getConfigManager().getMessages().getString(path + ".subtitle",
                "");

        if (titleStr.isEmpty() && subtitleStr.isEmpty())
            return;

        Component title = parse(titleStr, placeholders);
        Component subtitle = parse(subtitleStr, placeholders);

        player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        if (input == null)
            return Component.empty();

        String processed = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        processed = convertLegacyToMiniMessage(processed);

        return MM.deserialize(processed);
    }

    private static String convertLegacyToMiniMessage(String input) {
        if (input == null)
            return "";

        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher hexMatcher = hexPattern.matcher(input);
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        String result = hexBuffer.toString();

        result = result.replace("&0", "<black>");
        result = result.replace("&1", "<dark_blue>");
        result = result.replace("&2", "<dark_green>");
        result = result.replace("&3", "<dark_aqua>");
        result = result.replace("&4", "<dark_red>");
        result = result.replace("&5", "<dark_purple>");
        result = result.replace("&6", "<gold>");
        result = result.replace("&7", "<gray>");
        result = result.replace("&8", "<dark_gray>");
        result = result.replace("&9", "<blue>");
        result = result.replace("&a", "<green>");
        result = result.replace("&b", "<aqua>");
        result = result.replace("&c", "<red>");
        result = result.replace("&d", "<light_purple>");
        result = result.replace("&e", "<yellow>");
        result = result.replace("&f", "<white>");
        result = result.replace("&k", "<obf>");
        result = result.replace("&l", "<b>");
        result = result.replace("&m", "<st>");
        result = result.replace("&n", "<u>");
        result = result.replace("&o", "<i>");
        result = result.replace("&r", "<reset>");

        return result;
    }

    public static Component parseItem(String input) {
        if (input == null)
            return Component.empty();

        if (input.contains("&") || input.matches(".*#[A-Fa-f0-9]{6}.*")) {
            String colored = colorLegacy(input);
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .deserialize(colored)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
        }
        return MM.deserialize(input)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
    }

    public static Component parseOrLegacy(String input, Map<String, String> placeholders) {
        String parsed = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            parsed = parsed.replace("<" + entry.getKey() + ">", entry.getValue());
        }

        if (parsed.contains("&")) {
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(parsed);
        }
        return MM.deserialize(parsed);
    }

    public static String get(String path) {
        return ShyamDuels.getInstance().getConfigManager().getMessages().getString(path, path);
    }

    public static String getMessage(String path) {
        return get(path);
    }

    public static String get(String path, Map<String, String> placeholders) {
        String msg = get(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            msg = msg.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return msg;
    }

    public static List<String> getList(String path) {
        return ShyamDuels.getInstance().getConfigManager().getMessages().getStringList(path);
    }

    public static List<String> getList(String path, Map<String, String> placeholders) {
        List<String> list = getList(path);
        List<String> parsed = new ArrayList<>();
        for (String line : list) {
            String msg = line;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
                msg = msg.replace("<" + entry.getKey() + ">", entry.getValue());
            }
            parsed.add(msg);
        }
        return parsed;
    }

    public static String parseLegacy(String input) {
        String colored = colorLegacy(input);
        Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize(colored);
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .serialize(comp);
    }

    @SuppressWarnings("deprecation")
    public static String colorLegacy(String input) {
        if (input == null)
            return "";

        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = hexPattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append("\u00A7").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        String result = buffer.toString();

        hexPattern = java.util.regex.Pattern.compile("#([A-Fa-f0-9]{6})");
        matcher = hexPattern.matcher(result);
        buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append("\u00A7").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        result = buffer.toString();

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String color(String input) {
        return colorLegacy(input);
    }

    public static Component parseColors(String input) {
        if (input == null)
            return Component.empty();

        if (input.contains("&") || input.matches(".*#[A-Fa-f0-9]{6}.*")) {
            String colored = colorLegacy(input);
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                    .deserialize(colored);
        }

        return MM.deserialize(input);
    }

    public static Component parseItemColors(String input) {
        return parseColors(input)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
    }

    public static void broadcast(String path, Map<String, String> placeholders) {
        org.bukkit.Bukkit.broadcast(parse(get(path, placeholders), Map.of()));
    }
}
