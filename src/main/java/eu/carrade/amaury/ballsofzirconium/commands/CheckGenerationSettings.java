/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.ballsofzirconium.commands;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium;
import eu.carrade.amaury.ballsofzirconium.commands.helpers.SpheresRelatedCommand;
import eu.carrade.amaury.ballsofzirconium.generation.generators.Generator;
import eu.carrade.amaury.ballsofzirconium.generation.generators.helpers.WithPatternGenerator;
import eu.carrade.amaury.ballsofzirconium.generation.postProcessing.PostProcessor;
import eu.carrade.amaury.ballsofzirconium.generation.postProcessing.ReplacePostProcessor;
import eu.carrade.amaury.ballsofzirconium.generation.postProcessing.SetPostProcessor;
import eu.carrade.amaury.ballsofzirconium.generation.structures.GeneratedSphere;
import eu.carrade.amaury.ballsofzirconium.generation.structures.StaticBuilding;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CommandInfo(name = "check-generation-settings")
public class CheckGenerationSettings extends SpheresRelatedCommand
{
	private World world;

	@Override
	protected void run() throws CommandException
	{
		if (!BallsOfZirconium.get().getGenerationManager().isEnabled())
			error(I.t("Cannot use generation-related tools: generation disabled (either it's disabled in map.yml or WorldEdit is missing)."));

		world = sender instanceof Player ? playerSender().getWorld() : Bukkit.getWorlds().get(0);

		int errorsCount = 0;

		for (final GeneratedSphere sphere : BallsOfZirconium.get().getGenerationManager().getSpheres())
		{
			errorsCount += check(sphere.getName(), new HashSet<>(sphere.getGenerators()), new HashSet<>(sphere.getPostProcessors()));
		}

		for (final StaticBuilding building : BallsOfZirconium.get().getGenerationManager().getBuildings())
		{
			errorsCount += check(building.getName(), null, new HashSet<>(building.getPostProcessors()));
		}

		if (errorsCount > 0) {
			info(I.tn("{0} error left to fix.", "{0} errors left to fix.", errorsCount));
		} else {
			success(I.t("Everything looks good!"));
			info(I.t("Please note that this tool only check patterns and masks for now."));
		}
	}

	private int check(@NotNull final String title, @Nullable final Set<Generator> generators, @Nullable final Set<PostProcessor> processors) {
		final Set<PatternError> errors = new HashSet<>();

		if (generators != null) {
			errors.addAll(checkGenerators(generators));
		}

		if (processors != null) {
			errors.addAll(checkPostProcessors(processors));
		}

		if (errors.isEmpty()) return 0;

		send(new RawText(title).style(ChatColor.RED, ChatColor.BOLD));
		errors.forEach(error -> info(I.t("In “{0}”: {1}", error.pattern, error.error)));
		info("");

		return errors.size();
	}

	private Set<PatternError> checkGenerators(@NotNull final Set<Generator> generators)
	{
		return checkPatternsStream(
				generators.stream()
						.filter(generator -> generator instanceof WithPatternGenerator)
						.map(generator -> {
							try
							{
								return (String) Reflection.getFieldValue(WithPatternGenerator.class, generator, "patternString");
							}
							catch (NoSuchFieldException | IllegalAccessException e)
							{
								return null;
							}
						})
						.filter(Objects::nonNull)
		);
	}

	private Set<PatternError> checkPostProcessors(@NotNull final Set<PostProcessor> processors)
	{
		final Set<String> patterns = new HashSet<>();
		final Set<String> masks = new HashSet<>();

		for (final PostProcessor processor : processors) {
			Set<String> patternsFields = null;
			Set<String> masksFields = null;
			final Class<?> clazz;

			if (processor instanceof SetPostProcessor) {
				patternsFields = Collections.singleton("pattern");
				clazz = SetPostProcessor.class;
			}
			else if (processor instanceof ReplacePostProcessor) {
				patternsFields = Collections.singleton("toPattern");
				masksFields = Collections.singleton("fromMask");
				clazz = ReplacePostProcessor.class;
			}
			else return Collections.emptySet(); // no patterns or masks

			patterns.addAll(patternsFields.stream().map(field -> {
				try
				{
					return (String) Reflection.getFieldValue(clazz, processor, field);
				}
				catch (NoSuchFieldException | IllegalAccessException e)
				{
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toSet()));

			if (masksFields != null)
			{
				masks.addAll(masksFields.stream().map(field -> {
					try
					{
						return (String) Reflection.getFieldValue(clazz, processor, field);
					}
					catch (NoSuchFieldException | IllegalAccessException e)
					{
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toSet()));
			}
		}

		final Set<PatternError> errors = checkPatternsStream(patterns.stream());
		errors.addAll(checkMasksStream(masks.stream()));

		return errors;
	}

	private Set<PatternError> checkPatternsStream(@NotNull Stream<String> patterns)
	{
		return patterns
				.map(pattern -> {
					final ParserContext context = new ParserContext();
					context.setWorld(BukkitAdapter.adapt(world));
					context.setActor(new BukkitCommandSender(BallsOfZirconium.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
					context.setExtent(null);
					context.setSession(null);

					try
					{
						WorldEdit.getInstance().getPatternFactory().parseFromInput(pattern, context);
						return null;
					}
					catch (final InputParseException e)
					{
						return new PatternError(pattern, e.getMessage());
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private Set<PatternError> checkMasksStream(@NotNull Stream<String> masks)
	{
		return masks
				.map(mask -> {
					final ParserContext context = new ParserContext();
					context.setWorld(BukkitAdapter.adapt(world));
					context.setActor(new BukkitCommandSender(BallsOfZirconium.get().getWorldEditDependency().getWE(), Bukkit.getConsoleSender()));
					context.setExtent(null);
					context.setSession(null);

					try
					{
						WorldEdit.getInstance().getMaskFactory().parseFromInput(mask, context);
						return null;
					}
					catch (final InputParseException e)
					{
						return new PatternError(mask, e.getMessage());
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private static class PatternError
	{
		private final String pattern;
		private final String error;

		public PatternError(@NotNull final String pattern, @NotNull final String error)
		{
			this.pattern = pattern;
			this.error = error;
		}
	}
}
