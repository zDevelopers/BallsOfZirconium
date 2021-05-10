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
package eu.carrade.amaury.ballsofzirconium.generation.utils

import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.EllipsoidRegion

object GeometryUtils {
    /**
     * Checks if a cuboid and a **sphere** intersects.
     *
     * @param cuboid A cuboid.
     * @param ellipsoid A sphere (first component of the radii used as radii).
     *
     * @return `true` if the cuboid and sphere intersects.
     */
    @JvmStatic
    fun intersects(cuboid: CuboidRegion, ellipsoid: EllipsoidRegion): Boolean {
        var distanceMin = 0f
        val sphereCenter = ellipsoid.center
        val cuboidMin = cuboid.minimumPoint
        val cuboidMax = cuboid.maximumPoint
        if (sphereCenter.x < cuboidMin.x) distanceMin += Math.pow(sphereCenter.x - cuboidMin.x, 2.0).toFloat() else if (sphereCenter.x > cuboidMax.x) distanceMin += Math.pow(sphereCenter.x - cuboidMax.x, 2.0).toFloat()
        if (sphereCenter.y < cuboidMin.y) distanceMin += Math.pow(sphereCenter.y - cuboidMin.y, 2.0).toFloat() else if (sphereCenter.y > cuboidMax.y) distanceMin += Math.pow(sphereCenter.y - cuboidMax.y, 2.0).toFloat()
        if (sphereCenter.z < cuboidMin.z) distanceMin += Math.pow(sphereCenter.z - cuboidMin.z, 2.0).toFloat() else if (sphereCenter.z > cuboidMax.z) distanceMin += Math.pow(sphereCenter.z - cuboidMax.z, 2.0).toFloat()
        return distanceMin <= Math.pow(ellipsoid.radius.x, 2.0)
    }
}
