package obfuscate.comand.argument;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerArg extends AbstractArg<StrikePlayer> {
    public PlayerArg(String name, String description) {
        super(name, description);
    }

    @Override
    public StrikePlayer parse(String argument) throws CommandArgParseException {
        MsdmPlugin.highlight("PlayerArg.parse: " + argument);
        var player = StrikePlayer.findByName(argument);
        MsdmPlugin.highlight("parsed: " + player);
        if (player == null) {
            throw new CommandArgParseException();
        }

        return player;
    }

    @Override
    public List<String> getOptions(ExecutionContext context) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    @Override
    public String getTypeName() {
        return "PlayerName";
    }
}
