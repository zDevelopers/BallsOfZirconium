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
package eu.carrade.amaury.BallsOfSteel.generation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.generation.structures.GeneratedSphere;
import eu.carrade.amaury.BallsOfSteel.generation.structures.StaticBuilding;
import eu.carrade.amaury.BallsOfSteel.generation.structures.Structure;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.FileUtils;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@WorkerAttributes (name = "bos-generation-metadata-io")
public class GenerationMetadata extends Worker
{
    private static final String TYPE_BUILDING = "building";
    private static final String TYPE_SPHERE = "sphere";
    private static final String TYPE_UNKNOWN = "unknown";

    private static Gson GSON = new Gson();
    private static Map<World, Map<Region, Structure>> worldMetadata = new ConcurrentHashMap<>();


    @Override
    public void onEnable()
    {
        super.onEnable();

        for (World world : Bukkit.getWorlds())
            loadGenerationMetadata(world, null);
    }

    /**
     * Returns the file the metadata are stored into.
     *
     * @param world The world.
     *
     * @return The file.
     */
    private static File getGenerationMetadataFile(final World world)
    {
        return new File(
                world.getWorldFolder(),
                "data" + File.separator + BallsOfSteel.BOS_NAMESPACE + File.separator + "metadata.json"
        );
    }

    /**
     * Registers an empty set of metadata for the given world, not trying to
     * load anything, assuming there is no data already saved.
     *
     * <p>Use with caution, as this can erase existing metadata!</p>
     *
     * @param world The world.
     */
    public static void unsafeCreateEmptyGenerationMetadata(final World world)
    {
        worldMetadata.put(world, new HashMap<Region, Structure>());
    }

    /**
     * Loads the metadata from the JSON dump for the given world.
     *
     * <p>Metadata are only loaded once. They will be queued for load, not
     * loaded instantaneously.</p>
     *
     * @param world    The world.
     * @param callback A callback to be executed when the metadata are loaded
     *                 and registered in {@link #worldMetadata}.
     */
    public static void loadGenerationMetadata(final World world, final Callback<Void> callback)
    {
        // If already loaded, skip
        if (hasMetadataForWorld(world))
        {
            if (callback != null) callback.call(null);
            return;
        }

        submitQuery(new WorkerRunnable<Map<Region, Structure>>()
        {
            @Override
            public Map<Region, Structure> run() throws Throwable
            {
                final File metadataFile = getGenerationMetadataFile(world);
                final String rawJSONMetadata = FileUtils.readFile(metadataFile).trim();

                // If there is nothing to load, skip too
                if (rawJSONMetadata.isEmpty()) return null;

                final JsonObject metadata = GSON.fromJson(rawJSONMetadata, JsonObject.class);
                final Map<Region, Structure> worldStructures = new HashMap<>();

                final JsonArray structures = metadata.getAsJsonArray("structures");

                if (structures != null)
                {
                    for (final JsonElement structureElement : structures)
                    {
                        try
                        {
                            final JsonObject jsonStructure = structureElement.getAsJsonObject();

                            final String name = jsonStructure.get("name").getAsString();
                            final String type = jsonStructure.get("type").getAsString().toLowerCase();

                            if (!Objects.equals(type, TYPE_BUILDING) && !Objects.equals(type, TYPE_SPHERE))
                            {
                                PluginLogger.error("Unknown structure type {0} in world {1} metadata, skipping", type, world.getName());
                                continue;
                            }

                            final Structure structure = type.equals(TYPE_BUILDING)
                                    ? BallsOfSteel.get().getGenerationManager().getBuilding(name)
                                    : BallsOfSteel.get().getGenerationManager().getSphere(name);

                            if (structure == null)
                            {
                                PluginLogger.error("Unknown structure named {0} in world {1} metadata, skipping", name, world.getName());
                                continue;
                            }

                            final Vector lowestCorner = ConfigurationValueHandlers.handleValue(jsonStructure.get("lowestCorner").getAsString(), Vector.class);
                            final Vector highestCorner = ConfigurationValueHandlers.handleValue(jsonStructure.get("highestCorner").getAsString(), Vector.class);

                            worldStructures.put(
                                    new CuboidRegion(
                                            (com.sk89q.worldedit.world.World) BukkitUtil.getLocalWorld(world),
                                            BukkitUtil.toVector(lowestCorner), BukkitUtil.toVector(highestCorner)
                                    ),
                                    structure
                            );
                        }
                        catch (ConfigurationParseException | NullPointerException | IllegalStateException e)
                        {
                            PluginLogger.error("Unable to load a structure from world {0}'s metadata, skipping", e, world.getName());
                        }
                    }
                }

                return worldStructures;
            }
        }, new WorkerCallback<Map<Region, Structure>>()
        {
            @Override
            public void finished(final Map<Region, Structure> worldData)
            {
                worldMetadata.put(world, worldData != null ? worldData : new HashMap<Region, Structure>());
                if (callback != null) callback.call(null);
            }

            @Override
            public void errored(final Throwable e)
            {
                PluginLogger.error("Unable to load Balls of Steel metadata for world {0}: malformed JSON metadata", e, world.getName());

                worldMetadata.put(world, new HashMap<Region, Structure>());
                if (callback != null) callback.call(null);
            }
        });
    }

