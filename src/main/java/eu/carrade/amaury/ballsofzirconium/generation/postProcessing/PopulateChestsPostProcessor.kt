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

import com.sk89q.jnbt.CompoundTag
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.entity.BaseEntity
import com.sk89q.worldedit.entity.Entity
import com.sk89q.worldedit.function.EntityFunction
import com.sk89q.worldedit.function.RegionFunction
import com.sk89q.worldedit.function.RegionMaskingFilter
import com.sk89q.worldedit.function.operation.OperationQueue
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.function.visitor.EntityVisitor
import com.sk89q.worldedit.function.visitor.RegionVisitor
import com.sk89q.worldedit.math.BlockVector3
import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium
import eu.carrade.amaury.ballsofzirconium.MapConfig
import eu.carrade.amaury.ballsofzirconium.generation.utils.GenerationConfig
import eu.carrade.amaury.ballsofzirconium.generation.utils.WorldEditUtils.parseMask
import fr.zcraft.quartzlib.components.i18n.I
import fr.zcraft.quartzlib.tools.PluginLogger
import org.apache.commons.lang.StringUtils
import org.bukkit.util.FileUtil
import java.io.File

class PopulateChestsPostProcessor(parameters: Map<*, *>) : PostProcessor(parameters) {

    // TODO Implement onlyEmpty option in chests populator
    private val onlyEmpty: Boolean = GenerationConfig.getValue(parameters, "only_empty", Boolean::class.javaPrimitiveType, true)
    private val chests: Boolean = GenerationConfig.getValue(parameters, "chests", Boolean::class.javaPrimitiveType, true)
    private val trappedChests: Boolean = GenerationConfig.getValue(parameters, "trapped_chests", Boolean::class.javaPrimitiveType, true)
    private val shulkerBoxes: Boolean = GenerationConfig.getValue(parameters, "shulker_boxes", Boolean::class.javaPrimitiveType, true)
    private val hoppers: Boolean = GenerationConfig.getValue(parameters, "hoppers", Boolean::class.javaPrimitiveType, true)
    private val dispensers: Boolean = GenerationConfig.getValue(parameters, "dispensers", Boolean::class.javaPrimitiveType, true)
    private val droppers: Boolean = GenerationConfig.getValue(parameters, "droppers", Boolean::class.javaPrimitiveType, true)
    private val furnaces: Boolean = GenerationConfig.getValue(parameters, "furnaces", Boolean::class.javaPrimitiveType, true)
    private val storageMinecarts: Boolean = GenerationConfig.getValue(parameters, "storage_minecarts", Boolean::class.javaPrimitiveType, true)
    private val hopperMinecarts: Boolean = GenerationConfig.getValue(parameters, "hopper_minecarts", Boolean::class.javaPrimitiveType, true)

    private var lootTable: String? = null
    private var lootTableFilename: String? = null
    private var lootTablePath: File? = null

