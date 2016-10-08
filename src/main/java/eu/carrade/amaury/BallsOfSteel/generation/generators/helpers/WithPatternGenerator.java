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
package eu.carrade.amaury.BallsOfSteel.generation.generators.helpers;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.Patterns;
import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.generation.generators.Generator;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Map;


public abstract class WithPatternGenerator extends Generator
{
    protected final String patternString;

    public WithPatternGenerator(Map parameters)
    {
        super(parameters);

        patternString = getValue(parameters, "pattern", String.class, "stone");
    }

    protected Pattern pattern(World world)
    {
        final ParserContext parserContext = new ParserContext();

        parserContext.setWorld(BukkitUtil.getLocalWorld(world));
        parserContext.setActor(new BukkitCommandSender(BallsOfSteel.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
        parserContext.setExtent(null);
        parserContext.setSession(null);

        try
        {
            return WorldEdit.getInstance().getPatternFactory().parseFromInput(patternString, parserContext);
        }
        catch (InputParseException e)
        {
            PluginLogger.warning("Invalid pattern: {0} ({1}). Using stone instead this time.", patternString, e.getMessage());
            return new BlockPattern(new BaseBlock(BlockID.STONE));
        }
    }

    protected com.sk89q.worldedit.patterns.Pattern oldPattern(World world)
    {
        return Patterns.wrap(pattern(world));
    }
}
