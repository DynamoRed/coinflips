package com.dynamored.coinflip.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.managers.AnimationManager;
import com.dynamored.coinflip.utils.Fireworks;
import com.dynamored.coinflip.utils.ItemBuilder;

public class CoinflipGame implements Comparable<CoinflipGame> {

	public static int sessionsCount = 1;

	private final UUID id;
	private int sessionId = -1;
	private final OfflinePlayer creator;
    private OfflinePlayer opponent;
    private GameWinner winner = GameWinner.NONE;
    private final double amount;
    private GameStatus status = GameStatus.WAITING;
	private Inventory creatorMenu;
	private Inventory opponentMenu;

    public CoinflipGame(OfflinePlayer creator, double amount) {
		this.id = UUID.randomUUID();
        this.creator = creator;
        this.amount = amount;
    }

    public CoinflipGame(UUID id, OfflinePlayer creator, double amount, GameStatus status, OfflinePlayer opponent, GameWinner winner, int sessionId) {
		this.id = id;
        this.creator = creator;
        this.amount = amount;
		this.status = status;
		this.opponent = opponent;
		this.winner = winner;
		this.sessionId = sessionId;
    }

	@Override
	public int compareTo(CoinflipGame game) {
		return this.getSessionId() - game.getSessionId();
	}

	public UUID getId() {
		return id;
	}

    public OfflinePlayer getCreator() {
        return creator;
    }

    public OfflinePlayer getOpponent() {
        return opponent;
    }

    public GameWinner getWinner() {
        return winner;
    }

    public OfflinePlayer getWinnerPlayer() {
        return winner == GameWinner.CREATOR ? this.getCreator() : this.getOpponent();
    }

    public double getAmount() {
        return amount;
    }

    public Inventory getCreatorMenu() {
        return creatorMenu;
    }

    public Inventory getOpponentMenu() {
        return opponentMenu;
    }

    public GameStatus getStatus() {
        return status;
    }

    public int getSessionId() {
        return sessionId;
    }

    private void setOpponent(OfflinePlayer opponent) {
        this.opponent = opponent;
    }

    private void setWinner(GameWinner winner) {
        this.winner = winner;
    }

    private void setStatus(GameStatus status) {
        this.status = status;
    }

    private void setCreatorMenu(Inventory menu) {
        this.creatorMenu = menu;
    }

    private void setOpponentMenu(Inventory menu) {
        this.opponentMenu = menu;
    }

