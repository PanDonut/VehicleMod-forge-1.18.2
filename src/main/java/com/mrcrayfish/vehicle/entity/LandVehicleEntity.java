package com.mrcrayfish.vehicle.entity;

import com.mrcrayfish.vehicle.common.SurfaceHelper;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.common.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.LandProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class LandVehicleEntity extends PoweredVehicleEntity
{
    protected Vec3 velocity = Vec3.ZERO;
    protected float traction;

    @OnlyIn(Dist.CLIENT)
    protected float frontWheelRotationSpeed;
    @OnlyIn(Dist.CLIENT)
    protected float frontWheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected float prevFrontWheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected float rearWheelRotationSpeed;
    @OnlyIn(Dist.CLIENT)
    protected float rearWheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected float prevRearWheelRotation;
    @OnlyIn(Dist.CLIENT)
    protected int wheelieCount;
    @OnlyIn(Dist.CLIENT)
    protected int prevWheelieCount;

    public LandVehicleEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    @Override
    public void onVehicleTick()
    {
        boolean oldCharging = this.charging;
        this.charging = this.canCharge() && this.velocity.length() < 5.0 && this.isHandbraking() && this.getThrottle() > 0;
        if(oldCharging && !this.charging && this.chargingAmount > 0F)
        {
            this.releaseCharge(this.chargingAmount);
        }
    }

    @Override
    public void onClientUpdate()
    {
        super.onClientUpdate();

        this.prevFrontWheelRotation = this.frontWheelRotation;
        this.prevRearWheelRotation = this.rearWheelRotation;
        this.prevWheelieCount = this.wheelieCount;

        if(this.isBoosting() && this.getControllingPassenger() != null)
        {
            if(this.wheelieCount < MAX_WHEELIE_TICKS)
            {
                this.wheelieCount++;
            }
        }
        else if(this.wheelieCount > 0)
        {
            this.wheelieCount--;
        }
    }

    @Override
    public void updateVehicleMotion()
    {
        this.motion = Vec3.ZERO;

        VehicleProperties properties = this.getProperties();

        // Gets the forward vector of the vehicle
        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());

        // Calculates the distance between the front and rear axel
        Transform bodyPosition = properties.getBodyTransform();

        // Performs the charging motion
        if(this.charging)
        {
            float speed = 0.1F;
            float steeringAngle = this.getSteeringAngle();
            Vec3 frontWheel = forward.scale((bodyPosition.getZ() + this.getFrontAxleOffset().z) * 0.0625 * bodyPosition.getScale());
            Vec3 nextPosition = frontWheel.subtract(frontWheel.yRot((float) Math.toRadians(steeringAngle)));
            Vec3 nextMovement = Vec3.ZERO.vectorTo(nextPosition).scale(speed);
            this.motion = this.motion.add(nextMovement);
            this.yRot -= (steeringAngle * speed);
            float forwardForce = Mth.clamp(this.getThrottle(), -1.0F, 1.0F);
            forwardForce *= this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
            this.chargingAmount = Mth.clamp(this.chargingAmount + forwardForce * 0.025F, 0.0F, 1.0F);
        }
        else
        {
            this.chargingAmount = 0F;
        }

        float friction = SurfaceHelper.getFriction(this);
        float enginePower = this.isOnGround() ? this.getEnginePower() : 0F;
        float brakePower = this.isOnGround() ? this.getBrakePower() : 0F;
        float drag = 0.001F;

        // TODO a lot of this can be broken up into methods
        // Updates the acceleration, applies drag and friction, then adds to the velocity
        float throttle = this.isHandbraking() || this.charging ? 0F : this.getThrottle();
        float forwardForce = enginePower * Mth.clamp(throttle, -1.0F, 1.0F);
        forwardForce *= this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
        if(this.isBoosting()) forwardForce += forwardForce * this.getSpeedMultiplier();
        if(this.getThrottle() < 0) forwardForce *= 0.4F;
        Vec3 acceleration = forward.scale(forwardForce).scale(0.05);
        if(this.velocity.length() < 0.05) this.velocity = Vec3.ZERO;
        Vec3 handbrakeForce = this.velocity.scale(this.isHandbraking() ? brakePower : 0F).scale(0.05);
        Vec3 frictionForce = this.velocity.scale(-friction).scale(0.05);
        Vec3 dragForce = this.velocity.scale(this.velocity.length()).scale(-drag).scale(0.05);
        acceleration = acceleration.add(dragForce).add(frictionForce).add(handbrakeForce);
        this.velocity = this.velocity.add(acceleration);

        // Clamps the speed based on the global speed limit
        this.velocity = CommonUtils.clampSpeed(this.velocity);

        if(this.isSliding() && this.getThrottle() > 0)
        {
            this.traction = this.getWheelType().map(IWheelType::getSlideTraction).orElse(1.0F);
        }
        else if(this.isHandbraking())
        {
            this.traction = 0.05F;
        }
        else
        {
            float wheelTraction = this.getWheelType().map(IWheelType::getBaseTraction).orElse(1.0F);
            float targetTraction = acceleration.length() > 0 ? (float) (wheelTraction * Mth.clamp((this.velocity.length() / acceleration.length()), 0.0F, 1.0F)) : wheelTraction;
            float side = this.canSlide() ? Mth.clamp(1.0F - (float) this.velocity.normalize().cross(forward.normalize()).length() / 0.3F, 0.0F, 1.0F) : 1.0F;
            this.traction = this.traction + (targetTraction - this.traction) * side * 0.15F;
        }

        //TODO test with steering at the rear
        //Gets the new position of the wheels
        double frontAxleOffset = (bodyPosition.getZ() + this.getFrontAxleOffset().z) * 0.0625 * bodyPosition.getScale();
        double rearAxleOffset = (bodyPosition.getZ() + this.getRearAxleOffset().z) * 0.0625 * bodyPosition.getScale();
        Vec3 worldFrontWheel = this.position().add(forward.scale(frontAxleOffset));
        Vec3 worldRearWheel = this.position().add(forward.scale(rearAxleOffset));
        worldFrontWheel = worldFrontWheel.add(this.velocity.yRot((float) Math.toRadians(this.getSteeringAngle())).scale(0.05));
        worldRearWheel = worldRearWheel.add(this.velocity.scale(0.05));

        //Updates the delta movement based on the new wheel positions
        Vec3 heading = worldFrontWheel.subtract(worldRearWheel).normalize();
        Vec3 nextPosition = worldRearWheel.add(heading.scale(-rearAxleOffset));
        Vec3 nextMovement = nextPosition.subtract(this.position());
        this.motion = this.motion.add(nextMovement);

        // Updates the velocity based on the heading
        float surfaceTraction = SurfaceHelper.getSurfaceTraction(this, this.traction);
        if(heading.dot(this.velocity.normalize()) > 0)
        {
            this.velocity = CommonUtils.lerp(this.velocity, heading.scale(this.velocity.length()), surfaceTraction);
        }
        else
        {
            Vec3 reverse = heading.scale(-1).scale(Math.min(this.velocity.length(), this.getMaxReverseSpeed()));
            this.velocity = CommonUtils.lerp(this.velocity, reverse, surfaceTraction);
        }

        // Calculates the difference from the old yaw to the new yaw
        if(!this.charging)
        {
            float vehicleDeltaYaw = CommonUtils.yaw(forward) - CommonUtils.yaw(heading);
            vehicleDeltaYaw = Mth.wrapDegrees(vehicleDeltaYaw);

            this.yRot -= vehicleDeltaYaw;
        }

        // Add gravity
        this.setDeltaMovement(this.getDeltaMovement().add(new Vec3(0, -0.08, 0)));
    }

    @Override
    protected void updateTurning()
    {
        if(this.level.isClientSide())
        {
            float targetAngle = !this.charging && this.isSliding() ? -Mth.clamp(this.getSteeringAngle() * 2, -this.getMaxSteeringAngle(), this.getMaxSteeringAngle()) : this.getSteeringAngle();
            this.renderWheelAngle = this.renderWheelAngle + (targetAngle - this.renderWheelAngle) * 0.3F;
        }
    }

    @Override
    public void createParticles()
    {
        if(this.canDrive())
        {
            super.createParticles();
        }
    }

    public boolean isRearWheelSteering()
    {
        return this.getFrontAxleOffset().z < this.getRearAxleOffset().z;
    }

    protected final boolean canCharge()
    {
        return this.getLandProperties().canCharge();
    }

    public final float getBrakePower()
    {
        return this.getLandProperties().getBrakePower();
    }

    public final float getMaxReverseSpeed()
    {
        return this.getLandProperties().getMaxReverseSpeed();
    }

    public final boolean canWheelie()
    {
        return this.getLandProperties().canWheelie();
    }

    protected final LandProperties getLandProperties()
    {
        return this.getProperties().getExtended(LandProperties.class);
    }

    public float getTraction()
    {
        return this.traction;
    }

    public Vec3 getVelocity()
    {
        return this.velocity;
    }

    public boolean canSlide()
    {
        return this.getLandProperties().canSlide();
    }

    public boolean isSliding()
    {
        if(this.canSlide())
        {
            Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
            return this.velocity.normalize().cross(forward.normalize()).length() >= 0.3;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateEngineSound()
    {
        super.updateEngineSound();

        if(this.isSliding() && this.getThrottle() > 0 && !this.isHandbraking() || this.isBoosting())
        {
            this.enginePitch = this.getMinEnginePitch() + (this.getMaxEnginePitch() - this.getMinEnginePitch()) * this.getThrottle();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void updateWheelRotations()
    {
        VehicleProperties properties = this.getProperties();
        double wheelCircumference = 24.0;
        double vehicleScale = properties.getBodyTransform().getScale();
        Vec3 forward = Vec3.directionFromRotation(this.getRotationVector());
        double direction = forward.dot(this.motion.normalize());

        if(this.isOnGround() || this.getThrottle() != 0)
        {
            this.rearWheelRotationSpeed = (float) (this.motion.length() * direction * 20);
        }
        else
        {
            this.rearWheelRotationSpeed *= 0.9;
        }

        if(this.isOnGround())
        {
            this.frontWheelRotationSpeed = (float) (this.motion.length() * direction * 20);
        }
        else
        {
            this.frontWheelRotationSpeed *= 0.9;
        }

        Wheel frontWheel = properties.getFirstFrontWheel();
        if(frontWheel != null && !this.charging)
        {
            double frontWheelCircumference = wheelCircumference * vehicleScale * frontWheel.getScaleY();
            double rotation = (this.frontWheelRotationSpeed * 16) / frontWheelCircumference;
            this.frontWheelRotation -= rotation * 20F;
        }

        if(this.isHandbraking() && !this.charging)
            return;

        if(this.charging)
        {
            this.rearWheelRotationSpeed = this.getEnginePower() * this.chargingAmount;
        }

        Wheel rearWheel = properties.getFirstRearWheel();
        if(rearWheel != null)
        {
            double rearWheelCircumference = wheelCircumference * vehicleScale * rearWheel.getScaleY();
            double rotation = (this.rearWheelRotationSpeed * 16) / rearWheelCircumference;
            this.rearWheelRotation -= rotation * 20F;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getFrontWheelRotation(float partialTicks)
    {
        return this.prevFrontWheelRotation + (this.frontWheelRotation - this.prevFrontWheelRotation) * partialTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public float getRearWheelRotation(float partialTicks)
    {
        return this.prevRearWheelRotation + (this.rearWheelRotation - this.prevRearWheelRotation) * partialTicks;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getWheelRotation(@Nullable Wheel wheel, float partialTicks)
    {
        if(wheel != null && wheel.getPosition() == Wheel.Position.REAR)
        {
            return this.getRearWheelRotation(partialTicks);
        }
        return this.getFrontWheelRotation(partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getWheelieProgress(float partialTicks)
    {
        float p = Mth.lerp(partialTicks, this.prevWheelieCount, this.wheelieCount) / (float) MAX_WHEELIE_TICKS;
        return 1.0F - (1.0F - p) * (1.0F - p);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        super.writeSpawnData(buffer);
        buffer.writeFloat(this.traction);
        buffer.writeDouble(this.velocity.x);
        buffer.writeDouble(this.velocity.y);
        buffer.writeDouble(this.velocity.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        super.readSpawnData(buffer);
        this.traction = buffer.readFloat();
        this.velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putFloat("traction", this.traction);
        CompoundTag velocity = new CompoundTag();
        velocity.putDouble("x", this.velocity.x);
        velocity.putDouble("y", this.velocity.y);
        velocity.putDouble("z", this.velocity.z);
        compound.put("velocity", velocity);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        this.traction = compound.getFloat("traction");
        CompoundTag velocity = compound.getCompound("velocity");
        this.velocity = new Vec3(velocity.getDouble("x"), velocity.getDouble("y"), velocity.getDouble("z"));
    }

    @Override
    protected boolean showTyreSmokeParticles()
    {
        return this.isSliding() && !this.isHandbraking() || super.showTyreSmokeParticles();
    }
}
