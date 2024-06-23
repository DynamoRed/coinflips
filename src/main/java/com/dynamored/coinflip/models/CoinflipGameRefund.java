package com.dynamored.coinflip.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import com.dynamored.coinflip.Coinflip;

public class CoinflipGameRefund {

	private final UUID id;
	private final UUID gameId;
	private final OfflinePlayer player;
    private final double amount;
	private boolean refunded = false;

    public CoinflipGameRefund(OfflinePlayer player, double amount, UUID gameId) {
		this.id = UUID.randomUUID();
        this.player = player;
		this.amount = amount;
		this.gameId = gameId;
    }

    public CoinflipGameRefund(UUID id, OfflinePlayer player, double amount, UUID gameId, boolean refunded) {
		this.id = id;
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
		this.getPlayer().getPlayer().sendMessage(Coinflip.getInstance().prefix + "§7[§a☄§7] " + Coinflip.getInstance().translate(this.getPlayer().getPlayer().getLocale(), "_Refunded_For_Game_", new HashMap<String, String>() {{
			put("amount", String.valueOf(getAmount()));
			put("currency", Coinflip.getEconomy().currencyNamePlural());
		}}));

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
