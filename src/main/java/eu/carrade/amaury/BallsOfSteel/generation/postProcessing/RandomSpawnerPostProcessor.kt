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

import com.sk89q.jnbt.CompoundTag
import com.sk89q.jnbt.ListTag
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.function.RegionFunction
import com.sk89q.worldedit.function.RegionMaskingFilter
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.function.visitor.RegionVisitor
import com.sk89q.worldedit.math.BlockVector3
import eu.carrade.amaury.BallsOfSteel.generation.utils.GenerationConfig
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils.parseMask
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.PluginLogger
import org.apache.commons.lang.StringUtils
import org.bukkit.entity.EntityType
import java.util.*

class RandomSpawnerPostProcessor(parameters: Map<*, *>) : PostProcessor(parameters) {
    private val spawnerEntityTypes: MutableList<EntityType> = ArrayList()
    private val spawnCount: Short? = GenerationConfig.getValueOrNull(parameters, "spawn_count", Short::class.java)
    private val spawnRange: Short? = GenerationConfig.getValueOrNull(parameters, "spawn_range", Short::class.java)
    private val minSpawnDelay: Short? = GenerationConfig.getValueOrNull(parameters, "min_spawn_delay", Short::class.java)
    private val maxSpawnDelay: Short? = GenerationConfig.getValueOrNull(parameters, "max_spawn_delay", Short::class.java)
    private val maxNearbyEntities: Short? = GenerationConfig.getValueOrNull(parameters, "max_nearby_entities", Short::class.java)
    private val requiredPlayerRange: Short? = GenerationConfig.getValueOrNull(parameters, "required_player_range", Short::class.java)

    init {
        val rawEntityTypes = GenerationConfig.getValue(parameters, "entities", List::class.java, emptyList<Any>())

        for (rawType in rawEntityTypes) {
            var entity: EntityType?
            val type = rawType.toString().replace(' ', '_')

            entity = try {
                EntityType.valueOf(type.uppercase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                EntityType.fromName(type.lowercase(Locale.getDefault()))
            }

            if (entity != null) {
                spawnerEntityTypes.add(entity)
            }
        }
    }

    @Throws(WorldEditException::class)
    override fun doProcess() {
        val randomizeSpawners = RegionFunction { position: BlockVector3? ->
            val spawnerEntityType = spawnerEntityTypes[random!!.nextInt(spawnerEntityTypes.size)]
            val block = session!!.getFullBlock(position)
            val nbt = (if (block.hasNbtData()) block.nbtData else CompoundTag(HashMap()))!!.createBuilder()
            nbt.put("SpawnData", CompoundTag(HashMap()).createBuilder()
                    .putString("id", spawnerEntityType.key.toString())
                    .build()
            )
            if (spawnCount != null) nbt.putShort("SpawnCount", spawnCount)
            if (spawnRange != null) nbt.putShort("SpawnRange", spawnRange)
            if (minSpawnDelay != null) nbt.putShort("MinSpawnDelay", (minSpawnDelay * 20).toShort())
            if (maxSpawnDelay != null) nbt.putShort("MaxSpawnDelay", (maxSpawnDelay * 20).toShort())
            if (maxNearbyEntities != null) nbt.putShort("MaxNearbyEntities", maxNearbyEntities)
            if (requiredPlayerRange != null) nbt.putShort("RequiredPlayerRange", requiredPlayerRange)

            // Ensure the spawner is properly randomized
            nbt.putShort("Delay", (-1).toShort())

            // Erases the SpawnPotentials key if present from a schematic or something,
            // as it will modifies the spawned entity.
            nbt.put("SpawnPotentials", ListTag(CompoundTag::class.java, emptyList()))
            block.nbtData = nbt.build()
            session!!.setBlock(position, block)
            true
        }
        val blocksMask = parseMask(session!!.world, "mob_spawner", session)
        val blocksVisitor = RegionVisitor(region, RegionMaskingFilter(blocksMask, randomizeSpawners))
        try {
            Operations.complete(blocksVisitor)
        } catch (e: WorldEditException) {
            PluginLogger.info("Unable to randomize monster spawners", e)
        }
    }

    override fun doName(): String {
        return I.t("Spawners randomization")
    }

    override fun doSettingsDescription(): List<String> {
        val entitiesNames: MutableList<String?> = ArrayList(spawnerEntityTypes.size)
        val settings: MutableList<String> = ArrayList(7)

        for (type in spawnerEntityTypes) entitiesNames.add(type.key.toString())
        settings.add(I.t("Entities: {0}", StringUtils.join(entitiesNames, ", ")))

        if (spawnCount != null) settings.add(I.t("Spawn count: {0}", spawnCount))
        if (spawnRange != null) settings.add(I.t("Spawn range: {0} blocks", spawnRange))
        if (minSpawnDelay != null) settings.add(I.t("Minimum spawn delay: {0} seconds", minSpawnDelay))
        if (maxSpawnDelay != null) settings.add(I.t("Maximum spawn delay: {0} seconds", maxSpawnDelay))
        if (maxNearbyEntities != null) settings.add(I.t("Max nearby entities: {0}", maxNearbyEntities))
        if (requiredPlayerRange != null) settings.add(I.t("Required player range: {0} blocks", requiredPlayerRange))

        return settings
    }
}
