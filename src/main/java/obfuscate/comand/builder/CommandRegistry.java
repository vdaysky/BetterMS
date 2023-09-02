package obfuscate.comand.builder;

import obfuscate.MsdmPlugin;
import org.bukkit.command.Command;

import java.lang.reflect.Method;
import java.util.Arrays;

public class CommandRegistry {

    public static void registerFakeCommand(Command whatCommand, MsdmPlugin plugin)
        throws ReflectiveOperationException {
            Method commandMap = plugin.getServer().getClass().getMethod("getCommandMap", null);
            Object cmdmap = commandMap.invoke(plugin.getServer(), null);
            Method register = cmdmap.getClass().getMethod("register", String.class,Command.class);
            register.invoke(cmdmap, whatCommand.getName(),whatCommand);
    }

    public static void register(CommandHandler command, String name, String... aliases) {
        try {
            registerFakeCommand(new FakeCommandRegistry(command, name, aliases), MsdmPlugin.getInstance());
            MsdmPlugin.logger().info("Registered " + name + " (" + Arrays.toString(aliases) + ") successfully.");
        } catch (Exception e) {
            System.err.println("=======================================");
            System.err.println("Reflective command registration failed:");
            e.printStackTrace();
            System.err.println("=======================================");
        }
    }
}
