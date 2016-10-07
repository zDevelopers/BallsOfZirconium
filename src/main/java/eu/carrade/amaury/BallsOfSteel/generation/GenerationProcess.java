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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.generators.Generator;
import eu.carrade.amaury.BallsOfSteel.generation.postProcessing.PostProcessor;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Represents a generation process to be applied by a populator somewhere.
 */
public class GenerationProcess extends AbstractGenerationTool
{
    private final String name;
    private boolean enabled;

    private final List<Generator> generators = new ArrayList<>();
    private final List<PostProcessor> postProcessors = new ArrayList<>();


    public GenerationProcess(final String name)
    {
        this(name, true);
    }

    public GenerationProcess(final String name, final boolean enabled)
    {
        this.name = name;
        this.enabled = enabled;
    }


    public String getName()
    {
        return name;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    public void addGenerator(final Generator generator)
    {
        generators.add(generator);
    }

    public void addPostProcessor(final PostProcessor processor)
    {
        postProcessors.add(processor);
    }

    public void addGenerators(final Collection<Generator> generators)
    {
        this.generators.addAll(generators);
    }

    public void addPostProcessors(final Collection<PostProcessor> processors)
    {
        postProcessors.addAll(processors);
    }

    public List<Generator> getGenerators()
    {
        return Collections.unmodifiableList(generators);
    }

    public List<PostProcessor> getPostProcessors()
    {
        return Collections.unmodifiableList(postProcessors);
    }


    /**
     * Applies this generation process to the given location.
     *
     * @param location The base location for generation.
     * @param random A source of randomness.
     * @return A region containing the modified blocks.
     */
    public Region applyAt(Location location, Random random)
    {
        final List<Region> affectedRegions = new ArrayList<>();
        final EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitUtil.getLocalWorld(location.getWorld()), Integer.MAX_VALUE);

        for (final Generator generator : generators)
        {
            try
            {
                final Region affected = generator.generate(session, location, random);
                if (affected != null) affectedRegions.add(affected);
            }
            catch (final Exception e)
            {
                PluginLogger.error("Exception occurred while executing generator {0}", e, generator.getClass().getName());
            }
        }

        final Region globallyAffectedRegion = new RegionIntersection(affectedRegions);

        for (final PostProcessor processor : postProcessors)
        {
            try
            {
                processor.process(session, globallyAffectedRegion, random);
            }
            catch (final Exception e)
            {
                PluginLogger.error("Exception occurred while executing post-processor {0}", e, processor.getClass().getName());
            }
        }

        return globallyAffectedRegion;
    }



    @ConfigurationValueHandler
    public static GenerationProcess handleGenerationProcess(final Map map) throws ConfigurationParseException
    {
        final String name = getValue(map, "name", String.class, "Unnamed generation process");
        final boolean enabled = getValue(map, "enabled", boolean.class, true);

        final GenerationProcess process = new GenerationProcess(name, enabled);

        if (map.containsKey("rules"))
        {
            process.addGenerators(ConfigurationValueHandlers.handleListValue(map.get("rules"), Generator.class));
        }
        if (map.containsKey("postActions"))
        {
            process.addPostProcessors(ConfigurationValueHandlers.handleListValue(map.get("postActions"), PostProcessor.class));
        }

        return process;
    }

    @ConfigurationValueHandler
    public static Pattern handleWEPattern(final Object pattern) throws ConfigurationParseException
    {
        try
        {
            final ParserContext parserContext = new ParserContext();

            parserContext.setWorld(BukkitUtil.getLocalWorld(MapConfig.WORLD.get()));
            parserContext.setActor(new BukkitCommandSender(BallsOfSteel.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
            parserContext.setExtent(null);
            parserContext.setSession(null);

            return WorldEdit.getInstance().getPatternFactory().parseFromInput(pattern.toString(), parserContext);
        }
        catch (InputParseException e)
        {
            throw new ConfigurationParseException("Invalid WorldEdit pattern: " + e.getMessage(), pattern);
        }
    }
}