    /**
     * Saves the loaded metadata as a JSON dump in the given world.
     *
     * <p>If no metadata are saved at all for this world (i.e. the world was
     * never loaded), the callback is called with {@code false}.</p>
     *
     * @param world    The world.
     * @param callback A callback to be executed when the metadata are saved
     *                 (boolean represents success).
     */
    private static void saveGenerationMetadata(final World world, final Callback<Boolean> callback)
    {
        if (!hasMetadataForWorld(world))
        {
            if (callback != null) callback.call(false);
            return;
        }

        submitQuery(new WorkerRunnable<Boolean>()
        {
            @Override
            public Boolean run() throws Throwable
            {
                final File metadataFile = getGenerationMetadataFile(world);
                metadataFile.getParentFile().mkdirs();

                final JsonArray jsonStructures = new JsonArray();

                for (final Map.Entry<Region, Structure> structure : worldMetadata.get(world).entrySet())
                {
                    final JsonObject jsonStructure = new JsonObject();

                    final com.sk89q.worldedit.Vector low = structure.getKey().getMinimumPoint();
                    final com.sk89q.worldedit.Vector high = structure.getKey().getMaximumPoint();

                    jsonStructure.addProperty("name", structure.getValue().getName());
                    jsonStructure.addProperty("type", structure.getValue() instanceof StaticBuilding ? TYPE_BUILDING : (structure.getValue() instanceof GeneratedSphere ? TYPE_SPHERE : TYPE_UNKNOWN));
                    jsonStructure.addProperty("lowestCorner", low.getBlockX() + "," + low.getBlockY() + "," + low.getBlockZ());
                    jsonStructure.addProperty("highestCorner", high.getBlockX() + "," + high.getBlockY() + "," + high.getBlockZ());

                    jsonStructures.add(jsonStructure);
                }

                final JsonObject dump = new JsonObject();
                dump.add("structures", jsonStructures);

                FileUtils.writeFile(metadataFile, GSON.toJson(dump));

                return true;
            }
        }, new WorkerCallback<Boolean>()
        {
            @Override
            public void finished(Boolean success)
            {
                if (!success)
                    PluginLogger.error("Unable to save world metadata for {0}, is the file {1} writable?", world.getName(), getGenerationMetadataFile(world));

                if (callback != null) callback.call(success);
            }

            @Override
            public void errored(Throwable e)
            {
                PluginLogger.error("Unable to save world metadata for {0}, is the file {1} writable?", e, world.getName(), getGenerationMetadataFile(world));
                if (callback != null) callback.call(false);
            }
        });
    }

    /**
     * Saves a structure region in the world metadata.
     *
     * @param structure The structure.
     * @param world     The world.
     * @param region    The region.
     */
    public static void saveStructure(final Structure structure, final World world, final Region region)
    {
        Callback<Void> saveFunction = new Callback<Void>()
        {
            @Override
            public void call(Void nothing)
            {
                worldMetadata.get(world).put(region, structure);
                saveGenerationMetadata(world, null);
            }
        };

        loadGenerationMetadata(world, saveFunction);
    }

