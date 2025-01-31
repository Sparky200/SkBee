package com.shanebeestudios.skbee.elements.text.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
@Name("Text Component - Hover Event")
@Description({"Create a new hover event. Can show texts, text components, an item or an entity to a player.",
        "'showing %itemtype%' requires Minecraft 1.18.2+"})
@Examples({"set {_t} to text component from \"Check out my cool tool!\"",
        "set hover event of {_t} to a new hover event showing player's tool",
        "send component {_t} to player"})
@Since("1.5.0")
public class ExprHoverEvent extends SimpleExpression<HoverEvent> {

    static {
        Skript.registerExpression(ExprHoverEvent.class, HoverEvent.class, ExpressionType.COMBINED,
                // TODO scheduled for removal of "item" (july 8/2023)
                "[a] [new] hover event showing [item] %strings/textcomponents/itemtypes/entities%");
    }

    private Expression<?> object;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        this.object = exprs[0];
        if (parseResult.expr.contains("showing item")) {
            Skript.warning("'item' is no longer required and subject for removal.");
        }
        return true;
    }

    @SuppressWarnings({"NullableProblems", "UnstableApiUsage", "deprecation"})
    @Nullable
    @Override
    protected HoverEvent[] get(Event event) {
        if (this.object.isSingle() && this.object.getSingle(event) instanceof Entity entity) {
            Key key = entity.getType().key();
            ShowEntity showEntity = ShowEntity.showEntity(key, entity.getUniqueId());
            return new HoverEvent[]{HoverEvent.hoverEvent(Action.SHOW_ENTITY, showEntity)};
        } else if (this.object.isSingle() && this.object.getSingle(event) instanceof ItemType itemType) {
            Key key = itemType.getMaterial().key();
            int amount = itemType.getAmount();
            BinaryTagHolder nbt = BinaryTagHolder.binaryTagHolder(itemType.getItemMeta().getAsString());
            ShowItem showItem = ShowItem.of(key, amount, nbt);
            return new HoverEvent[]{HoverEvent.hoverEvent(Action.SHOW_ITEM, showItem)};
        } else {
            List<ComponentWrapper> components = new ArrayList<>();
            for (Object object : this.object.getArray(event)) {
                if (object instanceof String string) {
                    components.add(ComponentWrapper.fromText(string));
                } else if (object instanceof ComponentWrapper component) {
                    components.add(component);
                }
            }
            ComponentWrapper hover = ComponentWrapper.fromComponents(components.toArray(new ComponentWrapper[0]), "\n");
            return new HoverEvent[]{HoverEvent.hoverEvent(Action.SHOW_TEXT, hover.getComponent())};
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends HoverEvent> getReturnType() {
        return HoverEvent.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        return "hover event showing " + this.object.toString(e, d);
    }

}
