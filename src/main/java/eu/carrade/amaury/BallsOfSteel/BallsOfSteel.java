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

import eu.carrade.amaury.BallsOfSteel.commands.AboutCommand;
import eu.carrade.amaury.BallsOfSteel.commands.ChatGlobalCommand;
import eu.carrade.amaury.BallsOfSteel.commands.ChatTeamCommand;
import eu.carrade.amaury.BallsOfSteel.commands.ChatToggleCommand;
import eu.carrade.amaury.BallsOfSteel.commands.CheckGenerationSettings;
import eu.carrade.amaury.BallsOfSteel.commands.ClearItemsCommand;
import eu.carrade.amaury.BallsOfSteel.commands.GenerateCommand;
import eu.carrade.amaury.BallsOfSteel.commands.GenerateSphereCommand;
import eu.carrade.amaury.BallsOfSteel.commands.RestartCommand;
import eu.carrade.amaury.BallsOfSteel.commands.SpheresCommand;
import eu.carrade.amaury.BallsOfSteel.commands.StartCommand;
import eu.carrade.amaury.BallsOfSteel.commands.TeamsCommand;
import eu.carrade.amaury.BallsOfSteel.dependencies.WorldEditDependency;
import eu.carrade.amaury.BallsOfSteel.game.BarManager;
import eu.carrade.amaury.BallsOfSteel.game.BoSChestsListener;
import eu.carrade.amaury.BallsOfSteel.game.BoSEquipmentManager;
import eu.carrade.amaury.BallsOfSteel.game.BoSGameManager;
import eu.carrade.amaury.BallsOfSteel.game.BoSScoreboardManager;
import eu.carrade.amaury.BallsOfSteel.game.MetadataActionBarManager;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationManager;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationMetadata;
import eu.carrade.amaury.BallsOfSteel.generation.generation.BallsOfSteelGenerator;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeamChatManager;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeamsManager;
import eu.carrade.amaury.BallsOfSteel.timers.Timers;
import fr.zcraft.quartzlib.core.QuartzPlugin;
import fr.zcraft.quartzlib.components.commands.Commands;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.core.QuartzLib;
import org.bukkit.generator.ChunkGenerator;


public final class BallsOfSteel extends QuartzPlugin
{
    /**
     * A namespace to be used when needed.
     */
    public static final String BOS_NAMESPACE = "balls_of_steel";


    private static BallsOfSteel instance;

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
            loadComponents(GenerationMetadata.class, MetadataActionBarManager.class);
        }

        Commands.register("bos",
                AboutCommand.class, ClearItemsCommand.class,
                ChatTeamCommand.class, ChatGlobalCommand.class, ChatToggleCommand.class,
                StartCommand.class, RestartCommand.class, TeamsCommand.class,
                SpheresCommand.class, GenerateSphereCommand.class, GenerateCommand.class,
                CheckGenerationSettings.class
        );

        Commands.registerShortcut("bos", ChatTeamCommand.class, "t");
        Commands.registerShortcut("bos", ChatGlobalCommand.class, "g");
        Commands.registerShortcut("bos", ChatToggleCommand.class, "togglechat");

        QuartzLib.registerEvents(new BoSChestsListener(this));
    }

    public static BallsOfSteel get()
    {
        return instance;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        return new BallsOfSteelGenerator();
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
