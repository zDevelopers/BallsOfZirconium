package eu.carrade.amaury.BallsOfSteel;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.Action;

import eu.carrade.amaury.BallsOfSteel.i18n.I18n;

public class BoSListener implements Listener {
	
	private BallsOfSteel p = null;
	private I18n i = null;
	
	public BoSListener(BallsOfSteel plugin) {
		this.p = plugin;
		this.i = p.getI18n();
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
	
	/**
	 * Used to prevent player to access the chest of another team.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent ev) {
		
		if(ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(!BoSUtils.isSharedChest(ev.getClickedBlock())) return;
		
		BoSTeam playerTeam = p.getTeamManager().getTeamForPlayer(ev.getPlayer());
		
		if(p.getGameManager().getTrackedChests().contains(ev.getClickedBlock().getLocation())
				&& (playerTeam == null
					|| (!ev.getClickedBlock().getLocation().equals(playerTeam.getChestLocation1())
					    && !ev.getClickedBlock().getLocation().equals(playerTeam.getChestLocation2())))) {
			
			ev.getPlayer().sendMessage(i.t("chests.otherTeamChest"));
			ev.setCancelled(true);
			
		}
	}
	
	/**
	 * Used to:
	 *  - change the gamemode of the player, if the game is not running;
	 *  - teleport the player to the spawn, if the game is not running;
	 *  - update the scoreboard;
	 *  - resurrect a player (if the player was offline).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev) {
		// Mainly useful on the first join.
		p.getScoreboardManager().setScoreboardForPlayer(ev.getPlayer());
		
		// The display name is reset when the player logs off.
		p.getTeamManager().colorizePlayer(ev.getPlayer());
	}
}
