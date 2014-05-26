package com.ells1231.MorePotions;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

public class PotionFactory {
	
	String visibleName;
	ArrayList<String> lore = new ArrayList<String>();
	ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
	Boolean splash = false;
	short look = 8193;
	int amount = 1;
	
	public ItemStack createPotion(){
		
		Potion potion = new Potion(look);
		ItemStack potItem = potion.toItemStack(1);
		PotionMeta meta = (PotionMeta)potItem.getItemMeta();
		
		short durability = constructDurability(false, splash, false, look);
		potItem.setDurability(durability);
		
		for(PotionEffect effect : effects){
			meta.addCustomEffect(effect, false);
		}
		
		if(visibleName != null)meta.setDisplayName(visibleName);
		
		meta.setLore(lore);
		
		potItem.setItemMeta(meta);
		
		potItem.setAmount(amount);
		
		//Test Again
		
		return potItem;
		
	}
	
	
	
	public Boolean getSplash() {
		return splash;
	}



	public PotionFactory setSplash(Boolean splash) {
		this.splash = splash;
		return this;
	}



	public short getLook() {
		return look;
	}



	public PotionFactory setLook(short look) {
		this.look = look;
		return this;
	}



	public int getAmount() {
		return amount;
	}



	public PotionFactory setAmount(int amount) {
		this.amount = amount;
		return this;
	}
	
	public String getVisibleName() {
		return visibleName;
	}

	public PotionFactory setVisibleName(String visibleName) {
		this.visibleName = visibleName;
		return this;
	}

	public ArrayList<PotionEffect> getEffects() {
		return effects;
	}

	public PotionFactory setEffects(ArrayList<PotionEffect> effects) {
		this.effects = effects;
		return this;
	}

	public ArrayList<String> getLore() {
		return lore;
	}
	
	public PotionFactory setLore(ArrayList<String> lore){
		this.lore = lore;
		return this;
	}
	
	public static short constructDurability(boolean GS, boolean GP, boolean RS, short base){
		//base - what the potion is coming from
		//GS - Glowstone
		//GP - Gunpowder
		//RS - Redstone
		
		//convert to binary
		String binBase = Integer.toBinaryString(base);
		
		//Fill the start with 0's
		binBase = StringUtils.repeat("0", 16-binBase.length())+binBase;
		
		//split binary into a list
		String[] baseList = binBase.split("");

		if (GP){
			baseList[2] = "1";
		}
		if (RS){ //Redstone - Duration
			baseList[11] = "0";
			baseList[10] = "1";
		}
		if (GS){ //Glowstone - Level
			baseList[11] = "1";
			baseList[10] = "0";
		}
		
		//String array convert back to string
		String OutputBinString = "";
		for (String Num:baseList){
			OutputBinString += Num;
		}
		
		//return int, converting string as base 2 to base 10
		return (short)Integer.parseInt(OutputBinString,2);

	}

}
