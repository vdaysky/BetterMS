package obfuscate.comand.argument;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.world.GameMap;
import obfuscate.world.MapManager;

import java.util.List;
import java.util.stream.Collectors;

public class GameMapArg extends AbstractArg<GameMap> {
    public GameMapArg(String name, String description) {
        super(name, description);
    }
    @Override
    public GameMap parse(String argument) throws CommandArgParseException {
        GameMap map = MapManager.getGameMap(argument);

        if (map == null)
            throw new CommandArgParseException();

        return map;
    }

    @Override
    public List<String> getOptions(ExecutionContext context) {
        return MapManager.getAvailableMaps().all().stream().map(GameMap::getName).collect(Collectors.toList());
    }

    @Override
    public String getTypeName() {
        return "GameMap";
    }
}
