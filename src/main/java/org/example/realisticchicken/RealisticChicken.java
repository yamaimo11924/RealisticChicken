package your.domain.minecraft.realisticChicken;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

public final class RealisticChicken extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("RealisticChicken enabled");

    }

    @Override
    public void onDisable() {

        getLogger().info("RealisticChicken disabled");

    }

    /**
     * ニワトリの自然な卵ドロップを無効化
     */
    @EventHandler
    public void onChickenLayEgg(EntityDropItemEvent event) {

        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.CHICKEN) return;

        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() == Material.EGG) {

            // 自動産卵をキャンセル
            event.setCancelled(true);

        }

    }

    /**
     * 繁殖時の処理
     * ・子どもスポーンをキャンセル
     * ・代わりに卵をドロップ
     */
    @EventHandler
    public void onBreed(EntityBreedEvent event) {

        if (!(event.getMother() instanceof org.bukkit.entity.Chicken)) return;

        // 子どもスポーン防止
        event.setCancelled(true);

        if (!(event.getMother() instanceof Animals mother)) return;
        if (!(event.getFather() instanceof Animals father)) return;

        // バニラと同じクールダウン（5分）
        mother.setAge(6000);
        father.setAge(6000);

        mother.setLoveModeTicks(0);
        father.setLoveModeTicks(0);

        var world = mother.getWorld();

        // 卵を1～3個ランダム
        int eggCount = ThreadLocalRandom.current().nextInt(1, 4);

        var loc = mother.getLocation().clone().add(0, 0.6, 0);

        world.dropItemNaturally(
                loc,
                new ItemStack(Material.EGG, eggCount)
        );

        // バニラと同じ経験値 1～7
        int exp = ThreadLocalRandom.current().nextInt(1, 8);

        ExperienceOrb orb = world.spawn(loc, ExperienceOrb.class);
        orb.setExperience(exp);
    }

}
