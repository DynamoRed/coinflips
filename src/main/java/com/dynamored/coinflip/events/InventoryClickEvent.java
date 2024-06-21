package com.dynamored.coinflip.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.dynamored.coinflip.Coinflip;

public class InventoryClickEvent implements Listener {

	@EventHandler
	public void onClick(org.bukkit.event.inventory.InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		if (item != null) {
			PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

			if (container.has(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN) && container.get(Coinflip.getInstance().nbt, PersistentDataType.BOOLEAN))
				event.setCancelled(true);

			if (event.getView().getTitle().startsWith("§7[§d☄§7] §5Coinflips")) {
				Player player = (Player) event.getWhoClicked();

				if (container.has(Coinflip.getInstance().nbtCoinflipsMenuPage, PersistentDataType.INTEGER)) {
					event.setCancelled(true);
					player.closeInventory();
					player.performCommand("cfs " + container.get(Coinflip.getInstance().nbtCoinflipsMenuPage, PersistentDataType.INTEGER));
				}

				if (container.has(Coinflip.getInstance().nbtCoinflipsMenu, PersistentDataType.STRING) && container.has(Coinflip.getInstance().nbtCoinflipsMenuIsOwner, PersistentDataType.BOOLEAN)) {
					event.setCancelled(true);
					player.closeInventory();
					player.performCommand("cf " + (event.getClick().equals(ClickType.RIGHT) && container.get(Coinflip.getInstance().nbtCoinflipsMenuIsOwner, PersistentDataType.BOOLEAN) ? "cancel" : "join") + " " + UUID.fromString(container.get(Coinflip.getInstance().nbtCoinflipsMenu, PersistentDataType.STRING)));
				}
			}

		}
	}
}
