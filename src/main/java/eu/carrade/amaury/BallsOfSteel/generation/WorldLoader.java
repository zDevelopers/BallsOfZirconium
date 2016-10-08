/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.BallsOfSteel.generation;

import com.sk89q.worldedit.Vector2D;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;


public class WorldLoader extends BukkitRunnable
{
    private static final int CHUNKS_PER_RUN = 21;

    private final World world;
    private final CommandSender logsReceiver;

    private final Vector2D min;
    private final Vector2D max;

    private int chunksToProcess;
    private int chunksProcessed;
    private Vector2D current;

    private int lastPercentageDisplayed = -1;


    /**
     * @param world The world to load
     * @param logsReceiver A receiver for the percentage updates. Can be {@code null};
     * @param corner1 The first corner of the area to load.
     * @param corner2 The other corner of the area to load.
     */
    public WorldLoader(final World world, final CommandSender logsReceiver, final Vector2D corner1, final Vector2D corner2)
    {
        this.world = world;
        this.logsReceiver = logsReceiver;

        min = Vector2D.getMinimum(corner1, corner2);
        max = Vector2D.getMaximum(corner1, corner2);
    }

    /**
     * @return The loaded world.
     */
    public World getWorld()
    {
        return world;
    }

    /**
     * Starts the loading process.
     */
    public void start()
    {
        chunksToProcess = ((max.getBlockX() - min.getBlockX()) * (max.getBlockZ() - min.getBlockZ())) / 256;
        chunksProcessed = 0;

        current = min;

        RunTask.timer(this, 1l, 20l);
    }

    /**
     * Internal use—loads {@link #CHUNKS_PER_RUN} chunks.
     */
    @Override
    public void run()
    {
        for (int i = 0; i < CHUNKS_PER_RUN; i++)
        {
            world.getChunkAt(world.getBlockAt(current.getBlockX(), 64, current.getBlockX())).load(true);
            chunksProcessed++;

            int percentage = Math.min((int) (100 * ((double) chunksProcessed) / ((double) chunksToProcess)), 100);
            if (percentage > lastPercentageDisplayed + 5 && logsReceiver != null)
            {
                lastPercentageDisplayed = percentage;
                logsReceiver.sendMessage(I.t("{gray}Loading remaining chunks for world {0}... ({1}%)", world.getName(), percentage));
            }

            // Next chunk

            current = current.setZ(current.getZ() + 16);

            if (current.getZ() > max.getZ())
            {
                current = current.setZ(min.getZ()).setX(current.getX() + 16);
            }
            if (current.getX() > max.getX())
            {
                cancel();

                if (logsReceiver != null)
                    logsReceiver.sendMessage(I.t("{gray}World {0} fully loaded.", world.getName()));

                return;
            }
        }
    }
}