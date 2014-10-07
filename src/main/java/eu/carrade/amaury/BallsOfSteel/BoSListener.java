package eu.carrade.amaury.BallsOfSteel;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;

public class BoSListener implements Listener {
	
	private BallsOfSteel p = null;
	
	public BoSListener(BallsOfSteel plugin) {
		this.p = plugin;
	}
	
	/**
	 * Used to prevent the diamond chests to be destroyed.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev) {
		if(p.getGameManager().getTrackedChests().contains(ev.getBlock().getLocation())) {
			ev.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent the diamond chests to be destroyed.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockBurn(BlockBurnEvent ev) {
		if(p.getGameManager().getTrackedChests().contains(ev.getBlock().getLocation())) {
			ev.setCancelled(true);
		}
	}
}
