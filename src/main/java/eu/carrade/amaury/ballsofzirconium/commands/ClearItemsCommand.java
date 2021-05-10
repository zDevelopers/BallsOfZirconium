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

import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;


@CommandInfo (name = "clearitems")
public class ClearItemsCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final World world;

        if (sender instanceof Player)
        {
            world = ((Player) sender).getWorld();
        }
        else if (sender instanceof BlockCommandSender)
        {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        else
        {
            world = BallsOfZirconium.get().getGameManager().getGameWorld();
        }

        if (world != null)
        {
            for (Entity entity : world.getEntities())
            {
                if (entity.getType() == EntityType.DROPPED_ITEM)
                {
                    switch (((Item) entity).getItemStack().getType())
                    {
                        case DIAMOND:
                        case DIAMOND_AXE:
                        case DIAMOND_HORSE_ARMOR:
                        case DIAMOND_BLOCK:
                        case DIAMOND_BOOTS:
                        case DIAMOND_CHESTPLATE:
                        case DIAMOND_HELMET:
                        case DIAMOND_HOE:
                        case DIAMOND_LEGGINGS:
                        case DIAMOND_ORE:
                        case DIAMOND_PICKAXE:
                        case DIAMOND_SHOVEL:
                        case DIAMOND_SWORD:
                            continue;
                        default:
                            entity.remove();
                    }
                }
            }
            success(I.t("{cs}All items, diamonds-based items excepted, were destroyed in the world {0}.", world.getName()));
        }
        else
        {
            error(I.t("{ce}Cannot clear the items from the console if the game's world is not set (i.e. world not set in the config and game not started)."));
        }
    }
}
