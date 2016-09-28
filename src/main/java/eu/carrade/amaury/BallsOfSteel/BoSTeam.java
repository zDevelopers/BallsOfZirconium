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

import fr.zcraft.zlib.components.i18n.I;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;


public class BoSTeam
{
    private BallsOfSteel plugin = null;

    private String name = null;
    private String internalName = null;
    private String displayName = null;
    private ChatColor color = null;

    private Location spawn = null;
    private InventoryHolder chest = null;
    private Location chestLocation1 = null;
    private Location chestLocation2 = null; // If the chest is a double chest

    private int diamonds = 0;

    private ArrayList<UUID> players = new ArrayList<>();


    public BoSTeam(String name, ChatColor color, BallsOfSteel plugin)
    {
        Validate.notNull(name, "The name cannot be null.");
        Validate.notNull(plugin, "The plugin cannot be null.");

        this.plugin = plugin;

        this.name = name;
        this.color = color;

        // We use a random internal name because the name of a team, in Minecraft vanilla, is limited
        // (16 characters max).
        Random rand = new Random();
        this.internalName = String.valueOf(rand.nextInt(99999999)) + String.valueOf(rand.nextInt(99999999));

        if (this.color != null)
        {
            this.displayName = color + name + ChatColor.RESET;
        }
        else
        {
            this.displayName = name;
        }

        Scoreboard sb = this.plugin.getScoreboardManager().getScoreboard();

        sb.registerNewTeam(this.internalName);
        Team t = sb.getTeam(this.internalName);

        if (this.color != null)
        {
            t.setPrefix(this.color.toString());
        }

        t.setCanSeeFriendlyInvisibles(plugin.getConfig().getBoolean("teams-options.canSeeFriendlyInvisibles", true));
        t.setAllowFriendlyFire(plugin.getConfig().getBoolean("teams-options.allowFriendlyFire", true));
    }

    /**
     * Sets the spawn point of this team.
     *
     * @param spawnPoint The spawn point.
     */
    public void setSpawnPoint(Location spawnPoint)
    {
        this.spawn = spawnPoint;
    }

    /**
     * Returns the spawn point of this team.
     *
     * @return The spawn point.
     */
    public Location getSpawnPoint()
    {
        return spawn;
    }

    /**
     * Sets the chest of this team, where diamonds are stored.
     *
     * @param chestLocation The chest. {@code Null} to unset the chest.
     *
     * @throws IllegalArgumentException If the block at the given location is not a chest.
     */
    public void setChest(Location chestLocation)
    {

        if (chestLocation == null)
        {
            chest = null;
            chestLocation1 = null;
            chestLocation2 = null;

            plugin.getGameManager().updateTrackedChests();

            return;
        }

        Block block = chestLocation.getWorld().getBlockAt(chestLocation);

        if (BoSUtils.isSharedChest(block))
        {
            chest = ((Chest) block.getState()).getInventory().getHolder();

            chestLocation1 = chestLocation.clone();

            if (chest instanceof DoubleChest)
            {
                // Looking for the second part of the chest
                World w = chestLocation.getWorld();

                Block[] possibilities = new Block[4];
                possibilities[0] = w.getBlockAt(chestLocation.clone().add(1, 0, 0));
                possibilities[1] = w.getBlockAt(chestLocation.clone().add(-1, 0, 0));
                possibilities[2] = w.getBlockAt(chestLocation.clone().add(0, 0, 1));
                possibilities[3] = w.getBlockAt(chestLocation.clone().add(0, 0, -1));

                Material originalType = block.getType();

                for (Block possibility : possibilities)
                {
                    if (BoSUtils.isSharedChest(possibility) && possibility.getType() == originalType)
                    {
                        chestLocation2 = possibility.getLocation();
                        break;
                    }
                }
            }
            else
            {
                chestLocation2 = null;
            }

            plugin.getGameManager().updateTrackedChests();
        }
        else
        {
            throw new IllegalArgumentException("The block at " + chestLocation + "is not a chest.");
        }
    }

    /**
     * Returns the inventory holder of the chest of this team.
     *
     * @return The inventory holder.
     */
    public InventoryHolder getChest()
    {
        return chest;
    }

    /**
     * Returns the location of the first part of the private chest of this team.
     * <p>
     * This is only {@code null} if there isn't any chest set.
     *
     * @return The location of the first part of the private chest.
     */
    public Location getChestLocation1()
    {
        return chestLocation1;
    }

    /**
     * Returns the location of the second part of the private chest of this team.
     * <p>
     * This is only not {@code null} if there is a chest set, and if this chest is a double chest.
     *
     * @return The location of the second part of the private chest.
     */
    public Location getChestLocation2()
    {
        return chestLocation2;
    }

