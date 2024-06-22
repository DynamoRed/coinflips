package com.dynamored.coinflip.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.managers.GameManager;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.utils.Head;
import com.dynamored.coinflip.utils.ItemBuilder;

public class GamesCommand implements TabExecutor {

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1) {
			return Arrays.asList("<page>", "help");
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

			List<String> subcommands = Arrays.asList("create", "join", "cancel", "help");
			if (args.length >= 1 && subcommands.contains(args[0])) {
				player.performCommand("coinflip " + args[0] + " " + (args.length >= 2 ? args[1] : ""));
				return true;
			}

			int page = args.length == 1 ? Integer.parseInt(args[0]) : 1;

			List<CoinflipGame> games = GameManager.getWaitingGames();

			int perPage = 21;
			int maxPage = (games.size() + perPage - 1) / perPage;
			if (maxPage <= 1) maxPage = 1;
			if (page <= 1) page = 1;
			if (page > maxPage) page = maxPage;

			Inventory coinflipsMenu = Bukkit.createInventory(null, 27, "§7[§d☄§7] §5Coinflips §8[" + page + "/" + maxPage + "]");

			ItemStack buffer = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§0▉").addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).getItemStack();
			coinflipsMenu.setItem(0, buffer);
			coinflipsMenu.setItem(18, buffer);

			coinflipsMenu.setItem(8, buffer);
			coinflipsMenu.setItem(26, buffer);

			if (page > 1)
				coinflipsMenu.setItem(9, new ItemBuilder(new Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=", "§e◀ Previous Page", "").getHead()).addItemNbt(Coinflip.getInstance().nbtCoinflipsMenuPage, PersistentDataType.INTEGER, page-1).getItemStack());
			else
				coinflipsMenu.setItem(9, buffer);

			if (games.size() > perPage * page)
				coinflipsMenu.setItem(17, new ItemBuilder(new Head("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=", "§eNext Page ▶", "").getHead()).addItemNbt(Coinflip.getInstance().nbtCoinflipsMenuPage, PersistentDataType.INTEGER, page+1).getItemStack());
			else
				coinflipsMenu.setItem(17, buffer);

			int idx = 0;
			int slot = 1;

			for (CoinflipGame game : games) {
				if (idx >= perPage * (page - 1) && idx < perPage * page && game.getCreator().isOnline()) {
					String name = "§6" + game.getCreator().getPlayer().getDisplayName() + "§7's game §f#" + game.getSessionId();
					List<String> lores = Arrays.asList("", "§7§l❁ §7Bet: §6" + game.getAmount() + " " + Coinflip.getEconomy().currencyNameSingular(), player.getUniqueId().equals(game.getCreator().getUniqueId()) ? "§7Cancel: §c[Right Click]" : "§7Join: §a[Left Click]");
					coinflipsMenu.setItem(slot, new ItemBuilder(new Head(null, name, game.getCreator().getPlayer()).getHead()).addLore(lores).addItemNbt(Coinflip.getInstance().nbtCoinflipsMenu, PersistentDataType.STRING, game.getId().toString()).addItemNbt(Coinflip.getInstance().nbtCoinflipsMenuIsOwner, PersistentDataType.BOOLEAN, player.getUniqueId().equals(game.getCreator().getUniqueId())).getItemStack());
					slot++;
					if (slot == 8) slot = 10;
					if (slot == 17) slot = 19;
				}

				idx++;
			}

			player.openInventory(coinflipsMenu);
		} else {
			sender.sendMessage(Coinflip.getInstance().prefix + "§7[§c☄§7] Reserved to players");
			return false;
		}

		return true;
	}
}
