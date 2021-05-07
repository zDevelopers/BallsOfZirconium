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
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.i18n.I18n;
import org.bukkit.entity.Player;

import java.util.List;


@CommandInfo (name = "about")
public class AboutCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        if (sender instanceof Player) sender.sendMessage("");

        info(I.t("{yellow}{0} - version {1}", BallsOfSteel.get().getDescription().getDescription(), BallsOfSteel.get().getDescription().getVersion()));

        String authors = "";
        final List<String> listAuthors = BallsOfSteel.get().getDescription().getAuthors();

        for (String author : listAuthors)
        {
            if (author == listAuthors.get(0))
            {
                // Nothing
            }
            else if (author == listAuthors.get(listAuthors.size() - 1))
            {
                authors += " " + I.t("and") + " ";
            }
            else
            {
                authors += ", ";
            }
            authors += author;
        }
        info(I.t("Plugin made with love by {0}.", authors));

        info(I.t("{aqua}------ Translations ------"));
        info(I.t("Current language: {0} (translated by {1}).", I18n.getPrimaryLocale().getDisplayName(), I18n.getLastTranslator(I18n.getPrimaryLocale())));
        info(I.t("Fallback language: {0} (translated by {1}).", I18n.getFallbackLocale().getDisplayName(), I18n.getLastTranslator(I18n.getFallbackLocale())));
        info(I.t("{aqua}------ License ------"));
        info(I.t("Published under the GNU General Public License (version 3)."));
    }
}
