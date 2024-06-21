package com.dynamored.coinflip.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.dynamored.coinflip.managers.GameManager;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.IllegalGameCancellation;

public class PlayerQuitEvent implements Listener {

	@EventHandler
	public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) throws IllegalGameCancellation {
		Player player = event.getPlayer();

		List<CoinflipGame> games = GameManager.getPlayerWaitingGames(player.getUniqueId());
		for (CoinflipGame game : games) game.cancel();
	}
}
