package obfuscate.game.state;

// those describe general behaviours some custom states should have
public enum GeneralGameStage
{
    // those nice names used for reflection, change carefully
    WARM_UP("WarmUp",
            StateTag.CAN_MOVE,
            StateTag.CAN_INTERACT,
            StateTag.JOINABLE,
            StateTag.RESPAWNABLE,
            StateTag.DAMAGE_ALLOWED,
            StateTag.OVERRIDE_BUYTIME),

    FREEZE_TIME("FreezeTime",
            StateTag.JOINABLE,
            StateTag.TICKABLE,
            StateTag.PAUSABLE),

    LIVE("Live",
            StateTag.CAN_MOVE,
            StateTag.CAN_INTERACT,
            StateTag.DAMAGE_ALLOWED,
            StateTag.TICKABLE
    ),

    ROUND_END("RoundEnd",
            StateTag.CAN_MOVE,
            StateTag.CAN_INTERACT,
            StateTag.DAMAGE_ALLOWED,
            StateTag.TICKABLE),

    GAME_END(
            "GameEnd",
            StateTag.TICKABLE,
            StateTag.CAN_MOVE
    ),

    BOMB_PLANT("BombPlant",
            StateTag.CAN_MOVE,
            StateTag.CAN_INTERACT,
            StateTag.DAMAGE_ALLOWED,
            StateTag.TICKABLE,
            StateTag.INVISIBLE_TIMER
    ),

    PAUSED("Paused",
            StateTag.TICKABLE,
            StateTag.JOINABLE),

    CUSTOM("Custom");

    String _niceName;
    StateTag[] tags;
    GeneralGameStage(String niceName, StateTag ... defaultTags)
    {
        _niceName = niceName;
        tags = defaultTags;
    }

    public String getNiceName()
    {
        return _niceName;
    }
    public StateTag[] getDefaultTags()
    {
        return tags;
    }
}
