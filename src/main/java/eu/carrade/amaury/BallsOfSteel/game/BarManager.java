package eu.carrade.amaury.BallsOfSteel.game;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.GameConfig;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.timers.Timer;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class BarManager extends ZLibComponent implements Listener
{
    private BossBar bar;

    @Override
    public void onEnable()
    {
        if (!GameConfig.USE_BAR.get())
            setEnabled(false);

        bar = Bukkit.createBossBar(GameConfig.BAR.TITLE.get(), GameConfig.BAR.BAR_COLOR_BEFORE_GAME.get(), GameConfig.BAR.BAR_STYLE_BEFORE_GAME.get());
        bar.setVisible(true);

        if (isEnabled())
        {
            useWaitingBar();

            for (final Player player : Bukkit.getOnlinePlayers())
            {
                bar.addPlayer(player);
            }
        }
    }

    @Override
    protected void onDisable()
    {
        bar.setVisible(false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        if (isEnabled())
        {
            bar.addPlayer(ev.getPlayer());
        }
    }

    public void useWaitingBar()
    {
        bar.setTitle(GameConfig.BAR.TITLE.get());
        bar.setColor(GameConfig.BAR.BAR_COLOR_BEFORE_GAME.get());
        bar.setStyle(GameConfig.BAR.BAR_STYLE_BEFORE_GAME.get());
        bar.setProgress(1.0);
    }

    public void useRunningBar()
    {
        bar.setTitle(getRunningBarTitle());
        bar.setColor(GameConfig.BAR.BAR_COLOR_DURING_GAME.get());
        bar.setStyle(GameConfig.BAR.BAR_STYLE_DURING_GAME.get());
        bar.setProgress(1.0);
    }

    public void updatePercentage()
    {
        final Timer timer = BallsOfSteel.get().getGameManager().getTimer();

        bar.setTitle(getRunningBarTitle());
        bar.setProgress(timer.getPercentage());

        if (timer.getPercentage() <= GameConfig.BAR.CLOSE_TO_END_PERCENTAGE.get())
            bar.setColor(GameConfig.BAR.BAR_COLOR_CLOSE_TO_END.get());
    }

    private String getRunningBarTitle()
    {
        final Timer timer = BallsOfSteel.get().getGameManager().getTimer();
        final String timerText = I.t("{aqua}Time left: {yellow}{0}", BallsOfSteel.get().getScoreboardManager().getTimerText(timer));

        if (GameConfig.BAR.DISPLAY_TITLE_DURING_GAME.get())
        {
            return GameConfig.BAR.TITLE.get() + ChatColor.GRAY + " ⋅ " + ChatColor.RESET + timerText;
        }
        else
        {
            return timerText;
        }
    }

    public void useEndBar()
    {
        bar.setTitle(I.t("{blue}The game is finished!"));

        bar.setColor(GameConfig.BAR.BAR_COLOR_AFTER_GAME.get());
        bar.setStyle(GameConfig.BAR.BAR_STYLE_AFTER_GAME.get());
        bar.setProgress(1.0);

        RunTask.later(new Runnable() {
            @Override
            public void run()
            {
                final BoSTeam winner = BallsOfSteel.get().getGameManager().getCurrentWinnerTeam();
                bar.setTitle(I.t("{green}The {0}{green} team won the game with {aqua}{1}{green} diamonds!", winner.getDisplayName(), winner.getDiamondsCount()));
            }
        }, 140l);
    }



/*
    public void setWaitingBar(Player player)
    {
        if (isEnabled())
        {
            //BarAPI.setMessage(player, I.t("{aqua}Balls {white}of {yellow}Steel"));
        }
    }


    public void setRunningBar()
    {
        if (isEnabled() && BallsOfSteel.get().getGameManager().isGameRunning())
        {
            for (Player player : BallsOfSteel.get().getGameManager().getGameWorld().getPlayers())
            {
                Timer timer = BallsOfSteel.get().getGameManager().getTimer();
                String message = I.t("{aqua}Time left: {yellow}{0}", BallsOfSteel.get().getScoreboardManager().getTimerText(timer));

                //BarAPI.setMessage(player, message, timer.getPercentage());
            }
        }
    }


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
    */
}
