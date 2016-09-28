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

package eu.carrade.amaury.BallsOfSteel.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;


public class BoSUtils
{
    /**
     * Extracts a string from a list of arguments, starting at the given index.
     *
     * @param args The raw arguments.
     * @param startIndex The index of the first item in the returned string (first argument given: 0).
     *
     * @return The extracted string.
     *
     * @throws IllegalArgumentException if the index of the first element is out of the bounds of the arguments' list.
     */
    public static String getStringFromCommandArguments(String[] args, int startIndex)
    {
        if (args.length < startIndex)
        {
            throw new IllegalArgumentException("The index of the first element is out of the bounds of the arguments' list.");
        }

        String text = "";

        for (int index = startIndex; index < args.length; index++)
        {
            if (index < args.length - 1)
            {
                text += args[index] + " ";
            }
            else
            {
                text += args[index];
            }
        }

        return text;
    }

    /**
     * Converts a string to a number of seconds.
     * <p>
     * Format:
     * <ul>
     *    <li><tt>mm</tt> – number of minutes;</li>
     *    <li><tt>mm:ss</tt> – minutes and seconds;</li>
     *    <li><tt>hh:mm:ss</tt> – hours, minutes and seconds.</li>
     * </ul>
     *
     *
     * @param text The text to be converted.
     * @return The number of seconds represented by this string.
     *
     * @throws IllegalArgumentException if the text is not formatted as above.
     * @throws NumberFormatException if the text between the colons cannot be converted in integers.
     */
    public static int string2Time(String text)
    {
        String[] split = text.split(":");

        switch (split.length)
        {
            // "mm"
            case 1:
                return Integer.valueOf(split[0]) * 60;

            // "mm:ss"
            case 2:
                return Integer.valueOf(split[0]) * 60 + Integer.valueOf(split[1]);

            // "hh:mm:ss"
            case 3:
                return Integer.valueOf(split[0]) * 3600 + Integer.valueOf(split[1]) * 60 + Integer.valueOf(split[2]);

            default:
                throw new IllegalArgumentException("Badly formatted string in string2time, formats allowed are mm, mm:ss or hh:mm:ss.");
        }
    }


    /**
     * Broadcasts a message to a world.
     *
     * @param world The world where the message will be broadcasted.
     * @param message The message to broadcast.
     */
    public static void worldBroadcast(World world, String message)
    {
        for (Player player : world.getPlayers())
        {
            player.sendMessage(message);
        }

        Bukkit.getConsoleSender().sendMessage("[" + world.getName() + "] " + message);
    }


    /**
     * Finds a safe spot where teleport the player, and teleport the player to that spot.
     * If a spot is not found, the player is not teleported, except if the force arg is set to true.
     *
     * Inspiration took in the WorldBorder plugin.
     *
     * @param player
     * @param location
     * @param force If true the player will be teleported to the exact given location if there is no safe spot.
     * @return true if the player was effectively teleported.
     */
    public static boolean safeTP(Player player, Location location, boolean force)
    {
        // If the target is safe or forced, let's go
        if (force || isSafeSpot(location))
        {
            player.teleport(location);
            return true;
        }

        final Location safeSpot = findSafeSpot(location);

        // A spot was found, let's teleport.
        if (safeSpot != null)
        {
            player.teleport(safeSpot);
            return true;
        }
        // No spot found; the teleportation is cancelled.
        else
        {
            return false;
        }
    }

    public static boolean safeTP(Player player, Location location)
    {
        return safeTP(player, location, false);
    }

    /**
     * Finds a safe spot in the given location.
     *
     * The spot is in the same X;Z coordinates.
     *
     * @param location The location where to find a safe spot.
     * @return A Location object representing the safe spot, or null if no safe spot is available.
     */
    public static Location findSafeSpot(Location location)
    {
        // We try to find a spot above or below the target (this is probably the good solution, because
        // if the spot is obstructed, because this is mainly used to teleport players back after their
        // death, the cause is likely to be a falling block or an arrow shot during a fall).

        Location safeSpot = null;
        final int maxHeight = (location.getWorld().getEnvironment() == World.Environment.NETHER) ? 125 : location.getWorld().getMaxHeight() - 2;

        for (int yGrow = location.getBlockY(), yDecr = location.getBlockY(); yDecr >= 1 || yGrow <= maxHeight; yDecr--, yGrow++)
        {
            // Above?
            if (yGrow < maxHeight)
            {
                Location spot = new Location(location.getWorld(), location.getBlockX(), yGrow, location.getBlockZ());
                if (isSafeSpot(spot))
                {
                    safeSpot = spot;
                    break;
                }
            }

            // Below?
            if (yDecr > 1 && yDecr != yGrow)
            {
                Location spot = new Location(location.getWorld(), location.getX(), yDecr, location.getZ());
                if (isSafeSpot(spot))
                {
                    safeSpot = spot;
                    break;
                }
            }
        }

        // A spot was found, we changes the pitch & yaw according to the original location.
        if (safeSpot != null)
        {
            safeSpot.setPitch(location.getPitch());
            safeSpot.setYaw(location.getYaw());
        }

        return safeSpot;
    }

