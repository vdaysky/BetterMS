package obfuscate.comand.argument;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.game.core.Game;
import obfuscate.gamemode.Competitive;

import java.util.List;
import java.util.stream.Collectors;

public class GameArg extends AbstractArg<Competitive> {
    public GameArg(String name, String description) {
        super(name, description);
    }

    @Override
    public List<String> getOptions(ExecutionContext context) {
        return MsdmPlugin.getGameServer().getGames().stream().map(Game::getId).map(x -> x.getObjId().toString()).collect(Collectors.toList());
    }

    @Override
    public String getTypeName() {
        return "GameId";
    }

    @Override
    public Competitive parse(String gameIdStr) throws CommandArgParseException {
        int gameId;

        try {
            gameId = Integer.parseInt(gameIdStr);
        } catch (NumberFormatException e) {
            throw new CommandArgParseException();
        }

        return MsdmPlugin.getGameServer().getGame(gameId);
    }
}
