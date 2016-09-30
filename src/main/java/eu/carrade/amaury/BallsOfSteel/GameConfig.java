package eu.carrade.amaury.BallsOfSteel;

import eu.carrade.amaury.BallsOfSteel.game.BoSGameManager;
import eu.carrade.amaury.BallsOfSteel.utils.BoSSound;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.Sound;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;


public class GameConfig extends Configuration
{
    static
    {
        ConfigurationValueHandlers.registerHandlers(BoSSound.class);
    }


    static public final ConfigurationItem<Locale> LANG = item("lang", Locale.getDefault());

    static public final ConfigurationItem<String> GAME_NAME = item("gameName", "Balls of Steel");
    static public final ConfigurationItem<String> DURATION = item("duration", "59:59");
    static public final ConfigurationItem<Boolean> USE_BAR = item("useBar", true);
    
    static public final StartSection START = section("start", StartSection.class);
    static public class StartSection extends ConfigurationSection
    {
        public final ConfigurationItem<BoSSound> SOUND = item("sound", new BoSSound(null));
    }
    
    
    static public final DiamondsSection DIAMONDS = section("diamonds", DiamondsSection.class);
    static public class DiamondsSection extends ConfigurationSection
    {
        public final SoundsSection SOUNDS = section("sounds", SoundsSection.class);
        static public class SoundsSection extends ConfigurationSection
        {
            public final ConfigurationItem<BoSSound> COUNT_INCREASE = item("countIncrease", new BoSSound(Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 3f));
            public final ConfigurationItem<BoSSound> COUNT_DECREASE = item("countDecrease", new BoSSound(null));
            public final ConfigurationItem<BoSSound> CHEST_LOCKED   = item("chestLocked", new BoSSound(Sound.BLOCK_CHEST_LOCKED));
        }
    }
    
    
    static public final EquipmentSection EQUIPMENT = section("equipment", EquipmentSection.class);
    static public class EquipmentSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> FOOD = item("food", true);
        public final ConfigurationItem<Boolean> BLOCKS = item("blocks", true);
        public final ConfigurationItem<Boolean> TOOLS = item("tools", true);
        public final ConfigurationItem<Boolean> SWORD = item("sword", false);
        public final ConfigurationItem<Boolean> BOW = item("bow", true);
        public final ConfigurationItem<BoSGameManager.PlayerArmorType> ARMOR = item("armor", BoSGameManager.PlayerArmorType.NONE);
    }
    
    static public final ConfigurationItem<Boolean> COLORIZE_CHAT = item("colorizeChat", true);
    
    static public final TeamsOptionsSection TEAMS_OPTIONS = section("teams-options", TeamsOptionsSection.class);
    static public class TeamsOptionsSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> CAN_SEE_FRIENDLY_INVISIBLES = item("canSeeFriendlyInvisibles", true);
        public final ConfigurationItem<Boolean> ALLOW_FRIENDLY_FIRE = item("allowFriendlyFire", true);
        public final ConfigurationItem<Integer> MAX_PLAYERS_PER_TEAM = item("maxPlayersPerTeam", 0);
    }
    
    static public final ConfigurationItem<Boolean> LOG_TEAM_CHAT = item("logTeamChat", false);
}
