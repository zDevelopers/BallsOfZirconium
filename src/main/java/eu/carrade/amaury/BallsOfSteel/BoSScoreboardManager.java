/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.BallsOfSteel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;

public class BoSScoreboardManager {
	
	private BallsOfSteel p = null;
	private I18n i = null;
	private Scoreboard sb = null;
	
	public BoSScoreboardManager(BallsOfSteel p) {
		this.p = p;
		this.i = p.getI18n();
		
		this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
	}
	
	/**
	 * Tells the player's client to use this scoreboard.
	 * 
	 * @param p The player.
	 */
	public void setScoreboardForPlayer(Player p) {
		p.setScoreboard(sb);
	}
	
	/**
	 * Returns the internal scoreboard.
	 * 
	 * @return The internal scoreboard.
	 */
	public Scoreboard getScoreboard() {
		return sb;
	}
}
