package org.unplayer.bungeemotdadaptive;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class PingListener implements Listener {

    private Configuration config;
    private final BungeeMotdAdaptive plugin;

    public PingListener(BungeeMotdAdaptive plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) {
                plugin.getLogger().warning("No se pudo crear el directorio de datos del plugin");
            }
        }

        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                plugin.getLogger().severe("Error al copiar archivo de configuración: " + e.getMessage());
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al cargar configuración: " + e.getMessage());
        }
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        int protocol = event.getConnection().getVersion();
        
        if (config.getBoolean("logging.show_version_info", true)) {
            plugin.getLogger().info("Ping desde cliente con protocolo: " + protocol);
        }
        
        // Obtener el MOTD actual
        ServerPing ping = event.getResponse();
        if (ping == null) return;
        
        // Obtener MOTDs desde la configuración
        String motdLegacy = config.getString("motd.legacy", "&6&l¡Bienvenido al servidor! &e&l1.8 Edition");
        String motdModern = config.getString("motd.modern", "&#FF6600&l¡Bienvenido al servidor! &#00FF66&lRGB Edition");
        
        // Obtener valores de protocolo desde la configuración
        int legacyMax = config.getInt("protocols.legacy_max", 47);
        
        // Determinar qué MOTD usar según la versión del protocolo
        String motdToUse;
        String versionInfo;
        
        if (protocol <= legacyMax) { // 1.8.x y anteriores
            motdToUse = motdLegacy;
            versionInfo = "Cliente 1.8 detectado, usando MOTD sin RGB";
        } else { // 1.9+ (todas las versiones modernas)
            motdToUse = motdModern;
            versionInfo = "Cliente 1.9+ detectado, usando MOTD con RGB";
        }
        
        if (config.getBoolean("logging.debug", true)) {
            plugin.getLogger().info(versionInfo);
        }
        
        // Aplicar colores usando la clase CC
        String coloredMotd = CC.color(motdToUse);
        
        // Aplicar el MOTD
        ping.setDescription(coloredMotd);
    }
}
