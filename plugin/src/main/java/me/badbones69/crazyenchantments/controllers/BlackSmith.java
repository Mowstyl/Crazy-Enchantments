package me.badbones69.crazyenchantments.controllers;

import me.badbones69.crazyenchantments.Methods;
import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.FileManager.Files;
import me.badbones69.crazyenchantments.api.currencyapi.Currency;
import me.badbones69.crazyenchantments.api.currencyapi.CurrencyAPI;
import me.badbones69.crazyenchantments.api.enums.Messages;
import me.badbones69.crazyenchantments.api.objects.BlackSmithResult;
import me.badbones69.crazyenchantments.api.objects.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.simpleyaml.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BlackSmith implements Listener {
	
	private CrazyEnchantments ce = CrazyEnchantments.getInstance();
	private int mainSlot = 10;
	private int subSlot = 13;
	private ItemStack denyBarrier = new ItemBuilder().setMaterial(Material.BARRIER).setName(Files.CONFIG.getFile().getString("Settings.BlackSmith.Results.None")).setLore(Files.CONFIG.getFile().getStringList("Settings.BlackSmith.Results.Not-Found-Lore")).build();
	private ItemStack redGlass = new ItemBuilder().setMaterial("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:14").setName(" ").build();
	private ItemStack blueGlass = new ItemBuilder().setMaterial("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:3").build();
	
	public static void openBlackSmith(Player player) {
		Inventory inv = Bukkit.createInventory(null, 27, Methods.color(Files.CONFIG.getFile().getString("Settings.BlackSmith.GUIName")));
		List<Integer> other = Arrays.asList(1, 2, 3, 4, 5, 6, 10, 12, 13, 15, 19, 20, 21, 22, 23, 24);
		List<Integer> result = Arrays.asList(7, 8, 9, 16, 18, 25, 26, 27);
		for(int i : other)
			inv.setItem(i - 1, new ItemBuilder().setMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:7").setName(" ").build());
		for(int i : result)
			inv.setItem(i - 1, new ItemBuilder().setMaterial("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:14").setName(" ").build());
		ItemStack item = new ItemBuilder().setMaterial(Material.BARRIER).setName(Files.CONFIG.getFile().getString("Settings.BlackSmith.Results.None")).build();
		if(Files.CONFIG.getFile().contains("Settings.BlackSmith.Results.Not-Found-Lore")) {
			for(String line : Files.CONFIG.getFile().getStringList("Settings.BlackSmith.Results.Not-Found-Lore")) {
				item = Methods.addLore(item, line);
			}
		}
		inv.setItem(16, item);
		player.openInventory(inv);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		List<Integer> result = Arrays.asList(7, 8, 9, 16, 18, 25, 26, 27);
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		FileConfiguration config = Files.CONFIG.getFile();
		if(inv != null) {
			if(e.getView().getTitle().equals(Methods.color(config.getString("Settings.BlackSmith.GUIName")))) {
				e.setCancelled(true);
				if(e.getCurrentItem() != null) {
					ItemStack item = e.getCurrentItem();
					int resultSlot = 16;
					if(e.getRawSlot() > 27) {// Click In Players Inventory
						if(item.getAmount() != 1) return;
						if(ce.hasEnchantments(item) || item.getType() == ce.getEnchantmentBook().getMaterial()) {
							if(item.getType() == ce.getEnchantmentBook().getMaterial()) {//Is a custom enchantment book.
								if(!ce.isEnchantmentBook(item)) {
									return;
								}
							}
							if(inv.getItem(mainSlot) == null) {//Main item slot is empty
								e.setCurrentItem(new ItemStack(Material.AIR));
								inv.setItem(mainSlot, item);//Moves clicked item to main slot
								playClick(player);
								if(inv.getItem(subSlot) != null) {//Sub item slot is not empty
									BlackSmithResult resultItem = new BlackSmithResult(player, inv.getItem(subSlot), inv.getItem(subSlot));
									if(resultItem.getCost() > 0) {//Items are upgradable
										inv.setItem(resultSlot, Methods.addLore(resultItem.getResultItem(),
										config.getString("Settings.BlackSmith.Results.Found")
										.replaceAll("%Cost%", resultItem.getCost() + "")
										.replaceAll("%cost%", resultItem.getCost() + "")));
										for(int i : result)
											inv.setItem(i - 1, blueGlass);
									}else {//Items are not upgradable
										inv.setItem(resultSlot, denyBarrier);
										for(int i : result)
											inv.setItem(i - 1, redGlass);
									}
								}
							}else {//Main item slot is not empty
								e.setCurrentItem(new ItemStack(Material.AIR));
								if(inv.getItem(subSlot) != null) {//Sub item slot is not empty
									e.setCurrentItem(inv.getItem(subSlot));//Moves sub slot item to clicked items slot
								}
								inv.setItem(subSlot, item);//Moves clicked item to sub slot
								playClick(player);
								BlackSmithResult resultItem = new BlackSmithResult(player, inv.getItem(subSlot), inv.getItem(subSlot));
								if(resultItem.getCost() > 0) {//Items are upgradable
									inv.setItem(resultSlot, Methods.addLore(resultItem.getResultItem(),
									config.getString("Settings.BlackSmith.Results.Found")
									.replaceAll("%Cost%", resultItem.getCost() + "")
									.replaceAll("%cost%", resultItem.getCost() + "")));
									for(int i : result)
										inv.setItem(i - 1, blueGlass);
								}else {//Items are not upgradable
									inv.setItem(resultSlot, denyBarrier);
									for(int i : result)
										inv.setItem(i - 1, redGlass);
								}
							}
						}
					}else {// Click In the Black Smith
						if(e.getRawSlot() == mainSlot || e.getRawSlot() == subSlot) {//Clicked either the Main slot or Sub slot
							e.setCurrentItem(new ItemStack(Material.AIR));//Sets the clicked slot to air
							if(Methods.isInventoryFull(player)) {//Gives clicked item back to player
								player.getWorld().dropItem(player.getLocation(), item);
							}else {
								player.getInventory().addItem(item);
							}
							inv.setItem(resultSlot, denyBarrier);
							for(int i : result)
								inv.setItem(i - 1, redGlass);
							playClick(player);
						}
						if(e.getRawSlot() == resultSlot) {//Clicks the result item slot
							if(inv.getItem(mainSlot) != null && inv.getItem(subSlot) != null) {//Main and Sub items are not empty
								BlackSmithResult resultItem = new BlackSmithResult(player, inv.getItem(subSlot), inv.getItem(subSlot));
								if(resultItem.getCost() > 0) {//Items are upgradeable
									if(player.getGameMode() != GameMode.CREATIVE) {
										if(Currency.isCurrency(config.getString("Settings.BlackSmith.Transaction.Currency"))) {
											Currency currency = Currency.getCurrency(config.getString("Settings.BlackSmith.Transaction.Currency"));
											if(CurrencyAPI.canBuy(player, currency, resultItem.getCost())) {
												CurrencyAPI.takeCurrency(player, currency, resultItem.getCost());
											}else {
												String needed = (resultItem.getCost() - CurrencyAPI.getCurrency(player, currency)) + "";
												if(currency != null) {
													HashMap<String, String> placeholders = new HashMap<>();
													placeholders.put("%money_needed%", needed);
													placeholders.put("%xp%", needed);
													switch(currency) {
														case VAULT:
															player.sendMessage(Messages.NEED_MORE_MONEY.getMessage(placeholders));
															break;
														case XP_LEVEL:
															player.sendMessage(Messages.NEED_MORE_XP_LEVELS.getMessage(placeholders));
															break;
														case XP_TOTAL:
															player.sendMessage(Messages.NEED_MORE_TOTAL_XP.getMessage(placeholders));
															break;
													}
												}
												return;
											}
										}
									}
									if(Methods.isInventoryFull(player)) {
										player.getWorld().dropItem(player.getLocation(), resultItem.getResultItem());
									}else {
										player.getInventory().addItem(resultItem.getResultItem());
									}
									inv.setItem(mainSlot, new ItemStack(Material.AIR));
									inv.setItem(subSlot, new ItemStack(Material.AIR));
									player.playSound(player.getLocation(), ce.getSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"), 1, 1);
									inv.setItem(resultSlot, denyBarrier);
									for(int i : result)
										inv.setItem(i - 1, redGlass);
								}else {
									player.playSound(player.getLocation(), ce.getSound("ENTITY_VILLAGER_NO", "VILLAGER_NO"), 1, 1);
								}
							}else {
								player.playSound(player.getLocation(), ce.getSound("ENTITY_VILLAGER_NO", "VILLAGER_NO"), 1, 1);
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(inv != null) {
					if(e.getView().getTitle().equals(Methods.color(Files.CONFIG.getFile().getString("Settings.BlackSmith.GUIName")))) {
						List<Integer> slots = new ArrayList<>();
						slots.add(mainSlot);
						slots.add(subSlot);
						boolean dead = e.getPlayer().isDead();
						for(int slot : slots) {
							if(inv.getItem(slot) != null) {
								if(inv.getItem(slot).getType() != Material.AIR) {
									if(dead) {
										e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), inv.getItem(slot));
									}else {
										if(Methods.isInventoryFull(((Player) e.getPlayer()))) {
											e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), inv.getItem(slot));
										}else {
											e.getPlayer().getInventory().addItem(inv.getItem(slot));
										}
									}
								}
							}
						}
						inv.clear();
					}
				}
			}
		}.runTaskLater(ce.getPlugin(), 0);
	}
	
	private void playClick(Player player) {
		player.playSound(player.getLocation(), ce.getSound("UI_BUTTON_CLICK", "CLICK"), 1, 1);
	}
	
}
