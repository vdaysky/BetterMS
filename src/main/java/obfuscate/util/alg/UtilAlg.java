package obfuscate.util.alg;

import obfuscate.MsdmPlugin;
import obfuscate.game.core.Game;
import obfuscate.game.core.PlayerStatus;
import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import obfuscate.mechanic.version.hitbox.HitArea;
import obfuscate.mechanic.version.hitbox.Hitbox;
import obfuscate.mechanic.version.LocationRecorder;
import obfuscate.mechanic.version.PlayerLocation;
import obfuscate.util.UtilEffect;
import obfuscate.util.UtilMath;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.java.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class UtilAlg
{
    public static TreeSet<String> sortKey(Set<String> toSort)
    {
        TreeSet<String> sortedSet = new TreeSet<String>();
        for (String cur : toSort)
            sortedSet.add(cur);

        return sortedSet;
    }

    public static Location getMidpoint(Location a, Location b)
    {
        return a.add(b.subtract(a).multiply(0.5));
    }

    public static Vector getTrajectory(Entity from, Entity to)
    {
        return getTrajectory(from.getLocation().toVector(), to.getLocation().toVector());
    }

    public static Vector getTrajectory(Location from, Location to)
    {
        return getTrajectory(from.toVector(), to.toVector());
    }

    public static Vector getTrajectory(Vector from, Vector to)
    {
        return to.subtract(from).normalize();
    }

    public static Vector getTrajectory2d(Entity from, Entity to)
    {
        return getTrajectory2d(from.getLocation().toVector(), to.getLocation().toVector());
    }

    public static Vector getTrajectory2d(Location from, Location to)
    {
        return getTrajectory2d(from.toVector(), to.toVector());
    }

    public static Vector getTrajectory2d(Vector from, Vector to)
    {
        return to.subtract(from).setY(0).normalize();
    }

    public static boolean HasSight(Location from, Player to)
    {
        return HasSight(from, to.getLocation()) || HasSight(from, to.getEyeLocation());
    }

    public static boolean HasSight(Location from, Location to)
    {
        //Clone Location
        Location cur = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ());

        double rate = 0.1;
        Vector vec = getTrajectory(from, to).multiply(0.1);

        while (UtilMath.offset(cur, to) > rate)
        {
            cur.add(vec);

            if (!UtilBlock.airFoliage(cur.getBlock()))
                return false;
        }

        return true;
    }

    public static int countWalls(Location from, Location to) {
        int walls = 0;
        //Clone Location
        Location cur = new Location(from.getWorld(), from.getX(), from.getY(), from.getZ());

        double rate = 0.8;
        Vector vec = getTrajectory(from, to).multiply(rate);

        while (UtilMath.offset(cur, to) > rate)
        {
            cur.add(vec);

            if (!UtilBlock.airFoliage(cur.getBlock()))
                walls++;
        }

        return walls;
    }


    /** trace in given direction limited by some distance.
     * Helper method */
//    public static TraceEvent traceIn(Location start, Vector dir, Hitbox hitbox, boolean tracePlayers, int maxDistance) {
//
//        Location current = start.clone();
//        Vector diff = dir.normalize().multiply(1);
//
//        while (start.distance(current) < maxDistance) {
////            Logger.print("do small trace from " + current.getBlock().getType() + " " + current.getX() + " " + current.getY() + " " + current.getZ() + " to "
////                    + current.clone().add(diff).getBlock().getType() + " " +
////                    + current.clone().add(diff).getX() + " " + current.clone().add(diff).getY() + " " + current.clone().add(diff).getZ() + " ");
//
//            TraceEvent result = traceIn(current, current.clone().add(diff), hitbox, tracePlayers);
//
//           //Logger.print("Small Trace done. has res: " + result.didHit());
//
//            if (result.didHit()) {
//                return result;
//            }
//
//            current.add(diff);
//        }
//
//        return new TraceEvent(null, null, null);
//    }

    /** trace out in given direction limited by some distance.
     * Helper method */
