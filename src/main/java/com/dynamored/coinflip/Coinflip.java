package com.dynamored.coinflip;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.dynamored.coinflip.commands.GameCommand;
import com.dynamored.coinflip.commands.GamesCommand;
import com.dynamored.coinflip.commands.HelpCommand;
import com.dynamored.coinflip.events.InventoryClickEvent;
import com.dynamored.coinflip.events.PlayerJoinEvent;
import com.dynamored.coinflip.events.PlayerQuitEvent;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.CoinflipGameRefund;
import com.dynamored.coinflip.models.GameStatus;
import com.dynamored.coinflip.models.GameWinner;
import com.dynamored.coinflip.models.IllegalGameCancellation;
import com.dynamored.coinflip.utils.Config;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Coinflip extends JavaPlugin {

	public List<File> files = new ArrayList<>();
	public final String prefix = "§7[§6Coinflip™§7] ";
	public final String consolePrefix = "&6[&edCoinflip&6] ";
	public File gamesFile;
	public YamlConfiguration gamesConfig;
	public File gamesRefundsFile;
	public YamlConfiguration gamesRefundsConfig;

	private Config config = new Config();
    private static Economy economy = null;

	public HashMap<UUID, CoinflipGame> games = new HashMap<>();
	public HashMap<UUID, CoinflipGameRefund> gamesRefunds = new HashMap<>();
	public NamespacedKey nbt;
	public NamespacedKey nbtCoinflipsMenu;
	public NamespacedKey nbtCoinflipsMenuPage;
	public NamespacedKey nbtCoinflipsMenuIsOwner;

	private static Coinflip instance;
	public static Coinflip getInstance() {
        return instance;
    }

	public void colorStr(String string) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.consolePrefix + string));
    }

    @Override
    public void onEnable() {
		instance = this;
		colorStr("&aStarting up...");

		if (!_setupEconomy()) {
			colorStr("&cCannot find Vault, disabling...");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.nbt = new NamespacedKey(instance, "dcoinflip");
		this.nbtCoinflipsMenu = new NamespacedKey(instance, "dcoinflip-menu");
		this.nbtCoinflipsMenuPage = new NamespacedKey(instance, "dcoinflip-page");
		this.nbtCoinflipsMenuIsOwner = new NamespacedKey(instance, "dcoinflip-is-owner");

		_setupFiles();
		_setupEvents();
		_setupCommands();
		colorStr("&eFiles, commands and event setup successfully!");

		_setupLists();
		colorStr("&eConfiguration loaded successfully!");

		CoinflipGame.sessionsCount = 1;
    }

    @Override
    public void onDisable() {
		colorStr("&cThe plugin is no longer operational.");
		Bukkit.getScheduler().cancelTasks(this);
    }

    private boolean _setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

	private void _setupLists() {
		if (this.gamesConfig != null) {
			ConfigurationSection section = this.gamesConfig.getConfigurationSection("games");
			if (section != null) {
				for(String id : section.getKeys(false)) {
					String creator = section.getConfigurationSection(id).getString("creator");
					String opponent = section.getConfigurationSection(id).getString("opponent");
					double amount = section.getConfigurationSection(id).getDouble("amount");
					String state = section.getConfigurationSection(id).getString("state");
					String winner = section.getConfigurationSection(id).getString("winner");
					int sessionId = section.getConfigurationSection(id).getInt("sessionId");

					CoinflipGame game = new CoinflipGame(
						Bukkit.getOfflinePlayer(UUID.fromString(creator)),
						amount,
						GameStatus.valueOf(state),
						opponent.length() == 0 ? null : Bukkit.getOfflinePlayer(UUID.fromString(opponent)),
						GameWinner.valueOf(winner),
						sessionId
					);
					game.save();

					if (game.getStatus() == GameStatus.STARTED) {
						try {
							game.cancel();
						} catch (IllegalGameCancellation e) {
							colorStr("§l§c/!!!\\ CANNOT REFUND CORRECTLY for game " + game.getId());
						}
					}
				}
			}
		}
		if (this.gamesRefundsConfig != null) {
			ConfigurationSection section = this.gamesRefundsConfig.getConfigurationSection("refunds");
			if (section != null) {
				for(String id : section.getKeys(false)) {
					String player = section.getConfigurationSection(id).getString("player");
					String game = section.getConfigurationSection(id).getString("game");
					double amount = section.getConfigurationSection(id).getDouble("amount");
					boolean refunded = section.getConfigurationSection(id).getBoolean("refunded");

					CoinflipGameRefund gameRefund = new CoinflipGameRefund(
						Bukkit.getOfflinePlayer(UUID.fromString(player)),
						amount,
						UUID.fromString(game),
						refunded
					);
					gameRefund.save();
				}
			}
		}

		ConfigurationSection section = getConfig().getConfigurationSection("coinflip");
		if (section != null) {
			int maxSimultaneousGamesPerPlayer = section.getInt("maxSimultaneousGamesPerPlayer");
			int maxAmountPerGame = section.getInt("amountPerGame.max");
			int minAmountPerGame = section.getInt("amountPerGame.min");

			this.config.maxSimultaneousGamesPerPlayer = maxSimultaneousGamesPerPlayer;
			this.config.maxAmountPerGame = maxAmountPerGame;
			this.config.minAmountPerGame = minAmountPerGame;
		}
	}

	private void _setupFiles() {
		saveDefaultConfig();

		this.gamesFile = new File(this.getDataFolder(), "games.yml");
		files.add(this.gamesFile);
		this.gamesConfig = YamlConfiguration.loadConfiguration(this.gamesFile);

		this.gamesRefundsFile = new File(this.getDataFolder(), "DO-NOT-TOUCH-refunds.yml");
		files.add(this.gamesRefundsFile);
		this.gamesRefundsConfig = YamlConfiguration.loadConfiguration(this.gamesRefundsFile);
	}

	private void _setupEvents() {
		getServer().getPluginManager().registerEvents(new PlayerQuitEvent(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinEvent(), this);
		getServer().getPluginManager().registerEvents(new InventoryClickEvent(), this);
	}

	private void _setupCommands() {
		getCommand("coinflips").setExecutor(new GamesCommand());
		getCommand("coinflip").setExecutor(new GameCommand());
		getCommand("coinfliphelp").setExecutor(new HelpCommand());
	}

    public static Economy getEconomy() {
        return economy;
    }

    public Config getConfiguration() {
        return this.config;
    }
}
