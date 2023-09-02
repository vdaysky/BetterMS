package obfuscate.team;

import obfuscate.game.player.StrikePlayer;

public interface MutablePlayerGroup<T extends StrikePlayer> extends PlayerGroup<T> {

    void add(T x);
    void remove(T x);

}
