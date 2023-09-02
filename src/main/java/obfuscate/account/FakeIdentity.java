package obfuscate.account;

import obfuscate.game.player.StrikePlayer;
import obfuscate.thirdparty.ProfileData;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class FakeIdentity
{
    private static ArrayList<UUID> disguiseNames = new ArrayList<UUID>()
    {
        {
            add(UUID.fromString("A"));
            add(UUID.fromString("B"));
            add(UUID.fromString("C"));
            add(UUID.fromString("D"));
            add(UUID.fromString("E"));
        }
    };

    private static HashMap<UUID, ProfileData> cached = new HashMap<UUID, ProfileData>();

    public static void fillCache()
    {
        for (UUID uuid : disguiseNames)
        {
            ensureCache(uuid);
        }
    }

    public static void ensureCache(UUID uuid)
    {
        if (!cached.containsKey(uuid))
            cached.put(uuid, requestHTTP(uuid));
    }

    public static void setDisguise(@Nonnull Player set_for, @Nonnull UUID skin, @Nonnull String name)
    {
        setSkin(((CraftPlayer) set_for).getHandle().getProfile(), cached.get(skin));
        setName(set_for, name);
        update(set_for);
    }

    private static void setSkin(GameProfile profile, ProfileData data)
    {
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", data.value, data.signature));
    }

    private static ProfileData requestHTTP(UUID uuid)
    {
        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String line;
                String reply = "";
                BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while (true)
                {
                    line = r.readLine();
                    if (line==null || line.equals(""))break;

                    reply += line;
                }

                JSONObject obj = (JSONObject) new JSONParser().parse(reply);
                JSONArray array = (JSONArray) obj.get("properties");
                String skin = (String)((JSONObject)array.get(0)).get("value");
                String signature = (String)((JSONObject)array.get(0)).get("signature");
                return new ProfileData("", skin, signature);

            } else {
                System.out.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UUID getRandomUUID()
    {
        return disguiseNames.get( (int)(Math.random() * disguiseNames.size()) );
    }

    public static boolean update(Player player_set_for)
    {
        for (Player player : player_set_for.getWorld().getPlayers())
        {
            player.hidePlayer(player_set_for);
            player.showPlayer(player_set_for);
        }
        return true;
    }

    static HashMap<UUID, Boolean> hasDummyName = new HashMap<>();

    public static boolean setName(@Nonnull Player player, @Nonnull String name)
    {

        if (player.getName().equalsIgnoreCase("dummy"))
            return false;
        System.out.println("player: " +  player + " " + player.getName() + player.getUniqueId());
        System.out.println(" map: " + hasDummyName);
        if (hasDummyName.containsKey(player.getUniqueId())){
            return false;
        }

        StrikePlayer sp = StrikePlayer.getOrCreate(player);
        sp.setVisibleName(name);

        // do this just to make sure my name wont appear in logs or when auto complete tab
        boolean res = setDummyName(player);
        if (res) {
            hasDummyName.put(player.getUniqueId(), true);
        }
        return res;
    }

    private static boolean setDummyName(Player player)
    {
        try
        {
            GameProfile playerProfile = ((CraftPlayer) player).getHandle().getProfile();
            Field ff = playerProfile.getClass().getDeclaredField("name");
            ff.setAccessible(true);
            ff.set(playerProfile, "legit name");
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
