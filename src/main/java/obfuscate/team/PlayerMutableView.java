package obfuscate.team;

import obfuscate.game.player.StrikePlayer;

public class PlayerMutableView<T extends StrikePlayer> extends PlayerView<T> implements MutablePlayerGroup<T>  {

    @Override
    public void remove(T x) {
        players.remove(x);
    }

}
