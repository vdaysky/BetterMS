package obfuscate.world;

import obfuscate.MsdmPlugin;
import obfuscate.util.Position;
import obfuscate.util.block.ComplexBoundingBox;
import obfuscate.util.block.RelBoundingBox;
import com.google.gson.GsonBuilder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class MapManager
{
    public enum MapMeta {
        RESPAWN_T,
        RESPAWN_CT,
        RESPAWN_RANDOM,
        BOMBSITE_A,
        BOMBSITE_B,
        SHOP_T,
        SHOP_CT,
    }

    private static MapCollection mapTemplates = null;

    private static final ArrayList<TempMap> tempMaps = new ArrayList<>();


    public static GameMap getRandomMap(@Nullable String tag)
    {
        if (tag == null)
            return getTemplateCollection().pickRandom();
        return getTemplateCollection().getWithTag(tag).pickRandom();
    }

    @Nullable
    public static GameMap getGameMap(String name)
    {
        return getTemplateCollection().get(name);
    }


    public static MapCollection getTemplateCollection()
    {
        if (mapTemplates != null) {
            return mapTemplates;
        }
        
        File mapsFolder = new File("templates");
        File[] listOfFiles = mapsFolder.listFiles();

        HashMap<String, GameMap> maps = new HashMap<>();

        for (File f : listOfFiles) {
            GameMap loadedMap = loadMapData(f.getName());
            if (loadedMap != null) {
                maps.put(f.getName(), loadedMap);
            }
        }

        mapTemplates = new MapCollection(maps);
        return mapTemplates;
    }

    public static void saveGameData(MapData _map)
    {
        GameMap map = (GameMap) _map;

        File file = new File("./templates/" + map.getName() + "/meta.json");
        if (!file.exists()) {
            return;
        }
        String dataPretty = new GsonBuilder().setPrettyPrinting().create().toJson(map.toObject());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(dataPretty);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameMap loadMapData(String mapName) {
        // load json file

        JSONParser parser = new JSONParser();
        JSONObject mapMeta;
        try {
            File file = new File("./templates/" + mapName + "/meta.json");
            if (!file.exists()) {
                return null;
            }
            mapMeta = (JSONObject) parser.parse(new FileReader("./templates/" + mapName + "/meta.json"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load meta.json for map " + mapName);
        }

        ArrayList<ArrayList<ArrayList<Double>>> tShop = (ArrayList<ArrayList<ArrayList<Double>>>) mapMeta.get("T Shop");
        ArrayList<ArrayList<ArrayList<Double>>> ctShop = (ArrayList<ArrayList<ArrayList<Double>>>) mapMeta.get("CT Shop");
        ArrayList<ArrayList<Double>> tRespawns = (ArrayList<ArrayList<Double>>) mapMeta.get("T Respawns");
        ArrayList<ArrayList<Double>> ctRespawns = (ArrayList<ArrayList<Double>>) mapMeta.get("CT Respawns");
        ArrayList<ArrayList<Double>> dmRespawns = (ArrayList<ArrayList<Double>>) mapMeta.get("DM Respawns");
        ArrayList<ArrayList<ArrayList<Double>>> bombsiteA = (ArrayList<ArrayList<ArrayList<Double>>>) mapMeta.get("Bomb Site A");
        ArrayList<ArrayList<ArrayList<Double>>> bombsiteB = (ArrayList<ArrayList<ArrayList<Double>>>) mapMeta.get("Bomb Site B");
        ArrayList<String> tags = (ArrayList<String>) mapMeta.get("Tags");

        return new GameMap(
            mapName,
            readPositionList(tRespawns),
            readPositionList(ctRespawns),
            readPositionList(dmRespawns),
            readComplexBox(tShop),
            readComplexBox(ctShop),
            readComplexBox(bombsiteA),
            readComplexBox(bombsiteB),
            tags
        );
    }

    private static ComplexBoundingBox readComplexBox(ArrayList<ArrayList<ArrayList<Double>>> data) {
        ArrayList<BoundingBox> boxes = new ArrayList<>();
        for (ArrayList<ArrayList<Double>> box : data) {
            boxes.add(readBox(box));
        }
        return new ComplexBoundingBox(boxes);
    }

    private static ArrayList<Position> readPositionList(ArrayList<ArrayList<Double>> data) {
        ArrayList<Position> positions = new ArrayList<>();
        for (ArrayList<Double> position : data) {
            positions.add(readPosition(position));
        }
        return positions;
    }

    private static Position readPosition(ArrayList<Double> data) {
        return new Position(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4));
    }

    private static BoundingBox readBox(ArrayList<ArrayList<Double>> data) {
        return new BoundingBox(
            data.get(0).get(0), // X1
            data.get(0).get(1), // Y1
            data.get(0).get(2), // Z1
            data.get(1).get(0), // X2
            data.get(1).get(1), // Y2
            data.get(1).get(2)  // Z2
        );
    }

    private static RelBoundingBox loadVolume(ConfigurationSection section, String subSection)
    {
        if (section == null)
            return null;

        ConfigurationSection teamSec = section.getConfigurationSection(subSection);

        if (teamSec == null)
            return null;

        List<Integer> centerData = teamSec.getIntegerList("center");
        int x = teamSec.getInt("x");
        int y = teamSec.getInt("y");
        int z = teamSec.getInt("z");
        RelBoundingBox box = new RelBoundingBox(
                new Vector(centerData.get(0), centerData.get(1), centerData.get(2)),
                x, y, z
        );
        return box;
    }

    private static HashMap<String, Position>loadPositions(ConfigurationSection section)
    {
        HashMap<String, Position> result = new HashMap<>();
        for (String positionName : section.getKeys(false))
        {
            List<Float> coordinates = section.getFloatList(positionName);
            if (coordinates.size() != 5){
                continue;
            }
            Position pos = new Position(coordinates.get(0), coordinates.get(1),coordinates.get(2),
                                        coordinates.get(3),coordinates.get(4));
            result.put(positionName, pos);
        }
        return result;
    }

    public static @Nullable TempMap loadMap(MapData mapData)
    {
        MsdmPlugin.logger().info("LoadMap inside " + Thread.currentThread().getName());
        String tempName = mapData.getName() + "-" + UUID.randomUUID();
        File worldsContainerPath = MsdmPlugin.getInstance().getServer().getWorldContainer();

        File template = new File("templates/", mapData.getName());

        File destination = new File(worldsContainerPath, tempName);

        if (!template.exists()) {
            MsdmPlugin.logger().warning("Template file for map " + mapData.getName() + " does not exist.");
            return null;
        }

        copyMap(template, destination);

        World world = Bukkit.createWorld(new WorldCreator(tempName));

        TempMap map = new TempMap(world, mapData);
        tempMaps.add(map);
        return map;
    }

    public static void unloadAllTemp()
    {
        for (TempMap map : ((ArrayList<TempMap>) tempMaps.clone()))
        {
            unloadMap(map);
        }
    }

    public static void unloadMap(TempMap map)
    {
        World world = Bukkit.getWorld(map.getTempName());
        if (world == null)
        {
            System.out.println("[WARN] tried to unload not loaded map");
            return;
        }
        for (Player player : world.getPlayers())
        {
            MsdmPlugin.logger().log(Level.SEVERE, "Player was not moved to a new game / another lobby by Lobby.");
            player.kickPlayer("You are not supposed to be here! report this to an admin");
        }
        ArrayList<NPC> toDelete = new ArrayList<>();
        for (NPC npc : CitizensAPI.getNPCRegistry())
        {
            if (npc.isSpawned() && npc.getEntity().getWorld() == map.getWorld())
            {
                toDelete.add(npc);
            }
        }
        for (NPC npc : toDelete)
        {
            npc.destroy();
        }

        Bukkit.getServer().unloadWorld(world, false);

        try
        {
            FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), map.getTempName()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        tempMaps.remove(map);
        System.out.println("unloaded " + map.getTempName());
    }

    private static void copyMap(File source, File target)
    {
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.lock"));

            if(ignore.contains(source.getName()))
                return;

            if(source.isDirectory()) {

                if(!target.exists()) {
                    target.mkdirs();
                }

                String[] files = source.list();

                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(target, file);

                    copyMap(srcFile, destFile);
                }
            } else {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0)
                    out.write(buffer, 0, length);

                in.close();
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Object> getMapConfig() {

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(MsdmPlugin.getInstance().getDataFolder(), "maps.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        StringBuilder resultStringBuilder = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while (true) {
                try {
                    if ((line = br.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                resultStringBuilder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return (HashMap<String, Object>) new JSONParser().parse(resultStringBuilder.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addSpawn(GameMap map, List<Rectangle> shape) {

    }

    public static MapCollection getAvailableMaps() {
        return getTemplateCollection();
    }
}
