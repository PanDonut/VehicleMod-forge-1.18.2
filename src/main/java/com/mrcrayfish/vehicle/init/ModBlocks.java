package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModBlocks
{
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> TRAFFIC_CONE = register("traffic_cone", TrafficConeBlock::new);
    public static final RegistryObject<Block> FLUID_EXTRACTOR = register("fluid_extractor", FluidExtractorBlock::new);
    public static final RegistryObject<Block> FLUID_MIXER = register("fluid_mixer", FluidMixerBlock::new);
    public static final RegistryObject<Block> GAS_PUMP = register("gas_pump", GasPumpBlock::new);
    public static final RegistryObject<Block> FLUID_PIPE = register("fluid_pipe", FluidPipeBlock::new);
    public static final RegistryObject<Block> FLUID_PUMP = register("fluid_pump", FluidPumpBlock::new);
    public static final RegistryObject<FuelDrumBlock> FUEL_DRUM = register("fuel_drum", FuelDrumBlock::new);
    public static final RegistryObject<FuelDrumBlock> INDUSTRIAL_FUEL_DRUM = register("industrial_fuel_drum", IndustrialFuelDrumBlock::new);
    public static final RegistryObject<Block> WORKSTATION = register("workstation", WorkstationBlock::new);
    public static final RegistryObject<Block> VEHICLE_CRATE = register("vehicle_crate", VehicleCrateBlock::new);
    public static final RegistryObject<Block> JACK = register("jack", JackBlock::new);
    public static final RegistryObject<Block> JACK_HEAD = register("jack_head", JackHeadBlock::new);
    public static final RegistryObject<LiquidBlock> FUELIUM = register("fuelium", () -> new LiquidBlock(ModFluids.FLOWING_FUELIUM, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    public static final RegistryObject<LiquidBlock> ENDER_SAP = register("ender_sap", () -> new LiquidBlock(ModFluids.FLOWING_ENDER_SAP, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    public static final RegistryObject<LiquidBlock> BLAZE_JUICE = register("blaze_juice", () -> new LiquidBlock(ModFluids.FLOWING_BLAZE_JUICE, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    //public static final Block BOOST_PAD = registerConstructor(new BlockBoostPad(), null);
    //public static final Block BOOST_RAMP = registerConstructor(new BlockBoostRamp(), null); //ItemBoostRamp::new
    //public static final Block STEEP_BOOST_RAMP = registerConstructor(new BlockSteepBoostRamp(), null);

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> block)
    {
        return ModBlocks.REGISTER.register(id, block);
    }
}