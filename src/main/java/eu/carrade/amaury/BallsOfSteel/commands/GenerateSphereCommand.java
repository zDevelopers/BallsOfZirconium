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
package eu.carrade.amaury.BallsOfSteel.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.commands.helpers.SpheresRelatedCommand;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationProcess;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


@CommandInfo (name = "generatesphere", usageParameters = "<nameWithoutSpaces> [posX, posY, posZ [, world]] [-s]")
public class GenerateSphereCommand extends SpheresRelatedCommand
{
    @Override
    protected void run() throws CommandException
    {
        if (!BallsOfSteel.get().getGenerationManager().isEnabled())
            error(I.t("Cannot use generation-related tools: generation disabled (either it's disabled in map.yml or WorldEdit is missing)."));

        boolean selectAfter = false;
        if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("-s"))
        {
            selectAfter = true;
            args = Arrays.copyOf(args, args.length - 1, String[].class);
        }

        if (args.length < 1)
            throwInvalidArgument(I.t("Sphere name required."));

        final GenerationProcess generator = getGenerationProcessParameter(0);
        final Location baseLocation;

        if (args.length == 1)
        {
            baseLocation = playerSender().getLocation();
        }
        else
        {
            if (args.length < 4) error(I.t("You must provide 3 coordinates or not at all."));

            final String rawX = args[1].trim();
            final String rawY = args[2].trim();
            final String rawZ = args[3].trim();

            final double x, y, z;
            final World world;

            x = (rawX.startsWith("~") ? playerSender().getLocation().getX() : 0) + parseDouble(rawX);
            y = (rawY.startsWith("~") ? playerSender().getLocation().getY() : 0) + parseDouble(rawY);
            z = (rawZ.startsWith("~") ? playerSender().getLocation().getZ() : 0) + parseDouble(rawZ);

            if (args.length >= 5)
            {
                world = Bukkit.getWorld(args[4]);
                if (world == null) error(I.t("No world {0} known.", args[4]));
            }
            else
            {
                if (sender instanceof Player)
                    world = playerSender().getWorld();
                else
                    world = BallsOfSteel.get().getGameManager().getGameWorld();
            }

            baseLocation = new Location(world, x, y, z);
        }

        final long time = System.currentTimeMillis();
        final Region region = generator.applyAt(baseLocation, new Random(), WorldEditUtils.newEditSession(baseLocation.getWorld(), sender instanceof Player ? (Player) sender : null));

        info(I.t("{gray}{0} generated in {1} ms. {2}", generator.getName(), System.currentTimeMillis() - time, selectAfter ? I.t("WorldEdit selection updated.") : ""));

        if (selectAfter && sender instanceof Player)
        {
            final LocalSession session = BallsOfSteel.get().getWorldEditDependency().getWE().getSession(playerSender());
            final RegionSelector regionSelector = session.getRegionSelector((com.sk89q.worldedit.world.World) BukkitUtil.getLocalWorld(baseLocation.getWorld()));

            regionSelector.selectPrimary(region.getMinimumPoint(), PermissiveSelectorLimits.getInstance());
            regionSelector.selectSecondary(region.getMaximumPoint(), PermissiveSelectorLimits.getInstance());

            session.dispatchCUISelection(BallsOfSteel.get().getWorldEditDependency().getWE().wrapPlayer(playerSender()));

            // Required to update the selection display if the player is using WE:CUI.
            // If it's not, the command is harmless and without output anyway.
            playerSender().performCommand("we cui");
        }
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        // Generators names
        if (args.length == 1)
        {
            return getMatchingGenerationProcesses(args[0]);
        }

        // Worlds
        else if (args.length == 5)
        {
            return getMatchingWorlds(args[4]);
        }

        else return null;
    }

    private double parseDouble(String doubleNumber) throws CommandException
    {
        if (doubleNumber.equals("~")) doubleNumber += "0"; // ~ is an alias for ~0
        final String rawNumber = doubleNumber.replace("~", "");

        try
        {
            return Double.valueOf(rawNumber);
        }
        catch (NumberFormatException e)
        {
            throwInvalidArgument(I.t("{0} is not a number", rawNumber));
            return 0;
        }
    }
}
