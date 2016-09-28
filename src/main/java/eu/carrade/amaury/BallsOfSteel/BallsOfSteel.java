/**
 * Plugin UltraHardcore (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014
 * Amaury Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.BallsOfSteel;

import eu.carrade.amaury.BallsOfSteel.integration.BarAPIWrapper;
import eu.carrade.amaury.BallsOfSteel.task.UpdateTimerTask;
import fr.zcraft.zlib.core.ZPlugin;


public final class BallsOfSteel extends ZPlugin
{
    private BoSCommand commandManager = null;
    private BoSTabCompleter tabCompleter = null;

    private BoSTeamManager teamManager = null;
    private BoSScoreboardManager scoreboardManager = null;
    private BoSTeamChatManager teamChatManager = null;
    private BoSGameManager gameManager = null;

    private BarAPIWrapper barAPIWrapper = null;

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();

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


        barAPIWrapper = new BarAPIWrapper(this);


        // Imports teams from the config.
        this.teamManager.importTeamsFromConfig();

        // Starts the task that updates the timers.
        // Started here, so a timer can be displayed before the start of the game
        // (example: countdown before the start).
        new UpdateTimerTask(this).runTaskTimer(this, 20l, 20l);
    }

    /**
     * Returns the game manager.
     */
    public BoSGameManager getGameManager()
    {
        return gameManager;
    }

    /**
     * Returns the team manager.
     */
    public BoSTeamManager getTeamManager()
    {
        return teamManager;
    }

    /**
     * Returns the scoreboard manager.
     */
    public BoSScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

    /**
     * Returns the command manager.
     */
    public BoSCommand getCommandManager()
    {
        return commandManager;
    }

    /**
     * Returns the team-chat manager.
     */
    public BoSTeamChatManager getTeamChatManager()
    {
        return teamChatManager;
    }


    public BarAPIWrapper getBarAPIWrapper()
    {
        return barAPIWrapper;
    }
}
