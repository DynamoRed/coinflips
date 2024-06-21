package com.dynamored.coinflip.managers;

import java.util.List;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.dynamored.coinflip.Coinflip;
import com.dynamored.coinflip.models.CoinflipGame;
import com.dynamored.coinflip.models.GameStatus;
import com.dynamored.coinflip.models.GameWinner;
import com.dynamored.coinflip.models.IllegalGameCancellation;
import com.dynamored.coinflip.models.IllegalGameEnd;
import com.dynamored.coinflip.utils.ItemBuilder;

public class AnimationManager {

	private int currentIndex = 0;
	private float step = 1;
	private long delay = 2;
	private int slotIndex = 8;
	private int ticks = 0;
	private int endCounter = 0;

	private List<ItemStack> items;

	public void animateMenu(CoinflipGame game) {
		if (!game.getCreator().isOnline() || !game.getOpponent().isOnline()) return;

		Player creator = game.getCreator().getPlayer();
		Player opponent = game.getOpponent().getPlayer();

		items = GameManager.generateHeads(creator, game.getOpponent().getPlayer(), game.getWinner());

        new BukkitRunnable() {
            @Override
            public void run() {
				if (game.getStatus() == GameStatus.CANCELED) this.cancel();
				else if (currentIndex >= items.size() - 9) {
                    this.cancel();

					game.getMenu().clear();
					game.getMenu().setItem(4, items.get(game.getWinner() == GameWinner.CREATOR ? 0 : 1));

					new BukkitRunnable() {
						@Override
						public void run() {
							if (endCounter == 7) {
								try {
									this.cancel();
									game.end();
								} catch (IllegalGameEnd | IllegalGameCancellation e) {
									game.sendPlayersMessage(Coinflip.getInstance().prefix + "§7[§d☄§7] An error occurred during your game §f#" + game.getSessionId());
								}
							}

							for (int i = 0; i < 9; i++) {
								ItemBuilder builder = new ItemBuilder(endCounter % 2 == 0 ? Material.GRAY_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE).setDisplayName("§6" + (game.getWinner() == GameWinner.CREATOR ? creator : opponent).getDisplayName() + " §6§lVICTORY !").addLore("§c" + (game.getAmount()*2) + Coinflip.getEconomy().currencyNameSingular()).addItemNbt(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN, true);
								if (i != 4) game.getMenu().setItem(i, builder.getItemStack());
							}

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

				game.getMenu().clear();

				for (int i = 0; i < 9; i++) {
                    int itemIndex = (currentIndex + i) % items.size();
                    game.getMenu().setItem(i, items.get(itemIndex));
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
