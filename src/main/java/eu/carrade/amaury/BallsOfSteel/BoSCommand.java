/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;

public class BoSCommand implements CommandExecutor {
	
	private BallsOfSteel p = null;
	
	private ArrayList<String> commands = new ArrayList<String>();
	private ArrayList<String> teamCommands = new ArrayList<String>();
	
	private I18n i = null;


	public BoSCommand(BallsOfSteel p) {
		this.p = p;
		this.i = p.getI18n();
		
		commands.add("about");
		commands.add("start");
		commands.add("team");
		commands.add("finish");
		
		teamCommands.add("add");
		teamCommands.add("remove");
		teamCommands.add("spawn");
		teamCommands.add("chest");
		teamCommands.add("join");
		teamCommands.add("leave");
		teamCommands.add("list");
		teamCommands.add("reset");
	}

	
	/**
	 * Handles a command.
	 * 
	 * @param sender The sender
	 * @param command The executed command
	 * @param label The alias used for this command
	 * @param args The arguments given to the command
	 * 
	 * @author Amaury Carrade
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		boolean ourCommand = false;
		for(String commandName : p.getDescription().getCommands().keySet()) {
			if(commandName.equalsIgnoreCase(command.getName())) {
				ourCommand = true;
				break;
			}
		}
		
		if(!ourCommand) {
			return false;
		}
		
		/** Team chat commands **/
		
		if(command.getName().equalsIgnoreCase("t")) { 
			doTeamMessage(sender, command, label, args);
			return true;
		}
		if(command.getName().equalsIgnoreCase("g")) { 
			doGlobalMessage(sender, command, label, args);
			return true;
		}
		if(command.getName().equalsIgnoreCase("togglechat")) { 
			doToggleTeamChat(sender, command, label, args);
			return true;
		}
		
		if(args.length == 0) {
			help(sender, args, false);
			return true;
		}
		
