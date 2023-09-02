package obfuscate.game.state;

public enum StateTag
{
    RESPAWNABLE,            // determines if player can be respawned instantly
    TICKABLE,               // can tick counter be decreased during stage
    PAUSABLE,               // whether game can be paused during this stage
    JOINABLE,               // whether player can join game during this stage as participant
    DAMAGE_ALLOWED,         // can players be damaged during stage
    CAN_INTERACT,           // can players interact with items during stage (guns, nades, bomb, etc)
    CAN_MOVE,               // can players move during stage
    OVERRIDE_BUYTIME,       // can players shop even if round time > buytime

    INVISIBLE_TIMER,          // whether timer is visible during this stage
    ;

}
