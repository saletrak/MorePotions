package com.ells1231.MorePotions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class StatsSaveHandler {
	File StatsFile;
	FileConfiguration StatsConfig;
	Main Plugin;

	public StatsSaveHandler(Main P){
		Plugin = P;
		StatsConfig = getStatsConfig();
		saveStatsConfig();
	}

	public void reloadStatsConfig() {
		if (StatsFile == null) {
			StatsFile = new File(Plugin.getDataFolder(), "StatsSave");
		}
		StatsConfig = YamlConfiguration.loadConfiguration(StatsFile);

		// Look for defaults in the jar
		InputStream defConfigStream = Plugin.getResource("StatsSave");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			StatsConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getStatsConfig() {
		if (StatsConfig == null) {
			reloadStatsConfig();
		}
		return StatsConfig;
	}

	public void saveStatsConfig() {
		if (StatsConfig == null || StatsFile == null) {
			return;
		}
		try {
			getStatsConfig().save(StatsFile);
		} catch (IOException ex) {
		}
	}

	public FileConfiguration GetStatsConfig(){
		return StatsConfig;
	}

}
