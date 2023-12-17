package obfuscate.game.core;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.event.custom.session.*;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;
import obfuscate.team.InGameTeamData;
import obfuscate.util.hotbar.SimpleHotbarMessenger;
import obfuscate.util.sidebar.Sidebar;
import obfuscate.util.sidebar.UniqueSidebar;
import obfuscate.util.time.Task;

import javax.annotation.Nullable;

/* data that resets on start of each game */
@Model(name = "playerSession")
public class GameSession extends SyncableObject
{

    // stop propagation to avoid circular update game -> sessions[] -> session -> game
    @Loadable(field = "game", propagateUpdate = false)
    private Competitive _game;

    @Loadable(field = "player")
    private StrikePlayer _holder;

    @Loadable(field = "roster")
    private InGameTeamData _teamData;

    @Loadable(field = "status")
    private PlayerStatus _status;

    @Loadable(field = "state")
    private PlayerState _state;

    private GameInventory gameInventory;
    private SimpleHotbarMessenger hotbarMessenger;

    private int kills=0, deaths=0;

    private Task respawnTask = null;
    private Task invulnerable_task = null;

    private boolean scoped = false;
    private Long scopedAt = null;

    private boolean invulnerable = false;
    private boolean isPlanting = false;
    private boolean alive = false;

    public GameSession()
    {
        waitFullyInitialized().thenSync(
            (x) -> {
                gameInventory = new GameInventory(this._holder);
                hotbarMessenger = new SimpleHotbarMessenger(this._holder);
                return null;
            }
        );
    }

    public void cancelInvulnerability()
    {
        invulnerable = false;
        if (getInvulnerableTask() != null)
            getInvulnerableTask().cancel();
    }

    public StrikePlayer getPlayer() {
        return _holder;
    }

    public Sidebar getSidebar()
    {
        return UniqueSidebar.getOrCreate(_holder);
    }

    public SimpleHotbarMessenger getHotbarMessenger()
    {
        return hotbarMessenger;
    }

    public void addKill()
    {
        setKills(kills+1);
    }

    public void addDeath()
    {
        setDeaths(deaths+1);
    }

    public int getKills()
    {
        return kills;
    }
    public boolean isPlanting()
    {
        return isPlanting;
    }

    public void setPlanting(boolean planting){
        isPlanting = planting;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public void setKills(int k) {
        kills = k;
        new KillCountUpdateEvent(k, _holder, _game).trigger();
        new KdUpdateEvent(kills, deaths, _holder, _game).trigger();
    }

    public void setDeaths(int k) {
        deaths = k;
        new DeathCountUpdateEvent(k, _holder, _game).trigger();
        new KdUpdateEvent(kills, deaths, _holder, _game).trigger();
    }

    public void resetKD() {
        setKills(0);
        setDeaths(0);
    }

    public boolean isAlive()
    {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public GameInventory getInventory()
    {
        return gameInventory;
    }

    public void setInvulnerability(boolean b)
    {
        if (!b) cancelInvulnerability();

        else
        {
            cancelInvulnerability();
            invulnerable = true;
        }
    }

    public PlayerState getState()
    {
        return _state;
    }

    public PlayerStatus getStatus() {
        return _status;
    }

    public @Nullable Task getRespawnTask()
    {
        return respawnTask;
    }

    public void setRespawnTask(Task task)
    {
        respawnTask = task;
    }

    public void cancelRespawnTask() {
        if (respawnTask != null)
            respawnTask.cancel();
    }

    public boolean isScoped()
    {
        return scoped;
    }

    public void addScope() {
        if (scoped)
            return;

        if ( !isAlive() )
            return;

        scoped = true;
        scopedAt = System.currentTimeMillis();
        _holder.addScopeEffect();
    }

    public void removeScope() {
        if (!_holder.isOnline()) {
            return;
        }

        if (!scoped)
            return;

        scoped = false;
        scopedAt = null;

        // update equipment (set helmet)
        _holder.equip(_game);
        _holder.removeScopeEffect();
    }

    public Long getScopedDuration() {
        if (scopedAt == null) {
            return 0L;
        }
        return System.currentTimeMillis() - scopedAt;
    }

    public void setScoped(boolean scope) {
        if (scope) {
            addScope();
        } else {
            removeScope();
        }
    }

    public InGameTeamData getRoster() {
        return _teamData;
    }

    public boolean isInvulnerable()
    {
        return invulnerable;
    }

    public void setInvulnerable(boolean invl)
    {
        invulnerable = invl;
    }

    public Task getInvulnerableTask()
    {
        return invulnerable_task;
    }

    public void setInvulnerableTask(Task task)
    {
        invulnerable_task = task;
    }

    public void reset()
    {
        resetKD();
        getInventory().clear();
        setScoped(false);
        setInvulnerable(false);
        setInvulnerability(false);
        setRespawnTask(null);
        setPlanting(false);
        cancelRespawnTask();
    }

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return null;
    }

    public boolean isInGame() {
        return _state == PlayerState.IN_GAME;
    }

    public boolean isSpectating() {
        return _status == PlayerStatus.SPECTATING;
    }
}
