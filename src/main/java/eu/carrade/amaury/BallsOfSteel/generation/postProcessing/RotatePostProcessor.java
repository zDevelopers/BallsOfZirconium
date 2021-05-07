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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.transform.AffineTransform;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.PluginLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class RotatePostProcessor extends PostProcessor
{
    private final Integer angleX;
    private final Integer angleY;
    private final Integer angleZ;

    public RotatePostProcessor(final Map<?, ?> parameters)
    {
        super(parameters);

        angleX = getAngle(parameters, "angleX");
        angleY = getAngle(parameters, "angleY");
        angleZ = getAngle(parameters, "angleZ");
    }

    /**
     * Returns an angle value (either a number for an angle, or null for random) from the parameters.
     *
     * @param parameters The parameters map.
     * @param key The parameter key.
     *
     * @return The angle value.
     */
    private Integer getAngle(final Map<?, ?> parameters, final String key)
    {
        final String rawAngle = getValue(parameters, key, String.class, "0").trim();

        if (rawAngle.equalsIgnoreCase("random"))
        {
            return null;
        }
        else
        {
            try { return Integer.valueOf(rawAngle); }
            catch (NumberFormatException e) { return 0; }
        }
    }

    /**
     * Returns an usable angle value (with random applied if needed)
     * from the stored angle from config.
     *
     * @param angle The angle ({@code null} for random).
     * @return The angle (the same if not {@code null}, a random one else).
     */
    private int getRealAngle(Integer angle)
    {
        if (angle == null)
            return new int[] {0, 90, 180, 270}[random.nextInt(4)];
        else
            return angle;
    }

    @Override
    protected void doProcess() throws WorldEditException
    {
        final int realAngle = getRealAngle(angleY);
        final AffineTransform transform = new AffineTransform()
                .rotateY(-realAngle)
                .rotateX(-getRealAngle(angleX))
                .rotateZ(-getRealAngle(angleZ));

        WorldEditUtils.applyTransform(session, region, transform);

        PluginLogger.info("Applied {0} - angleY {2} - transform {1}", doName(), transform, realAngle);
    }

    @Override
    public String doName()
    {
        return I.t("Rotation");
    }

    @Override
    public List<String> doSettingsDescription()
    {
        return Arrays.asList(
                I.t("X angle: {0}°", angleX == null ? I.t("<random>") : angleX),
                I.t("Y angle: {0}°", angleY == null ? I.t("<random>") : angleY),
                I.t("Z angle: {0}°", angleZ == null ? I.t("<random>") : angleZ)
        );
    }
}
