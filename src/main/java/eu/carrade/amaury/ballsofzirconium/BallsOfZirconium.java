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

package eu.carrade.amaury.ballsofzirconium;

import eu.carrade.amaury.ballsofzirconium.commands.AboutCommand;
import eu.carrade.amaury.ballsofzirconium.commands.ChatGlobalCommand;
import eu.carrade.amaury.ballsofzirconium.commands.ChatTeamCommand;
import eu.carrade.amaury.ballsofzirconium.commands.ChatToggleCommand;
import eu.carrade.amaury.ballsofzirconium.commands.CheckGenerationSettings;
import eu.carrade.amaury.ballsofzirconium.commands.ClearItemsCommand;
import eu.carrade.amaury.ballsofzirconium.commands.CurrentStructureCommand;
import eu.carrade.amaury.ballsofzirconium.commands.GenerateCommand;
import eu.carrade.amaury.ballsofzirconium.commands.GenerateSphereCommand;
import eu.carrade.amaury.ballsofzirconium.commands.RestartCommand;
import eu.carrade.amaury.ballsofzirconium.commands.SpheresCommand;
import eu.carrade.amaury.ballsofzirconium.commands.StartCommand;
import eu.carrade.amaury.ballsofzirconium.commands.TeamsCommand;
import eu.carrade.amaury.ballsofzirconium.dependencies.WorldEditDependency;
import eu.carrade.amaury.ballsofzirconium.game.BarManager;
import eu.carrade.amaury.ballsofzirconium.game.BoSChestsListener;
import eu.carrade.amaury.ballsofzirconium.game.BoSEquipmentManager;
import eu.carrade.amaury.ballsofzirconium.game.BoSGameManager;
import eu.carrade.amaury.ballsofzirconium.game.BoSScoreboardManager;
import eu.carrade.amaury.ballsofzirconium.game.MetadataActionBarManager;
import eu.carrade.amaury.ballsofzirconium.generation.GenerationData;
import eu.carrade.amaury.ballsofzirconium.generation.GenerationManager;
import eu.carrade.amaury.ballsofzirconium.generation.generation.BallsOfZirconiumGenerator;
import eu.carrade.amaury.ballsofzirconium.teams.BoSTeamChatManager;
import eu.carrade.amaury.ballsofzirconium.teams.BoSTeamsManager;
import eu.carrade.amaury.ballsofzirconium.timers.Timers;
import fr.zcraft.quartzlib.components.commands.Commands;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.core.QuartzLib;
import fr.zcraft.quartzlib.core.QuartzPlugin;
import org.bukkit.generator.ChunkGenerator;


public final class BallsOfZirconium extends QuartzPlugin
{
    /**
     * A namespace to be used when needed.
     */
    public static final String BOS_NAMESPACE = "balls_of_zirconium";


    private static BallsOfZirconium instance;

    private WorldEditDependency worldEditDependency = null;

    private BoSTeamsManager teamsManager = null;
    private BoSScoreboardManager scoreboardManager = null;
    private BoSTeamChatManager teamChatManager = null;
    private BoSGameManager gameManager = null;
    private BoSEquipmentManager equipmentManager = null;

    private BarManager barManager = null;

    private GenerationManager generationManager = null;


    @Override
    public void onEnable()
    {
        instance = this;

        saveDefaultConfig();
        // TODO save default map config folder to plugin directory

        worldEditDependency = loadComponent(WorldEditDependency.class);

        loadComponents(I18n.class, Commands.class, GameConfig.class, MapConfig.class, Timers.class);

        I18n.setPrimaryLocale(GameConfig.LANG.get());

        // Important: game manager and scoreboard manager before teams manager
        gameManager       = loadComponent(BoSGameManager.class);
        scoreboardManager = loadComponent(BoSScoreboardManager.class);
        teamsManager      = loadComponent(BoSTeamsManager.class);
        teamChatManager   = loadComponent(BoSTeamChatManager.class);
        equipmentManager  = loadComponent(BoSEquipmentManager.class);
        barManager        = loadComponent(BarManager.class);
        generationManager = loadComponent(GenerationManager.class);

        if (generationManager.isEnabled())
        {
            loadComponents(GenerationData.class, MetadataActionBarManager.class);
        }

        Commands.register("bos",
                AboutCommand.class, ClearItemsCommand.class,
                ChatTeamCommand.class, ChatGlobalCommand.class, ChatToggleCommand.class,
                StartCommand.class, RestartCommand.class, TeamsCommand.class,
                SpheresCommand.class, GenerateSphereCommand.class, GenerateCommand.class,
                CheckGenerationSettings.class, CurrentStructureCommand.class
        );

        Commands.registerShortcut("bos", ChatTeamCommand.class, "t");
        Commands.registerShortcut("bos", ChatGlobalCommand.class, "g");
        Commands.registerShortcut("bos", ChatToggleCommand.class, "togglechat");

        QuartzLib.registerEvents(new BoSChestsListener(this));
    }

    public static BallsOfZirconium get()
    {
        return instance;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        return new BallsOfZirconiumGenerator();
    }

    /**
     * Returns the game manager.
     */
    public BoSGameManager getGameManager()
    {
        return gameManager;
    }

    /**
     * Returns the equipments manager.
     */
    public BoSEquipmentManager getEquipmentManager()
    {
        return equipmentManager;
    }

    /**
     * Returns the team manager.
     */
    public BoSTeamsManager getTeamsManager()
    {
        return teamsManager;
    }

    /**
     * Returns the scoreboard manager.
     */
    public BoSScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

    /**
     * Returns the team-chat manager.
     */
    public BoSTeamChatManager getTeamChatManager()
    {
        return teamChatManager;
    }

    /**
     * Returns the bar manager.
     */
    public BarManager getBarManager()
    {
        return barManager;
    }

    public GenerationManager getGenerationManager()
    {
        return generationManager;
    }

    public WorldEditDependency getWorldEditDependency()
    {
        return worldEditDependency;
    }
}