    /**
     * Checks if a given location is safe.
     * A safe location is a location with two breathable blocks (aka transparent block or water)
     * over something solid (or water).
     *
     * @param location
     * @return true if the location is safe.
     */
    public static boolean isSafeSpot(Location location)
    {
        final Block blockCenter = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final Block blockAbove  = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        final Block blockBelow  = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());

        // Two breathable blocks, and the block below is solid, or liquid (but not lava)
        return (blockCenter.getType().isTransparent() || (blockCenter.isLiquid() && !blockCenter.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA)))
                && (blockAbove.getType().isTransparent() || (blockAbove.isLiquid() && !blockAbove.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA)))
                && (blockBelow.getType().isSolid() || blockBelow.getType().equals(Material.WATER) || blockBelow.getType().equals(Material.STATIONARY_WATER));

    }

    /**
     * Returns {@code true} if the given block is a shared chest (i.e. a chest or a trapped chest, not
     * an ender chest).
     *
     * @param block The block.
     * @return {@code True} if this block is a shared chest.
     */
    public static boolean isSharedChest(Block block)
    {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST;
    }

    /**
     * Spawns a random firework at the given location.
     *
     * Please note: because the power of a firework is an integer, the min/max heights
     * are with a precision of ±5 blocks.
     *
     * @param location The location where the firework will be spawned.
     * @param heightMin The minimal height of the explosion.
     * @param heightMax The maximal height of the explosion.
     *
     * @return The random firework generated.
     */
    public static Firework generateRandomFirework(Location location, int heightMin, int heightMax)
    {
        Random rand = new Random();

        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        int effectsCount = rand.nextInt(3) + 1;

        for (int i = 0; i < effectsCount; i++)
        {
            meta.addEffect(generateRandomFireworkEffect());
        }

        // One level of power is half a second of flight time.
        // In half a second, a firework fly ~5 blocks.
        // So, one level of power = ~5 blocks.
        meta.setPower((int) Math.min(Math.floor((heightMin / 5) + rand.nextInt(heightMax / 5)), 128D));

        firework.setFireworkMeta(meta);

        return firework;
    }

    /**
     * Generates a random firework effect.
     *
     * @return The firework effect.
     */
    private static FireworkEffect generateRandomFireworkEffect()
    {
        Random rand = new Random();
        Builder fireworkBuilder = FireworkEffect.builder();

        int colorCount = rand.nextInt(3) + 1;
        int trailCount = rand.nextInt(3) + 1;

        fireworkBuilder.flicker(rand.nextInt(3) == 1);
        fireworkBuilder.trail(rand.nextInt(3) == 1);

        for (int i = 0; i < colorCount; i++)
        {
            fireworkBuilder.withColor(generateRandomColor());
        }

        for (int i = 0; i < trailCount; i++)
        {
            fireworkBuilder.withFade(generateRandomColor());
        }

        // Random shape
        int shape = rand.nextInt(5);
        switch (shape)
        {
            case 0:
                fireworkBuilder.with(FireworkEffect.Type.BALL);
                break;
            case 1:
                fireworkBuilder.with(FireworkEffect.Type.BALL_LARGE);
                break;
            case 2:
                fireworkBuilder.with(FireworkEffect.Type.BURST);
                break;
            case 3:
                fireworkBuilder.with(FireworkEffect.Type.CREEPER);
                break;
            case 4:
                fireworkBuilder.with(FireworkEffect.Type.STAR);
                break;
        }

        return fireworkBuilder.build();
    }

    /**
     * Generates a random color.
     *
     * @return The color.
     */
    private static Color generateRandomColor()
    {
        Random rand = new Random();
        return Color.fromBGR(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }
}
