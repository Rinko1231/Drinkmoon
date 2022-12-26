package com.rinko1231.drinkmoon.registries;

import lekavar.lma.drinkbeer.items.BeerMugItem;
import lekavar.lma.drinkbeer.utils.ModGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "drinkmoon");

    //beer
    public static final RegistryObject<Item> TEST_BEER_MUG = ITEMS.register("test_beer_mug", () -> new BeerMugItem(BlockRegistry.TEST_BEER_MUG.get(), 2, Effects.DIG_SPEED, 1200, true));

    public static final RegistryObject<Item> LIQUOR_JAR = ITEMS.register("liquor_jar", () -> new BlockItem(com.rinko1231.drinkmoon.registries.BlockRegistry.LIQUOR_JAR.get(), new Item.Properties().tab(ModGroup.GENERAL)));
}
