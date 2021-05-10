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
package eu.carrade.amaury.ballsofzirconium.generation.utils

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitCommandSender
import com.sk89q.worldedit.extension.input.InputParseException
import com.sk89q.worldedit.extension.input.ParserContext
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.mask.Mask
import com.sk89q.worldedit.function.mask.Masks
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.function.pattern.Pattern
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector3
import com.sk89q.worldedit.math.transform.Transform
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.session.request.Request
import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium
import fr.zcraft.quartzlib.tools.PluginLogger
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.util.Vector
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object WorldEditUtils {

    /* ========== Adapters ========== */

    /**
     * Converts a Bukkit Vector to a WE Vector3.
     * @param vector The Bukkit vector.
     * @return The WE vector.
     */
    @JvmStatic
    fun asVector(vector: Vector): Vector3 {
        return Vector3.at(vector.x, vector.y, vector.z)
    }

    /**
     * Converts a Bukkit Vector to a WE BlockVector3.
     * @param vector The Bukkit vector.
     * @return The WE block vector.
     */
    @JvmStatic
    fun asBlockVector(vector: Vector): BlockVector3 {
        return BlockVector3.at(vector.x, vector.y, vector.z)
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
    @JvmStatic
    @Throws(IOException::class)
    fun loadSchematic(schematic: File): Clipboard {
        val format = ClipboardFormats.findByFile(schematic)
                ?: throw IOException("Cannot find suitable clipboard format for " + schematic.absolutePath)
        format.getReader(FileInputStream(schematic)).use { reader -> return reader.read() }
    }

    /**
     * Pastes a clipboard at the given location.
     *
     * @param session         The edit session we should paste into.
     * @param clipboard       The clipboard to paste.
     * @param at              The paste location.
     * @param ignoreAirBlocks `true` to paste only non-air blocks.
     *
     * @return A region containing the pasted clipboard.
     * @throws WorldEditException If a WorldEdit operation failed.
     */
    @JvmStatic
    @Throws(WorldEditException::class)
    fun pasteClipboard(session: EditSession?, clipboard: Clipboard?, at: BlockVector3, ignoreAirBlocks: Boolean): Region {
        val holder = ClipboardHolder(clipboard)
        val operation = holder.createPaste(session)
                .to(at)
                .copyEntities(true)
                .copyBiomes(true)
                .ignoreAirBlocks(ignoreAirBlocks)
                .build()
        Operations.complete(operation)
        return getRegionForClipboardPastedAt(holder, at)
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
    @JvmStatic
    fun getRegionForClipboardPastedAt(clipboard: Clipboard?, at: BlockVector3): Region {
        return getRegionForClipboardPastedAt(ClipboardHolder(clipboard), at)
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
    @JvmStatic
    fun getRegionForClipboardPastedAt(holder: ClipboardHolder, at: BlockVector3): Region {
        val clipboard = holder.clipboard
        val clipboardRegion = clipboard.region
        val clipboardOffset = clipboardRegion.minimumPoint.subtract(clipboard.origin)
        val realTo = at.add(holder.transform.apply(clipboardOffset.toVector3()).toBlockPoint())
        val max = realTo.add(
                holder.transform
                        .apply(clipboardRegion.maximumPoint.subtract(clipboardRegion.minimumPoint).toVector3())
                        .toBlockPoint()
        )

        return CuboidRegion(
                realTo.getMinimum(max).subtract(1, 1, 1),
                realTo.getMaximum(max).add(1, 1, 1)
        )
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
    @JvmOverloads
    @JvmStatic
    @Throws(WorldEditException::class)
    fun applyTransform(session: EditSession, region: Region, transform: Transform?, origin: BlockVector3 = region.center.toBlockPoint()) {
        val clipboard = BlockArrayClipboard(region)
        clipboard.origin = origin

        val copy = ForwardExtentCopy(session, region, clipboard, BlockVector3.ZERO)
        copy.isRemovingEntities = true
        copy.transform = transform
        Operations.complete(copy)
        session.replaceBlocks(
                region,
                Masks.alwaysTrue(),
                TypeApplyingPattern(session.world, BukkitAdapter.adapt(Material.AIR.createBlockData()))
        )
        pasteClipboard(session, clipboard, origin, true)
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
    @JvmStatic
    fun parsePattern(world: World?, pattern: String?): Pattern {
        return parsePattern(BukkitAdapter.adapt(world), pattern)
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
    @JvmStatic
    fun parsePattern(world: com.sk89q.worldedit.world.World?, pattern: String?): Pattern {
        val context = ParserContext()
        context.world = world
        context.actor = BukkitCommandSender(BallsOfZirconium.get().worldEditDependency.we, Bukkit.getConsoleSender())
        context.extent = null
        context.session = null

        return try {
            WorldEdit.getInstance().patternFactory.parseFromInput(pattern, context)
        } catch (e: InputParseException) {
            PluginLogger.error("Invalid pattern: {0} ({1}). Using stone instead this time.", pattern, e.message)
            TypeApplyingPattern(world, BukkitAdapter.adapt(Material.STONE.createBlockData()))
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
    @JvmStatic
    fun parseMask(world: World?, mask: String?, session: EditSession?): Mask {
        return parseMask(BukkitAdapter.adapt(world), mask, session)
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
    @JvmStatic
    fun parseMask(world: com.sk89q.worldedit.world.World?, mask: String?, session: EditSession?): Mask {
        val parserContext = ParserContext()
        parserContext.world = world
        parserContext.actor = BukkitCommandSender(BallsOfZirconium.get().worldEditDependency.we, Bukkit.getConsoleSender())
        parserContext.extent = session
        parserContext.session = null
        Request.request().world = world
        Request.request().editSession = session

        return try {
            WorldEdit.getInstance().maskFactory.parseFromInput(mask, parserContext)
        } catch (e: InputParseException) {
            PluginLogger.warning("Invalid mask: {0} ({1}). No mask used this time.", mask, e.message)
            Masks.alwaysTrue()
        }
    }
}
