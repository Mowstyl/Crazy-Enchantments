package me.badbones69.crazyenchantments.api.objects;

import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.FileManager.Files;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlackSmithResult {
	
	private int cost;
	private ItemStack resultItem;
	private static CrazyEnchantments ce = CrazyEnchantments.getInstance();
	
	public BlackSmithResult(Player player, ItemStack mainItem, ItemStack subItem) {
		ItemStack resultItem = mainItem.clone();
		int cost = 0;
		if(mainItem.getType() == ce.getEnchantmentBook().getMaterial() && subItem.getType() == ce.getEnchantmentBook().getMaterial()) {
			CEBook mainBook = ce.getCEBook(mainItem);
			CEBook subBook = ce.getCEBook(subItem);
			if(mainBook.getEnchantment() == subBook.getEnchantment()) {
				resultItem = mainBook.setLevel(mainBook.getLevel() + 1).buildBook();
				cost += Files.CONFIG.getFile().getInt("Settings.BlackSmith.Transaction.Costs.Book-Upgrade");
			}
		}else {
			if(mainItem.getType() == subItem.getType()) {
				HashMap<CEnchantment, Integer> duplicateEnchantments = new HashMap<>();
				HashMap<CEnchantment, Integer> newEnchantments = new HashMap<>();
				HashMap<CEnchantment, Integer> higherLevelEnchantments = new HashMap<>();
				int maxEnchants = ce.getPlayerMaxEnchantments(player);
				for(CEnchantment enchantment : ce.getEnchantmentsOnItem(mainItem)) {
					if(ce.hasEnchantment(subItem, enchantment)) {
						if(ce.getLevel(mainItem, enchantment) == ce.getLevel(subItem, enchantment)) {
							if(!duplicateEnchantments.containsKey(enchantment)) {
								duplicateEnchantments.put(enchantment, ce.getLevel(mainItem, enchantment));
							}
						}else {
							if(ce.getLevel(mainItem, enchantment) < ce.getLevel(subItem, enchantment)) {
								higherLevelEnchantments.put(enchantment, ce.getLevel(subItem, enchantment));
							}
						}
					}
				}
				for(CEnchantment enchantment : ce.getEnchantmentsOnItem(subItem)) {
					if(!duplicateEnchantments.containsKey(enchantment) && !higherLevelEnchantments.containsKey(enchantment)) {
						if(!ce.hasEnchantment(mainItem, enchantment)) {
							newEnchantments.put(enchantment, ce.getLevel(subItem, enchantment));
						}
					}
				}
				for(CEnchantment enchantment : duplicateEnchantments.keySet()) {
					int level = duplicateEnchantments.get(enchantment);
					if(level + 1 <= enchantment.getMaxLevel()) {
						resultItem = ce.addEnchantment(resultItem, enchantment, level + 1);
						cost += Files.CONFIG.getFile().getInt("Settings.BlackSmith.Transaction.Costs.Power-Up");
					}
				}
				for(CEnchantment enchantment : newEnchantments.keySet()) {
					if(Files.CONFIG.getFile().getBoolean("Settings.EnchantmentOptions.MaxAmountOfEnchantmentsToggle")) {
						if((Methods.getEnchantmentAmount(resultItem) + 1) <= maxEnchants) {
							if(enchantment != null) {
								resultItem = ce.addEnchantment(resultItem, enchantment, newEnchantments.get(enchantment));
								cost += Files.CONFIG.getFile().getInt("Settings.BlackSmith.Transaction.Costs.Add-Enchantment");
							}
						}
					}
				}
				for(CEnchantment enchantment : higherLevelEnchantments.keySet()) {
					resultItem = ce.addEnchantment(resultItem, enchantment, higherLevelEnchantments.get(enchantment));
					cost += Files.CONFIG.getFile().getInt("Settings.BlackSmith.Transaction.Costs.Power-Up");
				}
			}
		}
		this.resultItem = resultItem;
		this.cost = cost;
	}
	
	public int getCost() {
		return cost;
	}
	
	public ItemStack getResultItem() {
		return resultItem;
	}
	
}