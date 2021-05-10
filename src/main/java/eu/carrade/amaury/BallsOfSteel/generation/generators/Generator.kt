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
package eu.carrade.amaury.BallsOfSteel.generation.generators

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.*
import eu.carrade.amaury.BallsOfSteel.generation.structures.StructureSubProcessor
import eu.carrade.amaury.BallsOfSteel.generation.utils.GenerationConfig
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.Location
import java.util.*

/**
 * A generator, used to generate a shape, paste a schematic...
 */
abstract class Generator(parameters: Map<*, *>) : StructureSubProcessor() {
    protected val enabled: Boolean
    protected val probability: Float
    protected val offset: BlockVector3
    protected var session: EditSession? = null
    protected var baseLocation: Location? = null
    protected var random: Random? = null

    /**
     * Generates a thing.
     *
     * @param session If WorldEdit is used, changes should be made in this edit
     * session.
     * @param base    The base vector for the generation.
     * @param random  A random numbers generator; it should be used as possible
     * so the generated thing is constant for the same world
     * seed.
     *
     * @return A [Region] containing all the changes, used after for
     * post-processing. `null` if nothing was generated (disabled,
     * probability failed, max blocks changed exception...).
     */
    fun generate(session: EditSession?, base: Location, random: Random): Region? {
        return if (!enabled || random.nextFloat() >= probability) null else try {
            this.session = session
            baseLocation = base.add(offset.x.toDouble(), offset.y.toDouble(), offset.z.toDouble())
            this.random = random
            doGenerate()
        } catch (e: WorldEditException) {
            PluginLogger.error("Cannot generate ''{0}'': too many blocks changed.", e, javaClass.simpleName)
            null
        }
    }

    /**
     * A name for the generator.
     *
     * @return The name.
     */
    override val name: String
        get() = doName().trim { it <= ' ' }

    /**
     * @return A list describing each setting of the generator.
     */
    override val settingsDescription: List<String>
        get() {
            val settingsDescription: MutableList<String> = ArrayList(doSettingsDescription())
            if (offset != BlockVector3.ZERO) settingsDescription.add(I.t("Offset: {0}", offset))
            if (probability < 1) settingsDescription.add(I.t("Probability: {0}", probability))
            return settingsDescription
        }

    /**
     * Generates a thing.
     *
     * @return A [Region] containing all the changes, used after for
     * post-processing.
     */
    @Throws(WorldEditException::class)
    protected abstract fun doGenerate(): Region?

    /**
     * A name for the generator.
     * @return The name.
     */
    abstract fun doName(): String

    /**
     * @return A list describing each setting of the generator.
     */
    abstract fun doSettingsDescription(): List<String>?

    /**
     * @return The base location as a WorldEdit vector.
     */
    protected fun baseVector(): BlockVector3 {
        return BukkitAdapter.asBlockVector(baseLocation)
    }

    /**
     * @return The base location as a Bukkit location.
     */
    protected fun baseLocation(): Location? {
        return baseLocation
    }

    companion object {
        @ConfigurationValueHandler
        @Throws(ConfigurationParseException::class)
        fun handleGenerator(map: Map<*, *>): Generator {
            return GenerationConfig.handleGenerationTool<Generator>(map, Generator::class.java)
        }
    }

    init {
        enabled = GenerationConfig.getValue<Boolean>(parameters, "enabled", Boolean::class.javaPrimitiveType, true)
        probability = GenerationConfig.getValue<Float>(parameters, "probability", Float::class.javaPrimitiveType, 1f)
        offset = GenerationConfig.getValue<BlockVector3>(parameters, "offset", BlockVector3::class.java, BlockVector3.ZERO)
    }
}