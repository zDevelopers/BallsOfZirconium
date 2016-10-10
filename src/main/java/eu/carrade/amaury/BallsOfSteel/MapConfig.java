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
package eu.carrade.amaury.BallsOfSteel;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import eu.carrade.amaury.BallsOfSteel.generation.GenerationProcess;
import eu.carrade.amaury.BallsOfSteel.generation.StaticBuilding;
import eu.carrade.amaury.BallsOfSteel.generation.generators.Generator;
import eu.carrade.amaury.BallsOfSteel.generation.postProcessing.PostProcessor;
import eu.carrade.amaury.BallsOfSteel.teams.BoSTeam;
import eu.carrade.amaury.BallsOfSteel.utils.PitchedVector;
import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.World;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.*;


public class MapConfig extends ConfigurationInstance
{
    static
    {
        ConfigurationValueHandlers.registerHandlers(PitchedVector.class);
        ConfigurationValueHandlers.registerHandlers(BoSTeam.class);

        ConfigurationValueHandlers.registerHandlers(MapConfig.class);
        ConfigurationValueHandlers.registerHandlers(Generator.class);
        ConfigurationValueHandlers.registerHandlers(PostProcessor.class);
        ConfigurationValueHandlers.registerHandlers(GenerationProcess.class);
        ConfigurationValueHandlers.registerHandlers(StaticBuilding.class);
    }

    public MapConfig()
    {
        super("map.yml");
    }

    public final static ConfigurationItem<String> WORLD = item("world-name", "world");
    public final static ConfigurationList<BoSTeam> TEAMS = list("map-teams", BoSTeam.class);

    public final static GenerationSection GENERATION = section("generation", GenerationSection.class);
    public final static class GenerationSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Boolean> LOGS = item("logs", true);

        public final MapSection MAP = section("map", MapSection.class);
        public final static class MapSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> SEED = item("seed", "");

            public final BoundariesSection BOUNDARIES = section("boundaries", BoundariesSection.class);
            public final static class BoundariesSection extends ConfigurationSection
            {
                public final ConfigurationItem<Vector> CORNER_1 = item("corner1", Vector.ZERO);
                public final ConfigurationItem<Vector> CORNER_2 = item("corner2", Vector.ZERO);
            }

            public final ConfigurationItem<Vector> SPAWN = item("spawn", Vector.ZERO);
            public final ConfigurationItem<World.Environment> ENVIRONMENT = item("environment", World.Environment.NORMAL);

            public final ConfigurationItem<Boolean> GENERATE_AT_STARTUP = item("generateAtStartup", true);
            public final ConfigurationItem<Boolean> GENERATE_FULLY = item("generateFully", true);

            public final ConfigurationItem<Integer> DISTANCE_BETWEEN_SPHERES = item("distanceBetweenSpheres", 32);
            public final ConfigurationItem<Integer> DISTANCE_BETWEEN_SPHERES_AND_STATIC_BUILDINGS = item("distanceBetweenSpheresAndStaticBuildings", 48);
        }

        public final ConfigurationList<StaticBuilding> STATIC_BUILDINGS = list("staticBuildings", StaticBuilding.class);
        public final ConfigurationList<GenerationProcess> SPHERES = list("spheres", GenerationProcess.class);
    }


    @ConfigurationValueHandler
    public static Vector handleVector(Object obj) throws ConfigurationParseException
    {
        return BukkitUtil.toVector(ConfigurationValueHandlers.handleValue(obj, org.bukkit.util.Vector.class));
    }
}
