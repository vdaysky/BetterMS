package obfuscate.util.hotbar;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class HotbarMessager {

    public static void sendActionbar(Player p, String message) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
//        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
//
//        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
//
//        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
    }

}
