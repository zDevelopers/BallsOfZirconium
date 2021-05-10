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
package eu.carrade.amaury.ballsofzirconium.generation.utils

import com.sk89q.worldedit.math.BlockVector2
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.runners.RunTask
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable

class WorldLoader(
        /**
         * @return The loaded world.
         */
        val world: World,

        private val logsReceiver: CommandSender?,
        corner1: BlockVector2,
        corner2: BlockVector2
) : BukkitRunnable() {

    companion object {
        private const val CHUNKS_PER_RUN = 21
    }

    private val min: BlockVector2 = corner1.getMinimum(corner2)
    private val max: BlockVector2 = corner1.getMaximum(corner2)
    private var chunksToProcess = 0
    private var chunksProcessed = 0
    private var current: BlockVector2? = null
    private var lastPercentageDisplayed = -1

    /**
     * Starts the loading process.
     */
    fun start() {
        chunksToProcess = (max.blockX - min.blockX) * (max.blockZ - min.blockZ) / 256
        chunksProcessed = 0
        current = min
        RunTask.timer(this, 1L, 20L)
    }

    /**
     * Internal use—loads [.CHUNKS_PER_RUN] chunks.
     */
    override fun run() {
        for (i in 0 until CHUNKS_PER_RUN) {
            world.loadChunk(current!!.blockX shr 4, current!!.blockX shr 4)
            chunksProcessed++

            val percentage = (100 * chunksProcessed.toDouble() / chunksToProcess.toDouble()).toInt().coerceAtMost(100)

            if (percentage > lastPercentageDisplayed + 5 && logsReceiver != null) {
                lastPercentageDisplayed = percentage
                logsReceiver.sendMessage(I.t("{gray}Loading remaining chunks for world {0}... ({1}%)", world.name, percentage))
            }

            // Next chunk
            current = current!!.add(0, 16)

            current?.let {
                if (it.z > max.z) {
                    current = BlockVector2.at(it.x + 16, min.z)
                }

                if (it.x > max.x) {
                    cancel()
                    world.keepSpawnInMemory = true // Ensures the spawn is kept in memory now.
                    logsReceiver?.sendMessage(I.t("{gray}World {0} fully loaded.", world.name))
                    return
                }
            }
        }
    }
}