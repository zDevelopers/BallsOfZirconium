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

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeamManager;
import eu.carrade.amaury.BallsOfSteel.utils.BoSUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@CommandInfo (name = "team")
public class TeamsCommand extends Command
{
    private final List<String> colors = new ArrayList<>();
    private final BoSTeamManager tm = BallsOfSteel.get().getTeamManager();

    public TeamsCommand()
    {
        this.colors.add("aqua");
        this.colors.add("black");
        this.colors.add("blue");
        this.colors.add("darkaqua");
        this.colors.add("darkblue");
        this.colors.add("darkgray");
        this.colors.add("darkgreen");
        this.colors.add("darkpurple");
        this.colors.add("darkred");
        this.colors.add("gold");
        this.colors.add("gray");
        this.colors.add("green");
        this.colors.add("lightpurple");
        this.colors.add("red");
        this.colors.add("white");
        this.colors.add("yellow");
    }


    @Override
    protected void run() throws CommandException
    {
        // No action provided: doc
        if (args.length == 0)
        {
            if (sender instanceof Player) sender.sendMessage("");
            sender.sendMessage(I.t("{aqua}------ Team commands ------"));
            sender.sendMessage(I.t("{cc}/bos team add <color> {ci}: adds a team with the provided color."));
            sender.sendMessage(I.t("{cc}/bos team add <color> <name ...> {ci}: adds a named team with the provided name and color."));
            sender.sendMessage(I.t("{cc}/bos team remove <name ...> {ci}: removes a team"));
            sender.sendMessage(I.t("{cc}/bos team spawn [x,y,z | x,z] <name ...> {ci}: sets the spawn point of the team (location of the sender or coordinates)."));
            sender.sendMessage(I.t("{cc}/bos team chest [x,y,z] <name ...> {ci}: sets the chest of this team (where the diamonds will be stored), using the given coordinates or the block the sender is looking at."));
            sender.sendMessage(I.t("{cc}/bos team join <player> <teamName ...>{ci}: adds a player inside the given team. The name of the team is it color, or the explicit name given."));
            sender.sendMessage(I.t("{cc}/bos team leave <player> {ci}: removes a player from his team."));
            sender.sendMessage(I.t("{cc}/bos team list {ci}: lists the teams and their players."));
            sender.sendMessage(I.t("{cc}/bos team reset {ci}: removes all teams."));
            if (sender instanceof Player) sender.sendMessage("");
        }
        else
        {
            switch (args[0].toLowerCase())
            {
                case "add":     handleAdd();     break;
                case "remove":  handleRemove();  break;
                case "spawn":   handleSpawn();   break;
                case "chest":   handleChest();   break;
                case "join":    handleJoin();    break;
                case "leave":   handleLeave();   break;
                case "list":    handleList();    break;
                case "reset":   handleReset();   break;

                default:
                    error(I.t("{ce}Unknown command. See /bos team for available commands."));
            }
        }
    }

    private void handleAdd() throws CommandException
    {
        // /bos team add <color>
        if (args.length == 2)
        {

            ChatColor color = BallsOfSteel.get().getTeamManager().getChatColorByName(args[1]);

            if (color == null)
            {
                error(I.t("{ce}Unable to add the team, check the color name. Tip: use Tab to autocomplete."));
            }
            else
            {
                try
                {
                    tm.addTeam(color, args[1].toLowerCase());
                }
                catch (IllegalArgumentException e)
                {
                    error(I.t("{ce}This team already exists."));
                }
                success(I.t("{cs}Team {0}{cs} added.", color.toString() + args[1]));
            }

        }

        // /bos team add <color> <name ...>
        else if (args.length >= 3)
        {

            ChatColor color = BallsOfSteel.get().getTeamManager().getChatColorByName(args[1]);

            if (color == null)
            {
                error(I.t("{ce}Unable to add the team, check the color name. Tip: use Tab to autocomplete."));
            }
            else
            {
                String name = BoSUtils.getStringFromCommandArguments(args, 2);

                try
                {
                    tm.addTeam(color, name);
                }
                catch (IllegalArgumentException e)
                {
                    error(I.t("{ce}This team already exists."));
                    return;
                }
                success(I.t("{cs}Team {0}{cs} added.", color.toString() + name));
            }

        }
        else
        {
            error(I.t("{ce}Syntax error, see /uh team."));
        }
    }

