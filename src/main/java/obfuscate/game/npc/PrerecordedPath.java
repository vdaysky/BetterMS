package obfuscate.game.npc;

import obfuscate.MsdmPlugin;
import obfuscate.world.GameMap;

import java.io.*;
import java.util.ArrayList;

public class PrerecordedPath
{
    private static final  String EXTENSION = ".path";

    private static String getExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public static PrerecordedPath load(GameMap map, String pathName)
    {
        try
        {
            File f = new File(MsdmPlugin.getInstance().getDataFolder().getPath() + "/paths/" + map.getName(), pathName + EXTENSION);
            BufferedReader br = new BufferedReader(new FileReader(f));
            ArrayList<Double[]> locations = new ArrayList<>();
            int i = 0;
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                Double[] doubles = new Double[5];
                doubles[0] = Double.valueOf(line.split(" ")[0]);
                doubles[1] = Double.valueOf(line.split(" ")[1]);
                doubles[2] = Double.valueOf(line.split(" ")[2]);
                doubles[3] = Double.valueOf(line.split(" ")[3]);
                doubles[4] = Double.valueOf(line.split(" ")[4]);
                locations.add(doubles);
                i++;
            }
            return new PrerecordedPath(map, pathName, locations);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("[ERROR] couldn't load path " + pathName + " for " + map.getName());
        }
        return null;
    }

    public static ArrayList<String> getPathNames(GameMap map)
    {
        ArrayList<String> names = new ArrayList<>();
        File mapFolder = new File(MsdmPlugin.getInstance().getDataFolder().getPath() + "/paths/" + map.getName());

        if (!mapFolder.exists())
            return names;

        for (File pathFile : mapFolder.listFiles())
        {
            if(pathFile.isFile() && getExtension(pathFile).equalsIgnoreCase(EXTENSION))
            {
                names.add(pathFile.getName().replace(EXTENSION, ""));
            }
        }
        return names;
    }

    public static void save(GameMap map, String pathName, ArrayList<Double[]> points)
    {
        try
        {
            File parent = new File(MsdmPlugin.getInstance().getDataFolder().getPath() + "/paths/" + map.getName());
            File f = new File(parent, pathName + EXTENSION);

            if (!f.exists())
            {
                parent.mkdirs();
                f.createNewFile();
            }
            else
            {
                System.out.println("[WARN] Override file " + pathName);
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (Double[] point : points)
            {
                bw.write(point[0] + " " + point[1] + " " + point[2] + " " + point[3] + " " + point[4] + "\n");
            }
            bw.flush();
            bw.close();
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    GameMap map;
    String pathName;
    ArrayList<Double[]> points;

    private PrerecordedPath(GameMap map, String pathName, ArrayList<Double[]> points)
    {
        this.map = map;
        this.pathName = pathName;
        this.points = points;
    }

    public static boolean delete(GameMap map, String pathName)
    {
        if (getPathNames(map).contains(pathName + EXTENSION))
        {
            File file = new File(new File(MsdmPlugin.getInstance().getDataFolder().getPath() + "/paths/" + map.getName() + "/"), pathName);
            if (file.exists())
            {
                return file.delete();
            }
        }
        return false;
    }

    public GameMap getMap()
    {
        return map;
    }

    public String getPathName()
    {
        return pathName;
    }

    public ArrayList<Double[]> getPoints() {
        return points;
    }
}
