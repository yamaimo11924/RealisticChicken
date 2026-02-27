package your.domain.minecraft.realisticChicken;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class RealisticChicken extends JavaPlugin implements Listener {

    private NamespacedKey eggKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("RealisticChicken enabled");
        eggKey = new NamespacedKey(this, "RC_fertilized");

        String lang = getConfig().getString("language", "en");
        LanguageManager.setLanguage(lang);
    }

    @Override
    public void onDisable() {
        getLogger().info("RealisticChicken disabled");
    }

    /** 自動産卵を無精卵に変換 */
    @EventHandler
    public void onChickenLayEgg(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof Chicken chicken)) return;

        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() != Material.EGG) return;

        event.setCancelled(true);

        ItemStack egg = createEgg(false);
        chicken.getWorld().dropItemNaturally(chicken.getLocation().add(0, 0.5, 0), egg);
    }

    /** 繁殖時の処理（子どもを作らず有精卵をドロップ） */
    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getMother() instanceof Chicken mother)) return;
        if (!(event.getFather() instanceof Chicken father)) return;

        event.setCancelled(true);

        mother.setAge(6000);
        father.setAge(6000);
        mother.setLoveModeTicks(0);
        father.setLoveModeTicks(0);

        World world = mother.getWorld();

        int eggCount = ThreadLocalRandom.current().nextInt(1, 4);
        for (int i = 0; i < eggCount; i++) {
            ItemStack egg = createEgg(true);
            world.dropItemNaturally(mother.getLocation().add(0, 0.5, 0), egg);
        }

        int exp = ThreadLocalRandom.current().nextInt(1, 8);
        ExperienceOrb orb = world.spawn(mother.getLocation().add(0, 0.5, 0), ExperienceOrb.class);
        orb.setExperience(exp);
    }

    /** 有精卵か無精卵かを判定してItemStackを作成 */
    private ItemStack createEgg(boolean fertilized) {
        ItemStack egg = new ItemStack(Material.EGG, 1);
        ItemMeta meta = egg.getItemMeta();
        if (meta != null) {
            meta.lore(java.util.List.of(LanguageManager.getEggLore(fertilized)));
            meta.getPersistentDataContainer().set(eggKey, PersistentDataType.INTEGER, fertilized ? 1 : 0);
            egg.setItemMeta(meta);
        }
        return egg;
    }
    /** 卵を投げた時にPersistentDataをコピー */
    @EventHandler
    public void onEggThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Egg egg)) return;
        if (!(egg.getShooter() instanceof Player player)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.EGG) return;

        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;

        Integer val = meta.getPersistentDataContainer().get(eggKey, PersistentDataType.INTEGER);
        if (val == null) return;

        egg.getPersistentDataContainer().set(eggKey, PersistentDataType.INTEGER, val);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG) {
            event.setCancelled(true);
        }
    }
    /** 卵がヒットした時の処理 */

    @EventHandler
    public void onEggHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg egg)) return;

        Integer val = egg.getPersistentDataContainer().get(eggKey, PersistentDataType.INTEGER);
        boolean fertilized = val != null && val == 1;

        World world = egg.getWorld();
        Location loc = egg.getLocation();

        world.spawnParticle(
                Particle.ITEM,
                loc,
                10,
                0.2, 0.2, 0.2,
                0.1,
                new ItemStack(Material.EGG)
        );

        if (!fertilized) {
            egg.remove();
            return;
        }
        
        if (ThreadLocalRandom.current().nextInt(8) == 0) {
            int chicksToSpawn = 1;
            if (ThreadLocalRandom.current().nextInt(4) == 0) chicksToSpawn = 4;

            for (int i = 0; i < chicksToSpawn; i++) {
                Chicken chick = world.spawn(loc, Chicken.class);
                chick.setAge(-24000);
            }
        }

        egg.remove();
    }

}
