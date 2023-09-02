package obfuscate.util.chat;

import net.md_5.bungee.api.chat.*;

import java.util.ArrayList;

public class MessageBuilder {

    private final ArrayList<TextComponent> components = new ArrayList<>();

    public MessageBuilder() {

    }

    public MessageBuilder(TextComponent component) {
        components.add(component);
    }

    public static MessageBuilder of(String text) {
        return new MessageBuilder(new TextComponent(text));
    }

    public MessageBuilder append(MessageBuilder component) {
        components.add(component.build());
        return this;
    }

    public MessageBuilder append(String text) {
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

    public MessageBuilder command(String command) {
        TextComponent component = this.build();
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return new MessageBuilder(component);
    }

    /** Set hover effect on entire contents of this builder */
    public MessageBuilder hover(String hover) {
        TextComponent component = this.build();
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        return new MessageBuilder(component);
    }

    public MessageBuilder link(String s) {
        TextComponent component = this.build();
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, s));
        return new MessageBuilder(component);
    }
}
