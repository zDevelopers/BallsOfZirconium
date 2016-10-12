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
package eu.carrade.amaury.BallsOfSteel.generation.generators;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.generation.generators.helpers.WithRadiusGenerator;
import fr.zcraft.zlib.components.i18n.I;

import java.util.Map;


public class CuboidGenerator extends WithRadiusGenerator
{
    public CuboidGenerator(Map parameters)
    {
        super(parameters);
    }

    @Override
    protected Region doGenerate() throws MaxChangedBlocksException
    {
        Region region = new CuboidRegion(
                baseVector().subtract(radius.getX() / 2, radius.getY() / 2, radius.getZ() / 2),
                baseVector().add(radius.getX() / 2, radius.getY() / 2, radius.getZ() / 2)
        );

        session.setBlocks(region, oldPattern(baseLocation.getWorld()));
        return region;
    }

    @Override
    public String doDescription()
    {
        return I.t("Cuboid faces {gray}(size {0}, pattern '{1}')", radius, patternString);
    }
}
