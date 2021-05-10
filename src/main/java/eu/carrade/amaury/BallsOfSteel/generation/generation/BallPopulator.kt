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
package eu.carrade.amaury.BallsOfSteel.generation.generation

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.regions.EllipsoidRegion
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel
import eu.carrade.amaury.BallsOfSteel.MapConfig
import eu.carrade.amaury.BallsOfSteel.generation.utils.GeometryUtils.intersects
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import java.util.*
import kotlin.math.floor

class BallPopulator : BlockPopulator() {
    private val generationManager = BallsOfSteel.get().generationManager

    override fun populate(world: World, random: Random, chunk: Chunk) {
        val time = System.currentTimeMillis()
        val generationManager = BallsOfSteel.get().generationManager

        val yMin = generationManager.lowestCorner.blockY
        val yMax = generationManager.highestCorner.blockY

        val spheresFreeDistance = MapConfig.GENERATION.MAP.DISTANCE_BETWEEN_SPHERES.get()
        val buildingsFreeDistance = MapConfig.GENERATION.MAP.DISTANCE_BETWEEN_SPHERES_AND_STATIC_BUILDINGS.get()
        val spheresInThisChunk = random.nextInt(floor((yMax - yMin).toDouble() / (spheresFreeDistance.toDouble() * 3)).toInt()) + 1

        val worldEditWorld = BukkitAdapter.adapt(world)

        spheresLoop@ for (i in 0 until spheresInThisChunk) {
            // Determines where the sphere should be generated
            val generationWindowHeight = (floor((yMax - yMin).toDouble()) / spheresInThisChunk.toDouble()).toInt()
            val localYMin = i * generationWindowHeight
            val localYMax = localYMin + generationWindowHeight


            // Finds a random point in the chunk section
            val base = Location(world, ((chunk.x shl 4) + random.nextInt(16)).toDouble(), (random.nextInt(localYMax - localYMin) + localYMin).toDouble(), ((chunk.z shl 4) + random.nextInt(16)).toDouble())
            if (!this.generationManager.isInsideBoundaries(base)) continue
            val baseVector = BukkitAdapter.asBlockVector(base)


            // Proximity checks
            val noSpheresRegion = EllipsoidRegion(worldEditWorld, baseVector, Vector3.at(spheresFreeDistance.toDouble(), spheresFreeDistance.toDouble(), spheresFreeDistance.toDouble()))
            val noBuildingsRegion = EllipsoidRegion(worldEditWorld, baseVector, Vector3.at(buildingsFreeDistance.toDouble(), buildingsFreeDistance.toDouble(), buildingsFreeDistance.toDouble()))


            // We check if any sphere is too close
            for (nearChunk in noSpheresRegion.chunkCubes) {
                val snapshot = world.getChunkAt(nearChunk.blockX, nearChunk.blockZ).getChunkSnapshot(false, false, false)
                if (!snapshot.isSectionEmpty(nearChunk.blockY.coerceAtLeast(0).coerceAtMost(15))) {
                    continue@spheresLoop
                }
            }


            // We also check if this position is correct regarding the static buildings private zones
            for (privateBuildingRegion in generationManager.buildingsRegions) {
                if (intersects(privateBuildingRegion, noBuildingsRegion)) {
                    continue@spheresLoop
                }
            }


            // Generation
            val process = this.generationManager.getRandomSphere(random)
            process!!.applyAt(base, random)

            if (this.generationManager.logged) {
                PluginLogger.info("Sphere {0} generated at {1} in {2} ms", process.name, base.toVector(), System.currentTimeMillis() - time)
            }
        }
    }
}