    private void handleRemove() throws CommandException
    {
        // /bos team remove <teamName>
        if (args.length >= 2)
        {
            String name = BoSUtils.getStringFromCommandArguments(args, 1);
            if (!tm.removeTeam(name))
            {
                error(I.t("{ce}This team does not exists."));
            }
            else
            {
                success(I.t("{cs}Team {0} deleted.", name));
            }
        }
        else
        {
            error(I.t("{ce}Syntax error, see /uh team."));
        }
    }

    private void handleSpawn() throws CommandException
    {
        Location spawnPoint = null;

        World world;
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
            world = BallsOfSteel.get().getServer().getWorlds().get(0);
        }

        String nameTeamWithoutCoords = null, nameTeamWithCoords = null, teamName = null;
        if (args.length >= 2)
        {
            nameTeamWithCoords = BoSUtils.getStringFromCommandArguments(args, 2);
        }
        if (args.length >= 1)
        {
            nameTeamWithoutCoords = BoSUtils.getStringFromCommandArguments(args, 1);
        }

        // /bos spawn <team ...>
        if (BallsOfSteel.get().getTeamManager().getTeam(nameTeamWithoutCoords) != null)
        {
            if (!(sender instanceof Player))
            {
                error(I.t("{ce}You must specify the coordinates from the console."));
            }

            spawnPoint = ((Player) sender).getLocation();
            teamName = nameTeamWithoutCoords;
        }

