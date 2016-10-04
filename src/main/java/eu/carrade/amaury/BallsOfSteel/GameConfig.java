package eu.carrade.amaury.BallsOfSteel;

import eu.carrade.amaury.BallsOfSteel.game.BoSEquipmentManager;
import eu.carrade.amaury.BallsOfSteel.utils.BoSSound;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

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

    static public final BarSection BAR = section("bar", BarSection.class);
    static public class BarSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<String> TITLE = item("title", "Balls of Steel");
        public final ConfigurationItem<Boolean> DISPLAY_TITLE_DURING_GAME = item("displayTitleDuringGame", false);

        public final ConfigurationItem<BarColor> BAR_COLOR_BEFORE_GAME = item("barColorBeforeGame", BarColor.YELLOW);
        public final ConfigurationItem<BarStyle> BAR_STYLE_BEFORE_GAME = item("barStyleBeforeGame", BarStyle.SOLID);
        public final ConfigurationItem<BarColor> BAR_COLOR_DURING_GAME = item("barColorDuringGame", BarColor.YELLOW);
        public final ConfigurationItem<BarColor> BAR_COLOR_CLOSE_TO_END = item("barColorCloseToEnd", BarColor.RED);
        public final ConfigurationItem<BarStyle> BAR_STYLE_DURING_GAME = item("barStyleDuringGame", BarStyle.SEGMENTED_6);
        public final ConfigurationItem<BarColor> BAR_COLOR_AFTER_GAME = item("barColorAfterGame", BarColor.YELLOW);
        public final ConfigurationItem<BarStyle> BAR_STYLE_AFTER_GAME = item("barStyleAfterGame", BarStyle.SOLID);

        public final ConfigurationItem<Double> CLOSE_TO_END_PERCENTAGE = item("closeToEndPercentage", 0.167);
    }

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
        public final ConfigurationItem<BoSEquipmentManager.PlayerArmorType> ARMOR = item("armor", BoSEquipmentManager.PlayerArmorType.NONE);

        public final ConfigurationItem<Boolean> SURVIVAL_MODE = item("survivalMode", false);
        public final ConfigurationItem<Boolean> UNLOCK_WITH_ANY_MATERIAL = item("unlockWithAnyMaterial", false);
        public final ConfigurationItem<Boolean> CRAFT_ENCHANTED = item("craftEnchanted", true);
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
