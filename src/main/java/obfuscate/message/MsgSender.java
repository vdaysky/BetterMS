package obfuscate.message;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import obfuscate.MsdmPlugin;
import obfuscate.util.chat.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum MsgSender
{
    SERVER(C.cBlue + "Server> " + C.Reset, "§8>>§r "),
    GAME("§9Game>§r ", "§8>>§r "),
    DEATHMATCH("§9Deathmatch>§r ", "§8>>§r "),
    PLUGIN("§8§l[§6Plugin§8§l]", "§8:§r "),
    NPC_MANAGER("§8§l[§NPC§8§l]", "§8:§r "),
    NONE("", ""),
    CMD("§8§l[§6CMD§8§l]", "§8:§r"),
    LOBBY("[Lobby]", " >> "),
    RANK("[RankMe]", " >> "),

    MPS(C.cBlue + "MyPlayerServer> " + C.Reset, "§8>>§r "),

    JOIN(C.cDGray + "Join> " + C.cGray, " "),
    LEAVE(C.cDGray + "Quit> " + C.cGray, " "),

    ;

    String _prefix;
    String _suffix;

    MsgSender(String prefix, String suffix)
    {
        _prefix = prefix;
        _suffix = suffix;
    }

    public String getPrefix() {
        return _prefix;
    }

    public String getSuffix() {
        return _suffix;
    }

    public String form(String message) {
        return getPrefix() + getSuffix() + message;
    }

    public TextComponent form(TextComponent message) {
        TextComponent root = new TextComponent();

        BaseComponent component = root;

        for (BaseComponent c : TextComponent.fromLegacyText(getPrefix() + getSuffix())) {
            component.addExtra(c);
            component = c;
        }
        component.addExtra(message);
        return root;
    }
}
