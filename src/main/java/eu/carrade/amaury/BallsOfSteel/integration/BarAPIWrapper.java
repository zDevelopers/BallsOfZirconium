package eu.carrade.amaury.BallsOfSteel.integration;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.GameConfig;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.timers.Timer;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLibComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class BarAPIWrapper extends ZLibComponent
{
    private boolean enabled = false;
    private boolean wanted = false;

    @Override
    public void onEnable()
    {
        Plugin barTest = Bukkit.getServer().getPluginManager().getPlugin("BarAPI");

        if (barTest == null || !barTest.isEnabled())
        {
            BallsOfSteel.get().getLogger().warning("BarAPI is not present, so the integration was disabled.");
            return;
        }

        enabled = true;
        wanted = GameConfig.USE_BAR.get();
    }

    /**
     * Returns true if the BarAPI is available.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Returns true if the bar is enabled in the config.
     */
    public boolean isWanted()
    {
        return wanted;
    }

    /**
     * Returns true if the bar is available and wanted.
     */
    public boolean isNeeded()
    {
        return enabled && wanted;
    }

    /**
     * Displays the waiting bar (when the game is not started) to the given
     * player.
     *
     * @param player The player.
     */
    public void setWaitingBar(Player player)
    {
        if (isNeeded())
        {
            //BarAPI.setMessage(player, I.t("{aqua}Balls {white}of {yellow}Steel"));
        }
    }

    /**
     * Displays the timer in the bar, for the players in the world of the game.
     */
    public void setRunningBar()
    {
        if (isNeeded() && BallsOfSteel.get().getGameManager().isGameRunning())
        {
            for (Player player : BallsOfSteel.get().getGameManager().getGameWorld().getPlayers())
            {
                Timer timer = BallsOfSteel.get().getGameManager().getTimer();
                String message = I.t("{aqua}Time left: {yellow}{0}", BallsOfSteel.get().getScoreboardManager().getTimerText(timer));

                //BarAPI.setMessage(player, message, timer.getPercentage());
            }
        }
    }

    /**
     * Displays the end bar, with two states: <ol> <li>a message saying the game
     * is ended (7 seconds);</li> <li>a message displaying the winner
     * (permanent).</li> </ol>
     */
    public void setEndBar()
    {
        if (isNeeded())
        {
            for (Player player : BallsOfSteel.get().getGameManager().getGameWorld().getPlayers())
            {
                //BarAPI.setMessage(player, I.t("{blue}The game is finished!"), 7);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(BallsOfSteel.get(), new BukkitRunnable()
            {

                @Override
                public void run()
                {
                    BoSTeam winner = BallsOfSteel.get().getGameManager().getCurrentWinnerTeam();

                    for (Player player : BallsOfSteel.get().getGameManager().getGameWorld().getPlayers())
                    {
                        //BarAPI.setMessage(player, I.t("{green}The {0}{green} team won the game with {aqua}{1}{green} diamonds!", winner.getDisplayName(), String.valueOf(winner.getDiamondsCount())));
                    }
                }

            }, 140l);
        }
    }
}
