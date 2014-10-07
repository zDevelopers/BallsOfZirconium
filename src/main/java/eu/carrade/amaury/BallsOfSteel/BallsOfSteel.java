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

import org.bukkit.plugin.java.JavaPlugin;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;
import eu.carrade.amaury.BallsOfSteel.task.UpdateTimerTask;

public final class BallsOfSteel extends JavaPlugin {
	
	private BoSCommand commandManager = null;
	private BoSTabCompleter tabCompleter = null;
	
	private BoSTeamManager teamManager = null;
	private BoSScoreboardManager scoreboardManager = null;
	private BoSTeamChatManager teamChatManager = null;
	private BoSGameManager gameManager = null;
	
	private I18n i18n = null;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		if(getConfig().getString("lang") == null) {
			i18n = new I18n(this);
		}
		else {
			i18n = new I18n(this, getConfig().getString("lang"));
		}
		
		teamManager = new BoSTeamManager(this);
		teamChatManager = new BoSTeamChatManager(this);
		scoreboardManager = new BoSScoreboardManager(this);
		gameManager = new BoSGameManager(this);

		commandManager = new BoSCommand(this);
		tabCompleter = new BoSTabCompleter(this);
		
		getCommand("bos").setExecutor(commandManager);		
		getCommand("t").setExecutor(commandManager);
		getCommand("g").setExecutor(commandManager);
		getCommand("togglechat").setExecutor(commandManager);
		
		getCommand("bos").setTabCompleter(tabCompleter);
		getCommand("togglechat").setTabCompleter(tabCompleter);
		
		
		getServer().getPluginManager().registerEvents(new BoSListener(this), this);
		
		
		// Imports teams from the config.
		this.teamManager.importTeamsFromConfig();
		
		// Starts the task that updates the timers.
		// Started here, so a timer can be displayed before the start of the game
		// (example: countdown before the start).
		new UpdateTimerTask(this).runTaskTimer(this, 20l, 20l);
		
		getLogger().info(i18n.t("load.loaded"));
	}
	
	/**
	 * Returns the game manager.
	 * 
	 * @return
	 */
	public BoSGameManager getGameManager() {
		return gameManager;
	}
	
	/**
	 * Returns the team manager.
	 * 
	 * @return
	 */
	public BoSTeamManager getTeamManager() {
		return teamManager;
	}
	
	/**
	 * Returns the scoreboard manager.
	 * 
	 * @return
	 */
	public BoSScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}
	
	/**
	 * Returns the command manager.
	 * 
	 * @return
	 */
	public BoSCommand getCommandManager() {
		return commandManager;
	}
	
	/**
	 * Returns the team-chat manager.
	 * @return
	 */
	public BoSTeamChatManager getTeamChatManager() {
		return teamChatManager;
	}
	
	/**
	 * Returns the internationalization manager.
	 * 
	 * @return
	 */
	public I18n getI18n() {
		return i18n;
	}
}
