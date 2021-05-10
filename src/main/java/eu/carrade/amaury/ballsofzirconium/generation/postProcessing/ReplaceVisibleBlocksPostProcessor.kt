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
package eu.carrade.amaury.ballsofzirconium.generation.postProcessing

import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.function.RegionFunction
import com.sk89q.worldedit.function.RegionMaskingFilter
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.function.visitor.RegionVisitor
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.registry.BundledBlockData
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.parseMask
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.parsePattern
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.PluginLogger
import java.util.*

class ReplaceVisibleBlocksPostProcessor(parameters: Map<*, *>) : ReplacePostProcessor(parameters) {
    @Throws(WorldEditException::class)
    override fun doProcess() {
        val replacementPattern = parsePattern(session!!.world, toPattern)
        val replaceBlocksIfVisible = RegionFunction { position: BlockVector3 ->
            val blockData = BundledBlockData.getInstance()
            val neighbors: MutableSet<BlockVector3> = HashSet()
            neighbors.add(position.add(1, 0, 0))
            neighbors.add(position.add(0, 1, 0))
            neighbors.add(position.add(0, 0, 1))
            neighbors.add(position.add(-1, 0, 0))
            neighbors.add(position.add(0, -1, 0))
            neighbors.add(position.add(0, 0, -1))
            var isBlockVisible = false
            for (neighbor in neighbors) {
                val blockNeighbor = session!!.getBlock(neighbor)
                val material = blockData.getMaterialById(blockNeighbor.blockType.id) ?: continue
                if (material.isAir || !material.isOpaque || material.isTranslucent) {
                    isBlockVisible = true
                    break
                }
            }
            if (isBlockVisible) {
                session!!.setBlock(position, replacementPattern.applyBlock(position))
            }
            isBlockVisible
        }
        val blocksMask = parseMask(session!!.world, fromMask, session)
        val blocksVisitor = RegionVisitor(region, RegionMaskingFilter(blocksMask, replaceBlocksIfVisible))
        try {
            Operations.complete(blocksVisitor)
        } catch (e: WorldEditException) {
            PluginLogger.info("Unable to replace visible blocks", e)
        }
    }

    override fun doName(): String {
        return I.t("Visible blocks replacement")
    }

    override fun doSettingsDescription(): List<String> {
        return Arrays.asList(
                I.t("From: {0}", fromMask),
                I.t("To: {0}", toPattern)
        )
    }
}