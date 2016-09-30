package eu.carrade.amaury.BallsOfSteel.game;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.Config;
import eu.carrade.amaury.BallsOfSteel.utils.BoSSound;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.utils.BoSUtils;
import eu.carrade.amaury.BallsOfSteel.timers.TimerEndsEvent;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;


public class BoSListener implements Listener
{
    private BallsOfSteel p = null;

    BoSSound soundCountIncrease = null;
    BoSSound soundCountDecrease = null;
    BoSSound soundChestLocked = null;

    public BoSListener(BallsOfSteel plugin)
    {
        this.p = plugin;

        soundCountIncrease = Config.DIAMONDS.SOUNDS.COUNT_INCREASE.get();
        soundCountDecrease = Config.DIAMONDS.SOUNDS.COUNT_DECREASE.get();
        soundChestLocked   = Config.DIAMONDS.SOUNDS.CHEST_LOCKED.get();
    }

    /**
     * Used to prevent the diamond chests to be destroyed.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent ev)
    {
        if (p.getGameManager().getTrackedChests().containsKey(ev.getBlock().getLocation()))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to prevent the diamond chests to be destroyed.
     */
    @EventHandler
    public void onBlockBurn(BlockBurnEvent ev)
    {
        if (p.getGameManager().getTrackedChests().containsKey(ev.getBlock().getLocation()))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to prevent player to access the chest of another team.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev)
    {
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!BoSUtils.isSharedChest(ev.getClickedBlock())) return;

        BoSTeam playerTeam = p.getTeamManager().getTeamForPlayer(ev.getPlayer());

        if (p.getGameManager().getTrackedChests().containsKey(ev.getClickedBlock().getLocation())
                && (playerTeam == null || (!ev.getClickedBlock().getLocation().equals(playerTeam.getChestLocation1()) && !ev.getClickedBlock().getLocation().equals(playerTeam.getChestLocation2()))))
        {
            ev.getPlayer().sendMessage(I.t("{ce}You cannot open the chests of another team."));
            ev.setCancelled(true);

            if (soundChestLocked != null) soundChestLocked.play(ev.getPlayer());
        }
    }

    /**
     * Used to prevent items to be put on or removed from the diamond chests
     * using hoppers.
     */
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent ev)
    {
        Location from = null, to = null;

        if (ev.getSource().getHolder() instanceof Chest)
        {
            from = ((Chest) ev.getSource().getHolder()).getLocation();
        }
        else if (ev.getSource().getHolder() instanceof DoubleChest)
        {
            from = ((DoubleChest) ev.getSource().getHolder()).getLocation();
        }

        if (ev.getDestination().getHolder() instanceof Chest)
        {
            to = ((Chest) ev.getDestination().getHolder()).getLocation();
        }
        else if (ev.getDestination().getHolder() instanceof DoubleChest)
        {
            to = ((DoubleChest) ev.getDestination().getHolder()).getLocation();
        }

        // The Location returned by the DoubleChest's holders is the
        // location of the middle of the chest.
        // These locations cannot be compared to the stored ones.
        // Because we store, for each double chest, the location of the two sides of the
        // chest, we just floor the X and Z coordinates.
        if (from != null)
        {
            from.setX(from.getBlockX());
            from.setZ(from.getBlockZ());
        }
        if (to != null)
        {
            to.setX(to.getBlockX());
            to.setZ(to.getBlockZ());
        }

        if (p.getGameManager().getTrackedChests().containsKey(from)
                || p.getGameManager().getTrackedChests().containsKey(to))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to update the diamonds count of the teams.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev)
    {
        if (p.getGameManager().isGameRunning())
        {
            InventoryHolder holder = ev.getInventory().getHolder();
            Location chestLocation = null;

            if (holder instanceof Chest)
            {
                chestLocation = ((Chest) holder).getLocation();
            }
            else if (holder instanceof DoubleChest)
            {
                chestLocation = ((DoubleChest) holder).getLocation();
            }

            // Same problem as above; see onInventoryMoveItem(InventoryMoveItemEvent).
            if (chestLocation != null)
            {
                chestLocation.setX(chestLocation.getBlockX());
                chestLocation.setZ(chestLocation.getBlockZ());
            }

            BoSTeam team = p.getGameManager().getTrackedChests().get(chestLocation);
            if (team != null)
            {
                int diamonds = 0;
                int oldDiamondsCount = team.getDiamondsCount();

                for (ItemStack item : ev.getInventory())
                {
                    if (item != null && item.getType() == Material.DIAMOND)
                    {
                        diamonds += item.getAmount();
                    }
                }

                team.setDiamondsCount(diamonds);
                p.getScoreboardManager().updateDiamondsScore(team);

                if (diamonds > oldDiamondsCount)
                {
                    if (soundCountIncrease != null) soundCountIncrease.broadcast(p.getGameManager().getGameWorld());
                }
                else if (diamonds < oldDiamondsCount)
                {
                    if (soundCountDecrease != null) soundCountDecrease.broadcast(p.getGameManager().getGameWorld());
                }
            }
        }
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
        if (!p.getGameManager().isGameRunning())
        {
            p.getBarAPIWrapper().setWaitingBar(ev.getPlayer());
        }
        else if (ev.getPlayer().getWorld().equals(p.getGameManager().getGameWorld()) || p.getTeamManager().getTeamForPlayer(ev.getPlayer()) != null)
        {
            // Mainly useful on the first join.
            p.getScoreboardManager().setScoreboardForPlayer(ev.getPlayer());

            // The display name is reset when the player logs off.
            p.getTeamManager().colorizePlayer(ev.getPlayer());
        }
    }

    /**
     * Used to equip the players when they respawn.
     */
    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent ev)
    {
        if (p.getGameManager().isGameRunning() && ev.getPlayer().getWorld().equals(p.getGameManager().getGameWorld()))
        {
            if (p.getTeamManager().getTeamForPlayer(ev.getPlayer()) != null)
            {
                // The player is in a team, aka a "playing player".
                p.getGameManager().equipPlayer(ev.getPlayer());
            }
        }
    }

    /**
     * Used to stop the game when the timer ends.
     */
    @EventHandler
    public void onTimerEnds(TimerEndsEvent ev)
    {
        if (ev.getTimer().getName().equals(BoSGameManager.TIMER_NAME))
        {
            p.getGameManager().stop(true);
        }
    }
}
