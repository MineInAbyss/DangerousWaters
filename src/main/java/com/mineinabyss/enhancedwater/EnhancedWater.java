package com.mineinabyss.enhancedwater;

import org.bukkit.plugin.java.JavaPlugin;


public final class EnhancedWater extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getLogger().info("On enable has been called");

        getServer().getPluginManager().registerEvents(new WaterListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("onDisable has been invoked!");
    }
}