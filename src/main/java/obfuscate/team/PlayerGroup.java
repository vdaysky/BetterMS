package obfuscate.team;

import obfuscate.game.player.StrikePlayer;

import java.util.*;

public interface PlayerGroup <T extends StrikePlayer> extends Iterable<T> {

    /** @return new collection of players */

    @Override
    public Iterator<T> iterator();

    public int size();
    public boolean isEmpty();

    public boolean has(T player);
}
