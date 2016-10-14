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
package eu.carrade.amaury.BallsOfSteel.generation.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import fr.zcraft.zlib.tools.PluginLogger;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public final class WorldEditUtils
{
    private WorldEditUtils() {}



    /* ========== Edit sessions ========== */


    /**
     * Creates a new edit session.
     *
     * @param world The world this session will be used into.
     * @return A new session.
     */
    public static EditSession newEditSession(World world)
    {
        final EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        editSession.enableQueue();

        return editSession;
    }

    /**
     * Creates a new edit session.
     *
     * @param world The world this session will be used into.
     * @return A new session.
     */
    public static EditSession newEditSession(org.bukkit.World world)
    {
        return newEditSession(BukkitUtil.getLocalWorld(world));
    }

    /**
     * Creates a new edit session.
     *
     * @param world The world this session will be used into.
     * @param player The player using this session. Can be {@code null}; in this case this fallbacks to {@link #newEditSession(World)}.
     *
     * @return A new session.
     */
    public static EditSession newEditSession(World world, Player player)
    {
        Validate.isTrue(world != null || player != null, "Either world or player must be not null");

        if (player == null) return newEditSession(world);
        else                return BallsOfSteel.get().getWorldEditDependency().getWE().createEditSession(player);
    }

    /**
     * Creates a new edit session.
     *
     * @param world The world this session will be used into.
     * @param player The player using this session. Can be {@code null}; in this case this fallbacks to {@link #newEditSession(org.bukkit.World)}.
     *
     * @return A new session.
     */
    public static EditSession newEditSession(org.bukkit.World world, Player player)
    {
        return newEditSession(BukkitUtil.getLocalWorld(world), player);
    }

    /**
     * Creates a new edit session.
     *
     * @param player The player using this session.
     * @return A new session.
     */
    public static EditSession newEditSession(Player player)
    {
        return newEditSession((World) null, player);
    }



    /* ========== Clipboards & schematics ========== */


    /**
     * Loads a MCEdit schematic from a file.
     *
     * @param schematic The schematic file to load.
     *
     * @return A clipboard containing the schematic data.
     * @throws IOException If the schematic cannot be loaded for some reason.
     * @see #loadSchematic(File, ClipboardFormat) for other schematic formats
     * (if some are added one day).
     */
    public static Clipboard loadSchematic(File schematic) throws IOException
    {
        return loadSchematic(schematic, ClipboardFormat.SCHEMATIC);
    }

    /**
     * Loads a schematic from a file.
     *
     * @param schematic The schematic file to load.
     * @param format    The schematic format.
     *
     * @return A clipboard containing the schematic data.
     * @throws IOException If the schematic cannot be loaded for some reason.
     */
    public static Clipboard loadSchematic(File schematic, ClipboardFormat format) throws IOException
    {
        final Closer closer = Closer.create();

        try
        {
            final FileInputStream fis = closer.register(new FileInputStream(schematic));
            final BufferedInputStream bis = closer.register(new BufferedInputStream(fis));

            return format.getReader(bis).read(new BukkitWorld(null).getWorldData());
        }
        finally
        {
            try
            {
                closer.close();
            }
            catch (IOException ignored) {}
        }
    }

    /**
     * Pastes a clipboard at the given location.
     *
     * @param session         The edit session we should paste into.
     * @param holder          The clipboard holder to use to paste.
     * @param at              The paste location.
     * @param ignoreAirBlocks {@code true} to paste only non-air blocks.
     *
     * @return A region containing the pasted clipboard
     * @throws MaxChangedBlocksException If too many blocks are changed.
     */
    public static Region pasteClipboard(EditSession session, ClipboardHolder holder, Vector at, boolean ignoreAirBlocks) throws MaxChangedBlocksException
    {
        final WorldData worldData = session.getWorld().getWorldData();

        Operations.completeLegacy(holder
                        .createPaste(session, worldData)
                        .to(at)
                        .ignoreAirBlocks(ignoreAirBlocks)
                        .build()
        );

        return getRegionForClipboardPastedAt(holder, at);
    }

    /**
     * Pastes a clipboard at the given location.
     *
     * @param session         The edit session we should paste into.
     * @param clipboard       The clipboard to paste.
     * @param at              The paste location/
     * @param ignoreAirBlocks {@code true} to paste only non-air blocks.
     *
     * @return A region containing the pasted clipboard
     * @throws MaxChangedBlocksException If too many blocks are changed.
     */
    public static Region pasteClipboard(EditSession session, Clipboard clipboard, Vector at, boolean ignoreAirBlocks) throws MaxChangedBlocksException
    {
        return pasteClipboard(session, new ClipboardHolder(clipboard, session.getWorld().getWorldData()), at, ignoreAirBlocks);
    }


    /**
     * Returns the region where the clipboard will be pasted, supposing it will
     * be pasted at the given location.
     *
     * @param clipboard The clipboard.
     * @param at        The paste location.
     *
     * @return The region.
     */
    public static Region getRegionForClipboardPastedAt(Clipboard clipboard, Vector at)
    {
        return getRegionForClipboardPastedAt(new ClipboardHolder(clipboard, new BukkitWorld(null).getWorldData()), at);
    }

    /**
     * Returns the region where the clipboard will be pasted, supposing it will
     * be pasted at the given location.
     *
     * @param holder The clipboard holder.
     * @param at     The paste location.
     *
     * @return The region.
     */
    public static Region getRegionForClipboardPastedAt(ClipboardHolder holder, Vector at)
    {
        final Region clipboardRegion = holder.getClipboard().getRegion();
        final Vector clipboardOffset = clipboardRegion.getMinimumPoint().subtract(holder.getClipboard().getOrigin());

        final Vector realTo = at.add(holder.getTransform().apply(clipboardOffset));
        final Vector max = realTo.add(holder.getTransform().apply(clipboardRegion.getMaximumPoint().subtract(clipboardRegion.getMinimumPoint())));

        return new CuboidRegion(Vector.getMinimum(realTo, max).subtract(1, 1, 1), Vector.getMaximum(realTo, max).add(1, 1, 1));
    }



    /* ========== WorldEdit regions manipulations ========== */


    /**
     * Applies a transformation to a region, in-place.
     *
     * The region is copied into a buffer, the transformation is applied to it.
     * Then the old blocks are removed and the buffer applied instead.
     *
     * @param session The edit session in which the transformation is applied.
     * @param region The region to be transformed.
     * @param transform The transformation to apply.
     *
     * @throws MaxChangedBlocksException If too many blocks are changed.
     */
    public static void applyTransform(final EditSession session, final Region region, final Transform transform) throws MaxChangedBlocksException
    {
        applyTransform(session, region, transform, region.getCenter());
    }

    /**
     * Applies a transformation to a region, in-place.
     *
     * The region is copied into a buffer, the transformation is applied to it.
     * Then the old blocks are removed and the buffer applied instead.
     *
     * @param session The edit session in which the transformation is applied.
     * @param region The region to be transformed.
     * @param transform The transformation to apply.
     * @param origin The origin of the transformation.
     *
     * @throws MaxChangedBlocksException If too many blocks are changed.
     */
    public static void applyTransform(final EditSession session, final Region region, final Transform transform, final Vector origin) throws MaxChangedBlocksException
    {
        final BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        final ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
        Operations.completeLegacy(copy);

        final ClipboardHolder holder = new ClipboardHolder(clipboard, new BukkitWorld(null).getWorldData());
        holder.setTransform(transform);

        session.replaceBlocks(region, Masks.negate(new BlockMask(session, new BaseBlock(BlockID.AIR))), Patterns.wrap(new BlockPattern(new BaseBlock(BlockID.AIR))));
        session.flushQueue();

        pasteClipboard(session, holder, origin, true);
        session.flushQueue();
    }



    /* ========== WorldEdit patterns ========== */


    /**
     * Parses a pattern.
     *
     * @param world   The world this pattern will be used into.
     * @param pattern The pattern string.
     *
     * @return A WorldEdit pattern, or a default stone one if the pattern is
     * invalid.
     */
    public static Pattern parsePattern(org.bukkit.World world, String pattern)
    {
        return parsePattern(BukkitUtil.getLocalWorld(world), pattern);
    }

    /**
     * Parses a pattern.
     *
     * @param world   The world this pattern will be used into.
     * @param pattern The pattern string.
     *
     * @return A WorldEdit pattern, or a default stone one if the pattern is
     * invalid.
     */
    public static Pattern parsePattern(World world, String pattern)
    {
        final ParserContext parserContext = new ParserContext();

        parserContext.setWorld(world);
        parserContext.setActor(new BukkitCommandSender(BallsOfSteel.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
        parserContext.setExtent(null);
        parserContext.setSession(null);

        try
        {
            return WorldEdit.getInstance().getPatternFactory().parseFromInput(pattern, parserContext);
        }
        catch (InputParseException e)
        {
            PluginLogger.warning("Invalid pattern: {0} ({1}). Using stone instead this time.", pattern, e.getMessage());
            return new BlockPattern(new BaseBlock(BlockID.STONE));
        }
    }

    /**
     * Parses a pattern to a legacy pattern object.
     *
     * @param world   The world this pattern will be used into.
     * @param pattern The pattern string.
     *
     * @return A legacy WorldEdit pattern, or a default stone one if the pattern
     * is invalid.
     */
    public static com.sk89q.worldedit.patterns.Pattern parsePatternLegacy(org.bukkit.World world, String pattern)
    {
        return parsePatternLegacy(BukkitUtil.getLocalWorld(world), pattern);
    }

    /**
     * Parses a pattern to a legacy pattern object.
     *
     * @param world   The world this pattern will be used into.
     * @param pattern The pattern string.
     *
     * @return A legacy WorldEdit pattern, or a default stone one if the pattern
     * is invalid.
     */
    public static com.sk89q.worldedit.patterns.Pattern parsePatternLegacy(World world, String pattern)
    {
        return Patterns.wrap(parsePattern(world, pattern));
    }



    /* ========== WorldEdit masks ========== */


    /**
     * Parses a mask.
     *
     * @param world The world this mask will be used into.
     * @param mask  The mask string.
     *
     * @return A WorldEdit mask, or an always-matching one if the mask is
     * invalid.
     */
    public static Mask parseMask(org.bukkit.World world, String mask, EditSession session)
    {
        return parseMask(BukkitUtil.getLocalWorld(world), mask, session);
    }

    /**
     * Parses a mask.
     *
     * @param world The world this mask will be used into.
     * @param mask  The mask string.
     *
     * @return A WorldEdit mask, or an always-matching one if the mask is
     * invalid.
     */
    public static Mask parseMask(World world, String mask, EditSession session)
    {
        final ParserContext parserContext = new ParserContext();

        parserContext.setWorld(world);
        parserContext.setActor(new BukkitCommandSender(BallsOfSteel.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
        parserContext.setExtent(session);
        parserContext.setSession(null);

        Request.request().setWorld(world);
        Request.request().setEditSession(session);

        try
        {
            return WorldEdit.getInstance().getMaskFactory().parseFromInput(mask, parserContext);
        }
        catch (InputParseException e)
        {
            PluginLogger.warning("Invalid mask: {0} ({1}). No mask used this time.", mask, e.getMessage());
            return Masks.alwaysTrue();
        }
    }

    /**
     * Parses a mask to a legacy mask object.
     *
     * @param world The world this mask will be used into.
     * @param mask  The mask string.
     *
     * @return A legacy WorldEdit mask, or an always-matching one if the mask is
     * invalid.
     */
    public static com.sk89q.worldedit.masks.Mask parseMaskLegacy(org.bukkit.World world, String mask, EditSession session)
    {
        return parseMaskLegacy(BukkitUtil.getLocalWorld(world), mask, session);
    }

    /**
     * Parses a mask to a legacy mask object.
     *
     * @param world The world this mask will be used into.
     * @param mask  The mask string.
     *
     * @return A legacy WorldEdit mask, or an always-matching one if the mask is
     * invalid.
     */
    public static com.sk89q.worldedit.masks.Mask parseMaskLegacy(World world, String mask, EditSession session)
    {
        return Masks.wrap(parseMask(world, mask, session));
    }
}
