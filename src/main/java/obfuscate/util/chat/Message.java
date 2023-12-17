package obfuscate.util.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.ArrayList;

public class Message {

    private final ArrayList<TextComponent> components = new ArrayList<>();

    public Message() {

    }

    public static Message n() {
        return new Message();
    }

    public Message(TextComponent component) {
        components.add(component);
    }

    public static Message of(String text) {
        return new Message(new TextComponent(text));
    }

    public static Message space() {
        return of(" ");
    }

    public Message append(Message component) {
        components.add(component.build());
        return this;
    }

    public Message append(String text) {
        components.add(new TextComponent(text));
        return this;
    }

    public TextComponent build() {
        TextComponent result = new TextComponent();
        for (TextComponent component : components) {
            result.addExtra(component);
        }
        return result;
    }

    public Message command(String command) {
        TextComponent component = this.build();
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return new Message(component);
    }

    /** Set hover effect on entire contents of this builder */
    public Message hover(String hover) {
        TextComponent component = this.build();
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        return new Message(component);
    }

    public Message link(String s) {
        TextComponent component = this.build();
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, s));
        return new Message(component);
    }

    public Message green(Message builder) {
        TextComponent continuation = builder.build();
        continuation.setColor(ChatColor.GREEN);
        components.add(continuation);
        return this;
    }
    public Message green(String text) {
        return green(Message.of(text));
    }

    public Message gray(String text) {
        return gray(Message.of(text));
    }

    public Message color(Message builder, ChatColor color) {
        TextComponent continuation = builder.build();
        continuation.setColor(color);
        components.add(continuation);
        return this;
    }

    public Message color(String message, ChatColor color) {
        return color(Message.of(message), color);
    }

    public Message gray(Message builder) {
        return color(builder, ChatColor.GRAY);
    }

    public Message boldColor(ChatColor color, BaseComponent ... message) {
        TextComponent coloredText = new TextComponent(message);
        coloredText.setColor(color);
        coloredText.setBold(true);
        this.components.add(coloredText);
        return this;
    }

    public Message boldColor(String message, ChatColor color) {
        return boldColor(color, TextComponent.fromLegacyText(message));
    }

    public Message white(String message) {
        return color(message, ChatColor.WHITE);
    }

    public Message yellow(String string) {
        return color(string, ChatColor.YELLOW);
    }

    public Message boldGreen(String text) {
        return boldColor(ChatColor.GREEN, TextComponent.fromLegacyText(text));
    }

    public Message boldGreen(Message builder) {
        return boldColor(ChatColor.GREEN, builder.build());
    }

    public Message boldRed(String s) {
        return boldColor(ChatColor.RED, TextComponent.fromLegacyText(s));
    }
}
