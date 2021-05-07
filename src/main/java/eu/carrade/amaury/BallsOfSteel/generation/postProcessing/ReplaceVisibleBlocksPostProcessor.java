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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.PluginLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ReplaceVisibleBlocksPostProcessor extends ReplacePostProcessor
{
    public ReplaceVisibleBlocksPostProcessor(final Map<?, ?> parameters)
    {
        super(parameters);
    }

    @Override
    protected void doProcess() throws WorldEditException
    {
        final Pattern replacementPattern = WorldEditUtils.parsePattern(session.getWorld(), toPattern);

        final RegionFunction replaceBlocksIfVisible = position -> {
            final BundledBlockData blockData = BundledBlockData.getInstance();
            final Set<BlockVector3> neighbors = new HashSet<>();

            neighbors.add(position.add(1, 0, 0));
            neighbors.add(position.add(0, 1, 0));
            neighbors.add(position.add(0, 0, 1));
            neighbors.add(position.add(-1, 0, 0));
            neighbors.add(position.add(0, -1, 0));
            neighbors.add(position.add(0, 0, -1));

            boolean isBlockVisible = false;
            for (BlockVector3 neighbor : neighbors)
            {
                BlockState blockNeighbor = session.getBlock(neighbor);
                final BlockMaterial material = blockData.getMaterialById(blockNeighbor.getBlockType().getId());
                if (material == null) continue;

                if (material.isAir() || !material.isOpaque() || material.isTranslucent())
                {
                    isBlockVisible = true;
                    break;
                }
            }

            if (isBlockVisible)
            {
                session.setBlock(position, replacementPattern.applyBlock(position));
            }

            return isBlockVisible;
        };

        final Mask blocksMask = WorldEditUtils.parseMask(session.getWorld(), fromMask, session);
        final RegionVisitor blocksVisitor = new RegionVisitor(region, new RegionMaskingFilter(blocksMask, replaceBlocksIfVisible));

        try
        {
            Operations.complete(blocksVisitor);
        }
        catch (WorldEditException e)
        {
            PluginLogger.info("Unable to replace visible blocks", e);
        }
    }

    @Override
    public String doName()
    {
        return I.t("Visible blocks replacement");
    }

    @Override
    public List<String> doSettingsDescription()
    {
        return Arrays.asList(
                I.t("From: {0}", fromMask),
                I.t("To: {0}", toPattern)
        );
    }
}
