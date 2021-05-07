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
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.world.World;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public final class WorldEditUtils
{
    private WorldEditUtils() {}

    /* ========== Adapters ========== */

    /**
     * Converts a Bukkit Vector to a WE Vector3.
     * @param vector The Bukkit vector.
     * @return The WE vector.
     */
    public static Vector3 asVector(final Vector vector) {
        return Vector3.at(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Converts a Bukkit Vector to a WE BlockVector3.
     * @param vector The Bukkit vector.
     * @return The WE block vector.
     */
    public static BlockVector3 asBlockVector(final Vector vector) {
        return BlockVector3.at(vector.getX(), vector.getY(), vector.getZ());
    }


    /* ========== Clipboards & schematics ========== */

    /**
     * Loads a schematic from a file.
     *
     * @param schematic The schematic file to load.
     *
     * @return A clipboard containing the schematic data.
     * @throws IOException If the schematic cannot be loaded for some reason.
     */
    public static Clipboard loadSchematic(File schematic) throws IOException
    {
        final ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        if (format == null) {
            throw new IOException("Cannot find suitable clipboard format for " + schematic.getAbsolutePath());
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematic))) {
            return reader.read();
        }
    }

    /**
     * Pastes a clipboard at the given location.
     *
     * @param session         The edit session we should paste into.
     * @param clipboard       The clipboard to paste.
     * @param at              The paste location.
     * @param ignoreAirBlocks {@code true} to paste only non-air blocks.
     *
     * @return A region containing the pasted clipboard.
     * @throws WorldEditException If a WorldEdit operation failed.
     */
    public static Region pasteClipboard(final EditSession session, final Clipboard clipboard, final BlockVector3 at, final boolean ignoreAirBlocks) throws WorldEditException
    {
        final ClipboardHolder holder = new ClipboardHolder(clipboard);
        final Operation operation = holder.createPaste(session)
                .to(at)
                .copyEntities(true)
                .copyBiomes(true)
                .ignoreAirBlocks(ignoreAirBlocks)
                .build();

        Operations.complete(operation);

        return getRegionForClipboardPastedAt(holder, at);
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
    public static Region getRegionForClipboardPastedAt(final Clipboard clipboard, final BlockVector3 at) {
        return getRegionForClipboardPastedAt(new ClipboardHolder(clipboard), at);
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
    public static Region getRegionForClipboardPastedAt(final ClipboardHolder holder, final BlockVector3 at)
    {
        final Clipboard clipboard = holder.getClipboard();
        final Region clipboardRegion = clipboard.getRegion();
        final BlockVector3 clipboardOffset = clipboardRegion.getMinimumPoint().subtract(clipboard.getOrigin());

        final BlockVector3 realTo = at.add(holder.getTransform().apply(clipboardOffset.toVector3()).toBlockPoint());
        final BlockVector3 max = realTo.add(
                holder.getTransform()
                        .apply(clipboardRegion.getMaximumPoint().subtract(clipboardRegion.getMinimumPoint()).toVector3())
                        .toBlockPoint()
        );

        return new CuboidRegion(
                realTo.getMinimum(max).subtract(1, 1, 1),
                realTo.getMaximum(max).add(1, 1, 1)
        );
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
     * @throws WorldEditException if WorldEdit operations fail.
     */
    public static void applyTransform(final EditSession session, final Region region, final Transform transform) throws WorldEditException
    {
        applyTransform(session, region, transform, region.getCenter().toBlockPoint());
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
     * @throws WorldEditException if WorldEdit operations fail.
     */
    public static void applyTransform(final EditSession session, final Region region, final Transform transform, final BlockVector3 origin) throws WorldEditException
    {
        final BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        final ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, BlockVector3.ZERO);
        copy.setRemovingEntities(true);
        copy.setTransform(transform);

        Operations.complete(copy);

        session.replaceBlocks(
                region,
                Masks.alwaysTrue(),
                new TypeApplyingPattern(session.getWorld(), BukkitAdapter.adapt(Material.AIR.createBlockData()))
        );

        pasteClipboard(session, clipboard, origin, true);
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
        return parsePattern(BukkitAdapter.adapt(world), pattern);
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
        final ParserContext context = new ParserContext();
        context.setWorld(world);
        context.setActor(new BukkitCommandSender(BallsOfSteel.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
        context.setExtent(null);
        context.setSession(null);

        try
        {
            return WorldEdit.getInstance().getPatternFactory().parseFromInput(pattern, context);
        }
        catch (InputParseException e)
        {
            PluginLogger.error("Invalid pattern: {0} ({1}). Using stone instead this time.", pattern, e.getMessage());
            return new TypeApplyingPattern(world, BukkitAdapter.adapt(Material.STONE.createBlockData()));
        }
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
        return parseMask(BukkitAdapter.adapt(world), mask, session);
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
}
