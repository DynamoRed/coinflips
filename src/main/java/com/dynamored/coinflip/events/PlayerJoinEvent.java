package com.dynamored.coinflip.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.dynamored.coinflip.managers.GameManager;
import com.dynamored.coinflip.models.CoinflipGameRefund;

public class PlayerJoinEvent implements Listener {

	@EventHandler
	public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
		Player player = event.getPlayer();

		List<CoinflipGameRefund> gamesRefunds = GameManager.getPlayerRefunds(player.getUniqueId());
		for (CoinflipGameRefund gameRefund : gamesRefunds) gameRefund.refund();
	}
}
