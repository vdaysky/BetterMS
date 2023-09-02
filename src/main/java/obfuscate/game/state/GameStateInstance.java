package obfuscate.game.state;

import java.util.ArrayList;
import java.util.Arrays;

public class GameStateInstance
{
    private ArrayList<StateTag> tags = new ArrayList<>();
    String _name;
    GeneralGameStage _generalStage;

    public GameStateInstance(String stateName,               // Nice name to describe stage
                             GeneralGameStage generalStage,   // generalised stage time
                             StateTag ... tags
    )
    {
        _name = stateName;
        _generalStage = generalStage;
        if (tags.length == 0) {
            this.tags.addAll(Arrays.asList(generalStage.getDefaultTags()));
        }
        else {
            this.tags.addAll(Arrays.asList(tags));
        }
    }

    public boolean isLive() {
        return getGeneralStage() == GeneralGameStage.LIVE;
    }

    public boolean isFreezeTime() {
        return getGeneralStage() == GeneralGameStage.FREEZE_TIME;
    }

    public boolean isBombPlant() {
        return getGeneralStage() == GeneralGameStage.BOMB_PLANT;
    }

    public boolean isRoundEnd() {
        return getGeneralStage() == GeneralGameStage.ROUND_END;
    }

    public GeneralGameStage getGeneralStage()
    {
        return _generalStage;
    }
    public String getName()
    {
        return _name;
    }

    public boolean is(StateTag tag)
    {
        return tags.contains(tag);
    }

    public void setTag(StateTag tag)
    {
        if (!tags.contains(tag))
            tags.add(tag);
    }
    public void removeTag(StateTag tag)
    {
        tags.remove(tag);
    }

    public ArrayList<StateTag> getTags()
    {
        return tags;
    }

    /** Make one stage completely adopt every trait of another one */
    public void update(GameStateInstance pauseState) {
        this.tags = pauseState.getTags();
        this._name = pauseState.getName();
        this._generalStage = pauseState.getGeneralStage();
    }

    public boolean isWarmup() {
        return getGeneralStage() == GeneralGameStage.WARM_UP;
    }
}
