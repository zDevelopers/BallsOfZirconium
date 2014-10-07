package eu.carrade.amaury.BallsOfSteel;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
	 * Used to prevent items to be put on or removed from the diamond chests using hoppers.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent ev) {
		Location from = null, to = null;
		
		if(ev.getSource().getHolder() instanceof Chest) {
			from = ((Chest) ev.getSource().getHolder()).getLocation();
		}
		else if(ev.getSource().getHolder() instanceof DoubleChest) {
			from = ((DoubleChest) ev.getSource().getHolder()).getLocation();
		}
		
		if(ev.getDestination().getHolder() instanceof Chest) {
			to = ((Chest) ev.getDestination().getHolder()).getLocation();
		}
		else if(ev.getDestination().getHolder() instanceof DoubleChest) {
			to = ((DoubleChest) ev.getDestination().getHolder()).getLocation();
		}
		
		// The Location returned by the DoubleChest's holders is the
		// location of the middle of the chest.
		// These locations cannot be compared to the stored ones.
		// Because we store, for each double chest, the location of the two sides of the
		// chest, we just floor the X and Z coordinates.
		if(from != null) {
			from.setX(from.getBlockX());
			from.setZ(from.getBlockZ());
		}
		if(to != null) {
			to.setX(to.getBlockX());
			to.setZ(to.getBlockZ());
		}
		
		if(p.getGameManager().getTrackedChests().contains(from)
				|| p.getGameManager().getTrackedChests().contains(to)) {
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
