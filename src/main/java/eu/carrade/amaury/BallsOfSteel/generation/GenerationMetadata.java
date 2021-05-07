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
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.generation.structures.GeneratedSphere;
import eu.carrade.amaury.BallsOfSteel.generation.structures.StaticBuilding;
import eu.carrade.amaury.BallsOfSteel.generation.structures.Structure;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.quartzlib.components.worker.Worker;
import fr.zcraft.quartzlib.components.worker.WorkerAttributes;
import fr.zcraft.quartzlib.components.worker.WorkerCallback;
import fr.zcraft.quartzlib.components.worker.WorkerRunnable;
import fr.zcraft.quartzlib.tools.Callback;
import fr.zcraft.quartzlib.tools.FileUtils;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@WorkerAttributes (name = "bos-generation-metadata-io")
public class GenerationMetadata extends Worker implements Listener
{
    private static final String TYPE_BUILDING = "building";
    private static final String TYPE_SPHERE = "sphere";
    private static final String TYPE_UNKNOWN = "unknown";

    private static final Gson GSON = new Gson();
    private static final Map<World, Map<Structure, Region>> worldMetadata = new ConcurrentHashMap<>();

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
     * Loads the metadata from the JSON dump for the given world.
     *
     * <p>Metadata are only loaded once. They will be queued for load, not
     * loaded instantaneously.</p>
     *
     * @param world    The world.
     * @param callback A callback to be executed when the metadata are loaded
     *                 and registered in {@link #worldMetadata}.
     */
    private static void loadGenerationMetadata(final World world, final Callback<Void> callback)
    {
        // If already loaded, skip
        if (hasMetadataForWorld(world))
        {
            if (callback != null) callback.call(null);
            return;
        }

        submitQuery(new WorkerRunnable<Map<Structure, Region>>()
        {
            @Override
            public Map<Structure, Region> run() throws Throwable
            {
                final File metadataFile = getGenerationMetadataFile(world);
                final String rawJSONMetadata = FileUtils.readFile(metadataFile).trim();

                // If there is nothing to load, skip too
                if (rawJSONMetadata.isEmpty()) return null;

                final JsonObject metadata = GSON.fromJson(rawJSONMetadata, JsonObject.class);
                final Map<Structure, Region> worldStructures = new HashMap<>();

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
                                    structure,
                                    new CuboidRegion(
                                            BukkitAdapter.adapt(world),
                                            WorldEditUtils.asBlockVector(lowestCorner),
                                            WorldEditUtils.asBlockVector(highestCorner)
                                    )
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
        }, new WorkerCallback<Map<Structure, Region>>()
        {
            @Override
            public void finished(final Map<Structure, Region> worldData)
            {
                if (worldData != null) worldMetadata.put(world, worldData);
                if (callback != null) callback.call(null);

                PluginLogger.info("Loaded metadata {0}", worldData);
                PluginLogger.info("{0}", worldMetadata);
            }

            @Override
            public void errored(final Throwable e)
            {
                PluginLogger.error("Unable to load Balls of Steel metadata for world {0}: malformed JSON metadata", e, world.getName());

                worldMetadata.put(world, new HashMap<>());
                if (callback != null) callback.call(null);
            }
        });
    }

    /**
     * Saves the loaded metadata as a JSON dump in the given world.
     *
     * <p>If no metadata are saved at all for this world (i.e. the world was never loaded),
     * the callback is called with {@code false}.</p>
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

        submitQuery(new WorkerRunnable<Void>()
        {
            @Override
            public Void run() throws Throwable
            {
                final File metadataFile = getGenerationMetadataFile(world);
                metadataFile.getParentFile().mkdirs();

                final JsonArray jsonStructures = new JsonArray();

                for (final Map.Entry<Structure, Region> structure : worldMetadata.get(world).entrySet())
                {
                    final JsonObject jsonStructure = new JsonObject();

                    final BlockVector3 low = structure.getValue().getMinimumPoint();
                    final BlockVector3 high = structure.getValue().getMinimumPoint();

                    jsonStructure.addProperty("name", structure.getKey().getName());
                    jsonStructure.addProperty("type", structure.getKey() instanceof StaticBuilding ? TYPE_BUILDING : (structure.getKey() instanceof GeneratedSphere ? TYPE_SPHERE : TYPE_UNKNOWN));
                    jsonStructure.addProperty("lowestCorner", low.getBlockX() + "," + low.getBlockY() + "," + low.getBlockZ());
                    jsonStructure.addProperty("highestCorner", high.getBlockX() + "," + high.getBlockY() + "," + high.getBlockZ());

                    jsonStructures.add(jsonStructure);
                }

                final JsonObject dump = new JsonObject();
                dump.add("structures", jsonStructures);

                FileUtils.writeFile(metadataFile, GSON.toJson(dump));
                PluginLogger.info(GSON.toJson(dump));

                return null;
            }
        }, new WorkerCallback<Void>() {
            @Override
            public void finished(Void unused)
            {
                if (callback != null) callback.call(true);
            }

            @Override
            public void errored(Throwable e)
            {
                PluginLogger.info("Unable to save structures metadata for world {0}", e, world.getName());
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
        loadGenerationMetadata(world, nothing -> {
            worldMetadata.computeIfAbsent(world, k -> new HashMap<>()).put(structure, region);
            saveGenerationMetadata(world, null);
        });
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
        final Structure structure = getStructureAt(location);
        if (structure == null) return false;

        Callback<Void> removeFunction = nothing -> {
            worldMetadata.get(location.getWorld()).remove(structure);
            saveGenerationMetadata(location.getWorld(), null);
        };

        loadGenerationMetadata(location.getWorld(), nothing -> {
            worldMetadata.get(location.getWorld()).remove(structure);
            saveGenerationMetadata(location.getWorld(), null);
        });

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
        final Map<Structure, Region> regions = worldMetadata.get(location.getWorld());
        if (regions == null) return null;

        for (final Map.Entry<Structure, Region> structure : regions.entrySet())
        {
            if (structure.getValue().contains(BukkitAdapter.asBlockVector(location)))
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
            for (final Map.Entry<Structure, Region> structure : worldMetadata.get(bucket.getKey()).entrySet())
            {
                for (final Map.Entry<String, Location> location : bucket.getValue().entrySet())
                {
                    if (structure.getValue().contains(BukkitAdapter.asBlockVector(location.getValue())))
                    {
                        structures.put(location.getKey(), structure.getKey());
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
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        loadGenerationMetadata(ev.getPlayer().getWorld(), null);
    }
}
