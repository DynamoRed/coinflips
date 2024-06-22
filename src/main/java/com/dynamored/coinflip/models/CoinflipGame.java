package com.dynamored.coinflip.models;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.managers.AnimationManager;
import com.dynamored.coinflip.utils.Fireworks;
import com.dynamored.coinflip.utils.ItemBuilder;

public class CoinflipGame implements Comparable<CoinflipGame> {

	public static int sessionsCount = 1;

	private final UUID id = UUID.randomUUID();
	private int sessionId = -1;
	private final OfflinePlayer creator;
    private OfflinePlayer opponent;
    private GameWinner winner = GameWinner.NONE;
    private final double amount;
    private GameStatus status = GameStatus.WAITING;
	private Inventory menu;

    public CoinflipGame(OfflinePlayer creator, double amount) {
        this.creator = creator;
        this.amount = amount;
    }

    public CoinflipGame(OfflinePlayer creator, double amount, GameStatus status) {
        this.creator = creator;
        this.amount = amount;
		this.status = status;
    }

    public CoinflipGame(OfflinePlayer creator, double amount, GameStatus status, OfflinePlayer opponent) {
        this.creator = creator;
        this.amount = amount;
		this.status = status;
		this.opponent = opponent;
    }

    public CoinflipGame(OfflinePlayer creator, double amount, GameStatus status, OfflinePlayer opponent, GameWinner winner) {
        this.creator = creator;
        this.amount = amount;
		this.status = status;
		this.opponent = opponent;
		this.winner = winner;
    }

    public CoinflipGame(OfflinePlayer creator, double amount, GameStatus status, OfflinePlayer opponent, GameWinner winner, int sessionId) {
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

    public Inventory getMenu() {
        return menu;
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

    private void setMenu(Inventory menu) {
        this.menu = menu;
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

		String startMessage = Coinflip.getInstance().prefix + "§7[§a☄§7] §f" + this.getCreator().getPlayer().getDisplayName() + "§7's game §f#" + this.getSessionId() + " §7has begun";
		String startWithdrawMessage = Coinflip.getInstance().prefix + "§7[§b☄§7] §6" + this.getAmount() + Coinflip.getEconomy().currencyNamePlural() + " §7have been withdraw from your wallet and placed awaiting the outcome of the game";

		this.sendPlayersMessage(startMessage, startWithdrawMessage);

		Random random = new Random();
        int winnerNumber = random.nextInt(2);

		this.setWinner(winnerNumber == 0 ? GameWinner.CREATOR : GameWinner.OPPONENT);

		this.setMenu(Bukkit.createInventory(null, 9, "§7[§4☄§7] §4Bet: §l§c" + this.getAmount() + Coinflip.getEconomy().currencyNamePlural()));

		for (int i = 0; i < 9; i++) {
			ItemBuilder builder = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName("§0▉").addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
			this.getMenu().setItem(i, builder.getItemStack());
		}

		this.getCreator().getPlayer().openInventory(this.getMenu());
		this.getOpponent().getPlayer().openInventory(this.getMenu());

		if (this.save()) new AnimationManager().animateMenu(this);
		else Coinflip.getInstance().colorStr("Cannot save game " + this.getId());

		return true;
    }

    public boolean end() throws IllegalGameEnd, IllegalGameCancellation {
		if (this.getStatus() != GameStatus.STARTED || this.getWinner() == GameWinner.NONE) throw new IllegalGameEnd("Game is not started or winner is not defined");

		OfflinePlayer winnerPlayer = this.getWinner() == GameWinner.CREATOR ? this.getCreator() : this.getOpponent();
		OfflinePlayer looserPlayer = this.getWinner() == GameWinner.CREATOR ? this.getOpponent() : this.getCreator();

		if (!winnerPlayer.isOnline() || !looserPlayer.isOnline()) {
			this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§e☄§7] Your opponent is offline");
			return this.cancel();
		}

		Coinflip.getEconomy().depositPlayer(winnerPlayer, this.getAmount()*2);
		Fireworks.summon(winnerPlayer.getPlayer().getLocation());

		winnerPlayer.getPlayer().closeInventory();
		looserPlayer.getPlayer().closeInventory();

		Bukkit.broadcastMessage(Coinflip.getInstance().prefix + "§7[§d☄§7] §6" + winnerPlayer.getPlayer().getDisplayName() + " §7won a game against §f" + looserPlayer.getPlayer().getDisplayName() + " §7and won §6" + (this.getAmount() * 2) + " " + Coinflip.getEconomy().currencyNamePlural() + " §7!");
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

		this.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§e☄§7] Your game §f#" + this.getSessionId() + " §7has been canceled");
		this.setStatus(GameStatus.CANCELED);

		return this.save();
	}

	public void sendPlayersMessage(String ...message) {
		if (this.getCreator().isOnline()) this.getCreator().getPlayer().sendMessage(message);
		if (this.getOpponent() != null && this.getOpponent().isOnline()) this.getOpponent().getPlayer().sendMessage(message);
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
