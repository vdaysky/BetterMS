package obfuscate.util;

import obfuscate.game.core.Game;
import obfuscate.util.time.Task;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.Iterator;

public class InvulnerableItem
{
    private final Item item;
    private ItemData data;
    private Item copy;
    private boolean respawnScheduled = false;
    private static ArrayList<InvulnerableItem> items = new ArrayList<>();

    private InvulnerableItem(Item item)
    {
        this.item = item;
        copy = null;
        data = new ItemData(item);
        items.add(this);
    }

    public static void register(Item item)
    {
        new InvulnerableItem(item);
    }

    public static void itemBurn(Item item)
    {
        for (InvulnerableItem invitem : items)
        {
            if (invitem.getOriginal().equals(item) || (invitem.getCopyItem()!=null && invitem.getCopyItem().equals(item)) )
            {
                invitem.scheduleRespawn();
            }
        }
    }

    public Item getCopyItem()
    {
        return copy;
    }

    public Item getOriginal()
    {
        return item;
    }

    public void scheduleRespawn()
    {
        if (respawnScheduled)return;
        respawnScheduled=true;

        new Task(()->
        {
            respawn();
            respawnScheduled = false;
            new Task(this::respawn, 5).run();

        }, 10).run();

    }

    private void remove()
    {
        if (copy != null)
            copy.remove();
    }

    public static void pickUp(Item item)
    {
        Iterator<InvulnerableItem> iterator = items.iterator();
        while (iterator.hasNext())
        {
            InvulnerableItem invitem = iterator.next();

            if (invitem.getOriginal().equals(item) || (invitem.getCopyItem()!=null && invitem.getCopyItem().equals(item)) )
            {
                iterator.remove();
                invitem.remove();
            }
        }
    }

    public static void removeAll(Game game)
    {
        Iterator<InvulnerableItem> iterator = items.iterator();
        while (iterator.hasNext())
        {
            InvulnerableItem invitem = iterator.next();

            if (invitem.getOriginal().getWorld() == game.getTempMap().getWorld() )
            {
                iterator.remove();
                invitem.remove();
            }
        }
    }

    private void respawn()
    {
        if (copy != null)
            copy.remove();

        data = new ItemData(item);

        copy = data.loc.getWorld().dropItem(data.loc, data.stack);
        copy.setItemStack(data.stack);
        copy.setFireTicks(0);
        copy.setVelocity(data.velocity);
    }
}
