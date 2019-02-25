package com.ells1231.MorePotions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.ells1231.Stats.Metrics;
import com.ells1231.Stats.Metrics.Graph;
import com.ells1231.Stats.Metrics.Plotter;
import com.ells1231.Updater.Updater;
import com.ells1231.Updater.Updater.UpdateResult;


public class Main extends JavaPlugin{
	
	public StatsSaveHandler StatsSave;

	//Base Custom Potions
	CustomPotion ABSORPTION;
	CustomPotion BLINDNESS;
	CustomPotion CONFUSION ;
	CustomPotion DAMAGE_RESISTANCE;
	CustomPotion FAST_DIGGING;
	CustomPotion HEALTH_BOOST;
	CustomPotion HUNGER;
	CustomPotion JUMP;
	CustomPotion SATURATION;
	CustomPotion SLOW_DIGGING;
	CustomPotion WATER_BREATHING;
	CustomPotion WITHER;

	Map<String, CustomPotion> Recipes;
	Map<String, CustomPotion> PotsName;
	Map<PotionEffectType, CustomPotion> PotsType;
	Map<Integer, String> RecipeStrings;
	Map<Integer, String> RecipeItems;
	
	public Updater Update;
	private Logger Log;
	public Boolean UpdateAvailable;
	public Boolean AutoUpdated;
	
	public FileConfiguration File;
	

