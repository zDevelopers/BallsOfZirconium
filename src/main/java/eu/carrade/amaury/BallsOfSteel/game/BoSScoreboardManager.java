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

package eu.carrade.amaury.BallsOfSteel.game;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.GameConfig;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.timers.Timer;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.DecimalFormat;


public class BoSScoreboardManager extends ZLibComponent
{
    private Scoreboard sb = null;
    private Objective sidebar = null;

    private String sidebarTitle = null;
    private DecimalFormat format = new DecimalFormat("00");


    @Override
    protected void onEnable()
    {
        this.sidebarTitle = GameConfig.GAME_NAME.get();

        // The scoreboard manager is only available when at least a world is loaded.
        // If it's not the case, the scoreboard will be created when possible (see below).
        if (Bukkit.getWorlds().size() > 0)
            sb = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldLoadEvent ev)
    {
        // The scoreboard manager is only available when at least a world is loaded
        if (sb == null) sb = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    @Override
    protected void onDisable()
    {
        if (sidebar != null)
        {
            sidebar.unregister();
        }
    }

    /**
     * Initializes the scoreboard.
     * <p>
     * To be called when the game starts.
     */
    public void initScoreboard()
    {
        if (sb == null)
        {
            PluginLogger.error("Cannot initialize the scoreboard: no world loaded!");
            return;
        }

        if (sb.getObjective("Diamonds") != null)
        {
            sb.getObjective("Diamonds").unregister();
        }

        sidebar = sb.registerNewObjective("Diamonds", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateTimer();

        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getPlayers().size() == 0) continue;

            sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(1);
            sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(0);
        }

        for (Player player : BallsOfSteel.get().getGameManager().getGameWorld().getPlayers())
        {
            setScoreboardForPlayer(player);
        }
    }

    /**
     * Updates the timer displayed in the scoreboard title (if needed).
     */
    public void updateTimer()
    {
        final String timerText;
        if (!BallsOfSteel.get().getBarManager().isEnabled() && BallsOfSteel.get().getGameManager().isGameRunning())
        {
            timerText = I.t("{0}{gray}│ {gold}{1}", sidebarTitle, getTimerText(BallsOfSteel.get().getGameManager().getTimer()));
        }
        else
        {
            timerText = sidebarTitle;
        }

        sidebar.setDisplayName(getValidObjectiveDisplayName(timerText));
    }

    /**
     * Generates a printable version of the timer.
     * <p>
     * Format: {@code mm:ss}, or (if needed) {@code hh:mm:ss}.
     *
     * @param timer The timer.
     *
     * @return The string representation.
     */
    public String getTimerText(Timer timer)
    {
        String timerText = "";

        if (timer != null)
        {
            if (timer.getDisplayHoursInTimer())
            {
                timerText = I.t("{0}:{1}:{2}", format.format(timer.getHoursLeft()), format.format(timer.getMinutesLeft()), format.format(timer.getSecondsLeft()));
            }
            else
            {
                timerText = I.t("{0}:{1}", format.format(timer.getMinutesLeft()), format.format(timer.getSecondsLeft()));
            }
        }

        return timerText;
    }


    /**
     * Updates the diamonds score of the given team.
     *
     * @param team The team.
     */
    public void updateDiamondsScore(BoSTeam team)
    {
        sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(team.getDiamondsCount());
    }

    /**
     * Tells the player's client to use this scoreboard.
     *
     * @param p The player.
     */
    public void setScoreboardForPlayer(Player p)
    {
        if (sb != null)
            p.setScoreboard(sb);
    }

    /**
     * Returns the internal scoreboard.
     *
     * @return The internal scoreboard.
     */
    public Scoreboard getScoreboard()
    {
        return sb;
    }

    /**
     * Returns the given string, truncated at 16 characters.
     *
     * @param scoreName The original string.
     * @return The truncated string.
     */
    public String getValidScoreboardName(String scoreName)
    {
        return scoreName.substring(0, Math.min(scoreName.length(), 16));
    }

    /**
     * Returns the given string, truncated at 32 characters.
     *
     * @param scoreName The original string.
     * @return The truncated string.
     */
    public String getValidObjectiveDisplayName(String scoreName)
    {
        return scoreName.substring(0, Math.min(scoreName.length(), 32));
    }
}
