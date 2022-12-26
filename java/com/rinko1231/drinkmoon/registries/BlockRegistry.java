package com.rinko1231.drinkmoon.registries;

import lekavar.lma.drinkbeer.blocks.*;
import com.rinko1231.drinkmoon.blocks.LiquorJarBlock;
import lekavar.lma.drinkbeer.utils.ModGroup;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "drinkbeer");

    public static final RegistryObject<Block> LIQUOR_JAR = BLOCKS.register("liquor_jar", LiquorJarBlock::new);
    //beer
    public static final RegistryObject<Block> TEST_BEER_MUG = BLOCKS.register("test_beer_mug", BeerMugBlock::new);


}
