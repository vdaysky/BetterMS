package obfuscate.game.damage;

import obfuscate.game.player.StrikePlayer;
import obfuscate.util.java.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class DamageLog {

    private final HashMap<StrikePlayer, ArrayList<DamageEntry>> dealtDamageLog = new HashMap<>();

    public void logDealtDamage(@Nullable StrikePlayer damager, @NotNull StrikePlayer damagee, NamedDamageSource damageWith, DamageModifiers modifiers, double dealtDamage) {
        dealtDamageLog.computeIfAbsent(damager, k -> new ArrayList<>());
        dealtDamageLog.get(damager).add(new DamageEntry(damagee, damageWith, modifiers, dealtDamage));

    }

    public void reset() {
        dealtDamageLog.clear();
    }

    public static class DamageEntry {
        public StrikePlayer player;
        public NamedDamageSource damageWith;
        public double dealtDamage;
        public DamageModifiers modifiers;

        public DamageEntry(StrikePlayer player, NamedDamageSource damageWith, DamageModifiers modifiers, double dealtDamage) {
            this.player = player;
            this.damageWith = damageWith;
            this.dealtDamage = dealtDamage;
            this.modifiers = modifiers;
        }
    }

    public Pair<Integer, Double> getReceivedDamageFrom(StrikePlayer fromPlayer, StrikePlayer toPlayer) {
        var damageByPlayer = dealtDamageLog.get(fromPlayer);

        if (damageByPlayer == null) {
            return new Pair<>(0, 0d);
        }

       return damageByPlayer
        .stream()
        .filter(entry -> entry.player != null && entry.player.equals(toPlayer))
        .reduce(
        new Pair<>(0, 0d),
        (subtotal, element) -> {
            subtotal.setKey(subtotal.key() + 1);
            subtotal.setValue(subtotal.value() + element.dealtDamage);
            return subtotal;
        },
        (a, b) -> {
            a.setKey(a.key() + b.key());
            a.setValue(a.value() + b.value());
            return a;
        }
        );
    }

}