    @Throws(WorldEditException::class)
    override fun doProcess() {
        // If the loot table was invalid, we skip.
        if (lootTable == null) return

        // First, we copy the loot table file in the world folder if needed.
        // TODO Use data pack
        if (lootTablePath != null) {
            val lootTableWorldPath = File(
                    BukkitAdapter.adapt(session!!.world).worldFolder,
                    "data/loot_tables/$LOOT_TABLES_NAMESPACE/$lootTableFilename"
            )
            lootTableWorldPath.parentFile.mkdirs()
            FileUtil.copy(lootTablePath!!, lootTableWorldPath)
        }


        // The functions applying the NBT changes
        val updateLootNBTForBlocks = RegionFunction { position: BlockVector3? ->
            val block = session!!.getFullBlock(position)
            val nbt = if (block.hasNbtData()) block.nbtData else CompoundTag(HashMap())
            block.nbtData = nbt!!.createBuilder().putString("LootTable", lootTable).build()
            session!!.setBlock(position, block)
            true
        }

        val updateLootNBTForEntities = EntityFunction { entity: Entity? ->
            if (entity == null) return@EntityFunction false
            var state = entity.state
            val location = entity.location
            if (state == null) return@EntityFunction false

            when (state.type.id) {
                "minecraft:chest_minecart" -> if (!storageMinecarts) return@EntityFunction false
                "minecraft:hopper_minecart" -> if (!hopperMinecarts) return@EntityFunction false
                else -> return@EntityFunction false
            }

            val nbt = if (state.hasNbtData()) state.nbtData else CompoundTag(HashMap())
            state = BaseEntity(state.type, nbt!!.createBuilder().putString("LootTable", lootTable).build())
            entity.remove()

            if (session!!.createEntity(location, state) == null) {
                PluginLogger.error("Unable to re-create minecart entity while populating chests at {0}", location.toVector())
            }

            true
        }


        // The filter to select only what we want
        val masks: MutableList<String?> = ArrayList(7)
        if (chests) masks.add("chest")
        if (trappedChests) masks.add("trapped_chest")
        if (shulkerBoxes) masks.add("white_shulker_box,orange_shulker_box,magenta_shulker_box,light_blue_shulker_box,yellow_shulker_box,lime_shulker_box,pink_shulker_box,gray_shulker_box,silver_shulker_box,cyan_shulker_box,purple_shulker_box,blue_shulker_box,brown_shulker_box,green_shulker_box,red_shulker_box,black_shulker_box")
        if (hoppers) masks.add("hopper")
        if (dispensers) masks.add("dispenser")
        if (droppers) masks.add("dropper")
        if (furnaces) masks.add("furnace,lit_furnace")

        val blocksMask = parseMask(session!!.world, StringUtils.join(masks, ","), session)
        val blocksVisitor = RegionVisitor(region, RegionMaskingFilter(blocksMask, updateLootNBTForBlocks))
        val entitiesVisitor = EntityVisitor(session!!.getEntities(region).iterator(), updateLootNBTForEntities)

        Operations.complete(OperationQueue(blocksVisitor, entitiesVisitor))
    }

    override fun doName(): String {
        return I.t("Containers population")
    }

    override fun doSettingsDescription(): List<String> {
        val settings: MutableList<String> = ArrayList(2)
        val excluded: MutableList<String?> = ArrayList(7)
        if (!chests) excluded.add("chests")
        if (!trappedChests) excluded.add("trapped chests")
        if (!shulkerBoxes) excluded.add("shulker boxes")
        if (!hoppers) excluded.add("hoppers")
        if (!dispensers) excluded.add("dispensers")
        if (!droppers) excluded.add("droppers")
        if (!furnaces) excluded.add("furnaces")
        settings.add(I.t("Loot table: {0}", if (lootTableFilename != null) lootTableFilename else if (lootTable != null) lootTable else I.tc("loot_tables_settings_desc", "none")))
        if (excluded.size > 0) settings.add(I.t("Excluding: {0}", StringUtils.join(excluded, ", ")))
        return settings
    }

    companion object {
        private const val LOOT_TABLES_NAMESPACE = BallsOfZirconium.BOS_NAMESPACE
    }

    init {
        val rawLootTable: String = GenerationConfig.getValue<String>(parameters, "loot_table", String::class.java, "").toLowerCase().replace("../", "/").replace(".json", "")
        if (rawLootTable.contains(":")) {
            lootTable = rawLootTable
            lootTableFilename = null
            lootTablePath = null
        } else if (!rawLootTable.isEmpty()) {
            lootTable = LOOT_TABLES_NAMESPACE + ":" + rawLootTable
            lootTableFilename = "$rawLootTable.json"
            lootTablePath = File(MapConfig.MAP_LOOT_TABLES_DIRECTORY, lootTableFilename)
        } else {
            lootTable = null
            lootTableFilename = null
            lootTablePath = null
        }
    }
}