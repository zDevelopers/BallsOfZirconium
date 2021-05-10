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
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.*
import eu.carrade.amaury.ballsofzirconium.MapConfig
import eu.carrade.amaury.ballsofzirconium.generation.GenerationData
import eu.carrade.amaury.ballsofzirconium.generation.postProcessing.PostProcessor
import eu.carrade.amaury.ballsofzirconium.generation.utils.GenerationConfig
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.getRegionForClipboardPastedAt
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.loadSchematic
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.pasteClipboard
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.World
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Represents a static building generated at a given exact location to serve a specific purpose.
 */
class StaticBuilding(
    name: String,

    /**
     * The building paste location.
     */
    val pasteLocation: BlockVector3,

    /**
     * `true` if no sphere should be generated under this building.
     */
    nothingUnder: Boolean,

    /**
     * `true` if no sphere should be generated above this building.
     */
    nothingAbove: Boolean,

    /**
     * The schematic file.
     */
    private val schematicFile: File
) : Structure(name, name, true) {

    /**
     * @return The building data.
     */
    private val building: Clipboard

    /**
     * @return The region where nothing should be generated.
     */
    val privateRegion: Region

    init {
        building = loadSchematic(schematicFile)
        privateRegion = getRegionForClipboardPastedAt(building, pasteLocation)

        try {
            if (nothingAbove) privateRegion.expand(BlockVector3.at(0, 256, 0))
            if (nothingUnder) privateRegion.expand(BlockVector3.at(0, -256, 0))
        } catch (e: RegionOperationException) {
            PluginLogger.warning("Cannot expand private region for static building {0}, using base region.", e, name)
        }
    }

    /**
     * The list of post-processors to apply to this building when generated.
     */
    val postProcessors: MutableList<PostProcessor> = ArrayList()
        get() = Collections.unmodifiableList(field)

    fun build(world: World?, random: Random): Boolean {
        return try {
            val region: Region
            WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world)).use {
                    session -> region = pasteClipboard(session, building, pasteLocation, true)
            }

            for (processor in postProcessors) {
                try {
                    WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world)).use {
                            session -> processor.process(session, region, random)
                    }
                } catch (e: Throwable) {
                    PluginLogger.error("Exception occurred while executing post-processor {0} for static building {1}", e, processor.javaClass.name, name)
                }
            }

            if (world != null) {
                GenerationData.saveStructure(this, world, region)
            }

            true
        } catch (e: WorldEditException) {
            PluginLogger.error("Cannot build static building {0}: too many blocks changed.", e, name)
            false
        }
    }

    /**
     * Adds a post-processor.
     * @param processor The post-processor.
     */
    fun addPostProcessor(processor: PostProcessor) {
        postProcessors.add(processor)
    }

    /**
     * Adds multiple post-processors.
     * @param processors The processors.
     */
    fun addPostProcessors(processors: Collection<PostProcessor>?) {
        postProcessors.addAll(processors!!)
    }

    companion object {
        @ConfigurationValueHandler
        @Throws(ConfigurationParseException::class)
        fun handleStaticBuilding(map: Map<*, *>): StaticBuilding {
            val name: String = GenerationConfig.getValue(map, "name", String::class.java, "Unnamed static building")
            val schematicPath: String? = GenerationConfig.getValueOrNull(map, "schematic", String::class.java)
            val pasteLocation: BlockVector3? = GenerationConfig.getValueOrNull(map, "pasteAt", BlockVector3::class.java)
            val nothingAbove: Boolean = GenerationConfig.getValue(map, "nothingAbove", Boolean::class.javaPrimitiveType, false)
            val nothingUnder: Boolean = GenerationConfig.getValue(map, "nothingUnder", Boolean::class.javaPrimitiveType, false)

            if (schematicPath == null || pasteLocation == null) {
                throw ConfigurationParseException("Both schematic and pasteAt are required", map)
            }

            val schematicFile = File(MapConfig.MAP_SCHEMATICS_DIRECTORY, schematicPath)
            val building: StaticBuilding = try {
                StaticBuilding(name, pasteLocation, nothingUnder, nothingAbove, schematicFile)
            } catch (e: IOException) {
                throw ConfigurationParseException("Cannot load the schematic: I/O exception caught: " + e.message, schematicPath)
            }

            if (map.containsKey("postActions")) {
                building.addPostProcessors(ConfigurationValueHandlers.handleListValue(map["postActions"], PostProcessor::class.java))
            }

            return building
        }
    }
}
