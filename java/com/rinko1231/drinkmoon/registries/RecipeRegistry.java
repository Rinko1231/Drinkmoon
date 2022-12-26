package com.rinko1231.drinkmoon.registries;


import com.rinko1231.drinkmoon.recipes.JarBrewingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeRegistry {
    public static class Type {

        public static final IRecipeType<JarBrewingRecipe> JARBREWING = IRecipeType.register("drinkbeer:jarbrewing");
    }

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "drinkbeer");

    public static final RegistryObject<IRecipeSerializer<JarBrewingRecipe>> JARBREWING = RECIPE_SERIALIZERS.register("jarbrewing", JarBrewingRecipe.Serializer::new);
}
