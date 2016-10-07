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
package eu.carrade.amaury.BallsOfSteel.commands.helpers;

import eu.carrade.amaury.BallsOfSteel.BallsOfSteel;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationProcess;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.i18n.I;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public abstract class SpheresRelatedCommand extends BoSCommand
{
    protected List<String> getGeneratorsNamesWithoutSpaces()
    {
        List<String> names = new LinkedList<>();

        for (GenerationProcess process : BallsOfSteel.get().getGenerationManager().getGenerationProcesses())
        {
            names.add(process.getName().replace(" ", ""));
        }

        return names;
    }

    public GenerationProcess getGenerationProcessParameter(final int index) throws CommandException
    {
        final String name = args[index].trim();

        GenerationProcess generator = null;
        for (GenerationProcess process : BallsOfSteel.get().getGenerationManager().getGenerationProcesses())
        {
            if (process.getName().replace(" ", "").equalsIgnoreCase(name))
            {
                generator = process;
                break;
            }
        }

        if (generator == null)
            throwInvalidArgument(I.t("No generator found under the name {0}. Available generators: {1}.", name, StringUtils.join(getGeneratorsNamesWithoutSpaces(), ", ")));

        return generator;
    }

    public List<String> getMatchingGenerationProcesses(final String prefix)
    {
        final List<String> generatorsNamesWithoutSpaces = getGeneratorsNamesWithoutSpaces();
        Collections.sort(generatorsNamesWithoutSpaces);
        return getMatchingSubset(generatorsNamesWithoutSpaces, prefix);
    }
}
