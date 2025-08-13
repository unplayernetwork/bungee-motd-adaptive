package org.unplayer.bungeemotdadaptive;

import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeMotdAdaptive extends Plugin {

    @Override
    public void onEnable() {
        // Registrar el listener para el MOTD
        getProxy().getPluginManager().registerListener(this, new PingListener(this));
        getLogger().info("Plugin Bungee MOTD Adaptive habilitado - Detectando versiones de cliente");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Bungee MOTD Adaptive deshabilitado");
    }
}
