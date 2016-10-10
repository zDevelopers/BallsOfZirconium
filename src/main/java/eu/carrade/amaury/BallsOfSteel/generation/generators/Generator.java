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
package eu.carrade.amaury.BallsOfSteel.generation.generators;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.generation.utils.AbstractGenerationTool;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Location;

import java.util.Map;
import java.util.Random;


/**
 * A generator, used to generate a shape, paste a schematic...
 */
public abstract class Generator extends AbstractGenerationTool
{
    protected final boolean enabled;
    protected final float probability;
    protected final Vector offset;

    protected EditSession session = null;
    protected Location baseLocation = null;
    protected Random random = null;


    public Generator(final Map parameters)
    {
        enabled     = getValue(parameters, "enabled", boolean.class, true);
        probability = getValue(parameters, "probability", float.class, 1f);
        offset      = getValue(parameters, "offset", Vector.class, Vector.ZERO);
    }


    /**
     * Generates a thing.
     *
     * @param session If WorldEdit is used, changes should be made in this edit
     *                session.
     * @param base    The base vector for the generation.
     * @param random  A random numbers generator; it should be used as possible
     *                so the generated thing is constant for the same world
     *                seed.
     *
     * @return A {@link Region} containing all the changes, used after for
     * post-processing. {@code null} if nothing was generated (disabled,
     * probability failed, max blocks changed exception...).
     */
    public Region generate(final EditSession session, final Location base, final Random random)
    {
        if (!enabled || random.nextFloat() >= probability) return null;

        try
        {
            this.session = session;
            this.baseLocation = base.add(offset.getX(), offset.getY(), offset.getZ());
            this.random = random;

            return doGenerate();
        }
        catch (MaxChangedBlocksException e)
        {
            PluginLogger.error("Cannot generate ''{0}'': too many blocks changed.", e, getClass().getSimpleName());
            return null;
        }
    }

    /**
     * A description of the generator, with parameters values if relevant.
     * @return the description.
     */
    public String getDescription()
    {
        return (doDescription()
                + (!offset.equals(Vector.ZERO) ? " " + I.t("{gray}(offset: {0})", offset) : "")
                + (probability < 1 ? " " + I.t("{gray}(probability: {0})", probability) : "")).trim();
    }



    /**
     * Generates a thing.
     *
     * @return A {@link Region} containing all the changes, used after for
     * post-processing.
     */
    protected abstract Region doGenerate() throws MaxChangedBlocksException;


    /**
     * A description of the generator, with parameters values if relevant.
     * @return the description.
     */
    public abstract String doDescription();



    /**
     * @return The base location as a WorldEdit vector.
     */
    protected Vector baseVector()
    {
        return BukkitUtil.toVector(baseLocation);
    }

    /**
     * @return The base location as a Bukkit location.
     */
    protected Location baseLocation()
    {
        return baseLocation;
    }



    @ConfigurationValueHandler
    public static Generator handleGenerator(Map map) throws ConfigurationParseException
    {
        return handleGenerationTool(map, Generator.class);
    }
}
