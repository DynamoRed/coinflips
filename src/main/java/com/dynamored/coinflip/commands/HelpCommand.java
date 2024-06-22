package com.dynamored.coinflip.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynamored.coinflip.Coinflip;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class HelpCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		label = label.toLowerCase();

		if (sender instanceof Player){
			Player player = (Player) sender;

			TextComponent gamesBaseComponent = new TextComponent(" §6§l→ §eView currently available coinflip games: §f/coinflips ");

			TextComponent tryComponent = new TextComponent("§d[Try]");
			tryComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coinflips"));
			tryComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("See available games")));

			gamesBaseComponent.addExtra(tryComponent);

			TextComponent gameBaseComponent = new TextComponent(" §6§l→ §eCreate a new coinflip game: §f/coinflip create §7<amount>");

			tryComponent = new TextComponent("§d[Try]");
			tryComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/coinflip create 10"));
			tryComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Create a new coinflip game")));

			gameBaseComponent.addExtra(tryComponent);

			player.sendMessage(
				"",
				Coinflip.getInstance().prefix + "§7[§d☄§7] §fHelp Menu",
				""
			);

			player.spigot().sendMessage(gameBaseComponent);
			player.spigot().sendMessage(gamesBaseComponent);
			player.sendMessage("");
		} else {
			sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Reserved to players");
			return false;
		}

		return true;
	}
}
