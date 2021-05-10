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
package eu.carrade.amaury.BallsOfSteel.generation

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel
import eu.carrade.amaury.BallsOfSteel.MapConfig
import eu.carrade.amaury.BallsOfSteel.generation.generation.BallsOfSteelGenerator
import eu.carrade.amaury.BallsOfSteel.generation.structures.GeneratedSphere
import eu.carrade.amaury.BallsOfSteel.generation.structures.StaticBuilding
import eu.carrade.amaury.BallsOfSteel.generation.structures.Structure
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldLoader
import fr.zcraft.quartzlib.core.QuartzComponent
import fr.zcraft.quartzlib.tools.PluginLogger
import fr.zcraft.quartzlib.tools.reflection.Reflection
import fr.zcraft.quartzlib.tools.runners.RunAsyncTask
import org.apache.commons.lang.StringUtils
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.world.WorldLoadEvent
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.*

class GenerationManager : QuartzComponent(), Listener {
    val spheres: MutableSet<GeneratedSphere> = HashSet()
        get() = Collections.unmodifiableSet(field)
    private val spheresQueue: Queue<GeneratedSphere?> = ArrayDeque()

    val buildings: MutableSet<StaticBuilding> = HashSet()
    val buildingsRegions: MutableSet<CuboidRegion> = HashSet()
        get() = Collections.unmodifiableSet(field)

    /**
     * `true` if the generation should be logged.
     */
    var logged = false
        private set

    /**
     * The corner of the world with the lowest coordinates
     */
    lateinit var lowestCorner: BlockVector3
        private set

    /**
     * The corner of the world with the highest coordinates
     */
    lateinit var highestCorner: BlockVector3
        private set

    private lateinit var managedWorldsListFile: File
    private val managedWorlds: MutableSet<World> = HashSet()
    private val managedWorldsNames: MutableSet<String> = HashSet()
    private val currentlyLoadingWorldsNames: MutableSet<String> = HashSet()

    override fun onEnable() {
        if (!MapConfig.GENERATION.ENABLED.get()) {
            isEnabled = false
            return
        }

        if (!BallsOfSteel.get().worldEditDependency.isEnabled) {
            PluginLogger.error("Cannot use the generator without WorldEdit installed.")
            isEnabled = false
            return
        }

        logged = MapConfig.GENERATION.LOGS.get()

        // Loading generation processes & static buildings
        val generatedSpheres = MapConfig.GENERATION.SPHERES.get()
        val staticBuildings = MapConfig.GENERATION.STATIC_BUILDINGS.get()
        if (generatedSpheres.size == 0) PluginLogger.warning("No sphere loaded from config, you may have an error somewhere.")
        if (staticBuildings.size == 0) PluginLogger.warning("No static building loaded from config, you may have an error somewhere.")
        spheres.addAll(generatedSpheres)
        buildings.addAll(staticBuildings)
        recalculatePrivateBuildingRegions()

        // Loading map boundaries
        val corner1 = MapConfig.GENERATION.MAP.BOUNDARIES.CORNER_1.get()
        val corner2 = MapConfig.GENERATION.MAP.BOUNDARIES.CORNER_2.get()
        lowestCorner = corner1.getMinimum(corner2)
        highestCorner = corner1.getMaximum(corner2)

        // Loading managed worlds
        managedWorldsNames.add(MapConfig.WORLD.get())
        loadManagedWorlds()
        saveManagedWorlds()
    }


    /* ========== Generation processes and buildings ========== */

    /**
     * @param random A source of randomness.
     *
     * @return A random generation process.
     */
    fun getRandomSphere(random: Random): GeneratedSphere? {
        if (spheresQueue.isEmpty()) {
            val generationProcessesList: MutableList<GeneratedSphere?> = ArrayList()
            for (sphere: GeneratedSphere in spheres) if (sphere.enabled) generationProcessesList.add(sphere)
            generationProcessesList.shuffle(random)
            spheresQueue.addAll(generationProcessesList)
        }

        return spheresQueue.poll()
    }

