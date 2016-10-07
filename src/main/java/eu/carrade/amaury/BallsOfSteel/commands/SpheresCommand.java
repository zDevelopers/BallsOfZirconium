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
package eu.carrade.amaury.BallsOfSteel.commands;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.commands.helpers.SpheresRelatedCommand;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationProcess;
import eu.carrade.amaury.BallsOfSteel.generation.generators.Generator;
import eu.carrade.amaury.BallsOfSteel.generation.postProcessing.PostProcessor;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


@CommandInfo (name = "spheres", usageParameters = "[sphereName]")
public class SpheresCommand extends SpheresRelatedCommand
{
    @Override
    protected void run() throws CommandException
    {
        if (!BallsOfSteel.get().getGenerationManager().isEnabled())
            error(I.t("Cannot use generation-related tools: generation disabled (either it's disabled in map.yml or WorldEdit is missing)."));

        if(sender instanceof Player) sender.sendMessage("");

        if (args.length > 0)
        {
            final GenerationProcess process = getGenerationProcessParameter(0);
            info(I.t("{green}{bold}Sphere: {darkgreen}{bold}{0}", process.getName()));

            info(I.tn("{darkgreen}{0} generator", "{darkgreen}{0} generators", process.getGenerators().size()));
            for (Generator generator : process.getGenerators())
            {
                info(I.t("{gray}- {reset}{0}", generator.getDescription()));
            }

            info(I.tn("{darkgreen}{0} post-processor", "{darkgreen}{0} post-processors", process.getPostProcessors().size()));
            for (PostProcessor postProcessor : process.getPostProcessors())
            {
                info(I.t("{gray}- {reset}{0}", postProcessor.getDescription()));
            }

            return;
        }

        final Set<GenerationProcess> generationProcesses = new TreeSet<>(new Comparator<GenerationProcess>()
        {
            @Override
            public int compare(GenerationProcess process1, GenerationProcess process2)
            {
                return process1.getName().compareTo(process2.getName());
            }
        });

        generationProcesses.addAll(BallsOfSteel.get().getGenerationManager().getGenerationProcesses());

        info(I.tn("{bold}{darkgreen}{0}{bold}{green} sphere registered.", "{bold}{darkgreen}{0}{bold}{green} spheres registered.", generationProcesses.size()));
        for (GenerationProcess process : generationProcesses)
        {
            send(new RawText("- ")
                            .color(ChatColor.GRAY)
                        .then(process.getName())
                            .color(process.isEnabled() ? ChatColor.DARK_GREEN : ChatColor.RED)
                            .hover(new RawText(I.t("Details about {0}", process.getName())))
                            .command(SpheresCommand.class, process.getName().replace(" ", ""))
                        .then(" (").color(ChatColor.GRAY)
                        .then(I.tn("{0} generator", "{0} generators", process.getGenerators().size())).color(ChatColor.GRAY)
                        .then(", ").color(ChatColor.GRAY)
                        .then(I.tn("{0} post-processor", "{0} post-processors", process.getPostProcessors().size())).color(ChatColor.GRAY)
                        .then(")").color(ChatColor.GRAY)
                    .build()
            );
        }
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
            return getMatchingGenerationProcesses(args[0]);

        return null;
    }
}