    private void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public boolean start(OfflinePlayer opponent) throws IllegalGameStart {
		if (this.getStatus() != GameStatus.WAITING || opponent.getUniqueId().equals(this.getCreator().getUniqueId())) throw new IllegalGameStart("Game is not waiting to be started");

		if (!this.getCreator().isOnline() || !opponent.isOnline()) throw new IllegalGameStart("At least one player is offline");

        this.setOpponent(opponent);
		this.setStatus(GameStatus.STARTED);

		Coinflip.getEconomy().withdrawPlayer(this.getCreator(), this.getAmount());
		Coinflip.getEconomy().withdrawPlayer(this.getOpponent(), this.getAmount());

		this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§a☄§7] ", "_Start_Message_");
		this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§a☄§7] ", "_Start_Message_Withdraw_");

		Random random = new Random();
        int winnerNumber = random.nextInt(2);

		this.setWinner(winnerNumber == 0 ? GameWinner.CREATOR : GameWinner.OPPONENT);

		this.setCreatorMenu(Bukkit.createInventory(null, 27, "§7[§d☄§7] §8#" + this.getSessionId() + " §7- §5" + Coinflip.getInstance().translate(this.getCreator().getPlayer().getLocale(), "_Bet_", null) + ": §6" + this.getAmount() + Coinflip.getEconomy().currencyNamePlural()));
		this.setOpponentMenu(Bukkit.createInventory(null, 27, "§7[§d☄§7] §8#" + this.getSessionId() + " §7- §5" + Coinflip.getInstance().translate(this.getOpponent().getPlayer().getLocale(), "_Bet_", null) + ": §6" + this.getAmount() + Coinflip.getEconomy().currencyNamePlural()));

		for (int i = 0; i < 27; i++) {
			ItemBuilder builder = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§0▉").addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
			this.setItemForBothMenus(i, builder.getItemStack());
		}

		this.getCreator().getPlayer().openInventory(this.getCreatorMenu());
		this.getOpponent().getPlayer().openInventory(this.getOpponentMenu());

		if (this.save()) new AnimationManager().animateMenu(this);
		else Coinflip.getInstance().colorStr("Cannot save game " + this.getId());

		return true;
    }

	public void setItemForBothMenus(int pos, ItemStack item) {
		this.getCreatorMenu().setItem(pos, item);
		this.getOpponentMenu().setItem(pos, item);
	}

    public boolean end() throws IllegalGameEnd, IllegalGameCancellation {
		if (this.getStatus() != GameStatus.STARTED || this.getWinner() == GameWinner.NONE) throw new IllegalGameEnd("Game is not started or winner is not defined");

		OfflinePlayer winnerPlayer = this.getWinner() == GameWinner.CREATOR ? this.getCreator() : this.getOpponent();
		OfflinePlayer looserPlayer = this.getWinner() == GameWinner.CREATOR ? this.getOpponent() : this.getCreator();

		if (!winnerPlayer.isOnline() || !looserPlayer.isOnline()) {
			this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§e☄§7] ", "_Offline_Opponent_");
			return this.cancel();
		}

		Coinflip.getEconomy().depositPlayer(winnerPlayer, this.getAmount()*2);
		Fireworks.summon(winnerPlayer.getPlayer().getLocation());

		for (Player online : Bukkit.getOnlinePlayers()) {
			online.sendMessage(Coinflip.getInstance().prefix + "§7[§d☄§7] " + Coinflip.getInstance().translate(online.getLocale(), "_Won_Game_Broadcast_", new HashMap<String, String>() {{
				put("winnerName", winnerPlayer.getPlayer().getDisplayName());
				put("looserName", looserPlayer.getPlayer().getDisplayName());
				put("amount", String.valueOf(getAmount() * 2));
				put("currency", Coinflip.getEconomy().currencyNamePlural());
			}}));
		}

		this.setStatus(GameStatus.ENDED);

		return this.save();
	}

    public boolean cancel() throws IllegalGameCancellation {
		if (this.getStatus() != GameStatus.STARTED && this.getStatus() != GameStatus.WAITING) throw new IllegalGameCancellation("Game is not started or waiting");

		if (this.getStatus() == GameStatus.STARTED) {
			CoinflipGameRefund creatorRefund = new CoinflipGameRefund(this.getCreator(), this.getAmount(), this.getId());
			CoinflipGameRefund opponentRefund = new CoinflipGameRefund(this.getOpponent(), this.getAmount(), this.getId());

			if (!this.getCreator().isOnline() || !creatorRefund.refund()) creatorRefund.save();
			if (!this.getOpponent().isOnline() || !opponentRefund.refund()) opponentRefund.save();

			if (this.getCreator().isOnline()) this.getCreator().getPlayer().closeInventory();
			if (this.getOpponent().isOnline()) this.getOpponent().getPlayer().closeInventory();
		}

		this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§e☄§7] ", "_Game_Cancelled_");
		this.setStatus(GameStatus.CANCELED);

		return this.save();
	}

	public void sendPlayersMessage(String ...message) {
		List<OfflinePlayer> receivers = new ArrayList<>();

		if (this.getCreator().isOnline()) receivers.add(this.getCreator());
		if (this.getOpponent() != null && this.getOpponent().isOnline()) receivers.add(this.getOpponent());

		for (OfflinePlayer player : receivers) {
			List<String> finals = new ArrayList<>();

			for (String messageComponent : message) {
				if (messageComponent.startsWith("_") && messageComponent.endsWith("_")) {
					finals.add(Coinflip.getInstance().translate(player.getPlayer().getLocale(), messageComponent, new HashMap<String, String>() {{
						put("playerName", player.getPlayer().getDisplayName());
						put("gameId", String.valueOf(getSessionId()));
						put("amount", String.valueOf(getAmount()));
						put("currency", Coinflip.getEconomy().currencyNamePlural());
					}}));
				} else finals.add(messageComponent);
			}

			player.getPlayer().sendMessage(String.join("", finals));
		}
	}

	public boolean save() {
		if (this.getSessionId() == -1) {
			this.setSessionId(CoinflipGame.sessionsCount);
			CoinflipGame.sessionsCount = CoinflipGame.sessionsCount + 1;
		}

		YamlConfiguration gamesConfig = Coinflip.getInstance().gamesConfig;
		gamesConfig.set("games." + this.id + ".creator", this.getCreator().getUniqueId().toString());
		gamesConfig.set("games." + this.id + ".opponent", this.getOpponent() != null ? this.getOpponent().getUniqueId().toString() : "");
		gamesConfig.set("games." + this.id + ".amount", this.getAmount());
		gamesConfig.set("games." + this.id + ".state", this.getStatus().name());
		gamesConfig.set("games." + this.id + ".winner", this.getWinner().name());
		gamesConfig.set("games." + this.id + ".sessionId", this.getSessionId());

		try {
			gamesConfig.save(Coinflip.getInstance().gamesFile);
			Coinflip.getInstance().games.put(this.id, this);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
