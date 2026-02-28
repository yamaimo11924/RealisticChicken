package your.domain.minecraft.realisticChicken;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Particle;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class RealisticChicken extends JavaPlugin implements Listener {

    private final NamespacedKey rc1Key = new NamespacedKey(this, "rc1_no_spawn");

    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        String lang = this.getConfig().getString("language", "en");
        new NamespacedKey(this, "RC_type");
        LanguageManager.setLanguage(lang);
        this.getLogger().info("RealisticChicken enabled");
    }

    public void onDisable() {
        this.getLogger().info("RealisticChicken disabled");
    }

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent event) {

        ItemStack drop = event.getItemDrop().getItemStack();

        if (drop.getType() == Material.EGG
                || drop.getType().getKey().toString().equals("minecraft:blue_egg")
                || drop.getType().getKey().toString().equals("minecraft:brown_egg")) {

            ItemMeta meta = drop.getItemMeta();

            if (meta != null) {
                meta.lore(List.of(LanguageManager.getInfertileEggLore()));
                drop.setItemMeta(meta);
            }
        }
    }

    @EventHandler
    public void onChickenBreed(EntityBreedEvent event) {

        if (event.isCancelled()) return;

        if (!(event.getMother() instanceof Chicken mother)) return;
        if (!(event.getFather() instanceof Chicken father)) return;

        event.setCancelled(true);

        mother.setAge(6000);
        father.setAge(6000);

        mother.setLoveModeTicks(0);
        father.setLoveModeTicks(0);

        Material eggType;

        if (mother.getVariant() == Chicken.Variant.WARM) {
            eggType = Material.BROWN_EGG;
        } else if (mother.getVariant() == Chicken.Variant.COLD) {
            eggType = Material.BLUE_EGG;
        } else {
            eggType = Material.EGG;
        }

        int eggCount = ThreadLocalRandom.current().nextInt(1, 4);

        for (int i = 0; i < eggCount; i++) {
            ItemStack egg = new ItemStack(eggType);

            ItemMeta meta = egg.getItemMeta();
            if (meta != null) {
                meta.lore(List.of(LanguageManager.getFertilizedEggLore()));
                egg.setItemMeta(meta);
            }

            mother.getWorld().dropItemNaturally(mother.getLocation().add(0, 0.5, 0), egg);
        }

        int exp = ThreadLocalRandom.current().nextInt(1, 8);
        mother.getWorld().spawn(
                mother.getLocation().add(0, 0.5, 0),
                ExperienceOrb.class,
                orb -> orb.setExperience(exp)
        );

    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Egg egg)) return;

        ItemStack item = egg.getItem();
        if (item == null) return;

        NamespacedKey itemKey = new NamespacedKey(this, "egg_item_id");
        egg.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, item.getType().name());

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            boolean isRC1 = false;
            boolean isInfertile = false;
            StringBuilder loreOutput = new StringBuilder();

            for (var line : Objects.requireNonNull(meta.lore())) {
                String text = PlainTextComponentSerializer.plainText().serialize(line);
                loreOutput.append(text).append(" | ");
                if (line.color() == NamedTextColor.AQUA && text.contains("【RC1】")) isRC1 = true;
                if (text.contains("無精卵") || text.contains("Infertile")) isInfertile = true;
            }

            NamespacedKey rc1Key = new NamespacedKey(this, "rc1_no_spawn");
            NamespacedKey infertileKey = new NamespacedKey(this, "rc1_infertile");
            if (isRC1) egg.getPersistentDataContainer().set(rc1Key, PersistentDataType.BYTE, (byte)1);
            if (isInfertile) egg.getPersistentDataContainer().set(infertileKey, PersistentDataType.BYTE, (byte)1);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg egg)) return;

        NamespacedKey itemKey = new NamespacedKey(this, "egg_item_id");
        NamespacedKey infertileKey = new NamespacedKey(this, "rc1_infertile");

        String itemId = egg.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        Byte infertileFlag = egg.getPersistentDataContainer().get(infertileKey, PersistentDataType.BYTE);

        if (infertileFlag != null && infertileFlag == 1 && itemId != null) {

            Material mat = Material.getMaterial(itemId);
            if (mat != null) {
                egg.getWorld().spawnParticle(
                        Particle.ITEM,
                        egg.getLocation(),
                        8,
                        0.2, 0.2, 0.2,
                        0.05,
                        new ItemStack(mat)
                );
            }
            egg.remove();
        }
    }

    @EventHandler
    public void onEggHatch(ThrownEggHatchEvent event) {
        Egg egg = event.getEgg();

        NamespacedKey infertileKey = new NamespacedKey(this, "rc1_infertile");

        Byte rc1Flag = egg.getPersistentDataContainer().get(rc1Key, PersistentDataType.BYTE);
        Byte infertileFlag = egg.getPersistentDataContainer().get(infertileKey, PersistentDataType.BYTE);

        if ((rc1Flag != null && rc1Flag == 1) || (infertileFlag != null && infertileFlag == 1)) {
            event.setHatching(false);
        }
    }
}
