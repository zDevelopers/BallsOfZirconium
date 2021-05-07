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
import eu.carrade.amaury.BallsOfSteel.generation.structures.GeneratedSphere;
import eu.carrade.amaury.BallsOfSteel.generation.structures.StructureSubProcessor;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.components.rawtext.RawTextPart;
import fr.zcraft.quartzlib.tools.commands.PaginatedTextView;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Iterator;
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

        if (sender instanceof Player) sender.sendMessage("");

        int page = -1;
        if (args.length > 0)
        {
            try { page = Integer.valueOf(args[0]); }
            catch (NumberFormatException ignored) {} // not a page, instead a sphere
        }

        if (args.length > 0 && page == -1)
        {
            displaySphere(getGeneratedSphereParameter(0));
            return;
        }

        final Set<GeneratedSphere> spheres = new TreeSet<>(new Comparator<GeneratedSphere>()
        {
            @Override
            public int compare(GeneratedSphere process1, GeneratedSphere process2)
            {
                return process1.getName().compareTo(process2.getName());
            }
        });

        spheres.addAll(BallsOfSteel.get().getGenerationManager().getSpheres());


        new SpheresPagination()
                .setData(spheres.toArray(new GeneratedSphere[spheres.size()]))
                .setCurrentPage(page)
                .setItemsPerPage(PaginatedTextView.DEFAULT_LINES_IN_EXPANDED_CHAT_VIEW - 2)
                .display(sender);
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
            return getMatchingSphere(args[0]);

        return null;
    }

    private void displaySphere(GeneratedSphere sphere)
    {
        info(I.t("{green}{bold}{0} Sphere", sphere.getName()) + (!sphere.isEnabled() ? " " + I.t("{gray}(disabled)") : ""));

        displayStructureSubProcessor(
                new RawText().then(I.tn("{0} generator", "{0} generators", sphere.getGenerators().size())).color(ChatColor.WHITE).style(ChatColor.BOLD),
                sphere.getGenerators()
        );

        displayStructureSubProcessor(
                new RawText().then(I.tn("{0} post-processor", "{0} post-processors", sphere.getPostProcessors().size())).color(ChatColor.WHITE).style(ChatColor.BOLD),
                sphere.getPostProcessors()
        );

        if (sender instanceof Player)
            info(I.t("Hover the generators and post-processors for details."));
    }

    private void displayStructureSubProcessor(RawTextPart base, List<? extends StructureSubProcessor> processors)
    {
        if (sender instanceof Player)
        {
            RawTextPart line = base == null ? new RawText() : base;
            line.then(processors.size() > 0 ? "\n» " : "").color(ChatColor.GRAY);

            for (final Iterator<? extends StructureSubProcessor> iterator = processors.iterator(); iterator.hasNext(); )
            {
                final StructureSubProcessor processor = iterator.next();

                line.then(processor.getName())
                        .color(ChatColor.WHITE)
                        .hover(new RawText()
                                .then(processor.getName() + "\n").color(ChatColor.WHITE)
                                .then(processor.getIdentifier() + "\n\n").color(ChatColor.DARK_GRAY)
                                .then(StringUtils.join(processor.getSettingsDescription(), "\n" + ChatColor.GRAY)).color(ChatColor.GRAY)
                        );

                if (iterator.hasNext())
                    line.then(", ").color(ChatColor.WHITE);
            }

            line.then(".").color(ChatColor.WHITE);
            send(line.build());
        }
        else
        {
            send(base.build());

            for (final StructureSubProcessor processor : processors)
            {
                info(ChatColor.GRAY + "- "
                        + ChatColor.WHITE + processor.getName()
                        + ChatColor.GRAY + " (" + StringUtils.join(processor.getSettingsDescription(), " - ") + ")"
                );
            }
        }
    }

    private class SpheresPagination extends PaginatedTextView<GeneratedSphere>
    {
        @Override
        protected void displayHeader(CommandSender receiver)
        {
            int availableSpheres = data().length;
            int enabledSpheres = 0;

            for (final GeneratedSphere process : data())
                if (process.isEnabled())
                    enabledSpheres++;

            if (enabledSpheres == availableSpheres)
                receiver.sendMessage(I.tn("{darkgreen}{bold}{0}{green}{bold} sphere registered.", "{darkgreen}{bold}{0}{green}{bold} spheres registered.", availableSpheres));
            else
                receiver.sendMessage(I.tn("{darkgreen}{bold}{0}{green}{bold} sphere registered. {green}{1} enabled.", "{darkgreen}{bold}{0}{green}{bold} spheres registered. {green}{1} enabled.", availableSpheres, availableSpheres, enabledSpheres));
        }

        @Override
        protected void displayItem(CommandSender receiver, GeneratedSphere process)
        {
            RawMessage.send(receiver,
                    new RawText("- ")
                            .color(ChatColor.GRAY)
                            .then(process.getName())
                            .color(process.isEnabled() ? ChatColor.GREEN : ChatColor.RED)
                            .hover(new RawText()
                                    .then(process.getName()).color(process.isEnabled() ? ChatColor.GREEN : ChatColor.RED)
                                    .then(!process.isEnabled() ? " " + I.t("{gray}(disabled)") : "").then("\n")
                                    .then(I.tn("{0} generator", "{0} generators", process.getGenerators().size())).color(ChatColor.GRAY).then("\n")
                                    .then(I.tn("{0} post-processor", "{0} post-processors", process.getPostProcessors().size())).color(ChatColor.GRAY).then("\n\n")
                                    .then(I.t("{gray}» {white}Click{gray} for details"))
                            )
                            .command(SpheresCommand.class, process.getName().replace(" ", ""))
                            .then(" (").color(ChatColor.WHITE)
                            .then(I.tn("{0} processor", "{0} processors", process.getGenerators().size() + process.getPostProcessors().size())).color(ChatColor.WHITE)
                            .then(")").color(ChatColor.WHITE)
                            .build()
            );
        }

        @Override
        protected String getCommandToPage(int page)
        {
            return build(String.valueOf(page));
        }
    }
}
