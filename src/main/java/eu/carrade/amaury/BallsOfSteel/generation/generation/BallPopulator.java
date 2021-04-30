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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationManager;
import eu.carrade.amaury.BallsOfSteel.generation.structures.GeneratedSphere;
import eu.carrade.amaury.BallsOfSteel.generation.utils.GeometryUtils;
import fr.zcraft.quartzlib.tools.PluginLogger;
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
    public void populate(final World world, final Random random, final Chunk chunk)
    {
        final long time = System.currentTimeMillis();

        final GenerationManager generationManager = BallsOfSteel.get().getGenerationManager();

        final int yMin = generationManager.getLowestCorner().getBlockY();
        final int yMax = generationManager.getHighestCorner().getBlockY();

        final Integer spheresFreeDistance = MapConfig.GENERATION.MAP.DISTANCE_BETWEEN_SPHERES.get();
        final Integer buildingsFreeDistance = MapConfig.GENERATION.MAP.DISTANCE_BETWEEN_SPHERES_AND_STATIC_BUILDINGS.get();

        final int spheresInThisChunk = random.nextInt((int) Math.floor(((double) (yMax - yMin)) / ((double) spheresFreeDistance * 3))) + 1;

        final com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(world);

        spheresLoop:
        for (int i = 0; i < spheresInThisChunk; i++)
        {
            // Determines where the sphere should be generated
            final int generationWindowHeight = (int) (Math.floor(yMax - yMin) / ((double) spheresInThisChunk));
            final int localYMin = i * generationWindowHeight;
            final int localYMax = localYMin + generationWindowHeight;

            // Finds a random point in the chunk section
            final Location base = new Location(world, (chunk.getX() << 4) + random.nextInt(16), random.nextInt(localYMax - localYMin) + localYMin, (chunk.getZ() << 4) + random.nextInt(16));
            if (!this.generationManager.isInsideBoundaries(base)) continue;

            final BlockVector3 baseVector = BukkitAdapter.asBlockVector(base);


            // Proximity checks

            final EllipsoidRegion noSpheresRegion = new EllipsoidRegion(worldEditWorld, baseVector, Vector3.at(spheresFreeDistance, spheresFreeDistance, spheresFreeDistance));
            final EllipsoidRegion noBuildingsRegion = new EllipsoidRegion(worldEditWorld, baseVector, Vector3.at(buildingsFreeDistance, buildingsFreeDistance, buildingsFreeDistance));


            // We check if any sphere is too close
            for (final BlockVector3 nearChunk : noSpheresRegion.getChunkCubes())
            {
                final ChunkSnapshot snapshot = world.getChunkAt(nearChunk.getBlockX(), nearChunk.getBlockZ()).getChunkSnapshot(false, false, false);
                if (!snapshot.isSectionEmpty(Math.min(Math.max(nearChunk.getBlockY(), 0), 15)))
                {
                    continue spheresLoop;
                }
            }

            // We also check if this position is correct regarding the static buildings private zones
            for (CuboidRegion privateBuildingRegion : generationManager.getBuildingsPrivateRegions())
            {
                if (GeometryUtils.intersects(privateBuildingRegion, noBuildingsRegion))
                {
                    continue spheresLoop;
                }
            }


            // Generation
            final GeneratedSphere process = this.generationManager.getRandomSphere(random);
            process.applyAt(base, random);


            if (this.generationManager.isLogged())
            {
                PluginLogger.info("Sphere {0} generated at {1} in {2} ms", process.getName(), base.toVector(), System.currentTimeMillis() - time);
            }
        }
    }
}
