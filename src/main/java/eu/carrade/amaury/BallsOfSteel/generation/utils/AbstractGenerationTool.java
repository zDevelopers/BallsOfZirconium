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

import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.quartzlib.tools.PluginLogger;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public abstract class AbstractGenerationTool
{
    /**
     * Returns a value from a configuration parameters map, or a default if
     * invalid or not found.
     *
     * Internally uses {@link ConfigurationValueHandlers} to convert the value,
     * so the classes must be registered!
     *
     * @param params        The map of parameters.
     * @param key           The parameters key.
     * @param parameterType The parameter type for this key
     * @param defaultValue  A default value to return if the given parameter is
     *                      invalid or not found.
     * @param <T>           The parameter type
     *
     * @return The value, of the default value if invalid.
     */
    protected static <T> T getValue(final Map<?, ?> params, final String key, final Class<T> parameterType, final T defaultValue)
    {
        try
        {
            return params.containsKey(key) ? ConfigurationValueHandlers.handleValue(params.get(key), parameterType) : defaultValue;
        }
        catch (ConfigurationParseException e)
        {
            PluginLogger.warning("Invalid configuration for generator on key {0}: {1} (value: ''{2}''). Using default value ''{3}''.", key, e.getMessage(), e.getValue(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Tries to find a class from its name, by combining suffixes, packages, capitalization.
     *
     * @param name The class base name to search for.
     * @param optionalPackage An optional package to search in.
     * @param optionalSuffix An optional suffix to test, appended to the class name.
     * @param superClass The superclass this class must have.
     * @param <T> The superclass type this class must have.
     *
     * @return The {@link Class}, if found; else, {@code null}.
     */
    protected static <T> Class<? extends T> getClassFromName(final String name, final String optionalPackage, final String optionalSuffix, final Class<T> superClass)
    {
        final List<String> possibilities = Arrays.asList(
                optionalPackage + "." + name,
                optionalPackage + "." + name + optionalSuffix,
                optionalPackage + "." + StringUtils.capitalize(name),
                optionalPackage + "." + StringUtils.capitalize(name) + optionalSuffix,
                optionalPackage + "." + StringUtils.capitalize(name.toLowerCase()),
                optionalPackage + "." + StringUtils.capitalize(name.toLowerCase()) + optionalSuffix,
                name
        );

        for (String clazzName : possibilities)
        {
            try
            {
                final Class<?> clazz = Class.forName(clazzName);
                if (superClass.isAssignableFrom(clazz)) return (Class<? extends T>) clazz;
            }
            catch (final ClassNotFoundException ignored) { /* The search continues... */ }
        }

        return null;
    }

    protected static <T> T handleGenerationTool(final Map<?, ?> parameters, Class<? extends T> superClass) throws ConfigurationParseException
    {
        final String type = getValue(parameters, "type", String.class, null);
        final Class<? extends T> clazz = getClassFromName(type, superClass.getPackage().getName(), superClass.getSimpleName(), superClass);

        if (clazz == null) throw new ConfigurationParseException("Unable to load the " + type + " generator, maybe you misspelled it?", parameters);

        try
        {
            return clazz.getDeclaredConstructor(Map.class).newInstance(parameters);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException e)
        {
            throw new ConfigurationParseException("Unable to load the " + type + " generator: invalid constructor in class (accessible and with a single Map<?, ?> argument required)", parameters);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Unable to load the " + type + " generator: exception caught.", e);
        }
    }
}
