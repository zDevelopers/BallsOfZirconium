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
package eu.carrade.amaury.BallsOfSteel.utils;

import org.bukkit.ChatColor;


/**
 * Used to convert a string to a ChatColor object.
 */
public enum StringToChatColor
{
    AQUA("Aqua", ChatColor.AQUA),
    BLACK("Black", ChatColor.BLACK),
    BLUE("Blue", ChatColor.BLUE),
    DARK_AQUA("Darkaqua", ChatColor.DARK_AQUA),
    DARK_BLUE("Darkblue", ChatColor.DARK_BLUE),
    DARK_GRAY("Darkgray", ChatColor.DARK_GRAY),
    DARK_GREEN("Darkgreen", ChatColor.DARK_GREEN),
    DARK_PURPLE("Darkpurple", ChatColor.DARK_PURPLE),
    DARK_RED("Darkred", ChatColor.DARK_RED),
    GOLD("Gold", ChatColor.GOLD),
    GRAY("Gray", ChatColor.GRAY),
    GREEN("Green", ChatColor.GREEN),
    LIGHT_PURPLE("Lightpurple", ChatColor.LIGHT_PURPLE),
    RED("Red", ChatColor.RED),
    WHITE("White", ChatColor.WHITE),
    YELLOW("Yellow", ChatColor.YELLOW);

    private String name;
    private ChatColor color;

    StringToChatColor(String name, ChatColor color)
    {
        this.name = name;
        this.color = color;
    }

    public static ChatColor getChatColorByName(String name)
    {
        for (StringToChatColor stcc : values())
        {
            if (stcc.name.equalsIgnoreCase(name)) return stcc.color;
        }

        for (ChatColor cc : ChatColor.values())
        {
            if (cc.name().equalsIgnoreCase(name)) return cc;
        }

        return null;
    }
}
