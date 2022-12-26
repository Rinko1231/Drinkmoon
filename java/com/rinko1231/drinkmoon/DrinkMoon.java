package com.rinko1231.drinkmoon;

import com.rinko1231.drinkmoon.registries.ItemRegistry;
import com.rinko1231.drinkmoon.registries.BlockRegistry;

import com.rinko1231.drinkmoon.registries.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
@Mod(Utils.MODID)
public class DrinkMoon {

    public DrinkMoon(){

        ItemRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());


        BlockRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());

        TileEntityRegistry.TILE_ENTITY.register(FMLJavaModLoadingContext.get().getModEventBus());

        ContainerTypeRegistry.CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RecipeRegistry.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }


}
