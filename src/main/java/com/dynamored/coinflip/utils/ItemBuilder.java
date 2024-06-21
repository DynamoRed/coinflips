package com.dynamored.coinflip.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * ItemBuilder class allows you to easily create ItemStacks.
 *
 * @author lokka30
 * @see ItemStack
 * @since 1.0.3
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ItemBuilder {

    /**
     * The current ItemStack in the ItemBuilder, as
     * set in the constructor and modified thereon.
     *
     * @since 3.1.0
     */
    private final ItemStack itemStack;
    private ItemMeta itemMeta;

    /**
     * Starts off the ItemBuilder using an existing ItemStack.
     *
     * @param itemStack to begin the ItemBuilder with.
     * @since 3.1.0
     */
    public ItemBuilder(final ItemStack itemStack) {
        this.itemStack = itemStack;
		this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Starts off the ItemBuilder with a new ItemStack of specified material.
     *
     * @param type (Material type) of the new ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder(final Material type) {
        this.itemStack = new ItemStack(type);
		this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Gets the current ItemStack (the built item).
     *
     * @return the current ItemStack.
     * @since 3.1.0
     */
    public ItemStack getItemStack() {
		this.itemStack.setItemMeta(this.itemMeta);
        return itemStack;
    }

    /**
     * Sets amount of item stack.
     *
     * @param amount to set on the item.
     * @return the amount
     * @since 3.1.0
     */
    public ItemBuilder setAmount(final int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets material type to ItemStack.
     *
     * @param type (Material) to set on the item.
     * @return the type
     * @since 3.1.0
     */
    public ItemBuilder setType(final Material type) {
        itemStack.setType(type);
        return this;
    }

    /**
     * Sets material type to ItemStack.
     * This method is an alias of the method {@link #setType(Material)}.
     *
     * @param material to set on the item.
     * @return Material to set.
     * @since 3.1.0
     */
    public ItemBuilder setMaterial(final Material material) {
        return setType(material);
    }

    /**
     * Replaces the existing ItemMeta of the ItemStack.
     *
     * @param itemMeta to set on the item.
     * @return The ItemMeta of this ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder setItemMeta(final ItemMeta itemMeta) {
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Sets display name to ItemStack.
     *
     * @param displayName to set on the item.
     * @return Display name of ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder setDisplayName(final String displayName) {
        itemMeta.setDisplayName(displayName);

        return this;
    }

    /**
     * Sets lore to ItemStack.
     *
     * @param lines of the lore to set on the item.
     * @return Lore of ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder setLore(final List<String> lines) {
        itemMeta.setLore(lines);
        return this;
    }

    /**
     * Sets lore to ItemStack.
     *
     * @param line of the lore to set on the item.
     * @return Lore of ItemStack to be set.
     * @since 3.1.0
     */
    public ItemBuilder setLore(final String line) {
        return setLore(Collections.singletonList(line));
    }

    /**
     * Adds lore to ItemStack.
     *
     * @param lines of the lore to add on the item.
     * @return Lore of ItemStack to be added.
     * @since 3.1.0
     */
    public ItemBuilder addLore(final List<String> lines) {
        ArrayList<String> newLore = new ArrayList<>();

        if (itemMeta.getLore() != null) newLore.addAll(itemMeta.getLore());
        newLore.addAll(lines);

        itemMeta.setLore(newLore);
        return this;
    }

    /**
     * Adds lore to ItemStack.
     *
     * @param line of the lore to add on the item.
     * @return Lore of item.
     * @since 3.1.0
     */
    public ItemBuilder addLore(final String line) {
        return addLore(Collections.singletonList(line));
    }

    /**
     * Sets damage to item in ItemStack.
     *
     * @param damage to set on the item.
     * @return Damage of item in ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder setDamage(final int damage) {
        if(!(itemMeta instanceof Damageable)) return this;

        ((Damageable) itemMeta).setDamage(damage);
        return this;
    }

    /**
     * Adds enchantments to item in ItemStack.
     *
     * @param enchantmentLevelsMap   to add on the item, a map of Enchantments with each of their corresponding levels.
     * @param ignoreLevelRestriction if set to 'true', levels may be higher than their maximum obtainable limits (e.g. Sharpness V).
     * @return The enchantments on item in ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder addEnchantments(final HashMap<Enchantment, Integer> enchantmentLevelsMap, final boolean ignoreLevelRestriction) {

        enchantmentLevelsMap.forEach(((enchantment, level) -> itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction)));

        return this;
    }

    /**
     * Adds enchantment to item in ItemStack.
     *
     * @param enchantment            to add on the item.
     * @param level                  of the specified enchantment.
     * @param ignoreLevelRestriction if set to 'true', the level may be higher than the enchantment's maximum obtainable limit (e.g. Sharpness V).
     * @return The enchantment on item in ItemStack.
     * @since 3.1.0
     */
    public ItemBuilder addEnchantment(final Enchantment enchantment, final int level, final boolean ignoreLevelRestriction) {

        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);

        return this;
    }

    /**
     * Adds item flags on item in ItemStack.
     *
     * @param itemFlags to add on the item.
     * @return Item flags on item.
     * @since 3.1.0
     */
    public ItemBuilder addItemFlags(final ItemFlag[] itemFlags) {

        itemMeta.addItemFlags(itemFlags);

        return this;
    }

    /**
     * Adds item flags on item in ItemStack.
     *
     * @param itemFlags to add on the item.
     * @return Item flags on item.
     * @since 3.1.0
     */
    public ItemBuilder addItemFlags(final List<ItemFlag> itemFlags) {
        return addItemFlags(itemFlags.toArray(new ItemFlag[0]));
    }

    /**
     * Adds item flag on item in ItemStack.
     *
     * @param itemFlag to add on the item.
     * @return Item flag on item.
     * @since 3.1.0
     */
    public ItemBuilder addItemFlag(final ItemFlag itemFlag) {
        return addItemFlags(Collections.singletonList(itemFlag));
    }

    public <T, Z> ItemBuilder addItemNbt(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {

        itemMeta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    /**
     * Sets unbreakable state of item in ItemStack.
     *
     * @param state whether the item should be unbreakable or not.
     * @return the unbreakable state of item.
     * @since 3.1.0
     */
    public ItemBuilder setUnbreakable(final boolean state) {


        itemMeta.setUnbreakable(state);
        return this;
    }

    /**
     * Adds a 'glowing' effect to the item by adding a useless enchantment.
     * This will hide ALL enchantments on the item, even enchantments that are not applied by this method.
     *
     * @return the glowing
     * @since 3.1.0
     */
    public ItemBuilder setGlowing() {
        Enchantment enchantment;

        if(itemStack.getType() == Material.BOW) {
            enchantment = Enchantment.LURE;
        } else {
            enchantment = Enchantment.ARROW_INFINITE;
        }

        addEnchantment(enchantment, 0, true);
        addItemFlag(ItemFlag.HIDE_ENCHANTS);

        return this;
    }

    /**
     * Sets skull owner.
     *
     * @param offlinePlayer Offline player to set the skull texture of.
     * @return the skull owner
     * @since 3.1.0
     */
    public ItemBuilder setSkullOwner(final OfflinePlayer offlinePlayer) {
        if(!(itemMeta instanceof SkullMeta)) return this;

        ((SkullMeta) itemMeta).setOwningPlayer(offlinePlayer);
        return this;
    }

    /**
     * Sets skull owner.
     *
     * @param username to set the skull texture of.
     * @return the skull owner
     * @since 3.1.0
     * @deprecated {@link #setSkullOwner(OfflinePlayer)} should be used wherever possible, this only exists for legacy compatibility.
     */
    @Deprecated
    public ItemBuilder setSkullOwner(final String username) {
        if(!(itemMeta instanceof SkullMeta)) return this;

        ((SkullMeta) itemMeta).setOwner(username);
        return this;
    }

}
