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
package eu.carrade.amaury.BallsOfSteel.generation.structures;

import eu.carrade.amaury.BallsOfSteel.generation.utils.AbstractGenerationTool;


/**
 * Represents a generated structure—either a sphere or a static building.
 *
 * <p>This abstract class is used to accept only a generated structure, as
 * intermediate {@linkplain eu.carrade.amaury.BallsOfSteel.generation.generators.Generator generators}
 * and {@link eu.carrade.amaury.BallsOfSteel.generation.postProcessing.PostProcessor post-processors}
 * also extends {@linkplain AbstractGenerationTool}.</p>
 */
public abstract class Structure extends AbstractGenerationTool
{
    protected boolean enabled;

    protected String name;
    protected String displayName = null;

    public String getName()
    {
        return name;
    }

    public String getDisplayName()
    {
        return displayName != null ? displayName : name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    protected void setDisplayName(String name)
    {
        this.displayName = name;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Structure structure = (Structure) o;

        return !(name != null ? !name.equals(structure.name) : structure.name != null);

    }

    @Override
    public int hashCode()
    {
        return (name != null ? name.hashCode() : 0)
                + 31 * (getClass().getName().hashCode());
    }
}