        // /bos spawn <x,y,z> <team ...>
        else if (BallsOfSteel.get().getTeamManager().getTeam(nameTeamWithCoords) != null)
        {
            teamName = nameTeamWithCoords;

            String[] coordinates = args[1].split(",");

            if (coordinates.length == 2)
            {
                try
                {
                    double x = Double.valueOf(coordinates[0]);
                    double z = Double.valueOf(coordinates[1]);

                    spawnPoint = new Location(world, x, world.getHighestBlockYAt(Location.locToBlock(x), Location.locToBlock(z)), z);
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}The coordinates need to be numbers."));
                }
            }
            else if (coordinates.length >= 3)
            {
                try
                {
                    double x = Double.valueOf(coordinates[0]);
                    double y = Double.valueOf(coordinates[1]);
                    double z = Double.valueOf(coordinates[2]);

                    spawnPoint = new Location(world, x, y, z);
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}The coordinates need to be numbers."));
                }
            }
            else
            {
                error(I.t("{ce}Syntax error, see /uh team."));
            }
        }

        // Unknown team
        if (teamName == null)
        {
            error(I.t("{ce}This team does not exists."));
        }

        BoSTeam team = BallsOfSteel.get().getTeamManager().getTeam(teamName); // This cannot be null, here.

        team.setSpawnPoint(spawnPoint);

        success(I.t("{cs}The spawn point of the team {0}{cs} is now {1};{2};{3}.", team.getDisplayName(), String.valueOf(spawnPoint.getBlockX()), String.valueOf(spawnPoint.getBlockY()), String.valueOf(spawnPoint.getBlockZ())));
    }

    private void handleChest() throws CommandException
    {
        Location chestLocation = null;

        World world;
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
            world = BallsOfSteel.get().getServer().getWorlds().get(0);
        }

        String nameTeamWithoutCoordinates = null, nameTeamWithCoordinates = null, teamName = null;
        if (args.length >= 2)
        {
            nameTeamWithCoordinates = BoSUtils.getStringFromCommandArguments(args, 2);
        }
        if (args.length >= 1)
        {
            nameTeamWithoutCoordinates = BoSUtils.getStringFromCommandArguments(args, 1);
        }

        // /bos chest <team ...>
        if (BallsOfSteel.get().getTeamManager().getTeam(nameTeamWithoutCoordinates) != null)
        {
            if (!(sender instanceof Player))
            {
                error(I.t("{ce}You must specify the coordinates from the console."));
            }

            teamName = nameTeamWithoutCoordinates;

            final Block chest = ((Player) sender).getTargetBlock((Set<Material>) null, 10);
            if (chest != null)
            {
                if (chest.getType() == Material.CHEST || chest.getType() == Material.TRAPPED_CHEST)
                {
                    chestLocation = chest.getLocation();
                }
                else
                {
                    error(I.t("{ce}You are not looking at a chest usable by more than one player!"));
                }
            }
            else
            {
                error(I.t("{ce}You are not looking at anything..."));
            }
        }

        // /bos chest <x,y,z> <team ...>
        else if (BallsOfSteel.get().getTeamManager().getTeam(nameTeamWithCoordinates) != null)
        {
            teamName = nameTeamWithCoordinates;

            String[] coordinates = args[1].split(",");

            if (coordinates.length >= 3)
            {
                try
                {
                    double x = Double.valueOf(coordinates[0]);
                    double y = Double.valueOf(coordinates[1]);
                    double z = Double.valueOf(coordinates[2]);

                    chestLocation = new Location(world, x, y, z);
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}The coordinates need to be numbers."));
                }
            }
            else
            {
                error(I.t("{ce}Syntax error, see /uh team."));
            }
        }

        // Unknown team
        if (teamName == null)
        {
            error(I.t("{ce}This team does not exists."));
        }

        final BoSTeam team = BallsOfSteel.get().getTeamManager().getTeam(teamName); // This cannot be null, here.

        try
        {
            team.setChest(chestLocation);
        }
        catch (IllegalArgumentException e)
        {
            error(I.t("{ce}There isn't any chest usable by more than one player here."));
        }

        success(I.t("{cs}This chest (at {1};{2};{3}) is now the private chest of the team {0}{cs}.", team.getDisplayName(), String.valueOf(chestLocation.getBlockX()), String.valueOf(chestLocation.getBlockY()), String.valueOf(chestLocation.getBlockZ())));
    }

    private void handleJoin() throws CommandException
    {
        // /bos team join <player> <teamName>
        if (args.length >= 3)
        {
            Player player = BallsOfSteel.get().getServer().getPlayer(args[1]);
            String teamName = BoSUtils.getStringFromCommandArguments(args, 2);

            if (player == null || !player.isOnline())
            {
                error(I.t("{ce}Unable to add the player {0} to the team {1}. The player must be connected.", args[1], teamName));
            }
            else
            {
                try
                {
                    tm.addPlayerToTeam(teamName, player);
                }
                catch (IllegalArgumentException e)
                {
                    error(I.t("{ce}This team does not exists."));
                }
                catch (RuntimeException e)
                {
                    error(I.t("{ce}The team {0}{ce} is full!", teamName));
                }

                final BoSTeam team = BallsOfSteel.get().getTeamManager().getTeam(teamName);
                success(I.t("{cs}The player {0} was successfully added to the team {1}", args[1], team.getDisplayName()));
            }
        }
        else
        {
            error(I.t("{ce}Syntax error, see /bos team."));
        }
    }

    private void handleLeave() throws CommandException
    {
        // /bos team leave <player>
        if (args.length == 2)
        {

            Player player = BallsOfSteel.get().getServer().getPlayer(args[1]);

            if (player == null || !player.isOnline())
            {
                error(I.t("{ce}The player {0} is disconnected!", args[1]));
            }
            else
            {
                tm.removePlayerFromTeam(player);
                success(I.t("{cs}The player {0} was successfully removed from his team.", args[1]));
            }
        }
        else
        {
            error(I.t("{ce}Syntax error, see /uh team."));
        }
    }

    private void handleList() throws CommandException
    {
        if (tm.getTeams().size() == 0)
        {
            error(I.t("{ce}There isn't any team to show."));
        }

        for (final BoSTeam team : tm.getTeams())
        {
            sender.sendMessage(I.t("{0} ({1} players)", team.getDisplayName(), ((Integer) team.getPlayers().size()).toString()));
            for (final OfflinePlayer player : team.getPlayers())
            {
                String bullet;
                if (player.isOnline())
                {
                    bullet = I.t("{green} • ");
                }
                else
                {
                    bullet = I.t("{red} • ");
                }

                sender.sendMessage(bullet + I.t("{0}", player.getName()));
            }
        }
    }

    private void handleReset() throws CommandException
    {
        tm.reset();
        success(I.t("{cs}All teams where removed."));
    }


    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
        {
            return getMatchingSubset(args[0], "add", "remove", "spawn", "chest", "join", "leave", "list", "reset");
        }

        else if (args.length == 2)
        {
            // Autocompletion for colors
            if (args[0].equalsIgnoreCase("add"))
            {
                return getMatchingSubset(colors, args[1]);
            }

            // Autocompletion for teams names
            else if (args[1].equalsIgnoreCase("remove"))
            {
                ArrayList<String> teamNames = new ArrayList<String>();
                for (BoSTeam team : BallsOfSteel.get().getTeamManager().getTeams())
                {
                    teamNames.add(team.getName());
                }
                return getMatchingSubset(teamNames, args[1]);
            }
        }

        return null;
    }
}
