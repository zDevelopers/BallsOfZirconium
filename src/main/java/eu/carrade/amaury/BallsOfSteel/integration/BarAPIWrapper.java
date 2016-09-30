package eu.carrade.amaury.BallsOfSteel.integration;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.Config;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.timers.BoSTimer;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class BarAPIWrapper
{
    private BallsOfSteel p = null;

    private boolean enabled = false;
    private boolean wanted = false;

    public BarAPIWrapper(BallsOfSteel plugin)
    {
        this.p = plugin;

        Plugin barTest = Bukkit.getServer().getPluginManager().getPlugin("BarAPI");

        if (barTest == null || !barTest.isEnabled())
        {
            p.getLogger().warning("BarAPI is not present, so the integration was disabled.");
            return;
        }

        enabled = true;
        wanted = Config.USE_BAR.get();
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
        if (isNeeded() && p.getGameManager().isGameRunning())
        {
            for (Player player : p.getGameManager().getGameWorld().getPlayers())
            {
                BoSTimer timer = p.getGameManager().getTimer();
                String message = I.t("{aqua}Time left: {yellow}{0}", p.getScoreboardManager().getTimerText(timer));

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
            for (Player player : p.getGameManager().getGameWorld().getPlayers())
            {
                //BarAPI.setMessage(player, I.t("{blue}The game is finished!"), 7);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(p, new BukkitRunnable()
            {

                @Override
                public void run()
                {
                    BoSTeam winner = p.getGameManager().getCurrentWinnerTeam();

                    for (Player player : p.getGameManager().getGameWorld().getPlayers())
                    {
                        //BarAPI.setMessage(player, I.t("{green}The {0}{green} team won the game with {aqua}{1}{green} diamonds!", winner.getDisplayName(), String.valueOf(winner.getDiamondsCount())));
                    }
                }

            }, 140l);
        }
    }
}
