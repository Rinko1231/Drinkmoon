package com.rinko1231.drinkmoon.recipes;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rinko1231.drinkmoon.registries.RecipeRegistry;
import lekavar.lma.drinkbeer.recipes.IBrewingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;


//TODO 先重写完酒桶
public class JarBrewingRecipe implements IRecipe<IBrewingInventory> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> input;
    private final ItemStack cup;
    private final int brewingTime;
    private final ItemStack result;

    public JarBrewingRecipe(ResourceLocation id, NonNullList<Ingredient> input, ItemStack cup, int brewingTime, ItemStack result) {
        this.id = id;
        this.input = input;
        this.cup = cup;
        this.brewingTime = brewingTime;
        this.result = result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(input);
        return result;
    }

    @Deprecated
    public NonNullList<Ingredient> getIngredient(){
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(input);
        return result;
    }

    @Deprecated
    public ItemStack geBeerCup(){
        return cup.copy();
    }

    public ItemStack getBeerCup(){
        return cup.copy();
    }

    @Override
    public boolean matches(IBrewingInventory p_77569_1_, World p_77569_2_) {
        List<Ingredient> testTarget = Lists.newArrayList(input);
        List<ItemStack> tested = p_77569_1_.getIngredients();
        for (ItemStack itemStack : tested) {
            int i = getLatestMatched(testTarget, itemStack);
            if (i == -1) return false;
            else testTarget.remove(i);
        }
        return testTarget.isEmpty();
    }

    private int getLatestMatched(List<Ingredient> testTarget, ItemStack tested) {
        for (int i = 0; i < testTarget.size(); i++) {
            if (testTarget.get(i).test(tested)) return i;
        }
        return -1;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(IBrewingInventory inventory) {
        return result.copy();
    }

    // Can Craft at any dimension
    @Override
    public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
        return true;
    }


    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book).
     * If your recipe has more than one possible result (e.g. it's dynamic and depends on its inputs),
     * then return an empty stack.
     */
    @Override
    public ItemStack getResultItem() {
        //For Safety, I use #copy
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeRegistry.JARBREWING.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return RecipeRegistry.Type.JARBREWING;
    }

    public int getRequiredCupCount() {
        return cup.getCount();
    }

    public boolean isCupQualified(IBrewingInventory inventory) {
        return inventory.getCup().getItem() == cup.getItem() && inventory.getCup().getCount() >= cup.getCount();
    }

    public int getBrewingTime() {
        return brewingTime;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<JarBrewingRecipe> {

        @Override
        public JarBrewingRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            NonNullList<Ingredient> ingredients = itemsFromJson(JSONUtils.getAsJsonArray(jsonObject, "ingredients"));
            ItemStack cup = CraftingHelper.getItemStack(JSONUtils.getAsJsonObject(jsonObject, "cup"), true);
            int brewing_time = JSONUtils.getAsInt(jsonObject, "brewing_time");
            ItemStack result = CraftingHelper.getItemStack(JSONUtils.getAsJsonObject(jsonObject, "result"), true);
            return new JarBrewingRecipe(resourceLocation, ingredients, cup, brewing_time, result);
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonArray) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < jsonArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i));
                ingredients.add(ingredient);
            }
            return ingredients;
        }

        @Nullable
        @Override
        public JarBrewingRecipe fromNetwork(ResourceLocation resourceLocation, PacketBuffer packetBuffer) {
            int i = packetBuffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            for (int j = 0; j < ingredients.size(); ++j) {
                ingredients.set(j, Ingredient.fromNetwork(packetBuffer));
            }
            ItemStack cup = packetBuffer.readItem();
            int brewingTime = packetBuffer.readVarInt();
            ItemStack result = packetBuffer.readItem();
            return new JarBrewingRecipe(resourceLocation, ingredients, cup, brewingTime, result);
        }

        @Override
        public void toNetwork(PacketBuffer packetBuffer, JarBrewingRecipe brewingRecipe) {
            packetBuffer.writeVarInt(brewingRecipe.input.size());
            for (Ingredient ingredient : brewingRecipe.input) {
                ingredient.toNetwork(packetBuffer);
            }
            packetBuffer.writeItem(brewingRecipe.cup);
            packetBuffer.writeVarInt(brewingRecipe.brewingTime);
            packetBuffer.writeItem(brewingRecipe.result);

        }
    }
}
