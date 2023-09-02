package obfuscate.config;

import obfuscate.MsdmPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ServerConfig {
    private String file = "server.yml";

    public ServerConfig() {

    }

    public YamlConfiguration loadConfig() {
        File f = new File(MsdmPlugin.getInstance().getDataFolder(), file);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        return config;
    }

    public String getBackendUrl() {
        return (String) loadConfig().get("backend.http.url");
    }

    public String getSocketUrl() {
        return (String) loadConfig().get("backend.ws.url");
    }

    public String getTelegramToken() {
        return loadConfig().getString("backend.telegram.token");
    }

    public String getChatId() {
        return loadConfig().getString("backend.telegram.chatId");
    }
}
