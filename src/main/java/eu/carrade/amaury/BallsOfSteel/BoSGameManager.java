/**
 *  Bukkit plugin Balls of Steel
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

public class BoSGameManager {
	
	private BallsOfSteel p = null;
	
	private boolean running = false;
	
	public BoSGameManager(BallsOfSteel plugin) {
		this.p = plugin;
	}
	
	/**
	 * Returns {@code true} if the game is launched.
	 * 
	 * @return {@code True} if the game is launched.
	 */
	public boolean isGameRunning() {
		return running;
	}
}
