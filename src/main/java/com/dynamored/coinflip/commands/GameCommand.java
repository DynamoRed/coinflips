package com.dynamored.coinflip.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.managers.GameManager;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.IllegalGameCancellation;
import com.dynamored.coinflip.models.IllegalGameStart;
import com.dynamored.coinflip.models.InvalidAmountRangeException;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class GameCommand implements TabExecutor {

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1) {
			return Arrays.asList("create");
		} else if (args.length == 2) {
			if (sender instanceof Player && args[0].equalsIgnoreCase("create")) {
				Player player = (Player) sender;
				player.playSound(player, Sound.UI_BUTTON_CLICK, .5f, 1);
				return Arrays.asList("<amount>");
			}
		}

		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		label = label.toLowerCase();
		for (int i = 0; i < args.length; i++) args[i] = args[i].toLowerCase();

		if (sender instanceof Player){
			Player player = (Player) sender;

			if (args.length == 1) {
				switch (args[0]) {
					case "create":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] Usage: /coinflip create <amount>");
						return false;
					case "join":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] Usage: /coinflip join <gameId>");
						return false;
					case "cancel":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] Usage: /coinflip cancel <gameId>");
						return false;
					default:
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Unknown subcommand");
						return false;
				}
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("create")) {
					try {
						double amount = Double.parseDouble(args[1]);

						if (amount <= 0 || amount < Coinflip.getInstance().getConfiguration().minAmountPerGame || amount > Coinflip.getInstance().getConfiguration().maxAmountPerGame) throw new InvalidAmountRangeException();

						if (!Coinflip.getEconomy().has(player, amount)) {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You do not have enough money in your bank account");
							return false;
						}

						if (GameManager.getPlayerWaitingGames(player.getUniqueId()).size() >= Coinflip.getInstance().getConfiguration().maxSimultaneousGamesPerPlayer) {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You have reached the maximum number of simultaneous games allowed");
							return false;
						}

						CoinflipGame game = new CoinflipGame(player, amount);
						game.save();

						TextComponent baseComponent = new TextComponent(Coinflip.getInstance().prefix + "§7[§d☄§7] §f" + player.getDisplayName() + " §7created a new coinflip for §6" + game.getAmount() + " " + Coinflip.getEconomy().currencyNamePlural() + " §7! ");

						TextComponent joinComponent = new TextComponent("§a[Join]");
						joinComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coinflip join " + game.getId()));
						joinComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to join !")));

						baseComponent.addExtra(joinComponent);

						TextComponent baseCreatorComponent = new TextComponent(Coinflip.getInstance().prefix + "§7[§a☄§7] You have just created a new coinflip, wait for an opponent to join for the game to start. ");

						TextComponent cancelComponent = new TextComponent("§c[Cancel]");
						cancelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coinflip cancel " + game.getId()));
						cancelComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to cancel")));

						baseCreatorComponent.addExtra(cancelComponent);

						for (Player online : Bukkit.getOnlinePlayers()) {
							if (online.getUniqueId().equals(player.getUniqueId())) online.spigot().sendMessage(baseCreatorComponent);
							else online.spigot().sendMessage(baseComponent);
						}

						Coinflip.getInstance().colorStr("§aCreating game " + game.getId().toString());

					} catch (NumberFormatException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Invalid amount");
						return false;
					} catch (InvalidAmountRangeException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Amount must be a positive number, between " + Coinflip.getInstance().getConfiguration().minAmountPerGame + " and " + Coinflip.getInstance().getConfiguration().maxAmountPerGame);
						return false;
					}
				} else if (args[0].equalsIgnoreCase("join")) {
					try {
						UUID id = UUID.fromString(args[1]);

						CoinflipGame game = GameManager.getById(id);
						if (game != null) {
							if (!Coinflip.getEconomy().has(player, game.getAmount())) {
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You do not have enough money in your bank account");
								return false;
							}

							if (game.getCreator().getUniqueId().equals(player.getUniqueId())) {
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You cannot join your own game");
								return false;
							}

							game.start(player);
						} else {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Invalid game ID");
							return false;
						}
					} catch (IllegalArgumentException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Invalid game ID");
						return false;
					} catch (IllegalGameStart e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] This game has started or is canceled, you cannot join it");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("cancel")) {
					try {
						UUID id = UUID.fromString(args[1]);

						CoinflipGame game = GameManager.getById(id);
						if (game != null) {
							if (game.getCreator().getUniqueId().equals(player.getUniqueId())) {
								game.cancel();
							} else {
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You are not the creator of this game, you cannot cancel it");
								return false;
							}
						} else {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Invalid game ID");
							return false;
						}
					} catch (IllegalArgumentException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Invalid game ID");
						return false;
					} catch (IllegalGameCancellation e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] You cannot cancel this game");
						return false;
					}
				} else {
					sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Unknown command arguments");
					return false;
				}
			}
		} else {
			sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Reserved to players");
			return false;
		}

		return true;
	}
}
