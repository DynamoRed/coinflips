package com.dynamored.coinflip.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.CoinflipGameRefund;
import com.dynamored.coinflip.models.GameStatus;
import com.dynamored.coinflip.models.GameWinner;
import com.dynamored.coinflip.utils.Head;
import com.dynamored.coinflip.utils.ItemBuilder;

public class GameManager {

	public static CoinflipGame getById(UUID id) {
		return Coinflip.getInstance().games.get(id);
	}

	public static List<CoinflipGame> getWaitingGames() {
		List<CoinflipGame> filteredGames = new ArrayList<CoinflipGame>();

		for (CoinflipGame game : Coinflip.getInstance().games.values()) {
			if (game.getStatus() == GameStatus.WAITING) filteredGames.add(game);
		}

		Collections.sort(filteredGames);

		return filteredGames;
	}

	public static List<CoinflipGame> getPlayerWaitingGames(UUID id) {
		List<CoinflipGame> filteredGames = new ArrayList<CoinflipGame>();

		for (CoinflipGame game : Coinflip.getInstance().games.values()) {
			if ((game.getStatus() == GameStatus.WAITING || game.getStatus() == GameStatus.STARTED) && (game.getCreator().getUniqueId().equals(id) ||(game.getOpponent() != null && game.getOpponent().getUniqueId().equals(id)))) filteredGames.add(game);
		}

		return filteredGames;
	}

	public static List<CoinflipGameRefund> getPlayerRefunds(UUID id) {
		List<CoinflipGameRefund> filteredGamesRefunds = new ArrayList<CoinflipGameRefund>();

		for (CoinflipGameRefund gameRefund : Coinflip.getInstance().gamesRefunds.values()) {
			if (!gameRefund.isRefunded() && gameRefund.getPlayer().getUniqueId().equals(id)) filteredGamesRefunds.add(gameRefund);
		}

		return filteredGamesRefunds;
	}

	public static List<ItemStack> generateHeads(Player creator, Player opponent, GameWinner winner) {
		List<ItemStack> list = new ArrayList<>();

        int n = 50;

        ItemStack itemA = new ItemBuilder(new Head(null, "§c" + creator.getDisplayName(), "", creator).getHead()).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).setSkullOwner(creator).getItemStack();
        ItemStack itemB = new ItemBuilder(new Head(null, "§b" + opponent.getDisplayName(), "", opponent).getHead()).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).setSkullOwner(opponent).getItemStack();

        for (int i = 0; i < n; i++) {
            if (i % 2 == 0)
                list.add(itemA);
            else
                list.add(itemB);
        }

		if (winner == GameWinner.OPPONENT) list.add(itemB);

		return list;
	}
}
