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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;

public class BoSScoreboardManager {
	
	private BallsOfSteel p = null;
	private I18n i = null;
	private Scoreboard sb = null;
	private Objective sidebar = null;
	
	public BoSScoreboardManager(BallsOfSteel p) {
		this.p = p;
		this.i = p.getI18n();
		
		this.sb = Bukkit.getScoreboardManager().getNewScoreboard();
	}
	
	/**
	 * Initializes the scoreboard.
	 * <p>
	 * To be called when the game starts.
	 */
	public void initScoreboard() {
		sidebar = sb.registerNewObjective("Diamonds", "dummy");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		sidebar.setDisplayName(i.t("scoreboard.title"));
		
		for(BoSTeam team : p.getTeamManager().getTeams()) {
			if(team.getPlayers().size() == 0) continue;
			
			sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(1);
			sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(0);
		}
		
		for(Player player : p.getGameManager().getGameWorld().getPlayers()) {
			setScoreboardForPlayer(player);
		}
	}
	
	/**
	 * Updates the diamonds score of the given team.
	 * 
	 * @param team The team.
	 */
	public void updateDiamondsScore(BoSTeam team) {
		sidebar.getScore(getValidScoreboardName(team.getDisplayName())).setScore(team.getDiamondsCount());
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
	
	public String getValidScoreboardName(String scoreName) {
		return scoreName.substring(0, Math.min(scoreName.length(), 16));
	}
}
