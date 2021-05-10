package eu.carrade.amaury.BallsOfSteel.generation.utils

import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers
import fr.zcraft.quartzlib.tools.PluginLogger
import org.apache.commons.lang.StringUtils
import java.lang.reflect.InvocationTargetException
import java.util.*

object GenerationConfig {

    /**
     * Returns a value from a configuration parameters map, or a default if
     * invalid or not found.
     *
     * Internally uses [ConfigurationValueHandlers] to convert the value,
     * so the classes must be registered!
     *
     * @param params        The map of parameters.
     * @param key           The parameters key.
     * @param parameterType The parameter type for this key
     * @param defaultValue  A default value to return if the given parameter is
     * invalid or not found.
     * @param <T>           The parameter type
     *
     * @return The value, of the default value if invalid.
    </T> */
    @JvmStatic
    fun <T> getValue(params: Map<*, *>, key: String?, parameterType: Class<T>?, defaultValue: T): T {
        return try {
            if (params.containsKey(key)) ConfigurationValueHandlers.handleValue(params[key], parameterType) else defaultValue
        } catch (e: ConfigurationParseException) {
            PluginLogger.warning("Invalid configuration for generator on key {0}: {1} (value: ''{2}''). Using default value ''{3}''.", key, e.message, e.value, defaultValue)
            defaultValue
        }
    }

    /**
     * Returns a value from a configuration parameters map, or null if invalid or not found.
     *
     * Internally uses [ConfigurationValueHandlers] to convert the value,
     * so the classes must be registered!
     *
     * @param params        The map of parameters.
     * @param key           The parameters key.
     * @param parameterType The parameter type for this key
     * invalid or not found.
     * @param <T>           The parameter type
     *
     * @return The value, of the default value if invalid.
    </T> */
    @JvmStatic
    fun <T> getValueOrNull(params: Map<*, *>, key: String?, parameterType: Class<T>?): T? {
        return try {
            if (params.containsKey(key)) ConfigurationValueHandlers.handleValue(params[key], parameterType) else null
        } catch (e: ConfigurationParseException) {
            PluginLogger.warning("Invalid configuration for generator on key {0}: {1} (value: ''{2}'').", key, e.message, e.value)
            null
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
     * @return The [Class], if found; else, `null`.
    </T> */
    @JvmStatic
    private fun <T> getClassFromName(name: String?, optionalPackage: String, optionalSuffix: String, superClass: Class<T>): Class<out T>? {
        val possibilities = listOf(
            "$optionalPackage.$name",
            "$optionalPackage.$name$optionalSuffix",
            "$optionalPackage.${StringUtils.capitalize(name)}",
            "$optionalPackage.${StringUtils.capitalize(name)}$optionalSuffix",
            "$optionalPackage.${StringUtils.capitalize(name!!.lowercase(Locale.getDefault()))}",
            "$optionalPackage.${StringUtils.capitalize(name.lowercase(Locale.getDefault()))}$optionalSuffix",
            name
        )

        for (clazzName in possibilities) {
            try {
                val clazz = Class.forName(clazzName)
                if (superClass.isAssignableFrom(clazz)) return clazz as Class<out T>
            } catch (ignored: ClassNotFoundException) { /* The search continues... */ }
        }

        return null
    }

    @JvmStatic
    @Throws(ConfigurationParseException::class)
    fun <T> handleGenerationTool(parameters: Map<*, *>, superClass: Class<out T>): T {
        val type = GenerationConfig.getValue(parameters, "type", String::class.java, "")

        if (type.isEmpty()) throw ConfigurationParseException("The generator type is required", parameters)

        val clazz = getClassFromName(type, superClass.getPackage().name, superClass.simpleName, superClass)
            ?: throw ConfigurationParseException("Unable to load the $type generator, maybe you misspelled it?", parameters)

        return try {
            clazz.getDeclaredConstructor(MutableMap::class.java).newInstance(parameters)
        } catch (e: NoSuchMethodException) {
            throw ConfigurationParseException("Unable to load the $type generator: invalid constructor in class (accessible and with a single Map<?, ?> argument required)", parameters)
        } catch (e: InstantiationException) {
            throw ConfigurationParseException("Unable to load the $type generator: invalid constructor in class (accessible and with a single Map<?, ?> argument required)", parameters)
        } catch (e: IllegalAccessException) {
            throw ConfigurationParseException("Unable to load the $type generator: invalid constructor in class (accessible and with a single Map<?, ?> argument required)", parameters)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Unable to load the $type generator: exception caught.", e)
        }
    }
}
