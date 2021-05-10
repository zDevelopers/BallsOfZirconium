package eu.carrade.amaury.ballsofzirconium.game;

import eu.carrade.amaury.ballsofzirconium.generation.GenerationData;
import eu.carrade.amaury.ballsofzirconium.generation.structures.Structure;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

public class MetadataActionBarManager extends QuartzComponent
{
    private BukkitTask locationUpdateTask = null;

	@Override
	protected void onEnable()
	{
		locationUpdateTask = RunTask.timer(() -> Bukkit.getOnlinePlayers().forEach(p -> {
			final Structure structure = GenerationData.getStructureAt(p.getLocation());
			if (structure != null) {
				MessageSender.sendActionBarMessage(
						p,
						new RawText(structure.getDisplayName()).style(ChatColor.GOLD, ChatColor.BOLD)
				);
			}
		}), 20, 20);
	}

	@Override
	protected void onDisable()
	{
		locationUpdateTask.cancel();
	}
}
