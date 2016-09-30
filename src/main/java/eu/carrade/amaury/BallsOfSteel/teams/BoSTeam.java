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

package eu.carrade.amaury.BallsOfSteel.teams;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.GameConfig;
import eu.carrade.amaury.BallsOfSteel.utils.BoSUtils;
import eu.carrade.amaury.BallsOfSteel.utils.PitchedVector;
import eu.carrade.amaury.BallsOfSteel.utils.StringToChatColor;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class BoSTeam
{
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


    public BoSTeam(String name, ChatColor color)
    {
        Validate.notNull(name, "The name cannot be null.");
        Validate.notNull(BallsOfSteel.get(), "The plugin cannot be null.");

        this.name = name;
        this.color = color;

        // We use a random internal name because the name of a team, in Minecraft vanilla, is limited
        // (16 characters max).
        final Random rand = new Random();
        this.internalName = String.valueOf(rand.nextInt(99999999)) + String.valueOf(rand.nextInt(99999999));

        if (this.color != null)
        {
            this.displayName = color + name + ChatColor.RESET;
        }
        else
        {
            this.displayName = name;
        }

        final Scoreboard sb = BallsOfSteel.get().getScoreboardManager().getScoreboard();
        final Team t = sb.registerNewTeam(this.internalName);

        if (this.color != null)
        {
            t.setPrefix(this.color.toString());
        }

        t.setCanSeeFriendlyInvisibles(GameConfig.TEAMS_OPTIONS.CAN_SEE_FRIENDLY_INVISIBLES.get());
        t.setAllowFriendlyFire(GameConfig.TEAMS_OPTIONS.ALLOW_FRIENDLY_FIRE.get());
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

            BallsOfSteel.get().getGameManager().updateTrackedChests();

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

            BallsOfSteel.get().getGameManager().updateTrackedChests();
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
     */
    public int getDiamondsCount()
    {
        return diamonds;
    }


    /**
     * Returns the name of the team.
     *
     * Can include spaces.
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
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns the players inside this team.
     */
    public ArrayList<OfflinePlayer> getPlayers()
    {
        ArrayList<OfflinePlayer> playersList = new ArrayList<OfflinePlayer>();

        for (UUID id : players)
        {
            Player player = BallsOfSteel.get().getServer().getPlayer(id);
            if (player != null)
            {
                playersList.add(player);
            }
            else
            {
                playersList.add(BallsOfSteel.get().getServer().getOfflinePlayer(id));
            }
        }

        return playersList;
    }

    /**
     * Returns the online players inside this team.
     */
    public ArrayList<Player> getOnlinePlayers()
    {
        ArrayList<Player> playersList = new ArrayList<>();

        for (UUID id : players)
        {
            Player player = BallsOfSteel.get().getServer().getPlayer(id);
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
        BallsOfSteel.get().getScoreboardManager().getScoreboard().getTeam(this.internalName).addPlayer(player);

        BallsOfSteel.get().getTeamsManager().colorizePlayer(player);
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
     */
    private void unregisterPlayer(OfflinePlayer player)
    {
        BallsOfSteel.get().getScoreboardManager().getScoreboard().getTeam(this.internalName).removePlayer(player);
        BallsOfSteel.get().getTeamsManager().colorizePlayer(player);
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
            Player player = BallsOfSteel.get().getServer().getPlayer(id);

            player.sendMessage(I.t("{darkaqua}You are no longer part of the {0}{darkaqua} team.", getDisplayName()));
            unregisterPlayer(player);
        }

        this.players.clear();

        // Then the scoreboard team is deleted.
        BallsOfSteel.get().getScoreboardManager().getScoreboard().getTeam(this.internalName).unregister();

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
     */
    public void teleportTo(Location lo)
    {
        Validate.notNull(lo, "The location cannot be null.");

        for (UUID id : players)
        {
            BallsOfSteel.get().getServer().getPlayer(id).teleport(lo);
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


    @ConfigurationValueHandler
    public static ChatColor handleChatColor(String color) throws ConfigurationParseException
    {
        final ChatColor chatColor = StringToChatColor.getChatColorByName(color);
        if (chatColor == null) throw new ConfigurationParseException("Invalid chat color", color);

        return chatColor;
    }

    @ConfigurationValueHandler
    public static BoSTeam handleTeam(Map map) throws ConfigurationParseException
    {
        final World world = BallsOfSteel.get().getGameManager().getGameWorld();


        if (!map.containsKey("name"))
            throw new ConfigurationParseException("Team name required", map);

        if (!map.containsKey("color"))
            throw new ConfigurationParseException("Team color required", map);

        if (!map.containsKey("chest"))
            throw new ConfigurationParseException("Team chest required", map);

        if (!map.containsKey("spawn"))
            throw new ConfigurationParseException("Team spawn required", map);


        final BoSTeam team = new BoSTeam(map.get("name").toString(), ConfigurationValueHandlers.handleValue(map.get("color").toString(), ChatColor.class));

        team.setSpawnPoint(ConfigurationValueHandlers.handleValue(map.get("spawn"), PitchedVector.class).toLocation(world));

        try
        {
            team.setChest(ConfigurationValueHandlers.handleValue(map.get("chest"), Vector.class).toLocation(world));
        }
        catch (IllegalArgumentException e)
        {
            throw new ConfigurationParseException("Invalid chest for the team " + team.getName() + ": " + e.getMessage(), map.get("chest").toString());
        }


        return team;
    }
}
