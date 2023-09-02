package obfuscate.game.core.traits;

import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.mechanic.item.objective.Bomb;

import java.util.Arrays;
import java.util.List;

public class SharedGameContext implements ISharedContext {

    private List<GameStateInstance> stages = Arrays.asList(
        new GameStateInstance(
            "Freeze Time",
            GeneralGameStage.FREEZE_TIME
        ),
        new GameStateInstance(
            "Live",
            GeneralGameStage.LIVE
        ),
        new GameStateInstance(
            "Round end",
            GeneralGameStage.ROUND_END
        )
    );
    private SidebarUpdater sidebarUpdater = new DefaultSidebarUpdater();

    private String name = "Competitive";

    private boolean _defersGameStart = false;

    private GameStateUpdater stateUpdater = new DefusalStateUpdater();


    private Bomb bomb = new Bomb();
    private StrikePlayer defuser = null;
    private StrikePlayer bombCarry = null;

    @Override
    public List<GameStateInstance> getStages() {
        return stages;
    }

    @Override
    public void setGameStages(List<GameStateInstance> stages) {
        this.stages = stages;
    }

    @Override
    public String getModeName() {
        return name;
    }

    @Override
    public void setModeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void defersGameStart(boolean _defersGameStart) {
        this._defersGameStart = _defersGameStart;
    }

    @Override
    public SidebarUpdater getSidebarUpdater() {
        return sidebarUpdater;
    }

    public void setSidebarUpdater(SidebarUpdater sidebarUpdater) {
        this.sidebarUpdater = sidebarUpdater;
    }

    @Override
    public GameStateUpdater getGameStateUpdater() {
        return stateUpdater;
    }

    @Override
    public void setGameStateUpdater(GameStateUpdater updater) {
        stateUpdater = updater;
    }

    public boolean defersGameStart() {
        return _defersGameStart;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public StrikePlayer getDefuser() {
        return defuser;
    }

    public StrikePlayer getBombCarry() {
        return bombCarry;
    }

    public void setBomb(Bomb bomb) {
        this.bomb = bomb;
    }

    public void setDefuser(StrikePlayer defuser) {
        this.defuser = defuser;
    }

    public void setBombCarry(StrikePlayer bombCarry) {
        this.bombCarry = bombCarry;
    }
}
