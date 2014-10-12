/**
 *  Bukkit plugin Balls of Steel
 *  Copyright (C) 2014 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */


package eu.carrade.amaury.BallsOfSteel;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;

public class BoSGameManager {
	
	private BallsOfSteel p = null;
	private I18n i = null;
	
	private boolean running = false;
	private ArrayList<Location> trackedChests = new ArrayList<Location>();
	
	public BoSGameManager(BallsOfSteel plugin) {
		this.p = plugin;
		this.i = p.getI18n();
	}
	
	/**
	 * Starts the game.
	 * 
	 * @param sender The sender of the start (will receive the error messages).
	 * 
	 * @throws IllegalStateException if the game is already started.
	 */
	public void start(CommandSender sender) {
		
		if(running) {
			throw new IllegalStateException("The game is already started!");
		}
		
		
		// Check: non-empty teams registered
		boolean onlyEmpty = true;
		for(BoSTeam team : p.getTeamManager().getTeams()) {
			if(team.getPlayers().size() != 0) {
				onlyEmpty = false;
				break;
			}
		}
		if(onlyEmpty) {
			sender.sendMessage(i.t("start.noTeams"));
			return;
		}
		
		
		// Check: all the teams with players inside needs to have a chest and a spawn point.
		boolean chestsOK = true;
		boolean spawnsOK = true;
		for(BoSTeam team : p.getTeamManager().getTeams()) {
			if(team.getPlayers().size() == 0) continue; // empty team
			
			if(team.getSpawnPoint() == null) {
				spawnsOK = false;
			}
			
			if(team.getChest() == null) {
				chestsOK = false;
			}
			
			if(!spawnsOK && !chestsOK) {
				break;
			}
		}
		
		if(!spawnsOK) {
			sender.sendMessage(i.t("start.noSpawnForSomeTeams"));
		}
		if(!chestsOK) {
			sender.sendMessage(i.t("start.noChestForSomeTeams"));
		}
		if(!(chestsOK && spawnsOK)) {
			return;
		}
		
		World w = null;
		
		// Teleportation, equipment
		for(BoSTeam team : p.getTeamManager().getTeams()) {
			if(team.getPlayers().size() == 0) continue;
			
			team.teleportTo(team.getSpawnPoint());
			for(OfflinePlayer oPlayer : team.getPlayers()) {
				if(oPlayer.isOnline()) {
					Player player = (Player) oPlayer;
					
					player.setHealth(20d);
					player.setFoodLevel(20);
					player.setSaturation(20f);
					player.setGameMode(GameMode.SURVIVAL);
					
					player.setBedSpawnLocation(team.getSpawnPoint(), true);
					
					equipPlayer(player);
				}
			}
			
			// We can assume that all teams are teleported in the same world.
			// We take the world of the spawn point of a team (the first non-empty).
			if(w == null) {
				w = team.getSpawnPoint().getWorld();
			}
		}
		
		// Environment
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setTime(6000);
		
		w.setThundering(false);
		w.setStorm(false);
		w.setWeatherDuration(10000); // TODO replace this using the duration of a game.
		
		w.setPVP(true);
		
		w.setSpawnFlags(false, false); // Disables all mobs spawn.
		
		// Sound
		new BoSSound(p.getConfig().getConfigurationSection("start.sound")).broadcast();
		
		// Message
		p.getServer().broadcastMessage(i.t("start.go"));
		
		running = true;
	}
	
	/**
	 * Returns {@code true} if the game is launched.
	 * 
	 * @return {@code True} if the game is launched.
	 */
	public boolean isGameRunning() {
		return running;
	}
	
	/**
	 * Updates the internal list of the tracked chests.
	 */
	public void updateTrackedChests() {
		trackedChests.clear();
		for(BoSTeam team : p.getTeamManager().getTeams()) {
			if(team.getChestLocation1() != null) {
				trackedChests.add(team.getChestLocation1());
			}
			if(team.getChestLocation2() != null) {
				trackedChests.add(team.getChestLocation2());
			}
		}
	}
	
	/**
	 * Return the chests belonging to a team.
	 * <p>
	 * Cached. Automatically updated using {@link #updateTrackedChests()}.
	 * 
	 * @return The location of the chests.
	 */
	public List<Location> getTrackedChests() {
		return trackedChests;
	}
	
	/**
	 * Equips the given player with iron tools.
	 * <p>
	 * Following the configuration:
	 * <pre>
	 *  - equipment.food: some food (1 stack of steak);
	 *  - equipment.blocks: 2 stack of dirt blocks;
	 *  - equipment.tools: pickaxe, axe, shovel;
	 *  - equipment.weapons: sword, infinity bow, one arrow;
	 *  - equipment.armor:
	 *     - "none": nothing;
	 *     - "weak": leather armor;
	 *     - "normal": chainmail armor;
	 *     - "strong": iron armor;
	 *     - "strong+": diamond armor.
	 * </pre>
	 * @param player The player to equip.
	 */
	public void equipPlayer(Player player) {
		PlayerInventory inv = player.getInventory();
		inv.clear();
		
		// Weapons
		if(p.getConfig().getBoolean("equipment.weapons")) {
			inv.addItem(new ItemStack(Material.IRON_SWORD, 1));
			
			ItemStack bow = new ItemStack(Material.BOW, 1);
			bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			inv.addItem(bow);
			
			inv.setItem(9, new ItemStack(Material.ARROW, 1));
		}
		
		// Tools
		if(p.getConfig().getBoolean("equipment.tools")) {
			inv.addItem(new ItemStack(Material.IRON_PICKAXE, 1));
			inv.addItem(new ItemStack(Material.IRON_AXE, 1));
			inv.addItem(new ItemStack(Material.IRON_SPADE, 1));
		}
		
		// Food & blocks
		boolean food = p.getConfig().getBoolean("equipment.food");
		boolean blocks = p.getConfig().getBoolean("equipment.blocks");
		if(food || blocks) {
			ItemStack foodStack = new ItemStack(Material.COOKED_BEEF, 64);
			ItemStack dirtStack = new ItemStack(Material.DIRT, 64);
			
			if(food)   inv.setItem(8, foodStack);
			
			if(blocks) {
				inv.setItem(7, dirtStack);
				
				if(!food) inv.setItem(8, dirtStack);
				else      inv.setItem(6, dirtStack);
			}
		}
		
		// Armor
		if(!p.getConfig().getString("equipment.armor", "none").equals("none")) {
			switch(p.getConfig().getString("equipment.armor")) {
				case "weak":
					inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
					inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
					inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
					inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
					break;
				case "strong":
					inv.setHelmet(new ItemStack(Material.IRON_HELMET));
					inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
					inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
					inv.setBoots(new ItemStack(Material.IRON_BOOTS));
					break;
				case "strong+":
					inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
					inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
					inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
					inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
					break;
				default: // "normal"
					inv.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
					inv.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
					inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
					inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
					break;
			}
		}
		
		inv.setHeldItemSlot(0);
		player.updateInventory(); // Deprecated but needed
	}
}