    /**
     * Sets the number of diamonds owned by this team.
     *
     * @param diamonds The count.
     */
    public void setDiamondsCount(int diamonds)
    {
        this.diamonds = diamonds;
    }

    /**
     * Returns the number of diamonds owned by this team.
     *
     * @return
     */
    public int getDiamondsCount()
    {
        return diamonds;
    }


    /**
     * Returns the name of the team.
     *
     * Can include spaces.
     *
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the display name of the team.
     *
     * This name is:
     *  - if the team is uncolored, the name of the team;
     *  - else, the name of the team with:
     *     - before, the color of the team;
     *     - after, the "reset" formatting mark (§r).
     *
     * @return
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns the players inside this team.
     *
     * @return
     */
    public ArrayList<OfflinePlayer> getPlayers()
    {
        ArrayList<OfflinePlayer> playersList = new ArrayList<OfflinePlayer>();

        for (UUID id : players)
        {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null)
            {
                playersList.add(player);
            }
            else
            {
                playersList.add(plugin.getServer().getOfflinePlayer(id));
            }
        }

        return playersList;
    }

    /**
     * Returns the online players inside this team.
     *
     * @return
     */
    public ArrayList<Player> getOnlinePlayers()
    {
        ArrayList<Player> playersList = new ArrayList<Player>();

        for (UUID id : players)
        {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null)
            {
                playersList.add(player);
            }
        }

        return playersList;
    }

    /**
     * Adds a player inside this team.
     *
     * @param player The player to add.
     */
    public void addPlayer(Player player)
    {
        Validate.notNull(player, "The player cannot be null.");

        players.add(player.getUniqueId());
        plugin.getScoreboardManager().getScoreboard().getTeam(this.internalName).addPlayer(player);

        plugin.getTeamManager().colorizePlayer(player);
    }

    /**
     * Removes a player from this team.
     *
     * Nothing is done if the player wasn't in this team.
     *
     * @param player The player to remove.
     */
    public void removePlayer(OfflinePlayer player)
    {
        Validate.notNull(player, "The player cannot be null.");

        players.remove(player.getUniqueId());
        unregisterPlayer(player);
    }

    /**
     * Unregisters a player from the scoreboard and uncolorizes the pseudo.
     *
     * Internal use, avoids a ConcurrentModificationException in this.deleteTeam()
     * (this.players is listed and emptied simultaneously, else).
     *
     * @param player
     */
    private void unregisterPlayer(OfflinePlayer player)
    {
        plugin.getScoreboardManager().getScoreboard().getTeam(this.internalName).removePlayer(player);
        plugin.getTeamManager().colorizePlayer(player);
    }

    /**
     * Deletes this team.
     *
     * The players inside the team are left without any team.
     */
    public void deleteTeam()
    {
        // We removes the players from the team (scoreboard team too)
        for (UUID id : players)
        {
            Player player = plugin.getServer().getPlayer(id);

            player.sendMessage(I.t("{darkaqua}You are no longer part of the {0}{darkaqua} team.", getDisplayName()));
            unregisterPlayer(player);
        }

        this.players.clear();

        // Then the scoreboard team is deleted.
        plugin.getScoreboardManager().getScoreboard().getTeam(this.internalName).unregister();

    }

    /**
     * Returns true if the given player is in this team.
     *
     * @param player The player to check.
     * @return true if the given player is in this team.
     */
    public boolean containsPlayer(Player player)
    {
        Validate.notNull(player, "The player cannot be null.");

        for (UUID playerInTeamID : players)
        {
            if (playerInTeamID.equals(player.getUniqueId()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the player with the given UUID is in this team.
     *
     * @param id The UUID of the player to check.
     * @return true if the given player is in this team.
     */
    public boolean containsPlayer(UUID id)
    {
        Validate.notNull(id, "The player cannot be null.");

        return players.contains(id);
    }

    /**
     * Teleports the entire team to the given location.
     *
     * @param lo
     */
    public void teleportTo(Location lo)
    {
        Validate.notNull(lo, "The location cannot be null.");

        for (UUID id : players)
        {
            plugin.getServer().getPlayer(id).teleport(lo);
        }
    }

    /**
     * Returns the color of the team.
     */
    public ChatColor getColor()
    {
        return color;
    }


    @Override
    public boolean equals(Object otherTeam)
    {
        return otherTeam instanceof BoSTeam && ((BoSTeam) otherTeam).getName().equals(this.getName());
    }
}