		String subcommandName = args[0].toLowerCase();
		
		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			try {
				Integer.valueOf(subcommandName);
				help(sender, args, false);
			} catch(NumberFormatException e) { // If the subcommand isn't a number, it's an error.
				help(sender, args, true);
			}
			return true;
		}
		
		// Second: is the sender allowed?
		if(!isAllowed(sender, subcommandName)) {
			unauthorized(sender, command);
			return true;
		}
		
		// Third: instantiation
		try {
			Class<? extends BoSCommand> cl = this.getClass();
			Class[] parametersTypes = new Class[]{CommandSender.class, Command.class, String.class, String[].class};
			
			Method doMethod = cl.getDeclaredMethod("do" + WordUtils.capitalize(subcommandName), parametersTypes);
			
			doMethod.invoke(this, new Object[]{sender, command, label, args});
			
			return true;
			
		} catch (NoSuchMethodException e) {
			// Unknown method => unknown subcommand.
			help(sender, args, true);
			return true;
			
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(i.t("cmd.errorLoad"));
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Prints the help.
	 * 
	 * @param sender
	 * @param args The arguments of the command.
	 * @param error True if the help is printed because the user typed an unknown command.
	 */
	private void help(CommandSender sender, String[] args, boolean error) {
		if(error) {
			sender.sendMessage(i.t("cmd.errorUnknown"));
			return;
		}
		
		if(sender instanceof Player) sender.sendMessage("");
		sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
		
		sender.sendMessage(i.t("cmd.legendHelp"));
		
		sender.sendMessage(i.t("cmd.helpStart"));
		sender.sendMessage(i.t("cmd.helpTeam"));
		sender.sendMessage(i.t("cmd.helpFinish"));
		sender.sendMessage(i.t("cmd.helpAbout"));
	}
	
	
	/**
	 * This method checks if an user is allowed to send a command.
	 * 
	 * @param sender
	 * @param subcommand
	 * @return boolean The allowance status.
	 */
	private boolean isAllowed(CommandSender sender, String subcommand) {
		if(sender instanceof Player) {
			if(sender.isOp()) {
				return true;
			}
			else if(sender.hasPermission("bos." + subcommand)) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * This method sends a message to a player who try to use a command without the permission.
	 * 
	 * @param sender
	 * @param command
	 */
	private void unauthorized(CommandSender sender, Command command) {
		sender.sendMessage(i.t("cmd.errorUnauthorized"));
	}
	
	
	/**
	 * This command prints some informations about the plugin and the translation.
	 * 
	 * Usage: /bos about
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doAbout(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) sender.sendMessage("");
		sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
		
		String authors = "";
		List<String> listAuthors = p.getDescription().getAuthors();
		for(String author : listAuthors) {
			if(author == listAuthors.get(0)) {
				// Nothing
			}
			else if(author == listAuthors.get(listAuthors.size() - 1)) {
				authors += " " + i.t("about.and") + " ";
			}
			else {
				authors += ", ";
			}
			authors += author;
		}
		sender.sendMessage(i.t("about.authors", authors));
		
		sender.sendMessage(i.t("about.i18n.title"));
		sender.sendMessage(i.t("about.i18n.selected", i.getSelectedLanguage(), i.getTranslator(i.getSelectedLanguage())));
		sender.sendMessage(i.t("about.i18n.fallback", i.getDefaultLanguage(), i.getTranslator(i.getDefaultLanguage())));
		sender.sendMessage(i.t("about.license.title"));
		sender.sendMessage(i.t("about.license.license"));
	}
	
	/**
	 * This command starts the game.
	 * 
	 * Usage: /bos start
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doStart(CommandSender sender, Command command, String label, String[] args) {
		try {
			p.getGameManager().start(sender);
		} catch(IllegalStateException e) {
			sender.sendMessage(i.t("start.already"));
		}
	}
	

	/**
	 * This command is used to manage the teams.
	 * 
	 * Usage: /bos team (for the doc).
	 * Usage: /bos team <add|remove|spawn|join|leave|list|reset> (see doc for details).
	 * 	
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTeam(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // No action provided: doc
			if(sender instanceof Player) sender.sendMessage("");
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.teamHelpTitle"));
			sender.sendMessage(i.t("cmd.teamHelpAdd"));
			sender.sendMessage(i.t("cmd.teamHelpAddName"));
			sender.sendMessage(i.t("cmd.teamHelpRemove"));
			sender.sendMessage(i.t("cmd.teamHelpSpawn"));
			sender.sendMessage(i.t("cmd.teamHelpChest"));
			sender.sendMessage(i.t("cmd.teamHelpJoin"));
			sender.sendMessage(i.t("cmd.teamHelpLeave"));
			sender.sendMessage(i.t("cmd.teamHelpList"));
			sender.sendMessage(i.t("cmd.teamHelpReset"));
		}
		else {
			BoSTeamManager tm = p.getTeamManager();
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) {
				if(args.length == 3) { // /bos team add <color>
					
					ChatColor color = p.getTeamManager().getChatColorByName(args[2]);
					
					if(color == null) {
						sender.sendMessage(i.t("team.add.errorColor"));
					}
					else {
						try {
							tm.addTeam(color, args[2].toLowerCase());
						}
						catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("team.add.errorExists"));
						}
						sender.sendMessage(i.t("team.add.added", color.toString() + args[2]));
					}
				
				}
				else if(args.length >= 4) { // /bos team add <color> <name ...>
					
					ChatColor color = p.getTeamManager().getChatColorByName(args[2]);
					
					if(color == null) {
						sender.sendMessage(i.t("team.add.errorColor"));
					}
					else {
						String name = BoSUtils.getStringFromCommandArguments(args, 3);
						
						try {
							tm.addTeam(color, name);
						}
						catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("team.add.errorExists"));
							return;
						}
						sender.sendMessage(i.t("team.add.added", color.toString() + name));
					}
					
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("remove")) {
				if(args.length >= 3) { // /bos team remove <teamName>
					String name = BoSUtils.getStringFromCommandArguments(args, 2);
					if(!tm.removeTeam(name)) {
						sender.sendMessage(i.t("team.remove.doesNotExists"));
					}
					else {
						sender.sendMessage(i.t("team.remove.removed", name));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("spawn")) {
				Location spawnPoint = null;
				
				World world;
				if(sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else if(sender instanceof BlockCommandSender) {
					world = ((BlockCommandSender) sender).getBlock().getWorld();
				}
				else {
					world = p.getServer().getWorlds().get(0);
				}
				
				String nameTeamWithoutCoords = null, nameTeamWithCoords = null, teamName = null;
				if(args.length >= 3) {
					nameTeamWithCoords = BoSUtils.getStringFromCommandArguments(args, 3);
				}
				if(args.length >= 2) {
					nameTeamWithoutCoords = BoSUtils.getStringFromCommandArguments(args, 2);
				}
				
				if(p.getTeamManager().getTeam(nameTeamWithoutCoords) != null) { // /bos spawn <team ...>
					if(!(sender instanceof Player)) {
						sender.sendMessage(i.t("team.spawn.noConsole"));
						return;
					}
					
					spawnPoint = ((Player) sender).getLocation();
					teamName   = nameTeamWithoutCoords;
				}
				else if(p.getTeamManager().getTeam(nameTeamWithCoords) != null) { // /bos spawn <x,y,z> <team ...>
					teamName = nameTeamWithCoords;
					
					String[] coords = args[2].split(",");
					
					if(coords.length == 2) {
						try {
							double x = Double.valueOf(coords[0]);
							double z = Double.valueOf(coords[1]);
							
							spawnPoint = new Location(world, x, world.getHighestBlockYAt(Location.locToBlock(x), Location.locToBlock(z)), z);
						} catch(NumberFormatException e) {
							sender.sendMessage(i.t("team.spawn.NaN"));
							return;
						}
					}
					else if(coords.length >= 3) {
						try {
							double x = Double.valueOf(coords[0]);
							double y = Double.valueOf(coords[1]);
							double z = Double.valueOf(coords[2]);
							
							spawnPoint = new Location(world, x, y, z);
						} catch(NumberFormatException e) {
							sender.sendMessage(i.t("team.spawn.NaN"));
							return;
						}
					}
					else {
						sender.sendMessage(i.t("team.syntaxError"));
						return;
					}
				}
				
				if(teamName == null) { // Unknown team
					sender.sendMessage(i.t("team.spawn.unknown"));
					return;
				}
				
				BoSTeam team = p.getTeamManager().getTeam(teamName); // This cannot be null, here.
				
				team.setSpawnPoint(spawnPoint);
				
				sender.sendMessage(i.t("team.spawn.set", team.getDisplayName(), String.valueOf(spawnPoint.getBlockX()), String.valueOf(spawnPoint.getBlockY()), String.valueOf(spawnPoint.getBlockZ())));
			}
			
			
			else if(subcommand.equalsIgnoreCase("chest")) {
				Location chestLocation = null;
				
				World world;
				if(sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else if(sender instanceof BlockCommandSender) {
					world = ((BlockCommandSender) sender).getBlock().getWorld();
				}
				else {
					world = p.getServer().getWorlds().get(0);
				}
				
				String nameTeamWithoutCoords = null, nameTeamWithCoords = null, teamName = null;
				if(args.length >= 3) {
					nameTeamWithCoords = BoSUtils.getStringFromCommandArguments(args, 3);
				}
				if(args.length >= 2) {
					nameTeamWithoutCoords = BoSUtils.getStringFromCommandArguments(args, 2);
				}
				
				if(p.getTeamManager().getTeam(nameTeamWithoutCoords) != null) { // /bos chest <team ...>
					if(!(sender instanceof Player)) {
						sender.sendMessage(i.t("team.chest.noConsole"));
						return;
					}
					
					teamName = nameTeamWithoutCoords;
					
					Block chest = ((Player) sender).getTargetBlock(null, 10);
					if(chest != null) {
						if(chest.getType() == Material.CHEST || chest.getType() == Material.TRAPPED_CHEST) {
							chestLocation = chest.getLocation();
						}
						else {
							sender.sendMessage(i.t("team.chest.notLookingAtAChest"));
							return;
						}
					}
					else {
						sender.sendMessage(i.t("team.chest.notLookingAtSomething"));
						return;
					}
				}
				else if(p.getTeamManager().getTeam(nameTeamWithCoords) != null) { // /bos spawn <x,y,z> <team ...>
					teamName = nameTeamWithCoords;
					
					String[] coords = args[2].split(",");
					
					if(coords.length >= 3) {
						try {
							double x = Double.valueOf(coords[0]);
							double y = Double.valueOf(coords[1]);
							double z = Double.valueOf(coords[2]);
							
							chestLocation = new Location(world, x, y, z);
						} catch(NumberFormatException e) {
							sender.sendMessage(i.t("team.chest.NaN"));
							return;
						}
					}
					else {
						sender.sendMessage(i.t("team.syntaxError"));
						return;
					}
				}
				
				if(teamName == null) { // Unknown team
					sender.sendMessage(i.t("team.chest.unknown"));
					return;
				}
				
				BoSTeam team = p.getTeamManager().getTeam(teamName); // This cannot be null, here.
				
				try {
					team.setChest(chestLocation);
				} catch(IllegalArgumentException e) {
					sender.sendMessage(i.t("team.chest.notAChest"));
					return;
				}
				
				sender.sendMessage(i.t("team.chest.set", team.getDisplayName(), String.valueOf(chestLocation.getBlockX()), String.valueOf(chestLocation.getBlockY()), String.valueOf(chestLocation.getBlockZ())));
			}
			
			
			else if(subcommand.equalsIgnoreCase("join")) {
				if(args.length >= 4) { // /bos team join <player> <teamName>
					
					Player player = p.getServer().getPlayer(args[2]);
					String teamName = BoSUtils.getStringFromCommandArguments(args, 3);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(i.t("team.addplayer.disconnected", args[2], teamName));
					}
					else {
						try {
							tm.addPlayerToTeam(teamName, player);
						} catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("team.addplayer.doesNotExists"));
							return;
						}
						catch(RuntimeException e) {
							sender.sendMessage(i.t("team.addplayer.full", teamName));
							return;
						}
						BoSTeam team = p.getTeamManager().getTeam(teamName);
						sender.sendMessage(i.t("team.addplayer.success", args[2], team.getDisplayName()));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("leave")) {
				if(args.length == 3) { // /bos team leave <player>
					
					Player player = p.getServer().getPlayer(args[2]);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(i.t("team.removeplayer.disconnected", args[2]));
					}
					else {
						tm.removePlayerFromTeam(player);
						sender.sendMessage(i.t("team.removeplayer.success", args[2]));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("list")) {
				if(tm.getTeams().size() == 0) {
					sender.sendMessage(i.t("team.list.nothing"));
					return;
				}
				
				for(final BoSTeam team : tm.getTeams()) {
					sender.sendMessage(i.t("team.list.itemTeam",  team.getDisplayName(), ((Integer) team.getPlayers().size()).toString()));
					for(final OfflinePlayer player : team.getPlayers()) {
						String bullet = null;
						if(player.isOnline()) {
							bullet = i.t("team.list.bulletPlayerOnline");
						}
						else {
							bullet = i.t("team.list.bulletPlayerOffline");
						}
						
						sender.sendMessage(bullet + i.t("team.list.itemPlayer", player.getName()));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("reset")) {
				tm.reset();
				sender.sendMessage(i.t("team.reset.success"));
			}
			
			else {
				sender.sendMessage(i.t("team.unknownCommand"));
			}
		}
	}
	
	/**
	 * This commands broadcast the winner(s) of the game and sends some fireworks at these players.
	 * It fails if there is more than one team alive.
	 * 
	 * Usage: /bos finish
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doFinish(CommandSender sender, Command command, String label, String[] args) {
		
//		try {
//			p.getGameManager().finishGame();
//			
//		} catch(IllegalStateException e) {
//			
//			if(e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_STARTED)) {
//				sender.sendMessage(i.t("finish.notStarted"));
//			}
//			else if(e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_FINISHED)) {
//				sender.sendMessage(i.t("finish.notFinished"));
//			}
//			else {
//				throw e;
//			}
//		}
		
	}
	
	/**
	 * This command, /t <message>, is used to send a team-message.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doTeamMessage(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /t
			sender.sendMessage(i.t("team.message.usage", "t"));
			return;
		}
		
		String message = "";
		for(Integer i = 0; i < args.length; i++) {
			message += args[i] + " ";
		}
		
		p.getTeamChatManager().sendTeamMessage((Player) sender, message);
	}
	
	/**
	 * This command, /g <message>, is used to send a global message.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doGlobalMessage(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /g
			sender.sendMessage(i.t("team.message.usage", "g"));
			return;
		}
		
		String message = "";
		for(Integer i = 0; i < args.length; i++) {
			message += args[i] + " ";
		}
		
		p.getTeamChatManager().sendGlobalMessage((Player) sender, message);
	}
	
	/**
	 * This command, /togglechat, is used to toggle the chat between the global chat and the team chat.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doToggleTeamChat(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /togglechat
			if(p.getTeamChatManager().toggleChatForPlayer((Player) sender)) {
				sender.sendMessage(i.t("team.message.toggle.nowTeamChat"));
			}
			else {
				sender.sendMessage(i.t("team.message.toggle.nowGlobalChat"));
			}
		}
		else { // /togglechat <another team>
			String teamName = BoSUtils.getStringFromCommandArguments(args, 0);
			BoSTeam team = p.getTeamManager().getTeam(teamName);
			
			if(team != null) {
				if(p.getTeamChatManager().toggleChatForPlayer((Player) sender, team)) {
					sender.sendMessage(i.t("team.message.toggle.nowOtherTeamChat", team.getDisplayName()));
				}
			}
			else {
				sender.sendMessage(i.t("team.message.toggle.unknownTeam"));
			}
		}
	}
	
	
	
	
	public ArrayList<String> getCommands() {
		return commands;
	}

	public ArrayList<String> getTeamCommands() {
		return teamCommands;
	}
}
