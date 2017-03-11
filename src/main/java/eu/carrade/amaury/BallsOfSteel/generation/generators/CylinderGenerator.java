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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import eu.carrade.amaury.BallsOfSteel.generation.generators.helpers.WithRadiusGenerator;
import fr.zcraft.zlib.components.i18n.I;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CylinderGenerator extends WithRadiusGenerator
{
    public CylinderGenerator(Map parameters)
    {
        super(parameters);
    }

    @Override
    protected Region doGenerate() throws MaxChangedBlocksException
    {
        session.makeCylinder(baseVector(), oldPattern(baseLocation.getWorld()), radius.getX(), radius.getZ(), radius.getBlockY(), true);

        final CylinderRegion region = new CylinderRegion(
                baseVector(), new Vector2D(radius.getX() + 1, radius.getZ() + 1),
                baseVector().getBlockY() - radius.getBlockY() - 4,
                baseVector().getBlockY() + radius.getBlockY() + 4
        );

        try { region.shift(new Vector(0, 2, 0)); }
        catch (RegionOperationException ignored) {}

        return region;
    }

    @Override
    public String doName()
    {
        return I.t("Cylinder");
    }

    @Override
    public List<String> doSettingsDescription()
    {
        return Arrays.asList(
                I.t("Radius: {0}", simpleRadius ? radius.getX() : radius),
                I.t("Pattern: {0}", patternString)
        );
    }
}
