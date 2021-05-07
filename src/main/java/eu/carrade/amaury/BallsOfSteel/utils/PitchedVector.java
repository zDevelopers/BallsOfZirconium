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
package eu.carrade.amaury.BallsOfSteel.utils;

import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;


public class PitchedVector extends Vector
{
    private float pitch = 0f;
    private float yaw = 0f;

    public PitchedVector() {}

    public PitchedVector(int x, int y, int z)
    {
        super(x, y, z);
    }

    public PitchedVector(double x, double y, double z)
    {
        super(x, y, z);
    }

    public PitchedVector(float x, float y, float z)
    {
        super(x, y, z);
    }

    public PitchedVector(int x, int y, int z, float yaw, float pitch)
    {
        super(x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public PitchedVector(double x, double y, double z, float yaw, float pitch)
    {
        super(x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public PitchedVector(float x, float y, float z, float yaw, float pitch)
    {
        super(x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public float getYaw()
    {
        return yaw;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    @Override
    public Location toLocation(World world)
    {
        final Location location = super.toLocation(world);

        location.setPitch(pitch);
        location.setYaw(yaw);

        return location;
    }

    @ConfigurationValueHandler
    public static PitchedVector handlePitchedVector(String str) throws ConfigurationParseException
    {
        return handlePitchedVector(Arrays.asList(str.split(",")));
    }

    @ConfigurationValueHandler
    public static PitchedVector handlePitchedVector(List list) throws ConfigurationParseException
    {
        final int size = list.size();

        if (size < 2)
            throw new ConfigurationParseException("Not enough values, at least 2 (x,z) are required.", list);
        if (size > 5)
            throw new ConfigurationParseException("Too many values, at most 5 (x,y,z,yaw,pitch) can be used.", list);

        switch (size)
        {
            case 2:
                return new PitchedVector(
                        ConfigurationValueHandlers.handleDoubleValue(list.get(0)),
                        0,
                        ConfigurationValueHandlers.handleDoubleValue(list.get(1))
                );

            case 3:
                return new PitchedVector(
                        ConfigurationValueHandlers.handleDoubleValue(list.get(0)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(1)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(2))
                );

            case 4:
                return new PitchedVector(
                        ConfigurationValueHandlers.handleDoubleValue(list.get(0)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(1)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(2)),
                        ConfigurationValueHandlers.handleFloatValue(list.get(3)),
                        0f
                );

            default:
                return new PitchedVector(
                        ConfigurationValueHandlers.handleDoubleValue(list.get(0)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(1)),
                        ConfigurationValueHandlers.handleDoubleValue(list.get(2)),
                        ConfigurationValueHandlers.handleFloatValue(list.get(3)),
                        ConfigurationValueHandlers.handleFloatValue(list.get(4))
                );
        }
    }

    @ConfigurationValueHandler
    public static PitchedVector handlePitchedVector(Object obj) throws ConfigurationParseException
    {
        if (obj instanceof List) return handlePitchedVector((List) obj);
        else                     return handlePitchedVector(obj.toString());
    }
}
