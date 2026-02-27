package your.domain.minecraft.realisticChicken;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LanguageManager {

    private static String currentLang = "en";

    public static void setLanguage(String lang) {
        currentLang = lang;
    }

    public static Component getEggLore(boolean fertilized) {
        if ("ja".equalsIgnoreCase(currentLang)) {
            return Component.text(fertilized ? "【RC＋】有精卵" : "【RC＋】無精卵",
                    fertilized ? NamedTextColor.GREEN : NamedTextColor.AQUA);
        } else {
            return Component.text(fertilized ? "[RC+] Fertilized Egg" : "[RC+] Infertile Egg",
                    fertilized ? NamedTextColor.GREEN : NamedTextColor.AQUA);
        }
    }
}
