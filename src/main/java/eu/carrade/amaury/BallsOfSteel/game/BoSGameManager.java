/**
 * Bukkit plugin Balls of Steel Copyright (C) 2014 Amaury Carrade
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
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.timers.Timer;
import eu.carrade.amaury.BallsOfSteel.timers.TimerEndsEvent;
import eu.carrade.amaury.BallsOfSteel.utils.BoSUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;


public class BoSGameManager extends ZLibComponent implements Listener
{
    private BallsOfSteel p = null;

    private boolean running = false;

    private World gameWorld = null;
    private Map<Location, BoSTeam> trackedChests = new HashMap<>();
    private Timer timer = null;

    private final static String TIMER_NAME = "eu.carrade.amaury.ballsofsteel";


    public BoSGameManager()
    {
        this.p = BallsOfSteel.get();
    }

    @Override
    protected void onEnable()
    {
        timer = new Timer(TIMER_NAME);

        try
        {
            timer.setDuration(BoSUtils.string2Time(GameConfig.DURATION.get()));
        }
        catch (IllegalArgumentException e)
        {
            timer.setDuration(3600); // One hour by default.
        }

        setGameWorld(MapConfig.WORLD.get());
    }


    /**
     * Starts the game.
     *
     * @param sender The sender of the start (will receive the error messages).
     *
     * @throws IllegalStateException if the game is already started.
     */
    public void start(final CommandSender sender)
    {
        if (running)
        {
            throw new IllegalStateException("The game is already started!");
        }


        // Check: non-empty teams registered
        boolean onlyEmpty = true;
        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getPlayers().size() != 0)
            {
                onlyEmpty = false;
                break;
            }
        }
        if (onlyEmpty)
        {
            sender.sendMessage(I.t("{ce}You cannot start the game without any non-empty team."));
            return;
        }


        // Check: all the teams with players inside needs to have a chest and a spawn point.
        boolean chestsOK = true;
        boolean spawnsOK = true;
        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getPlayers().size() == 0) continue; // empty team

            if (team.getSpawnPoint() == null)
            {
                spawnsOK = false;
            }

            if (team.getChest() == null)
            {
                chestsOK = false;
            }

            if (!spawnsOK && !chestsOK)
            {
                break;
            }
        }

        if (!spawnsOK)
        {
            sender.sendMessage(I.t("{ce}Some non-empty teams don't have a spawn point."));
        }
        if (!chestsOK)
        {
            sender.sendMessage(I.t("{ce}Some non-empty teams don't have a chest."));
        }
        if (!(chestsOK && spawnsOK))
        {
            return;
        }

        // Teleportation, equipment
        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getPlayers().size() == 0) continue;

            team.teleportTo(team.getSpawnPoint());
            team.setDiamondsCount(0);

            for (OfflinePlayer oPlayer : team.getPlayers())
            {
                if (oPlayer.isOnline())
                {
                    Player player = (Player) oPlayer;

                    player.setHealth(20d);
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    player.setGameMode(GameMode.SURVIVAL);

                    player.setBedSpawnLocation(team.getSpawnPoint(), true);

                    equipPlayer(player);
                }
            }

            // We can assume that all teams are teleported in the same world.
            // We take the world of the spawn point of a team (the first non-empty).
            if (getGameWorld() == null)
            {
                setGameWorld(team.getSpawnPoint().getWorld());
            }
        }

        // Environment
        gameWorld.setGameRuleValue("doDaylightCycle", "false");
        gameWorld.setTime(6000);

        gameWorld.setThundering(false);
        gameWorld.setStorm(false);
        gameWorld.setWeatherDuration(timer.getDuration() * 2 * 20);

        gameWorld.setPVP(true);

        // Timer
        timer.start();

        // Scoreboard
        BallsOfSteel.get().getScoreboardManager().initScoreboard();

        // Sound
        if (GameConfig.START.SOUND.get() != null)
            GameConfig.START.SOUND.get().broadcast(gameWorld);

        // Message
        BoSUtils.worldBroadcast(gameWorld, I.t("{green}--- GO ---"));

        running = true;
    }

    /**
     * Ends the game.
     * <p>
     * <ul>
     *   <li>Displays "game ended" in the chat;</li>
     *   <li>displays "game ended" in the boss bar (if available);</li>
     *   <li>displays, 7 seconds after, the name of the winner in the boss bar (fallback on the chat).</li>
     * </ul>
     *
     * @param broadcastStopInChat If true, a "game ended" message will be published in the chat.<br>
     * Set this to false if you restart the game before the end.
     */
    public void stop(boolean broadcastStopInChat)
    {
        if (broadcastStopInChat)
        {
            BoSUtils.worldBroadcast(getGameWorld(), I.t("{red}--- The End ---"));
        }

        setGameRunning(false);

        if (BallsOfSteel.get().getBarAPIWrapper().isNeeded())
        {
            BallsOfSteel.get().getBarAPIWrapper().setEndBar();
        }
        else
        {
            BallsOfSteel.get().getScoreboardManager().updateTimer(); // Hides the timer in the scoreboard, if this scoreboard is used.
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable()
        {
            @Override
            public void run()
            {
                BoSTeam winner = getCurrentWinnerTeam();
                String winners = "";
                int winnersCount = winner.getPlayers().size(), j = 0;
                for (OfflinePlayer player : winner.getPlayers())
                {
                    winners += player.getName();
                    if (j == winnersCount - 2)
                    {
                        winners += " " + I.t("and") + " ";
                    }
                    else if (j != winnersCount - 1)
                    {
                        winners += ", ";
                    }
                    j++;
                }

                BoSUtils.worldBroadcast(getGameWorld(), I.t("{green}Congratulations to {0} (team {1}{green}) for their victory!", winners, winner.getDisplayName()));
            }
        }, 140l);
    }

    /**
     * Restarts the game before the end of a game.
     */
    public void restart(CommandSender sender)
    {
        if (!isGameRunning())
        {
            start(sender);
        }
        else
        {
            stop(false);
            start(sender);
        }
    }

    /**
     * Returns {@code true} if the game is launched.
     *
     * @return {@code True} if the game is launched.
     */
    public boolean isGameRunning()
    {
        return running;
    }

    /**
     * Sets the running state of the game.
     *
     * @param running The running state.
     */
    protected void setGameRunning(boolean running)
    {
        this.running = running;
    }

    /**
     * Returns the world where the game take place.
     *
     * @return The world where the game take place. {@code Null} if the game is not started
     * and a world was not explicitly set in the config.
     */
    public World getGameWorld()
    {
        return gameWorld;
    }

    /**
     * Sets the world where the game take place.
     *
     * @param world The world where the game take place.
     */
    public void setGameWorld(World world)
    {
        gameWorld = world;
        BallsOfSteel.get().getLogger().info("Game world set: " + gameWorld.getName() + ".");
    }

    /**
     * Returns the timer of the game.
     *
     * @return The timer.
     */
    public Timer getTimer()
    {
        return timer;
    }

    /**
     * Updates the internal list of the tracked chests.
     */
    public void updateTrackedChests()
    {
        trackedChests.clear();
        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getChestLocation1() != null)
            {
                trackedChests.put(team.getChestLocation1(), team);
            }
            if (team.getChestLocation2() != null)
            {
                trackedChests.put(team.getChestLocation2(), team);
            }
        }
    }

    /**
     * Return the chests belonging to a team.
     * <p>
     * Cached. Automatically updated using {@link #updateTrackedChests()}.
     *
     * @return The location of the chests.
     */
    public Map<Location, BoSTeam> getTrackedChests()
    {
        return trackedChests;
    }

    /**
     * Returns the current winner of the game (aka the team with the highest diamond count).
     *
     * @return The team.
     */
    public BoSTeam getCurrentWinnerTeam()
    {
        int bestCount = -1;
        BoSTeam winner = null;

        for (BoSTeam team : BallsOfSteel.get().getTeamsManager().getTeams())
        {
            if (team.getDiamondsCount() > bestCount)
            {
                bestCount = team.getDiamondsCount();
                winner = team;
            }
        }

        return winner;
    }

    /**
     * Equips the given player with iron tools.
     * <p>
     * Following the configuration:
     * <pre>
     *  - equipment.food: some food (1 stack of steak);
     *  - equipment.blocks: 2 stack of dirt blocks;
     *  - equipment.tools: pickaxe, axe, shovel;
     *  - equipment.sword: sword;
     *  - equipment.bow: infinity bow, one arrow;
     *  - equipment.armor:
     *     - "none": nothing;
     *     - "weak": leather armor;
     *     - "normal": chainmail armor;
     *     - "strong": iron armor;
     *     - "very_strong": diamond armor.
     * </pre>
     * @param player The player to equip.
     */
    public void equipPlayer(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        // Weapons
        if (GameConfig.EQUIPMENT.SWORD.get())
        {
            inv.addItem(new ItemStack(Material.IRON_SWORD, 1));
        }
        if (GameConfig.EQUIPMENT.BOW.get())
        {
            ItemStack bow = new ItemStack(Material.BOW, 1);
            bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
            bow.addEnchantment(Enchantment.DURABILITY, 3);
            inv.addItem(bow);

            inv.setItem(9, new ItemStack(Material.ARROW, 1));
        }

        // Tools
        if (GameConfig.EQUIPMENT.TOOLS.get())
        {
            inv.addItem(new ItemStack(Material.IRON_PICKAXE, 1));
            inv.addItem(new ItemStack(Material.IRON_AXE, 1));
            inv.addItem(new ItemStack(Material.IRON_SPADE, 1));
        }

        // Food & blocks
        boolean food = GameConfig.EQUIPMENT.FOOD.get();
        boolean blocks = GameConfig.EQUIPMENT.BLOCKS.get();
        if (food || blocks)
        {
            ItemStack foodStack = new ItemStack(Material.COOKED_BEEF, 64);
            ItemStack dirtStack = new ItemStack(Material.DIRT, 64);

            if (food) inv.setItem(8, foodStack);

            if (blocks)
            {
                inv.setItem(7, dirtStack);

                if (!food) inv.setItem(8, dirtStack);
                else inv.setItem(6, dirtStack);
            }
        }

        // Armor
        switch (GameConfig.EQUIPMENT.ARMOR.get())
        {
            case NONE:
                break;

            case WEAK:
                inv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                inv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
                break;

            case NORMAL:
                inv.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                inv.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                inv.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                break;

            case STRONG:
                inv.setHelmet(new ItemStack(Material.IRON_HELMET));
                inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                inv.setBoots(new ItemStack(Material.IRON_BOOTS));
                break;

            case VERY_STRONG:
                inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                break;
        }

        inv.setHeldItemSlot(0);
        player.updateInventory(); // Deprecated but needed
    }



    /**
     * Used to:
     * - change the gamemode of the player, if the game is not running;
     * - teleport the player to the spawn, if the game is not running;
     * - update the scoreboard;
     * - resurrect a player (if the player was offline).
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent ev)
    {
        if (!isGameRunning())
        {
            BallsOfSteel.get().getBarAPIWrapper().setWaitingBar(ev.getPlayer());
        }
        else if (ev.getPlayer().getWorld().equals(getGameWorld()) || BallsOfSteel.get().getTeamsManager().getTeamForPlayer(ev.getPlayer()) != null)
        {
            // Mainly useful on the first join.
            BallsOfSteel.get().getScoreboardManager().setScoreboardForPlayer(ev.getPlayer());

            // The display name is reset when the player logs off.
            BallsOfSteel.get().getTeamsManager().colorizePlayer(ev.getPlayer());
        }
    }

    /**
     * Used to equip the players when they respawn.
     */
    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent ev)
    {
        if (isGameRunning() && ev.getPlayer().getWorld().equals(getGameWorld()))
        {
            final BoSTeam team = BallsOfSteel.get().getTeamsManager().getTeamForPlayer(ev.getPlayer());
            if (team != null)
            {
                // The player is in a team, aka a "playing player".
                equipPlayer(ev.getPlayer());

                RunTask.nextTick(new Runnable() {
                    @Override
                    public void run()
                    {
                        ev.getPlayer().teleport(team.getSpawnPoint());
                    }
                });
            }
        }
    }

    /**
     * Used to stop the game when the timer ends.
     */
    @EventHandler
    public void onTimerEnds(TimerEndsEvent ev)
    {
        if (ev.getTimer().getName().equals(TIMER_NAME))
        {
            stop(true);
        }
    }




    /**
     * The player's armor type
     */
    public enum PlayerArmorType
    {
        /**
         * No armor
         */
        NONE,

        /**
         * Leather armor
         */
        WEAK,

        /**
         * Chainmail armor
         */
        NORMAL,

        /**
         * Iron armor
         */
        STRONG,

        /**
         * Diamond armor
         */
        VERY_STRONG
    }
}
