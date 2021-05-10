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
package eu.carrade.amaury.ballsofzirconium.generation.structures

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.*
import eu.carrade.amaury.ballsofzirconium.generation.GenerationData
import eu.carrade.amaury.ballsofzirconium.generation.generators.Generator
import eu.carrade.amaury.ballsofzirconium.generation.postProcessing.PostProcessor
import eu.carrade.amaury.ballsofzirconium.generation.utils.GenerationConfig
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.Location
import java.util.*

/**
 * Represents a generation process to be applied by a populator somewhere.
 */
class GeneratedSphere(name: String, display: String?, enabled: Boolean = true) : Structure(name, display, enabled) {
    val generators: MutableList<Generator> = ArrayList()
        get() = Collections.unmodifiableList(field)

    val postProcessors: MutableList<PostProcessor> = ArrayList()
        get() = Collections.unmodifiableList(field)

    fun addGenerator(generator: Generator) {
        generators.add(generator)
    }

    fun addPostProcessor(processor: PostProcessor) {
        postProcessors.add(processor)
    }

    fun addGenerators(generators: Collection<Generator>) {
        this.generators.addAll(generators)
    }

    fun addPostProcessors(processors: Collection<PostProcessor>) {
        postProcessors.addAll(processors)
    }

    /**
     * Applies this generation process to the given location.
     *
     * @param location The base location for generation.
     * @param random   A source of randomness.
     *
     * @return A region containing the modified blocks.
     */
    fun applyAt(location: Location, random: Random): Region {
        val affectedRegions: MutableList<Region> = ArrayList()
        val world = BukkitAdapter.adapt(location.world)

        WorldEdit.getInstance().newEditSession(world).use { session ->
            for (generator in generators) {
                try {
                    val affected = generator.generate(session, location.clone(), random)
                    if (affected != null) affectedRegions.add(affected)
                } catch (e: Throwable) {
                    PluginLogger.error("Exception occurred while executing generator {0}", e, generator.javaClass.name)
                }
            }
        }

        val globallyAffectedRegion = RegionIntersection(affectedRegions)
        for (processor in postProcessors) {
            try {
                WorldEdit.getInstance().newEditSession(world).use { session -> processor.process(session, globallyAffectedRegion, random) }
            } catch (e: Throwable) {
                PluginLogger.error("Exception occurred while executing post-processor {0} on generation process {1}", e, processor.javaClass.name, name)
            }
        }

        location.world?.let { GenerationData.saveStructure(this, it, globallyAffectedRegion) }
        return globallyAffectedRegion
    }

    companion object {
        @ConfigurationValueHandler
        @Throws(ConfigurationParseException::class)
        fun handleGeneratedSphere(map: Map<*, *>): GeneratedSphere {
            val name: String = GenerationConfig.getValue(map, "name", String::class.java, "Unnamed sphere")
            val display: String = GenerationConfig.getValue(map, "display", String::class.java, name)
            val enabled: Boolean = GenerationConfig.getValue(map, "enabled", Boolean::class.javaPrimitiveType, true)
            val sphere = GeneratedSphere(name, display, enabled)

            if (map.containsKey("rules")) {
                sphere.addGenerators(ConfigurationValueHandlers.handleListValue(map["rules"], Generator::class.java))
            }

            if (map.containsKey("postActions")) {
                sphere.addPostProcessors(ConfigurationValueHandlers.handleListValue(map["postActions"], PostProcessor::class.java))
            }

            return sphere
        }
    }
}
