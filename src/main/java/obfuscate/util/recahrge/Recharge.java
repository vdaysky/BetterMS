/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package obfuscate.util.recahrge;

import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.util.time.Task;

import java.util.HashMap;


public class Recharge {
    
    double started;
    long ending;
    StrikeItem item;
    long delay;

    private static HashMap<StrikePlayer, HashMap<String, Long>> playerCooldowns = new HashMap<>();

    public Recharge(long delay, StrikeItem item)
    {
        this.delay = delay;
        this.item = item;
        this.started = System.currentTimeMillis();
        this.ending = System.currentTimeMillis() + delay;
    }

    public void reset()
    {
        this.started = System.currentTimeMillis();
        this.ending = System.currentTimeMillis() + delay;
    }

    public static boolean done(StrikePlayer player, String ability, long cooldown, Runnable callback)
    {
        playerCooldowns.computeIfAbsent(player, k -> new HashMap<>());

        boolean abilityNotSet = playerCooldowns.get(player).get(ability) == null;
        boolean abilityExpired = !abilityNotSet && playerCooldowns.get(player).get(ability) + cooldown <= System.currentTimeMillis();

        // if ability recharge was not set, it means it is available now
        // if ability expired, it will be available after a cool down again
        if (abilityNotSet || abilityExpired)
        {
            playerCooldowns.get(player).put(ability, System.currentTimeMillis());

            // schedule a callback to fire when ability will be available again
            if (callback != null) {
                new Task(
                        callback,
                        (int) (cooldown / 50f)
                ).run();
            }

            return true;
        }
        return false;
    }
    public static boolean done(StrikePlayer player, String ability, long cooldown) {
        return done(player, ability, cooldown, null);
    }
       
    public boolean done()
    {
        return System.currentTimeMillis() > ending;
    }

    public long timeLeft()
    {
        return ending - System.currentTimeMillis();
    }
    
}
