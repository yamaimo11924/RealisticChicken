package your.domain.minecraft.realisticChicken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LanguageManager {

    private static String currentLang = "en";

    public static void setLanguage(String lang) {
        currentLang = lang;
    }

    public static Component getInfertileEggLore() {

        if ("ja".equalsIgnoreCase(currentLang)) {
            return Component.text("【RC1】 無精卵", NamedTextColor.AQUA);
        } else {
            return Component.text("【RC1】 Unfertilized Egg", NamedTextColor.AQUA);
        }
    }

    public static Component getFertilizedEggLore() {

        if ("ja".equalsIgnoreCase(currentLang)) {
            return Component.text("【RC2】 有精卵", NamedTextColor.GREEN);
        } else {
            return Component.text("【RC2】 Fertilized Egg", NamedTextColor.GREEN);
        }
    }
}
