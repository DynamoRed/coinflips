package com.dynamored.coinflip.models;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import com.dynamored.coinflip.Coinflip;

public class CoinflipGameRefund {

	private final UUID id = UUID.randomUUID();
	private final UUID gameId;
	private final OfflinePlayer player;
    private final double amount;
	private boolean refunded = false;

    public CoinflipGameRefund(OfflinePlayer player, double amount, UUID gameId) {
        this.player = player;
		this.amount = amount;
		this.gameId = gameId;
    }

    public CoinflipGameRefund(OfflinePlayer player, double amount, UUID gameId, boolean refunded) {
        this.player = player;
		this.amount = amount;
		this.gameId = gameId;
		this.refunded = refunded;
    }

	public UUID getId() {
		return id;
	}

    public OfflinePlayer getPlayer() {
        return player;
    }

    public UUID getGameId() {
        return gameId;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public double getAmount() {
        return amount;
    }

	public boolean refund() {
		if (this.isRefunded() || !this.getPlayer().isOnline()) return false;

		Coinflip.getEconomy().depositPlayer(this.getPlayer(), this.getAmount());
		this.getPlayer().getPlayer().sendMessage(Coinflip.getInstance().prefix + "§7[§a☄§7] You have been refunded §6" + this.getAmount() + " " + Coinflip.getEconomy().currencyNamePlural() + " §7for an interrupted coinflip game");

		this.refunded = true;
		return this.save();
	}

	public boolean save() {
		YamlConfiguration gamesRefundsConfig = Coinflip.getInstance().gamesRefundsConfig;
		gamesRefundsConfig.set("refunds." + this.id + ".player", this.getPlayer().getUniqueId().toString());
		gamesRefundsConfig.set("refunds." + this.id + ".game", this.getGameId().toString());
		gamesRefundsConfig.set("refunds." + this.id + ".refunded", this.isRefunded());
		gamesRefundsConfig.set("refunds." + this.id + ".amount", this.getAmount());

		try {
			gamesRefundsConfig.save(Coinflip.getInstance().gamesRefundsFile);
			Coinflip.getInstance().gamesRefunds.put(this.id, this);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