//    public static TraceEvent traceOut(
//            @Nonnull Location start,
//            @Nonnull Vector dir,
//            @Nonnull Hitbox hitbox,
//            @Nullable StrikePlayer traceOutPlayer,
//            int maxDistance
//    ) {
//
//        Location current = start.clone();
//        Vector diff = dir.clone().normalize();
//
//        while (start.distance(current) < maxDistance) {
////            Logger.print("do small trace from " + current.getBlock().getType() + " " + current.getX() + " " + current.getY() + " " + current.getZ() + " to "
////                    + current.clone().add(diff).getBlock().getType() + " " +
////                    + current.clone().add(diff).getX() + " " + current.clone().add(diff).getY() + " " + current.clone().add(diff).getZ() + " ");
//
//            TraceEvent result = traceOut(current, current.clone().add(diff), hitbox, traceOutPlayer);
//
//            //Logger.print("Small Trace done. has res: " + result.didHit());
//
//            if (result.didHit()) {
//                return result;
//            }
//
//            current.add(diff);
//        }
//
//        return new TraceEvent(null, null, null);
//    }

//    /** ray traces from one location to another.
//     *
//     * @param  oldloc start location
//     * @param newloc end location
//     * @param hitbox hitbox used to determine player hits
//     * @param tracePlayers whether to trace player hits or not
//     *
//     *
//     * @ return TraceResult
//     *
//     * @see TraceEvent
//     */
//    public static TraceEvent traceIn(Location oldloc, Location newloc, Hitbox hitbox, boolean tracePlayers) {
//
//        Location start = oldloc.clone();
//        Vector diff = UtilAlg.getTrajectory(oldloc, newloc);
//        diff.normalize().multiply(0.1);
//
//        int traceIterations = 0;
//
//        while( start.distance(newloc) > 0.1 && traceIterations < 1000) {
//
//            traceIterations ++;
//
//            if (tracePlayers) {
//                // check for player hit
//                for (StrikePlayer player : StrikePlayer.getInWorld(oldloc.getWorld()) ) {
//
//                    //Logger.print(player.getName() +  " trace outside: " + traceOutside + " inside player: " + hitbox.hits(start, player) + " result: " + (traceOutside == hitbox.hits(start, player)) );
//                    if (hitbox.hits(start, player.getPlayer()) ) {
//                        //Logger.print("return player hit trace result");
//                        return new TraceEvent(player.getPlayer(), start.clone().subtract(diff), start);
//                    }
//                }
//            }
//
//            // check block hit
//            boolean blockHit = UtilBlock.isInsideBlock(start.getBlock(), start);
//            if (blockHit && !UtilBlock.airFoliage(start.getBlock())) {
//                //Logger.print("Collide with block: " + traceOutside + " block: " + start.getBlock().getType());
//                return new TraceEvent(null, start.clone().subtract(diff), start);
//            }
//
//            start.add(diff);
//        }
//
//        return  new TraceEvent(null, null, null);
//    }

