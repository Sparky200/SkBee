package com.shanebeestudios.skbee.elements.other.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Name("Nearest Entity")
@Description({"Returns the nearest entity around a location/entity.",
        "\nNOTE: When using `around entity`, this will exclude that entity in the search."})
@Examples({"kill nearest player in radius 10 around player",
        "damage nearest mob in radius 5 around player",
        "set {_near} to nearest entity in radius 50 around {_loc}",
        "set {_near} to nearest entity in radius 100 around location(100,100,100,world \"world\")",
        "teleport player to nearest player in radius 10 around player",
        "damage 10 nearest entity in radius 10 around player by 2"})
@Since("2.7.2")
@SuppressWarnings("NullableProblems")
public class ExprNearestEntity extends SimpleExpression<Entity> {

    static {
        Skript.registerExpression(ExprNearestEntity.class, Entity.class, ExpressionType.COMBINED,
                "[num:%number%] nearest %entitydata% in radius %number% (of|around) %location/entity%");
    }

    private Expression<Number> number;
    private Expression<EntityData<?>> entityData;
    private Expression<Number> radius;
    private Expression<Object> location;
    private boolean isSingle;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.number = (Expression<Number>) exprs[0];
        this.entityData = (Expression<EntityData<?>>) exprs[1];
        this.radius = (Expression<Number>) exprs[2];
        this.location = (Expression<Object>) exprs[3];
        this.isSingle = !parseResult.hasTag("num");
        return true;
    }

    @Override
    protected @Nullable Entity[] get(Event event) {
        Number number = this.number.getSingle(event);
        EntityData<?> entityData = this.entityData.getSingle(event);
        Number radius = this.radius.getSingle(event);
        Object object = this.location.getSingle(event);
        if (number == null || entityData == null || radius == null || object == null) return null;
        double rad = radius.doubleValue();

        List<Entity> nearby;
        if (object instanceof Entity entity) {
            nearby = getNearby(entity.getLocation(), entityData, rad, entity);
        } else {
            nearby = getNearby((Location) object, entityData, rad, null);
        }

        List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < number.intValue(); i++) {
            if (i < nearby.size()) {
                entities.add(nearby.get(i));
            }
        }
        return entities.toArray(new Entity[0]);
    }

    private List<Entity> getNearby(Location location, EntityData<?> entityData, double radius, Object exclude) {
        World world = location.getWorld();
        return world.getNearbyEntitiesByType(entityData.getType(), location, radius)
                .stream()
                .sorted(Comparator.comparing(entity -> entity.getLocation().distanceSquared(location)))
                .filter(entity -> entity != exclude)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSingle() {
        return this.isSingle;
    }

    @Override
    public @NotNull Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        String num = this.number != null ? this.number.toString(e, d) + " " : "";
        String data = this.entityData.toString(e, d);
        String radius = this.radius.toString(e, d);
        String loc = this.location.toString(e, d);
        return num + "nearest " + data + " in radius " + radius + " around " + loc;
    }

}