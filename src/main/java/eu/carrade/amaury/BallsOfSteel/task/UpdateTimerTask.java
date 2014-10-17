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

package eu.carrade.amaury.BallsOfSteel.task;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateTimerTask extends BukkitRunnable {

	private BallsOfSteel p = null;
	
	public UpdateTimerTask(BallsOfSteel p) {
		this.p = p;
	}
	
	@Override
	public void run() {
		if(p.getGameManager().isGameRunning()) {
			p.getGameManager().getTimer().update();
			p.getScoreboardManager().updateTimer(p.getGameManager().getTimer());
		}
	}
}