    /**
     * Recalculates the private regions of the static buildings, where nothing should be
     * generated.
     */
    private fun recalculatePrivateBuildingRegions() {
        buildingsRegions.clear()
        for (building: StaticBuilding in buildings) buildingsRegions.add(CuboidRegion.makeCuboid(building.privateRegion))
    }

    /**
     * Lookups for a structure with that name in the given set.
     *
     * @param name The name, tested case-insensitively and ignoring spaces.
     * @param structures A set containing structures.
     *
     * @return The structure found, or `null` if not found.
     */
    private fun getStructureFromName(name: String, structures: Set<Structure>): Structure? {
        val nameWithoutSpaces = name.replace(" ", "")
        for (structure: Structure in structures) {
            if (structure.name.replace(" ", "").equals(nameWithoutSpaces, ignoreCase = true)) {
                return structure
            }
        }
        return null
    }

    /**
     * Lookups for a sphere with that name in the given set.
     *
     * @param name The name, tested case-insensitively and ignoring spaces.
     *
     * @return The sphere found, or `null` if not found.
     */
    fun getSphere(name: String): GeneratedSphere? {
        return getStructureFromName(name, spheres) as GeneratedSphere?
    }

    /**
     * Lookups for a building with that name in the given set.
     *
     * @param name The name, tested case-insensitively and ignoring spaces.
     *
     * @return The building found, or `null` if not found.
     */
    fun getBuilding(name: String): StaticBuilding? {
        return getStructureFromName(name, buildings) as StaticBuilding?
    }
    /* ========== Misc ========== */
    /**
     * Checks if the given location is inside the defined boundaries of the map.
     * The world is not checked.
     *
     * @param location The location.
     *
     * @return `true` if inside the boundaries.
     */
    fun isInsideBoundaries(location: Location): Boolean {
        return (location.x >= lowestCorner.x
                ) && (location.y >= lowestCorner.y
                ) && (location.z >= lowestCorner.z
                ) && (location.x <= highestCorner.x
                ) && (location.y <= highestCorner.y
                ) && (location.z <= highestCorner.z)
    }


    /* ========== World creation ========== */

    /**
     * Create a world using the Balls of Steel generator.
     *
     * If the world already exists, it's generator and some options will be
     * updated.
     *
     * @param name A world name.
     *
     * @return A new world, or a reference to an existing world if it already
     * exists.
     */
    private fun createWorld(name: String): World? {
        currentlyLoadingWorldsNames.add(name)
        val world = WorldCreator(name)
                .environment(MapConfig.GENERATION.MAP.ENVIRONMENT.get())
                .generator(BallsOfSteelGenerator())
                .generateStructures(true)
                .createWorld()

        world?.let {
            it.setSpawnFlags(MapConfig.GENERATION.MAP.ALLOW_MONSTERS.get(), MapConfig.GENERATION.MAP.ALLOW_ANIMALS.get())
            it.setGameRule(GameRule.DO_MOB_SPAWNING, MapConfig.GENERATION.MAP.ALLOW_MONSTERS.get())

            if (it.environment == World.Environment.THE_END) {
                patchWorldAgainstDragon(world)
            }

            manageWorld(it)
        }

        return world
    }

    /**
     * Create a world using the Balls of Steel generator with the name defined
     * in map.yml, and returns a configured [WorldLoader] ready to be
     * used, without log receiver.
     *
     * If the world already exists, it's generator and some options will be
     * updated.
     *
     * @return A [WorldLoader] containing a reference to either a new
     * world or the world with the given name.
     */
    @JvmOverloads
    fun createWorldAndGetLoader(name: String = MapConfig.WORLD.get(), logsReceiver: CommandSender? = null): WorldLoader? {
        val corner1 = MapConfig.GENERATION.MAP.BOUNDARIES.CORNER_1.get().toBlockVector2()
        val corner2 = MapConfig.GENERATION.MAP.BOUNDARIES.CORNER_2.get().toBlockVector2()

        // We also load the chunks near the border to avoid load when players are close to it.
        // If the player logged out we switch to the console to display the logs.

        createWorld(name)?.let {
            return WorldLoader(
                    it,
                    if (logsReceiver is Player && !logsReceiver.isOnline) Bukkit.getConsoleSender() else logsReceiver,
                    corner1.getMinimum(corner2).subtract(32, 32),
                    corner1.getMaximum(corner2).add(32, 32)
            )
        }

        return null
    }

