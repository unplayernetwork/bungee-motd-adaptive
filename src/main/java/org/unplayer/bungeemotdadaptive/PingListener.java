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
        
        // Obtener el ping original para mantener toda la información
        ServerPing originalPing = event.getResponse();
        if (originalPing == null) {
            originalPing = new ServerPing();
        }
        
        // Crear una copia del ping original para no modificar el original
        ServerPing ping = new ServerPing();
        
        // Copiar toda la información del ping original
        ping.setVersion(originalPing.getVersion());
        ping.setPlayers(originalPing.getPlayers());
        ping.setDescription(originalPing.getDescription()); // Se sobrescribirá después
        ping.setFavicon(originalPing.getFavicon());
        
        // Obtener MOTDs desde la configuración
        String motdLegacy = config.getString("motd.legacy", "&6&l¡Bienvenido al servidor! &e&l1.7-1.16.4 Edition");
        String motdModern = config.getString("motd.modern", "&#FF6600&l¡Bienvenido al servidor! &#00FF66&lRGB Edition");
        
        // Obtener valores de protocolo desde la configuración
        int legacyMax = config.getInt("protocols.legacy_max", 736);
        
        // Determinar qué MOTD usar según la versión del protocolo del jugador específico
        String motdToUse;
        String versionInfo;
        
        if (protocol <= legacyMax) { // 1.7.x hasta 1.16.4
            motdToUse = motdLegacy;
            versionInfo = "Cliente 1.7.x-1.16.4 detectado, usando MOTD sin RGB";
        } else { // 1.16.5+ (todas las versiones modernas con RGB)
            motdToUse = motdModern;
            versionInfo = "Cliente 1.16.5+ detectado, usando MOTD con RGB";
        }
        
        if (config.getBoolean("logging.debug", true)) {
            plugin.getLogger().info(versionInfo + " - Protocolo: " + protocol);
        }
        
        // Aplicar colores usando la clase CC
        String coloredMotd = CC.color(motdToUse);
        
        // Establecer el MOTD individual para este jugador específico
        ping.setDescription(coloredMotd);
        
        // Aplicar el ping personalizado al evento
        event.setResponse(ping);
    }
}
