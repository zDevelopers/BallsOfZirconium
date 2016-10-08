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
package eu.carrade.amaury.BallsOfSteel.generation.generation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationManager;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationProcess;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;


public class BallPopulator extends BlockPopulator
{
    private final GenerationManager generationManager = BallsOfSteel.get().getGenerationManager();

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        final Location base = new Location(world, chunk.getX() * 16 + random.nextInt(16), random.nextInt(127), chunk.getZ() * 16 + random.nextInt(16));
        if (!generationManager.isInsideBoundaries(base))
            return;

        final Integer distance = MapConfig.GENERATION.MAP.DISTANCE_BETWEEN_SPHERES.get();
        final EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession((com.sk89q.worldedit.world.World) BukkitUtil.getLocalWorld(world), -1);
        final EllipsoidRegion region = new EllipsoidRegion((com.sk89q.worldedit.world.World) BukkitUtil.getLocalWorld(world), BukkitUtil.toVector(base), new Vector(distance, distance, distance));

        session.setFastMode(false);


        // We check if any sphere is too close
        for (final Vector nearChunk : region.getChunkCubes())
        {
            final ChunkSnapshot snapshot = world.getChunkAt(nearChunk.getBlockX(), nearChunk.getBlockZ()).getChunkSnapshot(false, false, false);
            if (!snapshot.isSectionEmpty(Math.max(nearChunk.getBlockY(), 0))) // TODO wtf why is this sometimes negative?!
                return;
        }

        
        final GenerationProcess process = generationManager.getRandomGenerationProcess(random);

        if (generationManager.isLogged())
            PluginLogger.info("Generating sphere {0} at {1}", process.getName(), base.toVector());

        process.applyAt(base, random, session);

        // Ensures all the blocks are wrote, as the populator checks for other spheres existence to
        // generate (or not).
        session.flushQueue();
    }
}
