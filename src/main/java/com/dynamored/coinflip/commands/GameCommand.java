package com.dynamored.coinflip.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
			return Arrays.asList("create", "help");
		} else if (args.length == 2) {
			if (sender instanceof Player && args[0].equalsIgnoreCase("create")) {
				Player player = (Player) sender;
				player.playSound(player, Sound.UI_BUTTON_CLICK, .5f, 1);
				return Arrays.asList(Coinflip.getInstance().translate(player.getLocale(), "_Amount_", null));
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

			if (args.length == 0) {
				player.performCommand("coinflips");
				return true;
			}

			if (args.length == 1) {
				switch (args[0]) {
					case "create":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Usage_", null) + ": /" + label + " create <amount>");
						return false;
					case "join":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Usage_", null) + ": /" + label + " join <gameId>");
						return false;
					case "cancel":
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§b☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Usage_", null) + ": /" + label + " cancel <gameId>");
						return false;
					case "help":
						player.performCommand("cfhelp");
						return true;
					default:
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Unknown_Subcommand_", null));
						return false;
				}
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("create")) {
					try {
						double amount = Double.parseDouble(args[1]);

						if (amount <= 0 || amount < Coinflip.getInstance().getConfiguration().minAmountPerGame || amount > Coinflip.getInstance().getConfiguration().maxAmountPerGame) throw new InvalidAmountRangeException();

						if (!Coinflip.getEconomy().has(player, amount)) {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Not_Enough_Money_", null));
							return false;
						}

						if (GameManager.getPlayerWaitingGames(player.getUniqueId()).size() >= Coinflip.getInstance().getConfiguration().maxSimultaneousGamesPerPlayer) {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Max_Simultaneous_Games_", null));
							return false;
						}

						CoinflipGame game = new CoinflipGame(player, amount);
						game.save();

						for (Player online : Bukkit.getOnlinePlayers()) {
							TextComponent baseComponent = new TextComponent(Coinflip.getInstance().prefix + "§7[§d☄§7] " + Coinflip.getInstance().translate(online.getLocale(), "_Created_New_Game_", new HashMap<String, String>() {{ put("playerName", player.getDisplayName()); put("amount", String.valueOf(game.getAmount())); put("currency", Coinflip.getEconomy().currencyNamePlural()); }}) + " ");

							TextComponent joinComponent = new TextComponent("§a[" + Coinflip.getInstance().translate(online.getLocale(), "_Join_", null) + "]");
							joinComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coinflip join " + game.getId()));
							joinComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Coinflip.getInstance().translate(online.getLocale(), "_Click_To_Join_", null))));

							baseComponent.addExtra(joinComponent);

							TextComponent baseCreatorComponent = new TextComponent(Coinflip.getInstance().prefix + "§7[§a☄§7] " + Coinflip.getInstance().translate(online.getLocale(), "_Created_New_Game_Waiting_Opponent_", null) + " ");

							TextComponent cancelComponent = new TextComponent("§c[" + Coinflip.getInstance().translate(online.getLocale(), "_Cancel_", null) + "]");
							cancelComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/coinflip cancel " + game.getId()));
							cancelComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Coinflip.getInstance().translate(online.getLocale(), "_Cannot_Cancel_", null))));

							baseCreatorComponent.addExtra(cancelComponent);

							if (online.getUniqueId().equals(player.getUniqueId())) online.spigot().sendMessage(baseCreatorComponent);
							else online.spigot().sendMessage(baseComponent);
						}

						Coinflip.getInstance().colorStr("§aCreating game " + game.getId().toString());

					} catch (NumberFormatException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Amount_", null));
						return false;
					} catch (InvalidAmountRangeException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Amount_Range_", new HashMap<String, String>() {{ put("minAmount", String.valueOf(Coinflip.getInstance().getConfiguration().minAmountPerGame)); put("maxAmount", String.valueOf(Coinflip.getInstance().getConfiguration().maxAmountPerGame)); put("currency", Coinflip.getEconomy().currencyNamePlural()); }}) + " ");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("join")) {
					try {
						UUID id = UUID.fromString(args[1]);

						CoinflipGame game = GameManager.getById(id);
						if (game != null) {
							if (!Coinflip.getEconomy().has(player, game.getAmount())) {
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Not_Enough_Money_", null));
								return false;
							}

							if (game.getCreator().getUniqueId().equals(player.getUniqueId())) {
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Cannot_Join_Own_", null));
								return false;
							}

							game.start(player);
						} else {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Game_Id_", null));
							return false;
						}
					} catch (IllegalArgumentException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Game_Id_", null));
						return false;
					} catch (IllegalGameStart e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Game_Started_Cannot_Join_", null));
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
								sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Not_Creator_Cannot_Cancel_", null));
								return false;
							}
						} else {
							sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Game_Id_", null));
							return false;
						}
					} catch (IllegalArgumentException e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Invalid_Game_Id_", null));
						return false;
					} catch (IllegalGameCancellation e) {
						sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Cannot_Cancel_", null));
						return false;
					}
				} else {
					sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] " + Coinflip.getInstance().translate(player.getLocale(), "_Unknown_Args_", null));
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