//    public static TraceEvent traceOut(Location oldloc, Location newloc, Hitbox hitbox, @Nullable StrikePlayer outOfPlayer) {
//
//        Location start = oldloc.clone();
//        Vector diff = UtilAlg.getTrajectory(oldloc, newloc);
//        diff.normalize().multiply(0.1);
//
//        int traceIterations = 0;
//
//        while( start.distance(newloc) > 0.1 && traceIterations < 1000) {
//
//            traceIterations ++;
//
//            if (outOfPlayer != null) {
//                // check for player hit
//                if (outOfPlayer.getPlayer() == null) {
//                    return new TraceEvent(null, null, null);
//                }
//
//                if (!hitbox.hits(start, outOfPlayer.getPlayer()) ) {
//                    return new TraceEvent(outOfPlayer.getPlayer(), start.clone().subtract(diff), start);
//                }
//            }
//            else {
//                boolean isInBlock = UtilBlock.isInsideBlock(start.getBlock(), start);
//
//                // check if we got out of a block
//                if (!isInBlock || UtilBlock.airFoliage(start.getBlock())) {
//                    return new TraceEvent(null, start.clone().subtract(diff), start);
//                }
//            }
//
//            start.add(diff);
//        }
//
//        return  new TraceEvent(null, null, null);
//    }


    public static float GetPitch(Vector vec)
    {
        double x = vec.getX();
        double y = vec.getY();
        double z = vec.getZ();
        double xz = Math.sqrt((x*x) + (z*z));

        double pitch = Math.toDegrees(Math.atan(xz/y));
        if (y <= 0)			pitch += 90;
        else				pitch -= 90;

        //Fix for two vectors at same Y giving 180
        if (pitch == 180)
            pitch = 0;

        return (float) pitch;
    }

    public static float GetYaw(Vector vec)
    {
        double x = vec.getX();
        double z = vec.getZ();

        double yaw = Math.toDegrees(Math.atan((-x)/z));
        if (z < 0)			yaw += 180;

        return (float) yaw;
    }

    public static Vector Normalize(Vector vec)
    {
        if (vec.length() > 0)
            vec.normalize();

        return vec;
    }

    public static Vector Clone(Vector vec)
    {
        return new Vector(vec.getX(), vec.getY(), vec.getZ());
    }

    public static <T> T Random(Set<T> set)
    {
        List<T> list = new ArrayList<T>();

        list.addAll(set);

        return Random(list);
    }


    public static <T> T Random(List<T> list)
    {
        if (list.isEmpty())
            return null;

        return list.get(UtilMath.r(list.size()));
    }

    public static boolean inBoundingBox(Location loc, Location cornerA,	Location cornerB)
    {
        if (loc.getX() <= Math.min(cornerA.getX(), cornerB.getX()))			return false;
        if (loc.getX() >= Math.max(cornerA.getX(), cornerB.getX()))			return false;

        if (cornerA.getY() != cornerB.getY())
        {
            if (loc.getY() <= Math.min(cornerA.getY(), cornerB.getY()))			return false;
            if (loc.getY() >= Math.max(cornerA.getY(), cornerB.getY()))			return false;
        }

        if (loc.getZ() <= Math.min(cornerA.getZ(), cornerB.getZ()))			return false;
        if (loc.getZ() >= Math.max(cornerA.getZ(), cornerB.getZ()))			return false;

        return true;
    }

    public static Vector cross(Vector a, Vector b)
    {
        double x = a.getY()*b.getZ() - a.getZ()*b.getY();
        double y = a.getZ()*b.getX() - a.getX()*b.getZ();
        double z = a.getX()*b.getY() - a.getY()*b.getX();

        return new Vector(x,y,z).normalize();
    }

    public static Vector getRight(Vector vec)
    {
        return cross(vec.clone().normalize(), new Vector(0,1,0));
    }

    public static Vector getLeft(Vector vec)
    {
        return getRight(vec).multiply(-1);
    }

    public static Vector getBehind(Vector vec)
    {
        return vec.clone().multiply(-1);
    }

    public static Vector getUp(Vector vec)
    {
        return getDown(vec).multiply(-1);
    }

    public static Vector getDown(Vector vec)
    {
        return cross(vec, getRight(vec));
    }

    public static Location getAverageLocation(ArrayList<Location> locs)
    {
        if (locs.isEmpty())
            return null;

        Vector vec = new Vector(0,0,0);
        double amount = 0;

        for (Location loc : locs)
        {
            vec.add(loc.toVector());
            amount++;
        }

        vec.multiply(1d/amount);

        return vec.toLocation(locs.get(0).getWorld());
    }

    public static Vector getAverageBump(Location source, ArrayList<Location> locs)
    {
        if (locs.isEmpty())
            return null;

        Vector vec = new Vector(0,0,0);
        double amount = 0;

        for (Location loc : locs)
        {
            vec.add(UtilAlg.getTrajectory(loc, source));
            amount++;
        }

        vec.multiply(1d/amount);

        return vec;
    }

    public static Location findClosest(Location mid, ArrayList<Location> locs)
    {
        Location bestLoc = null;
        double bestDist = 0;

        for (Location loc : locs)
        {
            double dist = UtilMath.offset(mid, loc);

            if (bestLoc == null || dist < bestDist)
            {
                bestLoc = loc;
                bestDist = dist;
            }
        }

        return bestLoc;
    }

    public static boolean isInPyramid(Vector a, Vector b, double angleLimit)
    {
        return (Math.abs(GetPitch(a) - GetPitch(b)) < angleLimit) && (Math.abs(GetYaw(a) - GetYaw(b)) < angleLimit);
    }

    public static boolean isTargetInPlayerPyramid(Player player, Player target, double angleLimit)
    {
        return isInPyramid(player.getLocation().getDirection(), UtilAlg.getTrajectory(player.getEyeLocation(), target.getEyeLocation()), angleLimit) ||
                isInPyramid(player.getLocation().getDirection(), UtilAlg.getTrajectory(player.getEyeLocation(), target.getLocation()), angleLimit);
    }

    public static Location getLocationAwayFromPlayers(ArrayList<Location> locs, ArrayList<Player> players)
    {
        Location bestLoc = null;
        double bestDist = 0;

        for (Location loc : locs)
        {
            double closest = -1;

            for (Player player : players)
            {
                //Different Worlds
                if (!player.getWorld().equals(loc.getWorld()))
                    continue;

                double dist = UtilMath.offsetSquared(player.getLocation(), loc);

                if (closest == -1 || dist < closest)
                {
                    closest = dist;
                }
            }

            if (closest == -1)
                continue;

            if (bestLoc == null || closest > bestDist)
            {
                bestLoc = loc;
                bestDist = closest;
            }
        }

        return bestLoc;
    }

    public static Location getLocationNearPlayers(ArrayList<Location> locs, ArrayList<Player> players, ArrayList<Player> dontOverlap)
    {
        Location bestLoc = null;
        double bestDist = 0;

        for (Location loc : locs)
        {
            double closest = -1;

            boolean valid = true;

            //Dont spawn on other players
            for (Player player : dontOverlap)
            {
                if (!player.getWorld().equals(loc.getWorld()))
                    continue;

                double dist = UtilMath.offsetSquared(player.getLocation(), loc);

                if (dist < 0.8)
                {
                    valid = false;
                    break;
                }
            }

            if (!valid)
                continue;

            //Find closest player
            for (Player player : players)
            {
                if (!player.getWorld().equals(loc.getWorld()))
                    continue;

                double dist = UtilMath.offsetSquared(player.getLocation(), loc);

                if (closest == -1 || dist < closest)
                {
                    closest = dist;
                }
            }

            if (closest == -1)
                continue;

            if (bestLoc == null || closest < bestDist)
            {
                bestLoc = loc;
                bestDist = closest;
            }
        }

        return bestLoc;
    }

    public static boolean intersectsWithBlock(Block block, Location loc) {
        double dx = loc.getX() - block.getX();
        double dy = loc.getY() - block.getY();
        double dz = loc.getZ() - block.getZ();

        return block.getCollisionShape().overlaps(new BoundingBox(dx, dy, dz, dx, dy, dz));
    }

    public static Pair<PlayerLocation, HitArea> intersectsWithPlayer(StrikePlayer player, Location shotAt, Hitbox hitbox, int retrospectTicks) {

        PlayerLocation playerLoc = null;

        if (retrospectTicks != 0) {
            playerLoc = LocationRecorder.getPlayerLocation(player, retrospectTicks);
        }

        if (playerLoc == null) {
            if (retrospectTicks != 0) {
                Logger.warning("Failed to find retrospective location, user current one");
            }
            playerLoc = new PlayerLocation(player.getLocation(), player.getEyeLocation());
        }

        HitArea area = hitbox.hits(shotAt, playerLoc);
        if (area != HitArea.MISS) {
            return new Pair<>(playerLoc, area);
        }

        return null;
    }

    public static boolean blockEquals(Block b1, Block b2) {
        return b1.getLocation().equals(b2.getLocation());
    }

    /** Check if this direction can pass freely within this block. Location is a location somewhere in this block. location + direction define this trace */
    public static WithinBlockTrace analyzeBlockMovement(
            Location loc,
            Vector normalDirection,
            Hitbox hitbox,
            boolean startedInBlock,
            boolean doBackTrace,
            int retrospectTicks,
            HashSet<Entity> hitEntities
    ) {

        final Block block = loc.getBlock();

        boolean isSmoke = block.getType() == Material.NETHER_PORTAL;

        Collection<Entity> entities = block.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5);

        // if we weren't inside block and current block is air we can skip
        // (obviously if there are no players close to this block)
        if (!startedInBlock && block.getType() == Material.AIR && entities.isEmpty()) {

            if (isSmoke) {
                ArrayList<TraceEventV2> events = new ArrayList<>();
                events.add(new TraceEventV2(loc, loc, false, null, null, null, true));
                return new WithinBlockTrace(false, events);
            }

            return null;
        }

        Vector step = normalDirection.clone().multiply(0.01);
        Location nextLocation = loc.clone();

        if (doBackTrace) {
            // go to edge of the block closest to entrypoint, so we can trace entire block in one loop
            while (blockEquals(nextLocation.clone().subtract(step).getBlock(), block)) {
                nextLocation.subtract(step);
            }
        }

        List<TraceEventV2> traceResults = new ArrayList<>();

        // loop while we can step forward without going through entire block
        while (blockEquals(nextLocation.clone().add(step).getBlock(), block)) {

            for (Entity ent : entities) {

                // don't hit same player/ent twice
                if (hitEntities.contains(ent))
                    continue;

                // nice custom hitbox for players
                if (ent instanceof Player) {
                    Player player = (Player) ent;
                    StrikePlayer stPlayer = StrikePlayer.getOrCreate(player);

                    Game game = stPlayer.getGame();
                    if (game == null)
                        continue;

                    var session = game.getGameSession(stPlayer);

                    if (session == null)
                        continue;

                    if (!session.isAlive() || session.getStatus() == PlayerStatus.SPECTATING) {
                        continue;
                    }

                    var hitInfo = intersectsWithPlayer(stPlayer, nextLocation, hitbox, retrospectTicks);
                    if (hitInfo != null) {

                        PlayerLocation playerLoc = hitInfo.key();
                        HitArea area = hitInfo.value();

                        traceResults.add(new TraceEventV2(
                                nextLocation.clone(),
                                nextLocation.clone().subtract(step),
                                true,
                                player,
                                playerLoc,
                                area,
                                false)
                        );
                        // only register hit player if there is an intersection with custom hitbox
                        hitEntities.add(player);
                    }
                // default hitbox for entities
                } else {
                    var box = ((CraftEntity) ent).getHandle().getBoundingBox();
                    Location entLoc = ent.getLocation();
                    boolean intersects = box.a(entLoc.getX(), entLoc.getY(), entLoc.getZ(), entLoc.getX(), entLoc.getY(), entLoc.getZ());
                    if (intersects) {
                        traceResults.add(new TraceEventV2(
                                nextLocation.clone(),
                                nextLocation.clone().subtract(step),
                                true,
                                ent,
                                null,
                                null,
                                false)
                        );
                        hitEntities.add(ent);
                    }
                }
            }

            // Temporary solution to kill chickens for Creeped_
            for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                if (entity instanceof Chicken) {
                    entity.remove();
                    UtilEffect.chickenExplosion(entity.getLocation());
                }
            }

            if (startedInBlock != intersectsWithBlock(block, nextLocation)) {

                startedInBlock = !startedInBlock;

                traceResults.add(
                    new TraceEventV2(
                        nextLocation.clone(),
                        nextLocation.clone().subtract(step),
                        startedInBlock,
                        null,
                        null,
                        null,
                        false
                    )
                );
            }

            nextLocation.add(step);
        }

        if (isSmoke) {
            traceResults.add(new TraceEventV2(nextLocation, nextLocation, false, null, null, null, true));
        }

        return new WithinBlockTrace(startedInBlock, traceResults);
    }

    public static TraceResult traceTickV2(Location start, Vector normalDirection, Double length, Hitbox hitbox, int retrospectTicks, HashSet<Entity> hitEntities) {
        double traveled = 0d;
        Double normalLength = normalDirection.length();
        Vector tinyMove = normalDirection.clone().multiply(0.1);

        ArrayList<TraceEventV2> traceResults = new ArrayList<>();

        Location currentLocation = start.clone();

        Block previousBlock = null;

        boolean wasInBlockOnLastIteration = UtilBlock.isInsideBlock(start); //intersectsWithBlock(start.getBlock(), start);
//        MsdmPlugin.highlight("Was in block on last iteration " + wasInBlockOnLastIteration);

        while (traveled < length) {

            if (previousBlock != null && blockEquals(currentLocation.getBlock(), previousBlock)) {
                traveled += tinyMove.length();
                currentLocation.add(tinyMove);
                continue;
            }

            // if we are at starting block, we don't want to trace back, because bullet never was behind start location.
            // if we don't do this, bullet will start behind player's head and will hit the shooter
            boolean doBackTrace = !blockEquals(start.getBlock(), currentLocation.getBlock());

            WithinBlockTrace trace = analyzeBlockMovement(
                    currentLocation,
                    normalDirection,
                    hitbox,
                    wasInBlockOnLastIteration,
                    doBackTrace,
                    retrospectTicks,
                    hitEntities
            );

            if (trace != null) {
                traceResults.addAll(trace.getTraces());
                wasInBlockOnLastIteration = trace.isInBlock();
            }

            previousBlock = currentLocation.getBlock();

            traveled += tinyMove.length();
            currentLocation.add(tinyMove);
        }

        return new TraceResult(traceResults, currentLocation);
    }

    public static HashSet<StrikePlayer> closePlayersToTrajectory(Location start, Vector normalDirection, Double length) {
        double traveled = 0d;
        Vector tinyMove = normalDirection.clone().multiply(0.1);
        HashSet<StrikePlayer> players = new HashSet<>();

        Location currentLocation = start.clone();
        while (traveled < length) {
            Collection<Entity> closeEntities = start.getWorld().getNearbyEntities(
                currentLocation,
                3, 3, 3,
                e -> e.getType() == EntityType.PLAYER
            );

            for (Entity e : closeEntities) {
                players.add(StrikePlayer.getOrCreate(((Player) e)));
            }

            traveled += tinyMove.length();
            currentLocation.add(tinyMove);
        }

        return players;
    }

    public static double lineToPointDistance(Vector start, Vector end, Vector point) {
        return point.clone().subtract(start)
                .crossProduct(
                        point.clone().subtract(end)
                ).length() / end.clone().subtract(start.clone()).length();
    }

    public static class VectorPair {
        public Vector collinear;
        public Vector orthogonal;

        public VectorPair(Vector collinear, Vector orthogonal) {
            this.collinear = collinear;
            this.orthogonal = orthogonal;
        }

        public Vector getCollinear() {
            return collinear;
        }

        public Vector getOrthogonal() {
            return orthogonal;
        }
    }

    /** Returns collinear and orthogonal components of vector relative to another vector
     * */
    public static VectorPair relativeComponents(Vector vector, Vector axis) {
        Matrix2D rotation = new Matrix2D(new double[][] {
            {axis.getX(), axis.getZ()},
            {axis.getZ(), -axis.getX()}
        });

        Matrix2D undo = rotation.inverse();

        Vector2D rotated = rotation.dot(new Vector2D(vector.getX(), vector.getZ()));

        Vector2D collinear = new Vector2D(rotated.getX(), 0);
        Vector2D orthogonal = new Vector2D(0, rotated.getY());

        Vector2D restoreCollinear = undo.dot(collinear);
        Vector2D restoreOrthogonal = undo.dot(orthogonal);

        return new VectorPair(
            new Vector(restoreCollinear.getX(), 0, restoreCollinear.getY()),
            new Vector(restoreOrthogonal.getX(), 0, restoreOrthogonal.getY())
        );
    }
}

