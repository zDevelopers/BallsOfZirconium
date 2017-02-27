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
package eu.carrade.amaury.BallsOfSteel.generation.postProcessing;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.util.Location;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PopulateChestsPostProcessor extends PostProcessor
{
    private final static String LOOT_TABLES_NAMESPACE = "bos";

    private final boolean onlyEmpty;
    private final boolean chests;
    private final boolean trappedChests;
    private final boolean shulkerBoxes;
    private final boolean hoppers;
    private final boolean dispensers;
    private final boolean droppers;
    private final boolean furnaces;
    private final boolean storageMinecarts;
    private final boolean hopperMinecarts;

    private final String lootTable;
    private final String lootTableFilename;
    private final File lootTablePath;

    public PopulateChestsPostProcessor(Map parameters)
    {
        super(parameters);

        onlyEmpty = getValue(parameters, "only_empty", boolean.class, true);
        chests = getValue(parameters, "chests", boolean.class, true);
        trappedChests = getValue(parameters, "trapped_chests", boolean.class, true);
        shulkerBoxes = getValue(parameters, "shulker_boxes", boolean.class, true);
        hoppers = getValue(parameters, "hoppers", boolean.class, true);
        dispensers = getValue(parameters, "dispensers", boolean.class, true);
        droppers = getValue(parameters, "droppers", boolean.class, true);
        furnaces = getValue(parameters, "furnaces", boolean.class, true);
        storageMinecarts = getValue(parameters, "storage_minecarts", boolean.class, true);
        hopperMinecarts = getValue(parameters, "hopper_minecarts", boolean.class, true);

        String rawLootTable = getValue(parameters, "loot_table", String.class, "").toLowerCase().replace("../", "/").replace(".json", "");
        if (rawLootTable.contains(":"))
        {
            lootTable = rawLootTable;
            lootTableFilename = null;
            lootTablePath = null;
        }
        else if (!rawLootTable.isEmpty())
        {
            lootTable = LOOT_TABLES_NAMESPACE + ":" + rawLootTable;
            lootTableFilename = rawLootTable + ".json";
            lootTablePath = new File(MapConfig.MAP_LOOT_TABLES_DIRECTORY, lootTableFilename);
        }
        else
        {
            lootTable = null;
            lootTableFilename = null;
            lootTablePath = null;
        }
    }

    @Override
    protected void doProcess() throws MaxChangedBlocksException
    {
        // If the loot table was invalid, we skip.
        if (lootTable == null) return;


        // First, we copy the loot table file in the world folder if needed.
        if (lootTablePath != null)
        {
            File lootTableWorldPath = new File(
                    BukkitUtil.toWorld((BukkitWorld) session.getWorld()).getWorldFolder(),
                    "data/loot_tables/" + LOOT_TABLES_NAMESPACE + "/" + lootTableFilename
            );

            lootTableWorldPath.getParentFile().mkdirs();
            FileUtil.copy(lootTablePath, lootTableWorldPath);
        }


        // The functions applying the NBT changes

        final RegionFunction updateLootNBTForBlocks = new RegionFunction()
        {
            @Override
            public boolean apply(Vector position) throws WorldEditException
            {
                final BaseBlock block = session.getBlock(position);
                final CompoundTag nbt = block.hasNbtData() ? block.getNbtData() : new CompoundTag(new HashMap<String, Tag>());
                block.setNbtData(nbt.createBuilder().putString("LootTable", lootTable).build());

                session.setBlock(position, block);
                return true;
            }
        };

        final EntityFunction updateLootNBTForEntities = new EntityFunction()
        {
            @Override
            public boolean apply(Entity entity) throws WorldEditException
            {
                if (entity == null) return false;

                BaseEntity state = entity.getState();
                Location location = entity.getLocation();

                if (state == null) return false;

                // No filter for entities in WorldEdit so we have to check that here
                switch (state.getTypeId())
                {
                    case "MinecartChest":
                    case "chest_minecart":
                        if (!storageMinecarts) return false;
                        break;

                    case "MinecartHopper":
                    case "hopper_minecart":
                        if (!hopperMinecarts) return false;
                        break;

                    default:
                        return false;
                }

                // The received type ID may be invalid, and Minecraft will not understand it when re-created.
                Map<String, String> rightTypes = new HashMap<>();
                rightTypes.put("MinecartChest", "chest_minecart");
                rightTypes.put("chest_minecart", "chest_minecart");
                rightTypes.put("MinecartHopper", "hopper_minecart");
                rightTypes.put("hopper_minecart", "hopper_minecart");

                final CompoundTag nbt = state.hasNbtData() ? state.getNbtData() : new CompoundTag(new HashMap<String, Tag>());
                state = new BaseEntity(rightTypes.get(state.getTypeId()), nbt.createBuilder().putString("LootTable", lootTable).build());

                entity.remove();
                if (session.createEntity(location, state) == null)
                    PluginLogger.error("Unable to re-create minecart entity while populating chests at {0}", location.toVector());

                return true;
            }
        };


        // The filter to select only what we want

        final List<String> masks = new ArrayList<>(7);

        if (chests)        masks.add("chest");
        if (trappedChests) masks.add("trapped_chest");
        if (shulkerBoxes)  masks.add("white_shulker_box,orange_shulker_box,magenta_shulker_box,light_blue_shulker_box,yellow_shulker_box,lime_shulker_box,pink_shulker_box,gray_shulker_box,silver_shulker_box,cyan_shulker_box,purple_shulker_box,blue_shulker_box,brown_shulker_box,green_shulker_box,red_shulker_box,black_shulker_box");
        if (hoppers)       masks.add("hopper");
        if (dispensers)    masks.add("dispenser");
        if (droppers)      masks.add("dropper");
        if (furnaces)      masks.add("furnace,lit_furnace");

        final Mask blocksMask = WorldEditUtils.parseMask(session.getWorld(), StringUtils.join(masks, ","), session);

        final RegionVisitor blocksVisitor = new RegionVisitor(region, new RegionMaskingFilter(blocksMask, updateLootNBTForBlocks));
        final EntityVisitor entitiesVisitor = new EntityVisitor(session.getEntities(region).iterator(), updateLootNBTForEntities);

        try
        {
            Operations.complete(new OperationQueue(blocksVisitor, entitiesVisitor));
        }
        catch (WorldEditException e)
        {
            PluginLogger.info("Unable to populate chests", e);
        }

        session.flushQueue();
    }

    @Override
    public String doDescription()
    {
        final List<String> excluded = new ArrayList<>(7);

        if (!chests)        excluded.add("chests");
        if (!trappedChests) excluded.add("trapped chests");
        if (!shulkerBoxes)  excluded.add("shulker boxes");
        if (!hoppers)       excluded.add("hoppers");
        if (!dispensers)    excluded.add("dispensers");
        if (!droppers)      excluded.add("droppers");
        if (!furnaces)      excluded.add("furnaces");

        return I.t("Containers population {gray}(loot table: '{0}')", lootTable)
                + (excluded.size() > 0 ? I.t("{gray}(excluding: {0})", StringUtils.join(excluded, ", ")) : "");
    }
}
