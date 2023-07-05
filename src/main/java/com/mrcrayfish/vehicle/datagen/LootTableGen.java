package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.world.storage.loot.functions.CopyFluidTanks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class LootTableGen extends LootTableProvider
{
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(Pair.of(BlockProvider::new, LootContextParamSets.BLOCK));

    public LootTableGen(DataGenerator generator)
    {
        super(generator);
    }

    @Override
    protected void validate(@NotNull Map<ResourceLocation, LootTable> map, @NotNull ValidationContext ctx)
    {}

    @Override
    @NotNull
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        return this.tables;
    }

    private static class BlockProvider extends BlockLoot
    {
        @Override
        protected void addTables()
        {
            this.add(ModBlocks.FLUID_EXTRACTOR.get(), BlockProvider::createFluidTankDrop);
            this.add(ModBlocks.FLUID_MIXER.get(), BlockProvider::createFluidTankDrop);
            this.add(ModBlocks.FUEL_DRUM.get(), BlockProvider::createFluidTankDrop);
            this.add(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), BlockProvider::createFluidTankDrop);
            this.dropSelf(ModBlocks.FLUID_PIPE.get());
            this.dropSelf(ModBlocks.FLUID_PUMP.get());
            this.dropSelf(ModBlocks.GAS_PUMP.get());
            this.dropSelf(ModBlocks.TRAFFIC_CONE.get());
            this.dropSelf(ModBlocks.WORKSTATION.get());
            this.dropSelf(ModBlocks.WORKSTATION.get());
            this.dropSelf(ModBlocks.JACK.get());
            this.dropSelf(ModBlocks.JACK_HEAD.get());
            this.add(ModBlocks.VEHICLE_CRATE.get(), BlockProvider::createVehicleCrateDrop);
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block.getRegistryName() != null && Reference.MOD_ID.equals(block.getRegistryName().getNamespace())).collect(Collectors.toSet());
        }

        protected static LootTable.Builder createFluidTankDrop(Block block)
        {
            return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block).apply(CopyFluidTanks.copyFluidTanks()))));
        }

        protected static LootTable.Builder createVehicleCrateDrop(Block block)
        {
            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(
                            block,
                            LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(block)
                                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                                    .copy("vehicle", "BlockEntityTag.vehicle")
                                                    .copy("color", "BlockEntityTag.color")
                                                    .copy("engineStack", "BlockEntityTag.engineStack")
                                                    .copy("creative", "BlockEntityTag.creative")
                                                    .copy("wheelStack", "BlockEntityTag.wheelStack")
                                            )
                                    )
                    ));
        }
    }
}
