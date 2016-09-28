/**
 * Plugin UltraHardcore (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014
 * Amaury Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.BallsOfSteel;

import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BoSTeamChatManager
{
    private BallsOfSteel p = null;

    private List<UUID> teamChatLocked = new ArrayList<>();
    private Map<UUID, BoSTeam> otherTeamChatLocked = new HashMap<>();

    public BoSTeamChatManager(BallsOfSteel p)
    {
        this.p = p;
    }

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     */
    public void sendTeamMessage(Player sender, String message)
    {
        sendTeamMessage(sender, message, null);
    }

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     * @param team If not null, this message will be considered as an external message from another player to this team.
     */
    public void sendTeamMessage(Player sender, String message, BoSTeam team)
    {

        // Permission check
        if (team == null && !sender.hasPermission("uh.teamchat.self"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to send a private message to your team."));
            return;
        }
        if (team != null && !sender.hasPermission("uh.teamchat.others"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to enter in the private chat of another team."));
            return;
        }

        final String rawMessage;
        final BoSTeam recipient;

        if (team == null)
        {
            rawMessage = I.t("{gold}[{0}{gold} -> his team] {reset}{1}", sender.getDisplayName(), message);
            recipient = p.getTeamManager().getTeamForPlayer(sender);

            if (recipient == null)
            {
                sender.sendMessage(I.t("{ce}You are not in a team!"));
                return;
            }
        }
        else
        {
            rawMessage = I.t("{gold}[{0}{gold} -> team {1}{gold}] {reset}{2}", sender.getDisplayName(), team.getDisplayName(), message);
            recipient = team;
        }

        sendRawTeamMessage(sender, rawMessage, recipient);
    }

    /**
     * Sends a raw team-message from the given player.
     *
     * @param sender The sender of this message.
     * @param rawMessage The raw message to be sent.
     * @param team The recipient of this message.
     */
    private void sendRawTeamMessage(final Player sender, String rawMessage, BoSTeam team)
    {

        // The message is sent to the players of the team...
        for (final Player player : team.getOnlinePlayers())
        {
            player.sendMessage(rawMessage);
        }

        // ... to the spies ...
        if (otherTeamChatLocked.containsValue(team))
        {
            for (UUID playerId : otherTeamChatLocked.keySet())
            {
                // The message is only sent to the spies not in the team, to avoid double messages
                if (otherTeamChatLocked.get(playerId).equals(team) && !team.containsPlayer(playerId))
                {
                    p.getServer().getPlayer(playerId).sendMessage(rawMessage);
                }
            }
        }

        // ... and to the console.
        if (p.getConfig().getBoolean("logTeamChat"))
        {
            p.getServer().getConsoleSender().sendMessage(rawMessage);
        }
    }

    /**
     * Sends a global message from the given player.
     *
     * @param sender The sender of this message.
     * @param message The message to be sent.
     */
    public void sendGlobalMessage(Player sender, String message)
    {
        // This message will be sent synchronously.
        // The players' messages are sent asynchronously.
        // That's how we differentiates the messages sent through /g and the messages sent using
        // the normal chat.

        sender.chat(message);
    }


    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @return true if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(Player player)
    {
        return toggleChatForPlayer(player, null);
    }

    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @param team The team to chat with. If null, the player's team will be used.
     * @return true if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final Player player, BoSTeam team)
    {

        // Permission check
        if (team == null && !player.hasPermission("uh.teamchat.self"))
        {
            player.sendMessage(I.t("{ce}You are not allowed to send a private message to your team."));
            return false;
        }
        if (team != null && !player.hasPermission("uh.teamchat.others"))
        {
            player.sendMessage(I.t("{ce}You are not allowed to enter in the private chat of another team."));
            return false;
        }


        // If the team is not null, we will always go to the team chat
        // Else, normal toggle

        if (team != null)
        {
            // if the player was in another team chat before, we removes it.
            teamChatLocked.remove(player.getUniqueId());
            otherTeamChatLocked.put(player.getUniqueId(), team);

            return true;
        }

        else
        {
            if (isAnyTeamChatEnabled(player))
            {
                teamChatLocked.remove(player.getUniqueId());
                otherTeamChatLocked.remove(player.getUniqueId());

                return false;
            }
            else
            {
                teamChatLocked.add(player.getUniqueId());

                return true;
            }
        }
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @param team If non-null, this will check if the given player is spying the current team.
     * @return
     */
    public boolean isTeamChatEnabled(Player player, BoSTeam team)
    {
        if (team == null)
        {
            return teamChatLocked.contains(player.getUniqueId());
        }
        else
        {
            BoSTeam lockedTeam = this.otherTeamChatLocked.get(player.getUniqueId());
            BoSTeam playerTeam = p.getTeamManager().getTeamForPlayer(player);
            return (lockedTeam != null && lockedTeam.equals(team)) || (playerTeam != null && playerTeam.equals(team));
        }
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @return
     */
    public boolean isTeamChatEnabled(Player player)
    {
        return this.isTeamChatEnabled(player, null);
    }

    /**
     * Returns true if the given player is in the team chat of another team.
     *
     * @param player The player.
     * @return
     */
    public boolean isOtherTeamChatEnabled(Player player)
    {
        return otherTeamChatLocked.containsKey(player.getUniqueId());
    }

    /**
     * Returns true if a team chat is enabled for the given player.
     *
     * @param player The player.
     * @return
     */
    public boolean isAnyTeamChatEnabled(Player player)
    {
        return (teamChatLocked.contains(player.getUniqueId()) || otherTeamChatLocked.containsKey(player.getUniqueId()));
    }

    /**
     * Returns the other team viewed by the given player, or null if the player is not in
     * the chat of another team.
     *
     * @param player The player.
     * @return
     */
    public BoSTeam getOtherTeamEnabled(Player player)
    {
        return otherTeamChatLocked.get(player.getUniqueId());
    }
}
