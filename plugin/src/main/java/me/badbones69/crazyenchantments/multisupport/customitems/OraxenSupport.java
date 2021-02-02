package me.badbones69.crazyenchantments.multisupport.customitems;

import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.provided.durability.DurabilityMechanic;
import io.th0rgal.oraxen.mechanics.provided.durability.DurabilityMechanicFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OraxenSupport {

    public static int getDamage(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !(item.getItemMeta() instanceof Damageable)) {
            return 0;
        }
        String oraxenId = OraxenItems.getIdByItem(item);
        ItemMeta itemMeta = item.getItemMeta();
        Damageable damageableMeta = (Damageable) itemMeta;
        DurabilityMechanicFactory durabilityFactory = DurabilityMechanicFactory.get();
        if (oraxenId == null || durabilityFactory.isNotImplementedIn(oraxenId)) {
            return damageableMeta.getDamage();
        }
        DurabilityMechanic durabilityMechanic = (DurabilityMechanic) durabilityFactory.getMechanic(oraxenId);
        int maxDurability = durabilityMechanic.getItemMaxDurability();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        Integer currentDurability = dataContainer.get(DurabilityMechanic.NAMESPACED_KEY, PersistentDataType.INTEGER);

        return currentDurability != null ? maxDurability - currentDurability : 0;
    }

    public static void setDamage(ItemStack item, int newDamage) {
        if (item == null || !item.hasItemMeta() || !(item.getItemMeta() instanceof Damageable)) {
            return;
        }
        String oraxenId = OraxenItems.getIdByItem(item);
        ItemMeta itemMeta = item.getItemMeta();
        Damageable damageableMeta = (Damageable) itemMeta;
        DurabilityMechanicFactory durabilityFactory = DurabilityMechanicFactory.get();
        if (oraxenId == null || durabilityFactory.isNotImplementedIn(oraxenId)) {
            damageableMeta.setDamage(newDamage);
        }
        else {
            DurabilityMechanic durabilityMechanic = (DurabilityMechanic) durabilityFactory.getMechanic(oraxenId);
            int maxDurability = durabilityMechanic.getItemMaxDurability();
            int newDurability = Math.max(Math.min(maxDurability - newDamage, maxDurability), 0);
            newDamage = maxDurability - newDurability;
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            dataContainer.set(DurabilityMechanic.NAMESPACED_KEY, PersistentDataType.INTEGER, newDurability);
            damageableMeta.setDamage(newDamage > 0 ?
                    (int) (((double) item.getType().getMaxDurability() * newDamage) / maxDurability) :
                    0);
        }
        item.setItemMeta(itemMeta);
    }
}
