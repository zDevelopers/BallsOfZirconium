package eu.carrade.amaury.BallsOfSteel.integration;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.BoSTimer;
import eu.carrade.amaury.BallsOfSteel.i18n.I18n;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class BarAPIWrapper
{

    private BallsOfSteel p = null;
    private I18n i = null;

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
        wanted = p.getConfig().getBoolean("useBar");

        i = p.getI18n();
    }

    /**
     * Returns true if the BarAPI is available.
     *
     * @return
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Returns true if the bar is enabled in the config.
     *
     * @return
     */
    public boolean isWanted()
    {
        return wanted;
    }

    /**
     * Returns true if the bar is available and wanted.
     *
     * @return
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
            BarAPI.setMessage(player, i.t("bar.notStarted"));
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
                String message = i.t("bar.timeLeft", p.getScoreboardManager().getTimerText(timer));

                BarAPI.setMessage(player, message, timer.getPercentage());
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
                BarAPI.setMessage(player, i.t("bar.ended"), 7);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(p, new BukkitRunnable()
            {

                @Override
                public void run()
                {
                    BoSTeam winner = p.getGameManager().getCurrentWinnerTeam();

                    for (Player player : p.getGameManager().getGameWorld().getPlayers())
                    {
                        BarAPI.setMessage(player, i.t("bar.winner", winner.getDisplayName(), String.valueOf(winner.getDiamondsCount())));
                    }
                }

            }, 140l);
        }
    }
}
