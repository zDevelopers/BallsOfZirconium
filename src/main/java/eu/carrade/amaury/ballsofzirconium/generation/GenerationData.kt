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
package eu.carrade.amaury.ballsofzirconium.generation

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.*
import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium
import eu.carrade.amaury.ballsofzirconium.generation.structures.GeneratedSphere
import eu.carrade.amaury.ballsofzirconium.generation.structures.StaticBuilding
import eu.carrade.amaury.ballsofzirconium.generation.structures.Structure
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.asBlockVector
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers
import fr.zcraft.quartzlib.components.worker.Worker
import fr.zcraft.quartzlib.components.worker.WorkerAttributes
import fr.zcraft.quartzlib.components.worker.WorkerCallback
import fr.zcraft.quartzlib.components.worker.WorkerRunnable
import fr.zcraft.quartzlib.tools.Callback
import fr.zcraft.quartzlib.tools.FileUtils
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.util.Vector
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@WorkerAttributes(name = "bos-generation-metadata-io")
class GenerationData : Worker(), Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onWorldLoad(ev: WorldLoadEvent) {
        loadGenerationMetadata(ev.world, null)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerChangedWorld(ev: PlayerChangedWorldEvent) {
        loadGenerationMetadata(ev.player.world, null)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(ev: PlayerJoinEvent) {
        loadGenerationMetadata(ev.player.world, null)
    }

    companion object {
        private const val TYPE_BUILDING = "building"
        private const val TYPE_SPHERE = "sphere"
        private const val TYPE_UNKNOWN = "unknown"
        private val GSON = Gson()
        private val worldMetadata: MutableMap<World, MutableMap<Structure, Region>> = ConcurrentHashMap()

        /**
         * Returns the file the metadata are stored into.
         *
         * @param world The world.
         *
         * @return The file.
         */
        private fun getGenerationMetadataFile(world: World?): File {
            return File(
                    world!!.worldFolder,
                    "data" + File.separator + BallsOfZirconium.BOS_NAMESPACE + File.separator + "metadata.json"
            )
        }

        /**
         * Loads the metadata from the JSON dump for the given world.
         *
         *
         * Metadata are only loaded once. They will be queued for load, not
         * loaded instantaneously.
         *
         * @param world    The world.
         * @param callback A callback to be executed when the metadata are loaded
         * and registered in [.worldMetadata].
         */
        private fun loadGenerationMetadata(world: World, callback: Callback<Void?>?) {
            // If already loaded, skip
            if (hasMetadataForWorld(world)) {
                callback?.call(null)
                return
            }

            submitQuery(object : WorkerRunnable<Map<Structure, Region>?>() {
                @Throws(Throwable::class)
                override fun run(): Map<Structure, Region>? {
                    val metadataFile = getGenerationMetadataFile(world)
                    val rawJSONMetadata = FileUtils.readFile(metadataFile).trim { it <= ' ' }

                    // If there is nothing to load, skip too
                    if (rawJSONMetadata.isEmpty()) return null

                    val metadata = GSON.fromJson(rawJSONMetadata, JsonObject::class.java)
                    val worldStructures: MutableMap<Structure, Region> = HashMap()
                    val structures = metadata.getAsJsonArray("structures")

                    if (structures != null) {
                        for (structureElement in structures) {
                            try {
                                val jsonStructure = structureElement.asJsonObject
                                val name = jsonStructure["name"].asString
                                val type = jsonStructure["type"].asString.lowercase(Locale.getDefault())

                                if (type != TYPE_BUILDING && type != TYPE_SPHERE) {
                                    PluginLogger.error("Unknown structure type {0} in world {1} metadata, skipping", type, world!!.name)
                                    continue
                                }

                                val structure = if (type == TYPE_BUILDING) BallsOfZirconium.get().generationManager.getBuilding(name) else BallsOfZirconium.get().generationManager.getSphere(name)
                                if (structure == null) {
                                    PluginLogger.error("Unknown structure named {0} in world {1} metadata, skipping", name, world!!.name)
                                    continue
                                }

                                val lowestCorner = ConfigurationValueHandlers.handleValue(jsonStructure["lowestCorner"].asString, Vector::class.java)
                                val highestCorner = ConfigurationValueHandlers.handleValue(jsonStructure["highestCorner"].asString, Vector::class.java)

                                worldStructures[structure] = CuboidRegion(
                                        BukkitAdapter.adapt(world),
                                        asBlockVector(lowestCorner),
                                        asBlockVector(highestCorner)
                                )
                            } catch (e: ConfigurationParseException) {
                                PluginLogger.error("Unable to load a structure from world {0}'s metadata, skipping", e, world!!.name)
                            } catch (e: NullPointerException) {
                                PluginLogger.error("Unable to load a structure from world {0}'s metadata, skipping", e, world!!.name)
                            } catch (e: IllegalStateException) {
                                PluginLogger.error("Unable to load a structure from world {0}'s metadata, skipping", e, world!!.name)
                            }
                        }
                    }

                    return worldStructures
                }
            }, object : WorkerCallback<MutableMap<Structure, Region>?> {
                override fun finished(worldData: MutableMap<Structure, Region>?) {
                    if (worldData != null) worldMetadata[world] = worldData
                    callback?.call(null)
                }

                override fun errored(e: Throwable) {
                    PluginLogger.error("Unable to load Balls of Steel metadata for world {0}: malformed JSON metadata", e, world!!.name)
                    worldMetadata[world] = HashMap()
                    callback?.call(null)
                }
            })
        }

        /**
         * Saves the loaded metadata as a JSON dump in the given world.
         *
         *
         * If no metadata are saved at all for this world (i.e. the world was never loaded),
         * the callback is called with `false`.
         *
         * @param world    The world.
         * @param callback A callback to be executed when the metadata are saved
         * (boolean represents success).
         */
        private fun saveGenerationMetadata(world: World?, callback: Callback<Boolean>?) {
            if (!hasMetadataForWorld(world)) {
                callback?.call(false)
                return
            }
            submitQuery(object : WorkerRunnable<Void?>() {
                @Throws(Throwable::class)
                override fun run(): Void? {
                    val metadataFile = getGenerationMetadataFile(world)
                    metadataFile.parentFile.mkdirs()
                    val jsonStructures = JsonArray()
                    for ((key, value) in worldMetadata[world]!!) {
                        val jsonStructure = JsonObject()
                        val low = value.minimumPoint
                        val high = value.minimumPoint
                        jsonStructure.addProperty("name", key.name)
                        jsonStructure.addProperty("type", if (key is StaticBuilding) TYPE_BUILDING else if (key is GeneratedSphere) TYPE_SPHERE else TYPE_UNKNOWN)
                        jsonStructure.addProperty("lowestCorner", low.blockX.toString() + "," + low.blockY + "," + low.blockZ)
                        jsonStructure.addProperty("highestCorner", high.blockX.toString() + "," + high.blockY + "," + high.blockZ)
                        jsonStructures.add(jsonStructure)
                    }
                    val dump = JsonObject()
                    dump.add("structures", jsonStructures)
                    FileUtils.writeFile(metadataFile, GSON.toJson(dump))
                    return null
                }
            }, object : WorkerCallback<Void?> {
                override fun finished(unused: Void?) {
                    callback?.call(true)
                }

                override fun errored(e: Throwable) {
                    PluginLogger.info("Unable to save structures metadata for world {0}", e, world!!.name)
                    callback?.call(false)
                }
            })
        }

        /**
         * Saves a structure region in the world metadata.
         *
         * @param structure The structure.
         * @param world     The world.
         * @param region    The region.
         */
        fun saveStructure(structure: Structure, world: World, region: Region) {
            loadGenerationMetadata(world) { nothing: Void? ->
                worldMetadata.computeIfAbsent(world) { k: World? -> HashMap() }[structure] = region
                saveGenerationMetadata(world, null)
            }
        }

        /**
         * Forget that there were a structure at the given location. Does nothing if
         * there was not.
         *
         * @param location The location.
         *
         * @return `false` if there were no structure at the given location.
         */
        @JvmStatic
        fun forgetStructureAt(location: Location): Boolean {
            val structure = getStructureAt(location) ?: return false

            location.world?.let {
                loadGenerationMetadata(it) { nothing: Void? ->
                    worldMetadata[location.world]!!.remove(structure)
                    saveGenerationMetadata(location.world, null)
                }
            }

            return true
        }

        /**
         * Checks if a given world have metadata loaded.
         *
         * @param world The world.
         *
         * @return `true` if metadata are loaded for this world.
         */
        fun hasMetadataForWorld(world: World?): Boolean {
            return worldMetadata.containsKey(world)
        }

        /**
         * Returns the structure and its region for a given location, according to the
         * loaded world metadata.
         *
         * @param location The location to look for structures at.
         * @return An Entry with the structure and its region, or null if nothing found.
         */
        private fun getStructureEntryFor(location: Location): MutableMap.MutableEntry<Structure, Region>? {
            val regions = worldMetadata[location.world]
                    ?: return null
            for (structure in regions.entries) {
                if (structure.value.contains(BukkitAdapter.asBlockVector(location))) {
                    return structure
                }
            }
            return null
        }

        /**
         * Returns the structure located at the given location, according to the
         * loaded world metadata.
         *
         * @param location The location to check.
         *
         * @return The [Structure], or `null` if no structure was found
         * at this place.
         */
        @JvmStatic
        fun getStructureAt(location: Location): Structure? {
            return getStructureEntryFor(location)?.key
        }

        /**
         * Bulk-check the structures at multiple locations.
         *
         *
         * This is better than [.getStructureAt] in a loop
         * because it will list the structures and check locations only once.
         *
         *
         * Warning: the returned map can contains `null` values.
         *
         * @param locations A map associating string identifiers to locations.
         *
         * @return A map associating the previous string identifiers to the
         * buildings at the given locations, or to `null` without structure.
         */
        fun getStructuresAt(locations: Map<String, Location>): Map<String, Structure?> {
            // We first need to separate the locations in buckets, one bucket per world.
            val locationBuckets: MutableMap<World?, MutableMap<String, Location>> = HashMap()
            for ((key, value) in locations) {
                if (!locationBuckets.containsKey(value.world)) locationBuckets[value.world] = HashMap()
                locationBuckets[value.world]!![key] = value
            }

            // Then we check structures for each world
            val structures: MutableMap<String, Structure?> = HashMap()

            // The map is before filled with nulls—they will be overridden later, if structures are found.
            for (key in locations.keys) structures[key] = null
            for ((key, value) in locationBuckets) {
                for ((key1, value1) in worldMetadata[key]!!) {
                    for ((key2, value2) in value) {
                        if (value1.contains(BukkitAdapter.asBlockVector(value2))) {
                            structures[key2] = key1
                        }
                    }
                }
            }
            return structures
        }

        /**
         * Returns the region for the structure located at the given location, according
         * to the loaded world metadata.
         *
         * @param location The location to check.
         *
         * @return The [Region], or `null` if no structure was found
         * at this place.
         */
        @JvmStatic
        fun getRegionForStructureAt(location: Location): Region? {
            return getStructureEntryFor(location)?.value
        }
    }
}
