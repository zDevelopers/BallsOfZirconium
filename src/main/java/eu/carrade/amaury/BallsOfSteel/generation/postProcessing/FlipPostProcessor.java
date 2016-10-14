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
package eu.carrade.amaury.BallsOfSteel.generation.postProcessing;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.util.Direction;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;

import java.util.Map;


public class FlipPostProcessor extends PostProcessor
{
    private final FlipDirection direction;

    public FlipPostProcessor(Map parameters)
    {
        super(parameters);

        direction = getValue(parameters, "direction", FlipDirection.class, FlipDirection.RANDOM);
    }

    @Override
    protected void doProcess() throws MaxChangedBlocksException
    {
        final Direction weDirection = getDirection(direction);
        if (weDirection == null) return;

        final AffineTransform transform = new AffineTransform().scale(weDirection.toVector().positive().multiply(-2).add(1, 1, 1));
        WorldEditUtils.applyTransform(session, region, transform, region.getMinimumPoint());

        PluginLogger.info("Flip, direction {0}, transformation {1}", weDirection, transform);
    }

    @Override
    public String doDescription()
    {
        return I.t("Flip {gray}(direction '{0}')", direction.toString().toLowerCase().replace('_', ' '));
    }

    /**
     * Returns a {@link Direction} from our internal {@link FlipDirection}, generating a random
     * direction if needed.
     *
     * @param direction The direction.
     * @return A {@link Direction}, or {@code null} if no transformation should be applied.
     */
    private Direction getDirection(final FlipDirection direction)
    {
        final Direction[] availableDirections;

        switch (direction)
        {
            case UP:
            case DOWN:
            case EAST:
            case WEST:
            case NORTH:
            case SOUTH:
                return Direction.valueOf(direction.name());

            case RANDOM:
                availableDirections = new Direction[] {null, Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
                break;

            case RANDOM_HORIZONTAL:
                availableDirections = new Direction[] {null, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
                break;

            case RANDOM_VERTICAL:
                availableDirections = new Direction[] {null, Direction.UP, Direction.DOWN};
                break;

            default:
                availableDirections = new Direction[] {null};
        }

        return availableDirections[random.nextInt(availableDirections.length)];
    }

    /**
     * The flip direction to use
     */
    public enum FlipDirection
    {
        UP,
        DOWN,
        EAST,
        WEST,
        NORTH,
        SOUTH,
        RANDOM,
        RANDOM_HORIZONTAL,
        RANDOM_VERTICAL
    }
}
