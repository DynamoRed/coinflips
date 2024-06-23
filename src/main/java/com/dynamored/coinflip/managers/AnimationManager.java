package com.dynamored.coinflip.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.GameStatus;
import com.dynamored.coinflip.models.GameWinner;
import com.dynamored.coinflip.models.IllegalGameCancellation;
import com.dynamored.coinflip.models.IllegalGameEnd;
import com.dynamored.coinflip.utils.Head;
import com.dynamored.coinflip.utils.ItemBuilder;
import com.dynamored.coinflip.utils.Maths;

public class AnimationManager {

	private int currentIndex = 0;
	private float step = 1;
	private long delay = 2;
	private int slotIndex = 8;
	private int ticks = 0;
	private int endCounter = 0;

	private final List<Material> glasses = Arrays.asList(Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE);
	private int resultsMaterialsCounter = 0;
	private ItemStack winnerHead;


	private List<ItemStack> items = new ArrayList<>();

	public void animateMenu(CoinflipGame game) {
		if (!game.getCreator().isOnline() || !game.getOpponent().isOnline()) return;

		Player creator = game.getCreator().getPlayer();
		Player opponent = game.getOpponent().getPlayer();

		for (int i = 0; i < 9; i++) {
			ItemBuilder builder = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).setDisplayName("§0▉").addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
			items.add(builder.getItemStack());
		}

		for (int i = 0; i < 27; i++) {
			ItemBuilder builder = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("§0▉").addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
			if (i == 4 || i == 22) game.setItemForBothMenus(i, new ItemBuilder(Material.GOLD_INGOT).setDisplayName("§6§l" + (i == 4 ? "⬇" : "⬆")).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).getItemStack());
			else game.setItemForBothMenus(i, builder.getItemStack());

			if (i == 8) i = 17;
		}

		items.addAll(GameManager.generateHeads(creator, game.getOpponent().getPlayer(), game.getWinner()));

		List<List<Material>> resultsMaterials = new ArrayList<>();

		for (int i = 0; i < 20; i++) {
			List<Material> resultsCombinaison = new ArrayList<>();
			Material lastMaterial = Material.BLACK_STAINED_GLASS_PANE;

			for (int y = 0; y < 9; y++) {
				Material pane = lastMaterial;
				while (pane.toString().equalsIgnoreCase(lastMaterial.toString())) pane = glasses.get(Maths.random(0, glasses.size(), true));
				lastMaterial = pane;
				resultsCombinaison.add(pane);
			}

			resultsMaterials.add(resultsCombinaison);
		}

		OfflinePlayer winnerPlayer = game.getWinner() == GameWinner.CREATOR ? creator : opponent;
		OfflinePlayer looserPlayer = game.getWinner() == GameWinner.CREATOR ? opponent : creator;

		Inventory winnerMenu = game.getWinner() == GameWinner.CREATOR ? game.getCreatorMenu() : game.getOpponentMenu();
		Inventory looserMenu = game.getWinner() == GameWinner.CREATOR ? game.getOpponentMenu() : game.getCreatorMenu();

        new BukkitRunnable() {
            @Override
            public void run() {
				if (game.getStatus() == GameStatus.CANCELED) this.cancel();
				else if (currentIndex >= items.size() - 9) {
                    this.cancel();
					new BukkitRunnable() {
						@Override
						public void run() {
							if (endCounter == 0) {
								winnerHead = new ItemBuilder(new Head(null, "§6§l" + Coinflip.getInstance().translate(winnerPlayer.getPlayer().getLocale(), "_Winner_", null) + " §f" + winnerPlayer.getPlayer().getDisplayName(), "", winnerPlayer.getPlayer()).getHead()).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).setSkullOwner(winnerPlayer.getPlayer()).getItemStack();
								winnerMenu.setItem(13, winnerHead);
								winnerHead = new ItemBuilder(new Head(null, "§6§l" + Coinflip.getInstance().translate(looserPlayer.getPlayer().getLocale(), "_Winner_", null) + " §f" + winnerPlayer.getPlayer().getDisplayName(), "", winnerPlayer.getPlayer()).getHead()).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true).setSkullOwner(winnerPlayer.getPlayer()).getItemStack();
								looserMenu.setItem(13, winnerHead);

								endCounter++;
								return;
							}

							if (endCounter == 7) {
								try {
									game.end();
								} catch (IllegalGameEnd | IllegalGameCancellation e) {
									game.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§d☄§7] ", "_Error_During_Game_");
								}
							}

							if (endCounter == 15) {
								this.cancel();

								for (int i = 0; i < 9; i++) {
									ItemBuilder builder = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§a" + Coinflip.getInstance().translate(winnerPlayer.getPlayer().getLocale(), "_You_Winner_", null)).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
									if (i != 4) winnerMenu.setItem(9+i, builder.getItemStack());
								}

								return;
							}

							List<Material> panes = resultsMaterials.get(resultsMaterialsCounter);

							for (int i = 0; i < 9; i++) {
								ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§c" + Coinflip.getInstance().translate(looserPlayer.getPlayer().getLocale(), "_You_Looser_", null)).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
								if (i != 4) looserMenu.setItem(9+i, builder.getItemStack());
							}

							for (int i = 0; i < 9; i++) {
								ItemBuilder builder = new ItemBuilder(panes.get(i)).setDisplayName("§a" + Coinflip.getInstance().translate(winnerPlayer.getPlayer().getLocale(), "_You_Winner_", null)).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
								if (i != 4) winnerMenu.setItem(9+i, builder.getItemStack());
							}

							resultsMaterialsCounter += 1;
							if (resultsMaterialsCounter >= resultsMaterials.size()) resultsMaterialsCounter = 0;

							switch (endCounter) {
								case 0:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.C));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.C));
									break;
								case 1:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.E));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.E));
									break;
								case 2:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.G));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.G));
									break;
								case 3:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(1, Tone.C));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(1, Tone.C));
									break;
								case 4:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.G));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.G));
									break;
								case 5:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.E));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.E));
									break;
								case 6:
									opponent.playNote(opponent.getLocation(), Instrument.BELL, Note.flat(0, Tone.C));
									creator.playNote(creator.getLocation(), Instrument.BELL, Note.flat(0, Tone.C));
									break;
							}

							endCounter++;
						}
					}.runTaskTimer(Coinflip.getInstance(), 0, 5);

                    return;
                }

				ticks++;

				if (ticks%delay != 0) return;

				for (int i = 0; i < 9; i++) {
                    int itemIndex = (currentIndex + i) % items.size();
                    game.setItemForBothMenus(9+i, items.get(itemIndex));
                }

				creator.playNote(creator.getLocation(), Instrument.XYLOPHONE, Note.flat(1, Tone.C));
				opponent.playNote(opponent.getLocation(), Instrument.XYLOPHONE, Note.flat(1, Tone.C));

				slotIndex = (slotIndex - 1 + 9) % 9;
				currentIndex++;
				step += .2;
				delay += step;
            }
        }.runTaskTimer(Coinflip.getInstance(), 0, 1);
	}
}
