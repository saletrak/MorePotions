package com.ells1231.MorePotions;

import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomPotion {
	private int Tier1Duration;
	private int Tier2Duration;
	private int Potency1;
	private int Potency2;
	private PotionEffectType Type;
	private short Look;
	private String Permission;
	private Main Plugin;
	private String Name;
	private int MaxLevel;
	
	public CustomPotion(Main Plgn, int D1, int D2, int P1, int P2, PotionEffectType T, short L, String P, String N, int Max){
		Tier1Duration = D1;
		Tier2Duration = D2;
		MaxLevel = Max;
		if (MaxLevel != -1 && P1 > MaxLevel){
			Potency1 = MaxLevel;
		}
		else{
			Potency1 = P1;
		}
		if (MaxLevel != -1 && P2 > MaxLevel){
			Potency2 = MaxLevel;
		}
		else{
			Potency2 = P2;
		}
		Type = T;
		Look = L;
		Permission = P;
		Plugin = Plgn;
		Name = N;
		
	}
	
	public String GetName(){
		return this.Name;
	}
	
	public ItemStack MakePotion(){
		return MakePotion(Look);
	}
	
	public ItemStack MakePotion(int Time, int Potency, boolean Splash){
		
		if (MaxLevel != -1 && Potency>MaxLevel){
			Potency = MaxLevel;
		}
		
		Potion Potion = new Potion(Look);
		
		ItemStack I = Potion.toItemStack(1);
		PotionMeta PM = (PotionMeta)I.getItemMeta();
		PM.addCustomEffect(new PotionEffect(Type,Time*20,Potency), true);	
		
		I.setDurability((short) Plugin.ConstructDurability(false, Splash, false, Look, Look));
		
		I.setItemMeta(PM);
		return I;
	}
	public ItemStack MakePotion(int Base){
		String BinaryString = Integer.toBinaryString(Base);
		int ZeroToAdd = 16-BinaryString.length();
		BinaryString = StringUtils.repeat("0", ZeroToAdd)+BinaryString;
		
		String[] BinarySplit = BinaryString.split("");

		int Level = Integer.valueOf(BinarySplit[11]);
		int Extended = Integer.valueOf(BinarySplit[10]);

		Potion Potion = new Potion(Base);
		
		ItemStack I = Potion.toItemStack(1);
		PotionMeta PM = (PotionMeta)I.getItemMeta();
		
		int Duration;
		int Potency;
		
		if (Extended == 1){
			Duration = Tier2Duration;
		}
		else{
			Duration = Tier1Duration;
		}
		if (Level == 1){
			Potency = Potency2;
			
		}
		else{
			Potency = Potency1;
		}
		
		PM.addCustomEffect(new PotionEffect(Type,Duration,Potency-1), true);

		
		I.setItemMeta(PM);
		I.setDurability((short)Base);
		return I;
	}

	public short getLook() {
		return Look;
	}

	public String getPermission() {
		return Permission;
	}

	public void IncreaseStat() {
		Plugin.StatsSave.GetStatsConfig().set(Name, Plugin.StatsSave.GetStatsConfig().getInt(Name)+1);
		Plugin.StatsSave.saveStatsConfig();
	}
	
	public int GetStat(){
		return Plugin.StatsSave.GetStatsConfig().getInt(Name);
	}
	
}


