package com.ells1231.MorePotions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

public class BrewListener implements Listener {
	Main Plugin;

	public BrewListener(Main Plugin) {
		this.Plugin = Plugin;
	}

	
	@EventHandler
	public void onBrewEvent(BrewEvent Event){
		boolean EventCancelled = false;
		
		BrewerInventory Inv = Event.getContents();
		ItemStack Ingredient = Inv.getIngredient();
		for (ItemStack I : Inv.getContents()){
			if (I != null && I.getType() == Material.POTION){
				String InitialBin =  Integer.toBinaryString(I.getDurability());
				InitialBin = StringUtils.repeat("0", 16-InitialBin.length())+InitialBin;
				int DamageValue = Integer.valueOf(InitialBin.substring(11, 16),2);
				
				CustomPotion Pot = Plugin.Recipes.get(String.valueOf(DamageValue) + "+" + String.valueOf(Inv.getIngredient().getType()));
				boolean InitialPot = true;
				if (Pot == null){
					InitialPot = false;
					PotionMeta PM = (PotionMeta)I.getItemMeta();
					
					if (PM.getCustomEffects().size() != 0){
						PotionEffectType PotType = PM.getCustomEffects().get(0).getType();
						for (PotionEffectType Key : Plugin.PotsType.keySet()) {

						    if(PotType.equals(Key)){
						    	Pot = Plugin.PotsType.get(Key);
						    }
						}						
					}
					
				}
				if (Pot != null && !InitialPot){
					int NewDamage = Plugin.ConstructDurability(Ingredient.getType() == Material.GLOWSTONE_DUST,
																Ingredient.getType() == Material.GUNPOWDER,
																Ingredient.getType() == Material.REDSTONE,
																Pot.getLook(), I.getDurability());
					ItemStack NewPotion = Pot.MakePotion(NewDamage);
					Event.setCancelled(true);
					EventCancelled = true;
					Inv.setItem(Inv.first(I), NewPotion);
				}
				else if(Pot!= null && InitialPot){
					ItemStack NewPotion = Pot.MakePotion();
					Event.setCancelled(true);
					EventCancelled = true;
					Inv.setItem(Inv.first(I), NewPotion);
					Pot.IncreaseStat();
				}
			}
			
		}
		if (EventCancelled){
			Ingredient.setAmount(Ingredient.getAmount()-1);
			Inv.setItem(3, Ingredient);
		}
	}
	
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent Event){
		ItemStack Item = Event.getItem();
		if (Event.getItem() != null && Event.getItem().getType() == Material.POTION){
			PotionMeta PM = (PotionMeta)Item.getItemMeta();
			if(PM.getCustomEffects().size() == 1 && !PM.hasLore()){
				CustomPotion Pot = Plugin.PotsType.get(PM.getCustomEffects().get(0).getType());
				if (Pot != null){
					if(!(Event.getPlayer().hasPermission(Pot.getPermission()) || Event.getPlayer().hasPermission("Morepotions.Potion.*") || Event.getPlayer().isOp())){
						
						Event.setCancelled(true);
						Event.getPlayer().sendMessage(ChatColor.BLUE+":( Sorry, you do not have the permission "+ChatColor.GOLD+Pot.getPermission()+ChatColor.BLUE+", so can't use that potion.");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent E){
		if(E.getPlayer().isOp() && Plugin.UpdateAvailable){
			E.getPlayer().sendMessage(ChatColor.GOLD+"A newer version of More Potions is available (v"+Plugin.Update.getLatestName().split("v")[1]+")");
		}
	}

}


