	@Override
	public void onEnable(){
		
		this.saveDefaultConfig();
		File = this.getConfig();
		this.Log = this.getLogger();
		this.UpdateAvailable = false;
		SetupHashMaps();
		
		
		StatsSave = new StatsSaveHandler(this);
		
		SetupMetrics();
		
		handleWaterBreathing();
		
		
		if (File.getBoolean("CheckUpdates")){
			if (File.getBoolean("AutoDownloadUpdates")){
				Update = new Updater(this, 65242, this.getFile(), Updater.UpdateType.DEFAULT, false);
				if (Update.getResult() == UpdateResult.SUCCESS){
					Log.info("More Potions has been updated!");
				}
			}
			else{
				Update = new Updater(this, 65242, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			}
			
			
			CheckUpdate();
		}
		
		this.getServer().getPluginManager().registerEvents(new BrewListener(this), this);

	}

	//GivePotion Ells1231 Blindness 120 15 true

	private void handleWaterBreathing() {
		//getVersion
		String Version = Bukkit.getVersion().split("MC: ")[1].split("\\)")[0];
		if (!File.getBoolean("AutoDisableWaterBreathing") && Version.contains("1.7")){
			File.set("AutoDisableWaterBreathing", true);
			File.set("WATER_BREATHING.Enabled", false);
			this.saveConfig();
			
			reloadConfig();
			File = this.getConfig();
			SetupHashMaps();
		}
		
	}

	public boolean onCommand(CommandSender Sender, Command Command, String Label, String[] args){
		
		if(Command.getName().equalsIgnoreCase("MP")){
			if(args.length > 0 && args[0].equalsIgnoreCase("give")){
				if (!Sender.hasPermission("MorePotions.Give") && !Sender.isOp()){
					Sender.sendMessage(ChatColor.BLUE+"Woops! You need the permission "+ChatColor.GOLD+"MorePotions.Give"+ChatColor.BLUE+" to use that command :(");
					return true;
				}
				if (!(args.length >= 6 && args.length <= 7)){
					Sender.sendMessage(ChatColor.GOLD+"Invalid command layout, use:");
					Sender.sendMessage(ChatColor.BLUE+"/MP Give <Player> <Type> <Duration> <Potency> <Splash true|false> [Amount]");
					return true;
				}
				Player Player = Bukkit.getPlayer(args[1]);
				if (Player == null){
					Sender.sendMessage(ChatColor.GOLD+"Could not find player: "+ChatColor.BLUE+args[1]);
					return true;
				}
				CustomPotion Pot = PotsName.get(StringUtils.lowerCase(args[2]));
				if (Pot == null){
					Sender.sendMessage(ChatColor.GOLD+"Could not find potion: "+ChatColor.BLUE+args[2]);
					Sender.sendMessage(ChatColor.GOLD+"Type "+ChatColor.BLUE+"/MP list"+ChatColor.GOLD+" for a list of the potions available");
					return true;
				}

				Integer T = null;
				Integer P = null;

				try{
					T = Integer.parseInt(args[3]);
				}
				catch(NumberFormatException e){
				}

				try{
					P = Integer.parseInt(args[4]);
				}
				catch(NumberFormatException e){
				}

				if (T == null){
					Sender.sendMessage(ChatColor.GOLD+"Invalid time (Should be a number): "+ChatColor.BLUE+args[3]);
					return true;
				}

				if (P == null){
					Sender.sendMessage(ChatColor.GOLD+"Invalid potency (Should be a number): "+ChatColor.BLUE+args[4]);
					return true;
				}
				
				int Amount = 1;
				
				if (args.length == 7){
					if(!isInteger(args[6])){
						Sender.sendMessage(ChatColor.GOLD+"Invalid amount (Should be a number): "+ChatColor.BLUE+args[6]);
						return true;
					}
					else{
						Amount = Integer.valueOf(args[6]);
					}
				}

				Boolean Splash = Boolean.parseBoolean(args[5]);
				
				ItemStack Potions = Pot.MakePotion(T, P, Splash);
				Potions.setAmount(Amount);

				Player.getInventory().addItem(Potions);

				return true;
				
			}//IF GIVE
			
			else if(args.length > 0 && args[0].equalsIgnoreCase("list")){
				if (!Sender.hasPermission("MorePotions.List") && !Sender.isOp()){
					Sender.sendMessage(ChatColor.BLUE+"Woops! You need the permission "+ChatColor.GOLD+"MorePotions.List"+ChatColor.BLUE+" to use that command :(");
					return true;
				}
				String PotionList = ChatColor.GOLD+ "Potions: "+ChatColor.BLUE;
				for (String Key : this.PotsName.keySet()) {
					PotionList = PotionList+ Key + ", ";

				}
				Sender.sendMessage(PotionList);
				return true;
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("recipes")){
				if (!Sender.hasPermission("MorePotions.Recipes") && !Sender.isOp()){
					Sender.sendMessage(ChatColor.BLUE+"Woops! You need the permission "+ChatColor.GOLD+"MorePotions.Recipes"+ChatColor.BLUE+" to use that command :(");
					return true;
				}
				if(File.getBoolean("FAST_DIGGING.Enabled") && Recipes.containsValue(FAST_DIGGING))Sender.sendMessage(ChatColor.BLUE+"Potion of Haste: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("FAST_DIGGING.Recipe") == 0) ? 1 : File.getInt("FAST_DIGGING.Recipe")));
				if(File.getBoolean("SLOW_DIGGING.Enabled") && Recipes.containsValue(SLOW_DIGGING))Sender.sendMessage(ChatColor.BLUE+"Potion of Dullness: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("SLOW_DIGGING.Recipe") == 0) ? 2 : File.getInt("SLOW_DIGGING.Recipe")));
				if(File.getBoolean("JUMP.Enabled") && Recipes.containsValue(JUMP))Sender.sendMessage(ChatColor.BLUE+"Potion of Leaping: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("JUMP.Recipe") == 0) ? 3 : File.getInt("JUMP.Recipe")));
				if(File.getBoolean("CONFUSION.Enabled") && Recipes.containsValue(FAST_DIGGING))Sender.sendMessage(ChatColor.BLUE+"Potion of Nausea: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("CONFUSION.Recipe") == 0) ? 4 : File.getInt("CONFUSION.Recipe")));
				if(File.getBoolean("DAMAGE_RESISTANCE.Enabled") && Recipes.containsValue(CONFUSION))Sender.sendMessage(ChatColor.BLUE+"Potion of Resistance: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("DAMAGE_RESISTANCE.Recipe") == 0) ? 5 : File.getInt("DAMAGE_RESISTANCE.Recipe")));
				if(File.getBoolean("WATER_BREATHING.Enabled") && Recipes.containsValue(WATER_BREATHING))Sender.sendMessage(ChatColor.BLUE+"Potion of Water Breathing: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("WATER_BREATHING.Recipe") == 0) ? 6 : File.getInt("WATER_BREATHING.Recipe")));
				if(File.getBoolean("BLINDNESS.Enabled") && Recipes.containsValue(BLINDNESS))Sender.sendMessage(ChatColor.BLUE+"Potion of Blindness: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("BLINDNESS.Recipe") == 0) ? 7 : File.getInt("BLINDNESS.Recipe")));
				if(File.getBoolean("HUNGER.Enabled") && Recipes.containsValue(HUNGER))Sender.sendMessage(ChatColor.BLUE+"Potion of Hunger: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("HUNGER.Recipe") == 0) ? 8 : File.getInt("HUNGER.Recipe")));
				if(File.getBoolean("WITHER.Enabled") && Recipes.containsValue(WITHER))Sender.sendMessage(ChatColor.BLUE+"Potion of Decay: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("WITHER.Recipe") == 0) ? 9 : File.getInt("WITHER.Recipe")));
				if(File.getBoolean("HEALTH_BOOST.Enabled") && Recipes.containsValue(HEALTH_BOOST))Sender.sendMessage(ChatColor.BLUE+"Potion of Health Boost: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("HEALTH_BOOST.Recipe") == 0) ? 10 : File.getInt("HEALTH_BOOST.Recipe")));
				if(File.getBoolean("ABSORPTION.Enabled") && Recipes.containsValue(ABSORPTION))Sender.sendMessage(ChatColor.BLUE+"Potion of Absorption: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("ABSORPTION.Recipe") == 0) ? 11 : File.getInt("ABSORPTION.Recipe")));
				if(File.getBoolean("SATURATION.Enabled") && Recipes.containsValue(SATURATION))Sender.sendMessage(ChatColor.BLUE+"Potion of Saturation: "+ChatColor.GOLD+RecipeStrings.get((File.getInt("SATURATION.Recipe") == 0) ? 12 : File.getInt("SATURATION.Recipe")));
				return true;
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("reload")){
					if (!Sender.hasPermission("MorePotions.Reload") && !Sender.isOp()){
						Sender.sendMessage(ChatColor.BLUE+"Woops! You need the permission "+ChatColor.GOLD+"MorePotions.Reload"+ChatColor.BLUE+" to use that command :(");
						return true;
					}
					reloadConfig();
					File = this.getConfig();
					SetupHashMaps();
					Sender.sendMessage(ChatColor.GOLD+"More Potions has been reloaded.");
				return true;
			}
			
		}//IF MP
		Sender.sendMessage(ChatColor.BLUE+"That command does not exist. Use "+ChatColor.GOLD+"/MP <give|list|recipes|reload>");
		return true;
	}

	public int ConstructDurability(boolean GS, boolean GP, boolean RS, short Look, short Base){
		String BinBase = Integer.toBinaryString(Base);
		String BinLook = Integer.toBinaryString(Look);

		BinBase = StringUtils.repeat("0", 16-BinBase.length())+BinBase;
		BinLook = StringUtils.repeat("0", 16-BinLook.length())+BinLook;

		String[] BaseList = BinBase.split("");
		String[] LookList = BinLook.split("");
		//Start with Base

		BaseList[15] = LookList[15];
		BaseList[14] = LookList[14];
		BaseList[13] = LookList[13];
		BaseList[12] = LookList[12];

		if (GP){
			BaseList[2] = "1";
		}
		if (RS){ //Redstone - Duration
			BaseList[11] = "0";
			BaseList[10] = "1";
		}
		if (GS){ //Glowstone - Level
			BaseList[11] = "1";
			BaseList[10] = "0";
		}

		String OutputBinString = "";
		for (String Num:BaseList){
			OutputBinString += Num;
		}

		return Integer.parseInt(OutputBinString,2);

	}
	
	public static boolean isInteger(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}
	
	public void SetupHashMaps(){
		ABSORPTION = new CustomPotion(this,File.getInt("ABSORPTION.Duration.Normal")*20,              File.getInt("ABSORPTION.Duration.Extended")*20,       File.getInt("ABSORPTION.Potency.Level1"),       File.getInt("ABSORPTION.Potency.Level2"),PotionEffectType.ABSORPTION, (short)8194,"MorePotions.Potion.Absorption","Absorption",-1) ;
		BLINDNESS = new CustomPotion(this,File.getInt("BLINDNESS.Duration.Normal")*20,                File.getInt("BLINDNESS.Duration.Extended")*20,        File.getInt("BLINDNESS.Potency.Level1"),        File.getInt("BLINDNESS.Potency.Level2"),PotionEffectType.BLINDNESS, (short)8202,"MorePotions.Potion.Blindness","Blindness",-1) ;
		CONFUSION  = new CustomPotion(this,File.getInt("CONFUSION.Duration.Normal")*20,               File.getInt("CONFUSION.Duration.Extended")*20,        File.getInt("CONFUSION.Potency.Level1"),        File.getInt("CONFUSION.Potency.Level2"),PotionEffectType.CONFUSION,(short)8206,"MorePotions.Potion.Confusion","Nausea",-1) ;
		DAMAGE_RESISTANCE = new CustomPotion(this,File.getInt("DAMAGE_RESISTANCE.Duration.Normal")*20,File.getInt("DAMAGE_RESISTANCE.Duration.Extended")*20,File.getInt("DAMAGE_RESISTANCE.Potency.Level1"),File.getInt("DAMAGE_RESISTANCE.Potency.Level2"),PotionEffectType.DAMAGE_RESISTANCE,(short)8201,"MorePotions.Potion.DamageResistance","Damage Resistance",-1) ;
		FAST_DIGGING  = new CustomPotion(this,File.getInt("FAST_DIGGING.Duration.Normal")*20,         File.getInt("FAST_DIGGING.Duration.Extended")*20,     File.getInt("FAST_DIGGING.Potency.Level1"),     File.getInt("FAST_DIGGING.Potency.Level2"),PotionEffectType.FAST_DIGGING,(short)8194,"MorePotions.Potion.FastDigging","Haste",-1) ;
		HEALTH_BOOST = new CustomPotion(this,File.getInt("HEALTH_BOOST.Duration.Normal")*20,          File.getInt("HEALTH_BOOST.Duration.Extended")*20,     File.getInt("HEALTH_BOOST.Potency.Level1"),     File.getInt("HEALTH_BOOST.Potency.Level2"),PotionEffectType.HEALTH_BOOST,(short)8193,"MorePotions.Potion.HealthBoost","Health Boost", 127) ;
		HUNGER = new CustomPotion(this,File.getInt("HUNGER.Duration.Normal")*20,                      File.getInt("HUNGER.Duration.Extended")*20,           File.getInt("HUNGER.Potency.Level1"),           File.getInt("HUNGER.Potency.Level2"),PotionEffectType.HUNGER,(short)8196,"MorePotions.Potion.Hunger","Hunger",-1) ;
		JUMP  = new CustomPotion(this,File.getInt("JUMP.Duration.Normal")*20,                         File.getInt("JUMP.Duration.Extended")*20,             File.getInt("JUMP.Potency.Level1"),             File.getInt("JUMP.Potency.Level2"),PotionEffectType.JUMP,(short)8201,"MorePotions.Potion.Jump","Jump",-1) ;
		SATURATION = new CustomPotion(this,File.getInt("SATURATION.Duration.Normal")*20,              File.getInt("SATURATION.Duration.Extended")*20,       File.getInt("SATURATION.Potency.Level1"),       File.getInt("SATURATION.Potency.Level2"),PotionEffectType.SATURATION,(short)8193,"MorePotions.Potion.Saturation","Saturation",-1) ;
		SLOW_DIGGING = new CustomPotion(this,File.getInt("SLOW_DIGGING.Duration.Normal")*20,          File.getInt("SLOW_DIGGING.Duration.Extended")*20,     File.getInt("SLOW_DIGGING.Potency.Level1"),     File.getInt("SLOW_DIGGING.Potency.Level2"),PotionEffectType.SLOW_DIGGING,(short)8194,"MorePotions.Potion.SlowDigging","Dullness",-1) ;
		WATER_BREATHING  = new CustomPotion(this,File.getInt("WATER_BREATHING.Duration.Normal")*20,   File.getInt("WATER_BREATHING.Duration.Extended")*20,  File.getInt("WATER_BREATHING.Potency.Level1"),  File.getInt("WATER_BREATHING.Potency.Level2"),PotionEffectType.WATER_BREATHING,(short)8200,"MorePotions.Potion.WaterBreathing","Water Breathing",-1) ;
		WITHER = new CustomPotion(this,File.getInt("WITHER.Duration.Normal")*20,                      File.getInt("WITHER.Duration.Extended")*20,           File.getInt("WITHER.Potency.Level1"),           File.getInt("WITHER.Potency.Level2"),PotionEffectType.WITHER,(short)8196 ,"MorePotions.Potion.Wither","Wither",-1) ;

		Recipes = new HashMap<String,CustomPotion>();
		//Water = 0
		//Awkward = 16
		//Mundane = 8192
		//Thick = 32
		
		RecipeStrings = new HashMap<Integer, String>();
		
		RecipeStrings.put(1 ,"Water Bottle + Glowstone Dust");
		RecipeStrings.put(2 ,"Water Bottle + Redstone Dust");
		RecipeStrings.put(3 ,"Water Bottle + Sugar");
		RecipeStrings.put(4 ,"Potion of Poison+ Fermented Spider Eye");
		RecipeStrings.put(5 ,"Water Bottle + Spider Eye");
		RecipeStrings.put(6 ,"Water Bottle + Fermented Spider Eye");
		RecipeStrings.put(7 ,"Swiftness Potion + Fermented Spider Eye");
		RecipeStrings.put(8 ,"Awkward Potion + Fermented Spider Eye");
		RecipeStrings.put(9 ,"Water Bottle + Blaze Powder");
		RecipeStrings.put(10 ,"Water Bottle + Glistering Melon");
		RecipeStrings.put(11 ,"Water Bottle + Magma Cream");
		RecipeStrings.put(12 ,"Water Bottle + Ghast Tear");
		
		RecipeItems = new HashMap<Integer, String>();
		
		RecipeItems.put(1 ,"0+GLOWSTONE_DUST");
		RecipeItems.put(2 ,"0+REDSTONE");
		RecipeItems.put(3 ,"0+SUGAR");
		RecipeItems.put(4 ,"4+FERMENTED_SPIDER_EYE");
		RecipeItems.put(5 ,"0+SPIDER_EYE");
		RecipeItems.put(6 ,"0+FERMENTED_SPIDER_EYE");
		RecipeItems.put(7 ,"2+FERMENTED_SPIDER_EYE");
		RecipeItems.put(8 ,"16+FERMENTED_SPIDER_EYE");
		RecipeItems.put(9 ,"0+BLAZE_POWDER");
		RecipeItems.put(10 ,"0+SPECKLED_MELON");
		RecipeItems.put(11 ,"0+MAGMA_CREAM");
		RecipeItems.put(12 ,"0+GHAST_TEAR");
		
		
		if (File.getBoolean("SLOW_DIGGING.Enabled")){
			int rec = File.getInt("SLOW_DIGGING.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(2),SLOW_DIGGING);
			}
			else{
				Recipes.put(RecipeItems.get(rec),SLOW_DIGGING);
			}
			
		}
		if (File.getBoolean("SATURATION.Enabled")){
			
			int rec = File.getInt("SATURATION.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(12),SATURATION);
			}
			else{
				Recipes.put(RecipeItems.get(rec),SATURATION);
			}
		}
		if (File.getBoolean("HEALTH_BOOST.Enabled")){
			int rec = File.getInt("HEALTH_BOOST.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(10),HEALTH_BOOST);
			}
			else{
				Recipes.put(RecipeItems.get(rec),HEALTH_BOOST);
			}
		}
		if (File.getBoolean("WATER_BREATHING.Enabled")){
			int rec = File.getInt("WATER_BREATHING.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(6),WATER_BREATHING);
			}
			else{
				Recipes.put(RecipeItems.get(rec),WATER_BREATHING);
			}
		}
		if (File.getBoolean("ABSORPTION.Enabled")){
			int rec = File.getInt("ABSORPTION.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(11),ABSORPTION);
			}
			else{
				Recipes.put(RecipeItems.get(rec),ABSORPTION);
			}
		}
		if (File.getBoolean("JUMP.Enabled")){
			int rec = File.getInt("JUMP.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(3),JUMP);
			}
			else{
				Recipes.put(RecipeItems.get(rec),JUMP);
			}
		}
		if (File.getBoolean("DAMAGE_RESISTANCE.Enabled")){
			int rec = File.getInt("DAMAGE_RESISTANCE.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(5),DAMAGE_RESISTANCE);
			}
			else{
				Recipes.put(RecipeItems.get(rec),DAMAGE_RESISTANCE);
			}
		}
		if (File.getBoolean("FAST_DIGGING.Enabled")){
			int rec = File.getInt("FAST_DIGGING.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(1),FAST_DIGGING);
			}
			else{
				Recipes.put(RecipeItems.get(rec),FAST_DIGGING);
			}
		}
		if (File.getBoolean("WITHER.Enabled")){
			int rec = File.getInt("WITHER.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(9),WITHER);
			}
			else{
				Recipes.put(RecipeItems.get(rec),WITHER);
			}
		}
		if (File.getBoolean("HUNGER.Enabled")){
			int rec = File.getInt("HUNGER.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(8),HUNGER);
			}
			else{
				Recipes.put(RecipeItems.get(rec),HUNGER);
			}
		}
		if (File.getBoolean("BLINDNESS.Enabled")){
			int rec = File.getInt("BLINDNESS.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(7),BLINDNESS);
			}
			else{
				Recipes.put(RecipeItems.get(rec),BLINDNESS);
			}
		}
		if (File.getBoolean("CONFUSION.Enabled")){
			int rec = File.getInt("CONFUSION.Recipe");
			if (rec == 0 || !RecipeItems.containsKey(rec)){
				Recipes.put(RecipeItems.get(4),CONFUSION);
			}
			else{
				Recipes.put(RecipeItems.get(rec),CONFUSION);
			}
		}

		PotsName = new HashMap<String,CustomPotion>();

		PotsName.put("blindness",BLINDNESS);
		PotsName.put("saturation",SATURATION);
		PotsName.put("healthboost",HEALTH_BOOST);
		PotsName.put("waterbreathing",WATER_BREATHING);
		PotsName.put("absorption",ABSORPTION);
		PotsName.put("jump",JUMP);
		PotsName.put("nausea",CONFUSION);
		PotsName.put("haste",FAST_DIGGING);
		PotsName.put("decay",WITHER);
		PotsName.put("hunger",HUNGER);
		PotsName.put("dullness",SLOW_DIGGING);
		PotsName.put("damageresistance",DAMAGE_RESISTANCE);

		PotsType = new HashMap<PotionEffectType,CustomPotion>();

		PotsType.put(PotionEffectType.BLINDNESS,BLINDNESS);
		PotsType.put(PotionEffectType.SATURATION,SATURATION);
		PotsType.put(PotionEffectType.HEALTH_BOOST,HEALTH_BOOST);
		PotsType.put(PotionEffectType.WATER_BREATHING,WATER_BREATHING);
		PotsType.put(PotionEffectType.ABSORPTION,ABSORPTION);
		PotsType.put(PotionEffectType.JUMP,JUMP);
		PotsType.put(PotionEffectType.CONFUSION,CONFUSION);
		PotsType.put(PotionEffectType.FAST_DIGGING,FAST_DIGGING);
		PotsType.put(PotionEffectType.WITHER,WITHER);
		PotsType.put(PotionEffectType.HUNGER,HUNGER);
		PotsType.put(PotionEffectType.SLOW_DIGGING,SLOW_DIGGING);
		PotsType.put(PotionEffectType.DAMAGE_RESISTANCE,DAMAGE_RESISTANCE);
	}

	public void CheckUpdate(){		
		if (Update.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE && !File.getBoolean("AutoDownloadUpdates")){
			Log.info("A newer version of More Potions is available (v"+Update.getLatestName().split("v")[1]+") from:");
			Log.info(Update.getLatestFileLink());
			this.UpdateAvailable = true;
		}
	}
	
	public void SetupMetrics(){
		try {
		    Metrics metrics = new Metrics(this);
		    Graph PotionsBrewed = metrics.createGraph("Potions Brewed");
		    
		    for (final CustomPotion Pot : this.PotsName.values()) {
				PotionsBrewed.addPlotter(new Plotter(Pot.GetName()){
					@Override
					public int getValue(){
						return Pot.GetStat();
					}
				});

			}
		    
		    metrics.start();
		} catch (IOException e) {
		}
	}
}
