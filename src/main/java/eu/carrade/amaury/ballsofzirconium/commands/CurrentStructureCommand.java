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
package eu.carrade.amaury.ballsofzirconium.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium;
import eu.carrade.amaury.ballsofzirconium.generation.GenerationData;
import eu.carrade.amaury.ballsofzirconium.generation.structures.StaticBuilding;
import eu.carrade.amaury.ballsofzirconium.generation.structures.Structure;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;

import java.util.List;


@CommandInfo (name = "current-structure", usageParameters = "[select|forget]")
public class CurrentStructureCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        if (args.length == 0)
        {
            current();
        }
        else
        {
            switch (args[0].toLowerCase())
            {
                case "forget":
                    forget();
                    break;

                case "select":
                    select();
                    break;

                default:
                    throwInvalidArgument(I.t("Unknown currentstructure sub-command {0}", args[0]));
            }
        }
    }

    private void current() throws CommandException
    {
        final Structure currentStructure = GenerationData.getStructureAt(playerSender().getLocation());

        if (currentStructure == null)
        {
            warning(I.t("There is no structure here."));
        }
        else if (currentStructure instanceof StaticBuilding)
        {
            info(I.t("You are in a building: {0}", currentStructure.getName()));
        }
        else
        {
            info(I.t("You are in a sphere: {0}", currentStructure.getName()));
        }
    }

    private void forget() throws CommandException
    {
        if (GenerationData.forgetStructureAt(playerSender().getLocation()))
        {
            success(I.t("The structure located where you are was removed from this world's metadata."));
        }
        else
        {
            warning(I.t("There is no structure here."));
        }
    }

    private void select() throws CommandException
    {
        if (!BallsOfZirconium.get().getWorldEditDependency().isEnabled())
        {
            warning(I.t("WorldEdit is required to use this command."));
            return;
        }

        final Region region = GenerationData.getRegionForStructureAt(playerSender().getLocation());

        if (region == null)
        {
            warning(I.t("There is no structure here."));
        }
        else
        {
            final LocalSession session = BallsOfZirconium.get().getWorldEditDependency().getWE().getSession(playerSender());
            final RegionSelector regionSelector = session.getRegionSelector(BukkitAdapter.adapt(playerSender().getWorld()));

            regionSelector.selectPrimary(region.getMinimumPoint(), PermissiveSelectorLimits.getInstance());
            regionSelector.selectSecondary(region.getMaximumPoint(), PermissiveSelectorLimits.getInstance());

            session.dispatchCUISelection(BallsOfZirconium.get().getWorldEditDependency().getWE().wrapPlayer(playerSender()));

            // Required to update the selection display if the player is using WE:CUI.
            // If it's not, the command is harmless and without output anyway.
            playerSender().performCommand("we cui");

            info(I.t("WorldEdit selection updated."));
        }
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
            return getMatchingSubset(args[0].toLowerCase(), "select", "forget");

        return null;
    }
}
