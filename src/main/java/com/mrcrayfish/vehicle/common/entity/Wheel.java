package com.mrcrayfish.vehicle.common.entity;

import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class Wheel
{
    public static final Vec3 DEFAULT_OFFSET = Vec3.ZERO;
    public static final Vec3 DEFAULT_SCALE = new Vec3(1, 1, 1);
    public static final float DEFAULT_WIDTH = 4.0F;
    public static final Side DEFAULT_SIDE = Side.NONE;
    public static final Position DEFAULT_POSITION = Position.NONE;
    public static final boolean DEFAULT_AUTO_SCALE = false;
    public static final boolean DEFAULT_PARTICLES = false;
    public static final boolean DEFAULT_RENDER = true;

    private final Vec3 offset;
    private final Vec3 scale;
    private final float width;
    private final Side side;
    private final Position position;
    private final boolean autoScale;
    private final boolean particles;
    private final boolean render;

    protected Wheel(Vec3 offset, Vec3 scale, float width, Side side, Position position, boolean autoScale, boolean particles, boolean render)
    {
        this.offset = offset;
        this.scale = scale;
        this.width = width;
        this.side = side;
        this.position = position;
        this.autoScale = autoScale;
        this.particles = particles;
        this.render = render;
    }

    public Vec3 getOffset()
    {
        return this.offset;
    }

    public Vec3 getScale()
    {
        return this.scale;
    }

    public float getOffsetX()
    {
        return (float) this.offset.x;
    }

    public float getOffsetY()
    {
        return (float) this.offset.y;
    }

    public float getOffsetZ()
    {
        return (float) this.offset.z;
    }

    public float getWidth()
    {
        return width;
    }

    public float getScaleX()
    {
        return (float) this.scale.x;
    }

    public float getScaleY()
    {
        return (float) this.scale.y;
    }

    public float getScaleZ()
    {
        return (float) this.scale.z;
    }

    public Side getSide()
    {
        return side;
    }

    public Position getPosition()
    {
        return position;
    }

    /**
     * Indicates that the wheel scale is to be generated. This is only used when loading vehicle
     * properties and has no other significant use.
     *
     * @return true if the wheel auto scaled
     */
    public boolean isAutoScale()
    {
        return this.autoScale;
    }

    /**
     * Determines if this wheels should spawn particles. Depending on the drivetrain of a vehicle,
     * the spawning of particles can be disabled. For instance, a rear wheel drive vehicle will only
     * spawn particles for the rear wheels as that's where the force to push the vehicle comes from.
     * It should be noted that there is no system in place that determines the drivetrain of a vehicle
     * and the spawning of particles is specified when adding wheels.
     *
     * @return if the wheel should spawn particles
     */
    public boolean shouldSpawnParticles()
    {
        return particles;
    }

    /**
     * Determines if this wheel should render. Some vehicles have wheels that are manually rendered
     * due the fact they need extra tranformations and rotations, and therefore shouldn't use the
     * wheel system and rather just be a placeholder.
     *
     * @return if the wheel should be rendered
     */
    public boolean shouldRender()
    {
        return render;
    }

    public Wheel rescale(Vec3 newScale)
    {
        return new Wheel(this.offset, newScale, this.width, this.side, this.position, this.autoScale, this.particles, this.render);
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        ExtraJSONUtils.write(object, "side", this.side, DEFAULT_SIDE);
        ExtraJSONUtils.write(object, "axle", this.position, DEFAULT_POSITION);
        ExtraJSONUtils.write(object, "offset", this.offset, DEFAULT_OFFSET);
        ExtraJSONUtils.write(object, "scale", this.scale, DEFAULT_SCALE);
        ExtraJSONUtils.write(object, "autoScale", this.autoScale, DEFAULT_AUTO_SCALE);
        ExtraJSONUtils.write(object, "particles", this.particles, DEFAULT_PARTICLES);
        ExtraJSONUtils.write(object, "render", this.render, DEFAULT_RENDER);
        return object;
    }

    public static Wheel fromJsonObject(JsonObject object)
    {
        Vec3 offset = ExtraJSONUtils.getAsVec3(object, "offset", DEFAULT_OFFSET);
        Vec3 scale = ExtraJSONUtils.getAsVec3(object, "scale", DEFAULT_SCALE);
        Wheel.Side side = ExtraJSONUtils.getAsEnum(object, "side", Wheel.Side.class, DEFAULT_SIDE);
        Wheel.Position position = ExtraJSONUtils.getAsEnum(object, "axle", Wheel.Position.class, DEFAULT_POSITION);
        boolean autoScale = GsonHelper.getAsBoolean(object, "autoScale", DEFAULT_AUTO_SCALE);
        boolean particles = GsonHelper.getAsBoolean(object, "particles", DEFAULT_PARTICLES);
        boolean render = GsonHelper.getAsBoolean(object, "render", DEFAULT_RENDER);
        return new Wheel(offset, scale, 4.0F, side, position, autoScale, particles, render);
    }

    public enum Side
    {
        LEFT(-1), RIGHT(1), NONE(1);

        int offset;

        Side(int offset)
        {
            this.offset = offset;
        }

        public int getOffset()
        {
            return offset;
        }
    }

    public enum Position
    {
        FRONT, REAR, NONE
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private Vec3 offset = DEFAULT_OFFSET;
        private Vec3 scale = DEFAULT_SCALE;
        private float width = DEFAULT_WIDTH;
        private Side side = DEFAULT_SIDE;
        private Position position = DEFAULT_POSITION;
        private boolean autoScale = DEFAULT_AUTO_SCALE;
        private boolean particles = DEFAULT_PARTICLES;
        private boolean render = DEFAULT_RENDER;

        public Builder setOffset(double x, double y, double z)
        {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Builder setScale(double scale)
        {
            this.scale = new Vec3(scale, scale, scale);
            return this;
        }

        public Builder setScale(double scaleX, double scaleY, double scaleZ)
        {
            this.scale = new Vec3(scaleX, scaleY, scaleZ);
            return this;
        }

        public Builder setWidth(float width)
        {
            this.width = width;
            return this;
        }

        public Builder setSide(Side side)
        {
            this.side = side;
            return this;
        }

        public Builder setPosition(Position position)
        {
            this.position = position;
            return this;
        }

        public Builder setAutoScale(boolean autoScale)
        {
            this.autoScale = autoScale;
            return this;
        }

        public Builder setParticles(boolean particles)
        {
            this.particles = particles;
            return this;
        }

        public Builder setRender(boolean render)
        {
            this.render = render;
            return this;
        }

        public Wheel build()
        {
            return new Wheel(this.offset, this.scale, this.width, this.side, this.position, this.autoScale, this.particles, this.render);
        }
    }
}