    /**
     * Forget that there were a structure at the given location. Does nothing if
     * there was not.
     *
     * @param location The location.
     *
     * @return {@code false} if there were no structure at the given location.
     */
    public static boolean forgetStructureAt(final Location location)
    {
        final Region region = getRegionForStructureAt(location);
        if (region == null) return false;

        Callback<Void> removeFunction = new Callback<Void>()
        {
            @Override
            public void call(Void nothing)
            {
                worldMetadata.get(location.getWorld()).remove(region);
                saveGenerationMetadata(location.getWorld(), null);
            }
        };

        loadGenerationMetadata(location.getWorld(), removeFunction);

        return true;
    }

    /**
     * Checks if a given world have metadata loaded.
     *
     * @param world The world.
     *
     * @return {@code true} if metadata are loaded for this world.
     */
    public static boolean hasMetadataForWorld(final World world)
    {
        return worldMetadata.containsKey(world);
    }

    /**
     * Returns the structure located at the given location, according to the
     * loaded world metadata.
     *
     * @param location The location to check.
     *
     * @return The {@link Structure}, or {@code null} if no structure was found
     * at this place.
     */
    public static Structure getStructureAt(final Location location)
    {
        if (!hasMetadataForWorld(location.getWorld())) return null;

        for (final Map.Entry<Region, Structure> structure : worldMetadata.get(location.getWorld()).entrySet())
        {
            if (structure.getKey().contains(BukkitUtil.toVector(location)))
            {
                return structure.getValue();
            }
        }

        return null;
    }

    /**
     * Returns the structure located at the given location, according to the
     * loaded world metadata.
     *
     * @param location The location to check.
     *
     * @return The {@link Structure}, or {@code null} if no structure was found
     * at this place.
     */
    public static Region getRegionForStructureAt(final Location location)
    {
        if (!hasMetadataForWorld(location.getWorld())) return null;

        for (final Map.Entry<Region, Structure> structure : worldMetadata.get(location.getWorld()).entrySet())
        {
            if (structure.getKey().contains(BukkitUtil.toVector(location)))
            {
                return structure.getKey();
            }
        }

        return null;
    }

    /**
     * Bulk-check the structures at multiple locations.
     *
     * <p>This is better than {@link #getStructureAt(Location)} in a loop
     * because it will list the structures and check locations only once.</p>
     *
     * <p>Warning: the returned map can contains {@code null} values.</p>
     *
     * @param locations A map associating string identifiers to locations.
     *
     * @return A map associating the previous string identifiers to the
     * buildings at the given locations, or to {@code null} without structure.
     */
    public static Map<String, Structure> getStructuresAt(final Map<String, Location> locations)
    {
        // We first need to separate the locations in buckets, one bucket per world.
        final Map<World, Map<String, Location>> locationBuckets = new HashMap<>();
        for (final Map.Entry<String, Location> location : locations.entrySet())
        {
            // If there is no metadata for this world, we don't create a bucket.
            if (!hasMetadataForWorld(location.getValue().getWorld())) continue;

            if (!locationBuckets.containsKey(location.getValue().getWorld()))
                locationBuckets.put(location.getValue().getWorld(), new HashMap<String, Location>());

            locationBuckets.get(location.getValue().getWorld()).put(location.getKey(), location.getValue());
        }

        // Then we check structures for each world
        final Map<String, Structure> structures = new HashMap<>();

        // The map is before filled with nulls—they will be overridden later, if structures are found.
        for (final String key : locations.keySet())
            structures.put(key, null);

        for (final Map.Entry<World, Map<String, Location>> bucket : locationBuckets.entrySet())
        {
            for (final Map.Entry<Region, Structure> structure : worldMetadata.get(bucket.getKey()).entrySet())
            {
                for (final Map.Entry<String, Location> location : bucket.getValue().entrySet())
                {
                    if (structure.getKey().contains(BukkitUtil.toVector(location.getValue())))
                    {
                        structures.put(location.getKey(), structure.getValue());
                    }
                }
            }
        }

        return structures;
    }


    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent ev)
    {
        loadGenerationMetadata(ev.getWorld(), null);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent ev)
    {
        loadGenerationMetadata(ev.getPlayer().getWorld(), null);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent ev)
    {
        saveGenerationMetadata(ev.getWorld(), null);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        for (World world : Bukkit.getWorlds())
            if (hasMetadataForWorld(world))
                saveGenerationMetadata(world, null);
    }
}
