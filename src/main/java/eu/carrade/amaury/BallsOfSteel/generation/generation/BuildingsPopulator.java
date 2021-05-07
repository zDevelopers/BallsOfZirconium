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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationManager;
import eu.carrade.amaury.BallsOfSteel.generation.structures.StaticBuilding;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;


public class BuildingsPopulator extends BlockPopulator
{
    private final GenerationManager generationManager = BallsOfSteel.get().getGenerationManager();

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk)
    {
        final Region chunkRegion = new CuboidRegion(
                BlockVector3.at(chunk.getX() << 4, 0, chunk.getZ() << 4),
                BlockVector3.at((chunk.getX() << 4) + 15, 256, (chunk.getZ() << 4) + 15)
        );

        for (final StaticBuilding building : generationManager.getBuildings())
        {
            if (chunkRegion.contains(building.getPasteLocation()))
            {
                final long time = System.currentTimeMillis();

                building.build(world, random);

                if (generationManager.isLogged())
                    PluginLogger.info("Build {0} generated at {1} in {2} ms", building.getName(), building.getPasteLocation(), System.currentTimeMillis() - time);
            }
        }
    }
}
