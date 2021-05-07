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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;


public final class GeometryUtils
{
    private GeometryUtils() {}


    /**
     * Checks if a cuboid and a <strong>sphere</strong> intersects.
     *
     * @param cuboid A cuboid.
     * @param ellipsoid A sphere (first component of the radii used as radii).
     *
     * @return {@code true} if the cuboid and sphere intersects.
     */
    public static boolean intersects(final CuboidRegion cuboid, final EllipsoidRegion ellipsoid)
    {
        float distanceMin = 0;

        final Vector3 sphereCenter = ellipsoid.getCenter();
        final BlockVector3 cuboidMin = cuboid.getMinimumPoint();
        final BlockVector3 cuboidMax = cuboid.getMaximumPoint();

        if (sphereCenter.getX() < cuboidMin.getX())
            distanceMin += Math.pow(sphereCenter.getX() - cuboidMin.getX(), 2);
        else if (sphereCenter.getX() > cuboidMax.getX())
            distanceMin += Math.pow(sphereCenter.getX() - cuboidMax.getX(), 2);

        if (sphereCenter.getY() < cuboidMin.getY())
            distanceMin += Math.pow(sphereCenter.getY() - cuboidMin.getY(), 2);
        else if (sphereCenter.getY() > cuboidMax.getY())
            distanceMin += Math.pow(sphereCenter.getY() - cuboidMax.getY(), 2);

        if (sphereCenter.getZ() < cuboidMin.getZ())
            distanceMin += Math.pow(sphereCenter.getZ() - cuboidMin.getZ(), 2);
        else if (sphereCenter.getZ() > cuboidMax.getZ())
            distanceMin += Math.pow(sphereCenter.getZ() - cuboidMax.getZ(), 2);

        return distanceMin <= Math.pow(ellipsoid.getRadius().getX(), 2);
    }
}
