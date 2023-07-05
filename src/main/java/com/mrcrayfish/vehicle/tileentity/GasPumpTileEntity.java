package com.mrcrayfish.vehicle.tileentity;

import com.mrcrayfish.framework_embedded.common.data.SyncedEntityData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.HermiteInterpolator;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.util.TileEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class GasPumpTileEntity extends TileEntitySynced
{
    private int fuelingEntityId;
    private Player fuelingEntity;

    private HermiteInterpolator cachedSpline;
    private boolean recentlyUsed;

    public GasPumpTileEntity(BlockPos pos, BlockState state)
    {
        super(ModTileEntities.GAS_PUMP.get(), pos, state);
    }

    public static void onServerTick(Level level, BlockPos pos, BlockState state, GasPumpTileEntity entity)
    {
        entity.onServerTick();
    }

    public static void onClientTick(Level level, BlockPos pos, BlockState state, GasPumpTileEntity entity)
    {
        entity.onClientTick();
    }

    public HermiteInterpolator getCachedSpline()
    {
        return cachedSpline;
    }

    public void setCachedSpline(HermiteInterpolator cachedSpline)
    {
        this.cachedSpline = cachedSpline;
    }

    public boolean isRecentlyUsed()
    {
        return recentlyUsed;
    }

    public void setRecentlyUsed(boolean recentlyUsed)
    {
        this.recentlyUsed = recentlyUsed;
    }

    @Nullable
    public FluidTank getTank()
    {
        BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.below());
        if(tileEntity instanceof GasPumpTankTileEntity)
        {
            return ((GasPumpTankTileEntity) tileEntity).getFluidTank();
        }
        return null;
    }

    public Player getFuelingEntity()
    {
        return this.fuelingEntity;
    }

    public void setFuelingEntity(@Nullable Player entity)
    {
        if(!this.level.isClientSide)
        {
            if(this.fuelingEntity != null)
            {
                SyncedEntityData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
            }
            this.fuelingEntity = null;
            this.fuelingEntityId = -1;
            if(entity != null)
            {
                this.fuelingEntityId = entity.getId();
                SyncedEntityData.instance().set(entity, ModDataKeys.GAS_PUMP, Optional.of(this.getBlockPos()));
            }
            this.syncToClient();
        }
    }

    public void onServerTick()
    {
        if(this.fuelingEntityId != -1)
        {
            if(this.fuelingEntity == null)
            {
                Entity entity = this.level.getEntity(this.fuelingEntityId);
                if(entity instanceof Player)
                {
                    this.fuelingEntity = (Player) entity;
                }
                else if(!this.level.isClientSide)
                {
                    this.fuelingEntityId = -1;
                    this.syncFuelingEntity();
                }
            }
        }

        if(this.fuelingEntity != null)
        {
            if(Math.sqrt(this.fuelingEntity.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5)) > Config.SERVER.maxHoseDistance.get() || !this.fuelingEntity.isAlive())
            {
                if(this.fuelingEntity.isAlive())
                {
                    this.level.playSound(null, this.fuelingEntity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                SyncedEntityData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
                this.fuelingEntityId = -1;
                this.fuelingEntity = null;
                this.syncFuelingEntity();
            }
        }
    }

    public void onClientTick()
    {
        this.onServerTick();

        if(this.fuelingEntity != null)
        {
            this.fuelingEntity = null;
        }
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);

        if(compound.contains("FuelingEntity", Tag.TAG_INT))
        {
            this.fuelingEntityId = compound.getInt("FuelingEntity");
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.putInt("FuelingEntity", this.fuelingEntityId);
    }

    private void syncFuelingEntity()
    {
        CompoundTag compound = new CompoundTag();
        this.saveAdditional(compound);
        TileEntityUtil.sendUpdatePacket(this, compound);
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }
}