    /**
     * Create a world using the Balls of Steel generator with the name defined
     * in map.yml, and returns a configured [WorldLoader] ready to be
     * used.
     *
     * If the world already exists, it's generator and some options will be
     * updated. If not, the world's preload will be disabled.
     *
     * @param logsReceiver A receiver for the generation logs (progress...). Can
     * be `null`.
     *
     * @return A [WorldLoader] containing a reference to either a new
     * world or the world with the given name.
     */
    fun createWorldAndGetLoader(logsReceiver: CommandSender?): WorldLoader? {
        return createWorldAndGetLoader(MapConfig.WORLD.get(), logsReceiver)
    }


    /* ========== World management and patching ========== */

    /**
     * Configures a world to be managed by BoS.
     *
     * @param world The world.
     */
    private fun manageWorld(world: World) {
        managedWorlds.add(world)
        managedWorldsNames.add(world.name)
        saveManagedWorlds()
    }

    /**
     * Loads the managed worlds from the file in the plugin directory.
     */
    private fun loadManagedWorlds() {
        managedWorldsListFile = File(BallsOfSteel.get().dataFolder, MANAGED_WORLDS_FILENAME)

        if (!managedWorldsListFile.exists()) {
            try {
                if (!managedWorldsListFile.parentFile.mkdirs()) {
                    PluginLogger.warning("Cannot create the {0} file directory to remember BoS worlds, you may have problems with End ones.", managedWorldsListFile.parent)
                }
                if (!managedWorldsListFile.createNewFile()) {
                    PluginLogger.warning("Cannot create the {0} file to remember BoS worlds, you may have problems with End ones.", managedWorldsListFile.absolutePath)
                }
            } catch (e: IOException) {
                PluginLogger.error("Error while creating {0} file to remember BoS worlds, you may have problems with End ones.", e, managedWorldsListFile.absolutePath)
            }
        }

        try {
            val builder = StringBuilder()

            BufferedReader(InputStreamReader(FileInputStream(managedWorldsListFile))).use { reader ->
                var line: String?
                while ((reader.readLine().also { line = it }) != null) builder.append(line)
            }

            for (name: String in builder.toString().split(",".toRegex()).toTypedArray()) {
                val cleanName = name.trim { it <= ' ' }
                if (cleanName.isNotEmpty()) managedWorldsNames.add(cleanName)
            }
        } catch (e: IOException) {
            PluginLogger.error("Error while loading managed worlds list. Generation may not be consistant on Balls of Steel worlds.", e)
        }
    }

    /**
     * Saves the managed worlds to the file in the plugin directory.
     */
    private fun saveManagedWorlds() {
        val rawManagedWorlds = StringUtils.join(managedWorldsNames, ",")
        RunAsyncTask.nextTick(Runnable {
            try {
                OutputStreamWriter(FileOutputStream(managedWorldsListFile)).use { writer -> writer.write(rawManagedWorlds) }
            } catch (e: IOException) {
                PluginLogger.error("Cannot save BallsOfSteel managed worlds", e)
            }
        })
    }

