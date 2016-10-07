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
package eu.carrade.amaury.BallsOfSteel.generation.postProcessing;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.generation.AbstractGenerationTool;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;

import java.util.Map;
import java.util.Random;


/**
 * A post-processor. This will work on a previously-generated region and apply some filters, replacements, things on it.
 */
public abstract class PostProcessor extends AbstractGenerationTool
{
    private final boolean enabled;
    private final float probability;

    private final Vector subRegionPos1;
    private final Vector subRegionPos2;


    public PostProcessor(final Map parameters)
    {
        enabled     = getValue(parameters, "enabled", boolean.class, true);
        probability = getValue(parameters, "probability", float.class, 1f);

        final Map regionParameters = getValue(parameters, "region", Map.class, null);
        if (regionParameters != null)
        {
            subRegionPos1 = getValue(regionParameters, "subPos1", Vector.class, Vector.ZERO);
            subRegionPos2 = getValue(regionParameters, "subPos2", Vector.class, Vector.ZERO);
        }
        else subRegionPos1 = subRegionPos2 = null;
    }


    /**
     * Applies a post-processing to a region.
     *
     * @param session If WorldEdit is used, changes should be made in this edit
     *                session.
     * @param region  The region where the post-processing should be applied.
     * @param random  A random numbers generator; it should be used as possible
     *                so the generated thing is constant for the same world
     *                seed.
     */
    public void process(final EditSession session, final Region region, final Random random)
    {
        if (!enabled || random.nextFloat() >= probability) return;

        // This processing is only applied to a sub region
        if (!subRegionPos1.equals(Vector.ZERO) || !subRegionPos2.equals(Vector.ZERO))
        {
            final Vector subRealPos1 = region.getMinimumPoint().add(subRegionPos1);
            final Vector subRealPos2 = region.getMinimumPoint().add(subRegionPos2);

            final Region subRegion = new CuboidRegion(
                    region.getWorld(),
                    Vector.getMaximum(region.getMinimumPoint(), Vector.getMinimum(subRealPos1, subRealPos2)),
                    Vector.getMinimum(region.getMinimumPoint(), Vector.getMaximum(subRealPos1, subRealPos2))
            );

            doProcess(session, subRegion, random);
        }
        else
        {
            doProcess(session, region, random);
        }
    }

    /**
     * Applies a post-processing to a region.
     *
     * @param session If WorldEdit is used, changes should be made in this edit
     *                session.
     * @param region  The region where the post-processing should be applied.
     * @param random  A random numbers generator; it should be used as possible
     *                so the generated thing is constant for the same world
     *                seed.
     */
    protected abstract void doProcess(EditSession session, Region region, Random random);

    /**
     * A description of the post-processor, with parameters values if relevant.
     * @return the description.
     */
    public abstract String getDescription();



    @ConfigurationValueHandler
    public static PostProcessor handlePostProcessor(Map map) throws ConfigurationParseException
    {
        return handleGenerationTool(map, PostProcessor.class);
    }
}
