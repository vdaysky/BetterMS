package obfuscate.game.core;

public abstract class DefusalGame extends TeamGame
{
//    protected Bomb bomb = null;
//    private StrikePlayer defuser = null;
//    private StrikePlayer bombCarry = null;
//
//    public StrikePlayer getBombHolder() {
//        return bombCarry;
//    }
//
//
//    @LocalEvent
//    public void tick(TimeEvent e)
//    {
//        if (e.getReason() != TimeEvent.UpdateReason.TICK)return;
//        updateBomb();
//    }
//
//    public void updateBomb()
//    {
//        if (bomb != null && bomb.isPlanted() && !bomb.isExploded() && !bomb.isDefused() && !bomb.isBeingPlanted())
//        {
//            for (StrikePlayer ct : getPlayers(StrikeTeam.CT, true))
//            {
//                if (bomb.canSee(ct))
//                {
//                    defuser = ct;
//                    bomb.startDefuse(this, defuser);
//                    break;
//                }
//            }
//        }
//    }
//
//    public void startBombPlant(StrikePlayer player)
//    {
//        if (bomb == null)return;
//
//        if (!bomb.canPlant(this, player))
//            return;
//
//        if (bomb != null && player.getItemInHand(this) == bomb)
//        {
//            // planter is never reset to null # i removed planter, planter = holder if bombIsBeingPlanted now
//            bomb.startPlant(this, player);
//        }
//    }
//
//    @LocalEvent
//    public void onDefuse(BombDefuseEvent e) {
//        // do not force set, because we don't want after-round defuse to mess up state chain
//        getStateManager().trySetNext(GameStateChangeEvent.ChangeReason.OBJECTIVE_COMPLETE);
//    }
//
//    @LocalEvent
//    public void pickupEvent(ItemPickUpEvent e)
//    {
//        StrikeTeam team = getPlayerRoster(e.getPlayer()).getTeam();
//
//        // prevent CT's picking up the bomb
//        if (team != StrikeTeam.T && e.getItem() instanceof Bomb)
//            e.setCancelled(true);
//
//        // prevent T's picking up kits
//        if (team != StrikeTeam.CT && e.getItem() instanceof DefusalKit)
//            e.setCancelled(true);
//
//        // keep track of who has the bomb
//        if (team == StrikeTeam.T && e.getItem() instanceof Bomb) {
//            bombCarry = e.getPlayer();
//        }
//    }
//
//    @LocalEvent
//    private void onBombDrop(ItemDropEvent e) {
//        // keep track of who has the bomb
//        if (e.getHolder() == bombCarry && e.getItem() == bomb) {
//            bombCarry = null;
//        }
//    }
//
//    @LocalEvent
//    private void onRoundStart(RoundStartEvent e)
//    {
//        // remove bomb block
//        if (bomb != null) {
//            if (bomb.isBeingPlanted()) {
//                bomb.cancelPlant(this, getBombHolder());
//            }
//
//            bomb.remove(false);
//            bomb.stopDefuse(this, defuser);
//        }
//
//        // remove bomb from previous round
//        if (bombCarry != null) {
//            bombCarry.getInventory(this).setItem(bomb.getSlot(), null);
//        }
//
//        // give bomb
//        bombCarry = getRandomMember(StrikeTeam.T, true);
//
//        if (bombCarry != null) {
//            bomb = new Bomb();
//            bomb.giveToPlayer(this, bombCarry, true);
//        }
//        else
//        {
//            System.out.println("[WARN] could not find T player to give bomb to");
//        }
//    }
//
//    public Bomb getBomb() {
//        return bomb;
//    }
//
//    @LocalEvent
//    private void playBombDropSound(ItemDropEvent e) {
//        if (e.getItem() instanceof Bomb) {
//            getSoundManager().dropBomb().forTeam(StrikeTeam.T).play();
//        }
//    }
//
//    @LocalEvent
//    public void onBombPlant(BombPlantEvent e) {
//
//        int bombTimeSeconds = getConfig().getValue(ConfigField.BOMB_EXPLODE_TIME).val(); // 45 seconds
//        GameStateInstance gameState = new GameStateInstance("Bomb planted", GeneralGameStage.BOMB_PLANT);
//        getStateManager().trySetActiveState(gameState, StatePlace.SET, bombTimeSeconds, GameStateChangeEvent.ChangeReason.REPLACED);
//        bombCarry = null;
//    }
}
