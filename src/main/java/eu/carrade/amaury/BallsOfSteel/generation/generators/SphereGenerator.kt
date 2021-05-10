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
package eu.carrade.amaury.BallsOfSteel.generation.generators

import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.regions.EllipsoidRegion
import com.sk89q.worldedit.regions.Region
import eu.carrade.amaury.BallsOfSteel.generation.generators.helpers.WithRadiusGenerator
import fr.zcraft.quartzlib.components.i18n.I
import java.util.*

class SphereGenerator(parameters: Map<*, *>) : WithRadiusGenerator(parameters) {
    @Throws(WorldEditException::class)
    override fun doGenerate(): Region? {
        session!!.makeSphere(baseVector(), pattern(baseLocation!!.world), radius!!.x.toDouble(), radius!!.y.toDouble(), radius!!.z.toDouble(), true)
        return EllipsoidRegion(session!!.world, baseVector(), radius!!.add(1, 1, 1).toVector3())
    }

    override fun doName(): String {
        return I.t("Sphere")
    }

    override fun doSettingsDescription(): List<String>? {
        return Arrays.asList(
                I.t("Radius: {0}", if (simpleRadius) radius!!.x else radius),
                I.t("Pattern: {0}", patternString)
        )
    }
}