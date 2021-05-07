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
package eu.carrade.amaury.BallsOfSteel.generation.structures;


import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationMetadata;
import eu.carrade.amaury.BallsOfSteel.generation.postProcessing.PostProcessor;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Represents a static building generated at a given exact location to serve a specific purpose.
 */
public class StaticBuilding extends Structure
{
    private final BlockVector3 pasteLocation;

    private final File schematicFile;
    private final Clipboard building;

    private final Region privateRegion;

    private final List<PostProcessor> postProcessors = new ArrayList<>();


    /**
     * Constructs a static building.
     *
     * @param name The building name.
     * @param pasteLocation The paste location.
     * @param nothingUnder {@code true} to prevent any sphere generation under this building.
     * @param nothingAbove {@code true} to prevent any sphere generation above this building.
     * @param schematicFile The schematic file to load.
     *
     * @throws IOException If the schematic file cannot be loaded.
     */
    public StaticBuilding(final String name, final BlockVector3 pasteLocation, final boolean nothingUnder, final boolean nothingAbove, final File schematicFile) throws IOException
    {
        this.name = name;
        this.enabled = true;

        this.pasteLocation = pasteLocation;

        this.schematicFile = schematicFile;
        this.building = WorldEditUtils.loadSchematic(schematicFile);

        this.privateRegion = WorldEditUtils.getRegionForClipboardPastedAt(building, pasteLocation);

        try
        {
            if (nothingAbove) privateRegion.expand(BlockVector3.at(0, 256, 0));
            if (nothingUnder) privateRegion.expand(BlockVector3.at(0, -256, 0));
        }
        catch (final RegionOperationException e)
        {
            PluginLogger.warning("Cannot expand private region for static building {0}, using base region.", e, name);
        }
    }

    public boolean build(final World world, final Random random)
    {
        try
        {
            final Region region;

            try (final EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world)))
            {
                region = WorldEditUtils.pasteClipboard(session, building, pasteLocation, true);
            }

            for (final PostProcessor processor : postProcessors)
            {
                try (final EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world)))
                {
                    processor.process(session, region, random);
                }
                catch (final Throwable e)
                {
                    PluginLogger.error("Exception occurred while executing post-processor {0} for static building {1}", e, processor.getClass().getName(), name);
                }
            }

            GenerationMetadata.saveStructure(this, world, region);

            return true;
        }
        catch (final WorldEditException e)
        {
            PluginLogger.error("Cannot build static building {0}: too many blocks changed.", e, name);
            return false;
        }
    }


    /**
     * @return The building name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The building paste location.
     */
    public BlockVector3 getPasteLocation()
    {
        return pasteLocation;
    }

    /**
     * @return The schematic file.
     */
    public File getSchematicFile()
    {
        return schematicFile;
    }

    /**
     * @return The building data.
     */
    public Clipboard getBuilding()
    {
        return building;
    }

    /**
     * @return The region where nothing should be generated.
     */
    public Region getPrivateRegion()
    {
        return privateRegion;
    }


    /**
     * Adds a post-processor.
     * @param processor The post-processor.
     */
    public void addPostProcessor(final PostProcessor processor)
    {
        postProcessors.add(processor);
    }

    /**
     * Adds multiple post-processors.
     * @param processors The processors.
     */
    public void addPostProcessors(final Collection<PostProcessor> processors)
    {
        postProcessors.addAll(processors);
    }

    /**
     * @return The registered post processors.
     */
    public List<PostProcessor> getPostProcessors()
    {
        return Collections.unmodifiableList(postProcessors);
    }



    @ConfigurationValueHandler
    public static StaticBuilding handleStaticBuilding(final Map<?, ?> map) throws ConfigurationParseException
    {
        final String name = getValue(map, "name", String.class, "Unnamed static building");
        final String schematicPath = getValue(map, "schematic", String.class, null);
        final BlockVector3 pasteLocation = getValue(map, "pasteAt", BlockVector3.class, null);

        final boolean nothingAbove = getValue(map, "nothingAbove", boolean.class, false);
        final boolean nothingUnder = getValue(map, "nothingUnder", boolean.class, false);

        if (schematicPath == null || pasteLocation == null)
            throw new ConfigurationParseException("Both schematic and pasteAt are required", map);

        File schematicFile = new File(MapConfig.MAP_SCHEMATICS_DIRECTORY, schematicPath);
        StaticBuilding building;
        try
        {
            building = new StaticBuilding(name, pasteLocation, nothingUnder, nothingAbove, schematicFile);
        }
        catch (IOException e)
        {
            throw new ConfigurationParseException("Cannot load the schematic: I/O exception caught: " + e.getMessage(), schematicPath);
        }

        if (map.containsKey("postActions"))
        {
            building.addPostProcessors(ConfigurationValueHandlers.handleListValue(map.get("postActions"), PostProcessor.class));
        }

        return building;
    }
}
