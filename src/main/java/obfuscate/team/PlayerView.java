package obfuscate.team;

import obfuscate.game.player.StrikePlayer;

import java.util.HashSet;
import java.util.Iterator;

public class PlayerView<T extends StrikePlayer> implements PlayerGroup<T> {

    protected HashSet<T> players = new HashSet<>();

    public PlayerView(Iterable<? extends T> data) {
        data.forEach(players::add);
    }

    public PlayerView(){}

    public void add(T x) {
        players.add(x);
    }

    public PlayerView<T> addAll(Iterable<? extends T> x) {
        x.forEach(players::add);
        return this;
    }

    @Override
    public Iterator<T> iterator() {
        return players.iterator();
    }

    @Override
    public int size() {
        return players.size();
    }

    @Override
    public boolean isEmpty() {
        return players.isEmpty();
    }

    @Override
    public boolean has(T player) {
        return players.contains(player);
    }
}
