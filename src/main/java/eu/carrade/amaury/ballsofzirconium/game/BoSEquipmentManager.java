/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.ballsofzirconium.game;

import eu.carrade.amaury.ballsofzirconium.GameConfig;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.components.rawtext.RawTextPart;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class BoSEquipmentManager extends QuartzComponent implements Listener
{
    private Map<UUID, Set<ToolsType>> playersUnlockedTools = new HashMap<>();


    /**
     * Equips the given player with tools, following the configuration and the
     * unlocks (if any).
     *
     * @param player The player to equip.
     */
    public void equipPlayer(final Player player)
    {
        if (GameConfig.EQUIPMENT.SURVIVAL_MODE.get())
        {
            final Set<ToolsType> unlockedTools = getUnlockedTools(player.getUniqueId());
            final Boolean configTools = GameConfig.EQUIPMENT.TOOLS.get();

            equipPlayer(player,
                    GameConfig.EQUIPMENT.SWORD.get() && unlockedTools.contains(ToolsType.SWORD),
                    GameConfig.EQUIPMENT.BOW.get() && unlockedTools.contains(ToolsType.BOW),
                    configTools && unlockedTools.contains(ToolsType.PICKAXE),
                    configTools && unlockedTools.contains(ToolsType.AXE),
                    configTools && unlockedTools.contains(ToolsType.SHOVEL),
                    unlockedTools.contains(ToolsType.ARMOR_HELMET),
                    unlockedTools.contains(ToolsType.ARMOR_CHESTPLATE),
                    unlockedTools.contains(ToolsType.ARMOR_LEGGINGS),
                    unlockedTools.contains(ToolsType.ARMOR_BOOTS)
            );
        }
        else
        {
            final Boolean tools = GameConfig.EQUIPMENT.TOOLS.get();

            equipPlayer(player,
                    GameConfig.EQUIPMENT.SWORD.get(),
                    GameConfig.EQUIPMENT.BOW.get(),
                    tools, tools, tools,
                    true, true, true, true
            );
        }
    }

    /**
     * Equips the given player with asked tools.
     *
     * Food and blocks are always given (excepted if disabled in the
     * configuration, of course).
     *
     * @param player         The player to equip.
     * @param giveSword      {@code true} to give a sword.
     * @param giveBow        {@code true} to give a bow.
     * @param givePickaxe    {@code true} to give a pickaxe.
     * @param giveAxe        {@code true} to give an axe.
     * @param giveShovel     {@code true} to give a shovel.
     * @param giveHelmet     {@code true} to give a helmet (type from the config; nothing given if set to NONE).
     * @param giveChestplate {@code true} to give a chestplate (type from the config; nothing given if set to NONE).
     * @param giveLeggings   {@code true} to give leggings (type from the config; nothing given if set to NONE).
     * @param giveBoots      {@code true} to give boots (type from the config; nothing given if set to NONE).
     */
    public void equipPlayer(final Player player,
                            final boolean giveSword, final boolean giveBow, final boolean givePickaxe, final boolean giveAxe, final boolean giveShovel,
                            final boolean giveHelmet, final boolean giveChestplate, final boolean giveLeggings, final boolean giveBoots)
    {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        // Weapons
        if (giveSword)
        {
            inv.addItem(new ItemStack(Material.IRON_SWORD, 1));
        }
        if (giveBow)
        {
            inv.setItem(9, new ItemStackBuilder(Material.ARROW)
                    .enchant(Enchantment.ARROW_INFINITE, 1)
                    .enchant(Enchantment.DURABILITY, 3)
                    .item()
            );
        }

        // Tools
        if (givePickaxe)
        {
            inv.addItem(new ItemStack(Material.IRON_PICKAXE, 1));
        }
        if (giveAxe)
        {
            inv.addItem(new ItemStack(Material.IRON_AXE, 1));
        }
        if (giveShovel)
        {
            inv.addItem(new ItemStack(Material.IRON_SHOVEL, 1));
        }

        // Food & blocks
        final boolean food = GameConfig.EQUIPMENT.FOOD.get();
        final boolean blocks = GameConfig.EQUIPMENT.BLOCKS.get();

        if (food || blocks)
        {
            ItemStack foodStack = new ItemStack(Material.COOKED_BEEF, 64);
            ItemStack dirtStack = new ItemStack(Material.DIRT, 64);

            if (food) inv.setItem(8, foodStack);

            if (blocks)
            {
                inv.setItem(7, dirtStack);

                if (!food) inv.setItem(8, dirtStack);
                else inv.setItem(6, dirtStack);
            }
        }

        // Armor
        switch (GameConfig.EQUIPMENT.ARMOR.get())
        {
            case NONE:
                break;

            case WEAK:
                if (giveHelmet)     inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                if (giveChestplate) inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                if (giveLeggings)   inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                if (giveBoots)      inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
                break;

            case NORMAL:
                if (giveHelmet)     inv.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                if (giveChestplate) inv.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                if (giveLeggings)   inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                if (giveBoots)      inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                break;

            case STRONG:
                if (giveHelmet)     inv.setHelmet(new ItemStack(Material.IRON_HELMET));
                if (giveChestplate) inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                if (giveLeggings)   inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                if (giveBoots)      inv.setBoots(new ItemStack(Material.IRON_BOOTS));
                break;

            case VERY_STRONG:
                if (giveHelmet)     inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                if (giveChestplate) inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                if (giveLeggings)   inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                if (giveBoots)      inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                break;
        }

        inv.setHeldItemSlot(0);
        player.updateInventory(); // Deprecated but needed
    }


    /**
     * Returns a set of unlocked tools for the given player.
     *
     * @param playerID The player's UUID
     *
     * @return A Set containing the unlocked {@link eu.carrade.amaury.ballsofzirconium.game.BoSEquipmentManager.ToolsType}.
     */
    private Set<ToolsType> getUnlockedTools(UUID playerID)
    {
        if (playersUnlockedTools.containsKey(playerID))
            return playersUnlockedTools.get(playerID);

        final Set<ToolsType> unlockedTools = new HashSet<>();
        playersUnlockedTools.put(playerID, unlockedTools);

        return unlockedTools;
    }

    /**
     * Unlocks a tool for a player.
     *
     * @param playerID The player UUID
     * @param tool     The tool to unlock.
     */
    private void unlock(final UUID playerID, final ToolsType tool)
    {
        // Unlocked only if enabled in the config
        if (isToolEnabled(tool))
        {
            final Set<ToolsType> unlockedTools = getUnlockedTools(playerID);
            if (unlockedTools.contains(tool)) return; // Already unlocked

            unlockedTools.add(tool);

            // Player notification
            final Player player = Bukkit.getPlayer(playerID);
            if (player.isOnline())
            {
                final Set<ToolsType> leftToUnlock = new HashSet<>();
                for (final ToolsType toolType : ToolsType.values())
                    if (isToolEnabled(toolType) && !unlockedTools.contains(toolType))
                        leftToUnlock.add(toolType);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1l, 1l);

                final RawTextPart notification = new RawText("")
                    .then(I.t("\u272F Tool unlocked:"))
                        .color(ChatColor.YELLOW)
                    .then(" ")
                    .then(tool.getName())
                        .color(ChatColor.GOLD)
                        .hover(new RawText()
                                .then(tool.getName()).style(ChatColor.GOLD, ChatColor.BOLD).then("\n")
                                .then(I.t("You'll obtain this tool on every respawn now. You'll no longer have to craft it.")).style(ChatColor.GRAY)
                            .build()
                        )
                    .then(".")
                        .color(ChatColor.YELLOW);

                if (!leftToUnlock.isEmpty())
                {
                    final RawTextPart<?> leftToUnlockHover = new RawText()
                            .then(I.t("What's left to unlock"))
                                .style(ChatColor.GOLD, ChatColor.BOLD)
                            .then("\n")
                            .then(I.t("You can still unlock these, by crafting them one time to get them on every respawn."))
                                .color(ChatColor.GRAY);

                    for (final ToolsType toolToUnlock : leftToUnlock)
                        leftToUnlockHover
                                .then("\n")
                                .then("- ").color(ChatColor.DARK_GRAY)
                                .then(StringUtils.capitalize(toolToUnlock.getName().toLowerCase())).color(ChatColor.WHITE);

                    notification.then(" ").then(I.t("Hover here to see what's next..."))
                            .color(ChatColor.GRAY)
                            .hover(leftToUnlockHover);
                }

                RawMessage.send(player, notification.build());
            }
        }
    }

    /**
     * Unlocks an armor piece.
     *
     * @param playerID  The player UUID.
     * @param armorType The armor type to unlock.
     * @param armor     The armor material, to check the armor material type
     *                  (leather, chainmail...) if {@code anyUnlock} is {@code
     *                  false}
     * @param anyUnlock {@code true} if any armor material should unlock. If
     *                  {@code false} the type will be checked against the
     *                  configuration and only unlocked if matching.
     */
    private void unlockArmor(final UUID playerID, final ToolsType armorType, final Material armor, final boolean anyUnlock)
    {
        if (anyUnlock || GameConfig.EQUIPMENT.ARMOR.get() == PlayerArmorType.armorToType(armor))
        {
            unlock(playerID, armorType);
        }
    }

    /**
     * Checks if a tool is enabled on the configuration.
     *
     * @param tool The tool to check.
     *
     * @return {@code true} if enabled.
     */
    private boolean isToolEnabled(ToolsType tool)
    {
        final boolean enabled;
        switch (tool)
        {
            case PICKAXE:
            case AXE:
            case SHOVEL:
                enabled = GameConfig.EQUIPMENT.TOOLS.get();
                break;

            case SWORD:
                enabled = GameConfig.EQUIPMENT.SWORD.get();
                break;

            case BOW:
                enabled = GameConfig.EQUIPMENT.BOW.get();
                break;

            case ARMOR_HELMET:
            case ARMOR_CHESTPLATE:
            case ARMOR_LEGGINGS:
            case ARMOR_BOOTS:
                enabled = GameConfig.EQUIPMENT.ARMOR.get() != PlayerArmorType.NONE;
                break;

            default:
                enabled = false;
        }

        return enabled;
    }


    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareItemCraft(final PrepareItemCraftEvent ev)
    {
        if (GameConfig.EQUIPMENT.CRAFT_ENCHANTED.get() && ev.getInventory() != null && ev.getInventory().getResult() != null)
        {
            switch (ev.getInventory().getResult().getType())
            {
                case BOW:
                    ev.getInventory().setResult(
                            new ItemStackBuilder(ev.getInventory().getResult())
                                    .enchant(Enchantment.ARROW_INFINITE, 1)
                                    .enchant(Enchantment.DURABILITY, 3)
                                .item()
                    );
                    break;
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowCraft(final CraftItemEvent ev)
    {
        if (GameConfig.EQUIPMENT.CRAFT_ENCHANTED.get()
                && ev.getInventory().getResult() != null
                && ev.getInventory().getResult().getType() == Material.BOW)
        {
            final PlayerInventory inventory = ev.getWhoClicked().getInventory();
            if (!inventory.contains(Material.ARROW))
            {
                // We try to place an arrow in the inner inventory (not the hotbar, it's ugly).
                for (int slot = 9; slot < inventory.getSize(); slot++)
                {
                    final ItemStack item = inventory.getItem(slot);
                    if (item == null || item.getType() == Material.AIR)
                    {
                        inventory.setItem(slot, new ItemStack(Material.ARROW));
                        break;
                    }
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemCraft(final CraftItemEvent ev)
    {
        if (!GameConfig.EQUIPMENT.SURVIVAL_MODE.get()) return;
        if (ev.getInventory() == null || ev.getInventory().getResult() == null)
            return;

        final UUID player = ev.getWhoClicked().getUniqueId();
        final Material craftedMaterial = ev.getInventory().getResult().getType();

        final boolean anyUnlock = GameConfig.EQUIPMENT.UNLOCK_WITH_ANY_MATERIAL.get();

        switch (craftedMaterial)
        {
            case WOODEN_PICKAXE:
            case STONE_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
                if (anyUnlock) unlock(player, ToolsType.PICKAXE);
                break;

            case IRON_PICKAXE:
                unlock(player, ToolsType.PICKAXE);
                break;


            case WOODEN_AXE:
            case STONE_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
                if (anyUnlock) unlock(player, ToolsType.AXE);
                break;

            case IRON_AXE:
                unlock(player, ToolsType.AXE);
                break;


            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
                if (anyUnlock) unlock(player, ToolsType.SHOVEL);
                break;

            case IRON_SHOVEL:
                unlock(player, ToolsType.SHOVEL);
                break;


            case WOODEN_SWORD:
            case STONE_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
                if (anyUnlock) unlock(player, ToolsType.SWORD);
                break;

            case IRON_SWORD:
                unlock(player, ToolsType.SWORD);
                break;


            case BOW:
                unlock(player, ToolsType.BOW);
                break;


            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case DIAMOND_HELMET:
                unlockArmor(player, ToolsType.ARMOR_HELMET, craftedMaterial, anyUnlock);
                break;

            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
                unlockArmor(player, ToolsType.ARMOR_CHESTPLATE, craftedMaterial, anyUnlock);
                break;

            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
                unlockArmor(player, ToolsType.ARMOR_LEGGINGS, craftedMaterial, anyUnlock);
                break;

            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case DIAMOND_BOOTS:
                unlockArmor(player, ToolsType.ARMOR_BOOTS, craftedMaterial, anyUnlock);
        }
    }


    /**
     * The tools a player can have and unlock. This enum is used to list
     * unlocked tools for each player.
     */
    public enum ToolsType
    {
        PICKAXE(I.t("PICKAXE")),
        AXE(I.t("AXE")),
        SHOVEL(I.t("SHOVEL")),
        SWORD(I.t("SWORD")),
        BOW(I.t("BOW")),

        ARMOR_HELMET(I.t("HELMET")),
        ARMOR_CHESTPLATE(I.t("CHESTPLATE")),
        ARMOR_LEGGINGS(I.t("LEGGINGS")),
        ARMOR_BOOTS(I.t("BOOTS"));

        private String name;

        ToolsType(final String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }


    /**
     * The player's armor type
     */
    public enum PlayerArmorType
    {
        /**
         * No armor
         */
        NONE,

        /**
         * Leather armor
         */
        WEAK,

        /**
         * Chainmail armor
         */
        NORMAL,

        /**
         * Iron armor
         */
        STRONG,

        /**
         * Diamond armor
         */
        VERY_STRONG;


        /**
         * Converts an armor to an armor type.
         *
         * @param armor The armor.
         *
         * @return The type, or {@code null} if no type match.
         */
        public static PlayerArmorType armorToType(Material armor)
        {
            switch (armor)
            {
                case LEATHER_HELMET:
                case LEATHER_CHESTPLATE:
                case LEATHER_LEGGINGS:
                case LEATHER_BOOTS:
                    return WEAK;

                case CHAINMAIL_HELMET:
                case CHAINMAIL_CHESTPLATE:
                case CHAINMAIL_LEGGINGS:
                case CHAINMAIL_BOOTS:
                    return NORMAL;

                case IRON_HELMET:
                case IRON_CHESTPLATE:
                case IRON_LEGGINGS:
                case IRON_BOOTS:
                    return STRONG;

                case DIAMOND_HELMET:
                case DIAMOND_CHESTPLATE:
                case DIAMOND_LEGGINGS:
                case DIAMOND_BOOTS:
                    return VERY_STRONG;

                default:
                    return null;
            }
        }
    }
}
