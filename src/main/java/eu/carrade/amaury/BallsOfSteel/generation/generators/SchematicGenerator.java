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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import eu.carrade.amaury.BallsOfSteel.MapConfig;
import eu.carrade.amaury.BallsOfSteel.generation.utils.WorldEditUtils;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.PluginLogger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SchematicGenerator extends Generator
{
    private final String schematicRelativePath;
    private final File schematicFile;
    private final Clipboard schematic;

    private final boolean skipAir;


    public SchematicGenerator(Map<?, ?> parameters)
    {
        super(parameters);

        skipAir = getValue(parameters, "skipAir", boolean.class, false);

        schematicRelativePath = getValue(parameters, "schematic", String.class, null);
        if (schematicRelativePath == null)
        {
            schematicFile = null;
            schematic = null;
            return;
        }

        schematicFile = new File(MapConfig.MAP_SCHEMATICS_DIRECTORY, schematicRelativePath);
        Clipboard clipboard;

        try
        {
            clipboard = WorldEditUtils.loadSchematic(schematicFile);
        }
        catch (IOException e)
        {
            PluginLogger.error("Unable to load schematic at {0}, will not be pasted", e, schematicFile.getAbsolutePath());
            clipboard = null;
        }

        schematic = clipboard;
    }

    @Override
    protected Region doGenerate() throws WorldEditException
    {
        if (schematic == null) return null;
        return WorldEditUtils.pasteClipboard(session, schematic, baseVector(), skipAir);
    }

    @Override
    public String doName()
    {
        return I.t("Schematic");
    }

    @Override
    public List<String> doSettingsDescription()
    {
        return Arrays.asList(
                I.t("File: {0}", schematicRelativePath != null ? schematicRelativePath : I.tc("schematics_settings_desc", "none")),
                I.t("Skipping air: {0}", skipAir ? I.t("yes") : I.t("no"))
        );
    }
}