    /**
     * Patches a world so the Ender Dragon will not spawn inside.
     *
     * This will update the world's NBT data to save a fake old dragon fight,
     * and patch the `EnderDragonBattle` object so the presence of a
     * portal is not checked.
     *
     * Nothing is done if the world environment is not [ ][org.bukkit.World.Environment.THE_END].
     *
     * @param world A world.
     */
    private fun patchWorldAgainstDragon(world: World?) {
        if (world!!.environment != World.Environment.THE_END) return
        try {
            val worldServer = Reflection.call(Reflection.getBukkitClassByName("CraftWorld").cast(world), "getHandle")
            val worldData = Reflection.call(worldServer, "getWorldData")
            val dimensionManager = Reflection.call(Reflection.getMinecraftClassByName("DimensionManager"), "valueOf", *arrayOf<Any>("THE_END"))
            val nbtCompound = Reflection.call(worldData, "a", dimensionManager)
            val dragonFightCompound = Reflection.call(nbtCompound, "getCompound", "DragonFight")
            val nbtSetBoolean = dragonFightCompound.javaClass.getMethod("setBoolean", String::class.java, Boolean::class.javaPrimitiveType)
            val nbtSetLong = dragonFightCompound.javaClass.getMethod("setLong", String::class.java, Long::class.javaPrimitiveType)
            val nbtSetBase = nbtCompound.javaClass.getMethod("set", String::class.java, Reflection.getMinecraftClassByName("NBTBase"))


            // Saves the fact the dragon was killed (sort of)
            val dragonUUID = UUID.randomUUID()
            nbtSetBoolean.invoke(dragonFightCompound, "DragonKilled", true)
            nbtSetBoolean.invoke(dragonFightCompound, "PreviouslyKilled", true)
            nbtSetLong.invoke(dragonFightCompound, "DragonUUIDMost", dragonUUID.mostSignificantBits)
            nbtSetLong.invoke(dragonFightCompound, "DragonUUIDLeast", dragonUUID.leastSignificantBits)
            nbtSetBase.invoke(nbtCompound, "DragonFight", dragonFightCompound)
            Reflection.call(worldData, "a", dimensionManager, nbtCompound)


            // Patches the EnderDragonBattle object to disable legacy dragon check
            val worldProvider = Reflection.getFieldValue(Reflection.getMinecraftClassByName("World"), worldServer, "worldProvider")
            if (worldProvider.javaClass.isAssignableFrom(Reflection.getMinecraftClassByName("WorldProviderTheEnd"))) {
                val enderDragonBattle = Reflection.call(worldProvider, "s")
                if (enderDragonBattle != null) Reflection.setFieldValue(enderDragonBattle, "n", false)
            } else {
                PluginLogger.error("Cannot patch world {0} against dragon: wrong world provider for world: expecting WorldProviderTheEnd, got {1}", world.name, worldProvider.javaClass)
            }
        } catch (e: ClassNotFoundException) {
            PluginLogger.error("Error while removing dragon from world {0}", e, world.name)
        } catch (e: InvocationTargetException) {
            PluginLogger.error("Error while removing dragon from world {0}", e, world.name)
        } catch (e: IllegalAccessException) {
            PluginLogger.error("Error while removing dragon from world {0}", e, world.name)
        } catch (e: NoSuchMethodException) {
            PluginLogger.error("Error while removing dragon from world {0}", e, world.name)
        } catch (e: NoSuchFieldException) {
            PluginLogger.error("Error while removing dragon from world {0}", e, world.name)
        }
    }

    /**
     * Used to patch the worlds when loaded, and to keep track of managed
     * worlds.
     */
    @EventHandler
    fun onWorldLoad(ev: WorldLoadEvent) {
        if (managedWorldsNames.contains(ev.world.name)) manageWorld(ev.world)
        if (managedWorlds.contains(ev.world) && ev.world.environment == World.Environment.THE_END) patchWorldAgainstDragon(ev.world)
    }

    /**
     * Used to patch the worlds when a player enter inside, as sometimes the
     * EnderDragonBattle object cannot be patched when the world was just
     * created.
     */
    @EventHandler
    fun onPlayerChangeWorld(ev: PlayerChangedWorldEvent) {
        if (managedWorlds.contains(ev.player.world)) patchWorldAgainstDragon(ev.player.world)
    }

    companion object {
        private const val MANAGED_WORLDS_FILENAME = "managed_worlds.dat"
    }
}
