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
package eu.carrade.amaury.BallsOfSteel.generation.postProcessing

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.MaxChangedBlocksException
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.*
import eu.carrade.amaury.BallsOfSteel.generation.structures.StructureSubProcessor
import eu.carrade.amaury.BallsOfSteel.generation.utils.GenerationConfig
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.PluginLogger
import java.util.*

/**
 * A post-processor. This will work on a previously-generated region and apply some filters, replacements, things on it.
 */
abstract class PostProcessor(parameters: Map<*, *>) : StructureSubProcessor() {
    private val enabled: Boolean = GenerationConfig.getValue(parameters, "enabled", Boolean::class.javaPrimitiveType, true)
    private val probability: Float = GenerationConfig.getValue(parameters, "probability", Float::class.javaPrimitiveType, 1f)
    private var subRegionPos1: BlockVector3? = null
    private var subRegionPos2: BlockVector3? = null

    /**
     * If WorldEdit is used, changes must be made in this edit session.
     */
    protected var session: EditSession? = null

    /**
     * The region where the changes should be applied.
     */
    protected var region: Region? = null

    /**
     * A random numbers generator; it should be used as possible
     * so the generated thing is constant for the same world seed.
     */
    protected var random: Random? = null

    /**
     * Applies a post-processing to a region.
     *
     * @param session If WorldEdit is used, changes should be made in this edit
     * session.
     * @param region  The region where the post-processing should be applied.
     * @param random  A random numbers generator; it should be used as possible
     * so the generated thing is constant for the same world
     * seed.
     */
    fun process(session: EditSession?, region: Region, random: Random) {
        if (!enabled || random.nextFloat() >= probability) return
        try {
            this.session = session
            this.random = random

            // If this processing is only applied to a sub region...
            if (subRegionPos1 != null || subRegionPos2 != null) {
                val subRealPos1 = region.minimumPoint.add(if (subRegionPos1 != null) subRegionPos1 else BlockVector3.ZERO)
                val subRealPos2 = region.minimumPoint.add(if (subRegionPos2 != null) subRegionPos2 else BlockVector3.ZERO)
                this.region = CuboidRegion(
                        region.world,
                        region.minimumPoint.getMaximum(subRealPos1.getMinimum(subRealPos2)),
                        region.minimumPoint.getMinimum(subRealPos1.getMaximum(subRealPos2))
                )
            } else {
                this.region = region
            }
            doProcess()
        } catch (e: MaxChangedBlocksException) {
            PluginLogger.error("Cannot apply the {0} post-processor: too many blocks changed.", e, javaClass.simpleName)
        } catch (e: WorldEditException) {
            e.printStackTrace()
        }
    }

    /**
     * A name for the post-processor.
     *
     * @return The name.
     */
    override val name: String
        get() = doName().trim { it <= ' ' }

    /**
     * @return A list describing each setting of the post-processor.
     */
    override val settingsDescription: List<String>
        get() {
            val settingsDescription: MutableList<String> = ArrayList(doSettingsDescription())
            if (subRegionPos1 != null || subRegionPos2 != null) settingsDescription.add(I.t("Restricted to {0}, {1}", if (subRegionPos1 != null) subRegionPos1 else BlockVector3.ZERO, if (subRegionPos2 != null) subRegionPos2 else BlockVector3.ZERO))
            if (probability < 1) settingsDescription.add(I.t("Probability: {0}", probability))
            return settingsDescription
        }

    /**
     * Applies a post-processing to a region.
     */
    @Throws(WorldEditException::class)
    protected abstract fun doProcess()

    /**
     * A name for the post-processor.
     * @return The name.
     */
    abstract fun doName(): String

    /**
     * @return A list describing each setting of the structure.
     */
    abstract fun doSettingsDescription(): List<String>

    companion object {
        @ConfigurationValueHandler
        @Throws(ConfigurationParseException::class)
        fun handlePostProcessor(map: Map<*, *>): PostProcessor {
            return GenerationConfig.handleGenerationTool(map, PostProcessor::class.java)
        }
    }

    init {
        val regionParameters = GenerationConfig.getValueOrNull(parameters, "region", MutableMap::class.java)
        if (regionParameters != null) {
            subRegionPos1 = GenerationConfig.getValue(regionParameters, "sub_pos_1", BlockVector3::class.java, BlockVector3.ZERO)
            subRegionPos2 = GenerationConfig.getValue(regionParameters, "sub_pos_2", BlockVector3::class.java, BlockVector3.ZERO)
        } else {
            subRegionPos2 = null
            subRegionPos1 = subRegionPos2
        }
    }
}
