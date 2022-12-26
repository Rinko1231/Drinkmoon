package com.rinko1231.drinkmoon.tileentities;


import com.rinko1231.drinkmoon.gui.LiquorJarContainer;

import com.rinko1231.drinkmoon.recipes.JarBrewingRecipe;
import lekavar.lma.drinkbeer.recipes.IBrewingInventory;
import com.rinko1231.drinkmoon.registries.RecipeRegistry;
import com.rinko1231.drinkmoon.registries.TileEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class LiquorJarTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IBrewingInventory {
    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    // This int will not only indicate remainingBrewTime, but also represent Standard Brewing Time if valid in "waiting for ingredients" stage
    private int remainingBrewTime;
    // 0 - waiting for ingredient, 1 - brewing, 2 - waiting for pickup product
    private int statusCode;
    public final IIntArray syncData = new IIntArray() {
        @Override
        public int get(int p_221476_1_) {
            switch (p_221476_1_) {
                case 0:
                    return remainingBrewTime;
                case 1:
                    return statusCode;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int p_221477_1_, int p_221477_2_) {
            switch (p_221477_1_) {
                case 0:
                    remainingBrewTime = p_221477_2_;
                    break;
                case 1:
                    statusCode = p_221477_2_;
                    break;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public LiquorJarTileEntity() {
        super(TileEntityRegistry.LIQUOR_JAR_TILEENTITY.get());
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            // waiting for ingredient
            if (statusCode == 0) {
                // ingredient slots must have no empty slot
                if (!getIngredients().contains(ItemStack.EMPTY)) {
                    // Try match Recipe
                    JarBrewingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.Type.JARBREWING, this, this.level).orElse(null);
                    if (canBrew(recipe)) {
                        // Show Standard Brewing Time & Result
                        showPreview(recipe);
                        // Check Weather have enough cup.
                        if (hasEnoughEmptyCap(recipe)) {
                            startBrewing(recipe);

                        }
                    }
                    // Time remainingBrewTime will be reset since it also represents Standard Brewing Time if valid in this stage
                    else {
                        clearPreview();
                    }
                } else {
                    clearPreview();
                }
            }
            // brewing
            else if (statusCode == 1) {
                if (remainingBrewTime > 0) {
                    remainingBrewTime--;
                }
                // Enter "waiting for pickup"
                else {
                    // Prevent wired glitch such as remainingTime been set to one;
                    remainingBrewTime = 0;
                    // Enter Next Stage
                    statusCode = 2;
                }
                setChanged();
            }
            // waiting for pickup
            else if (statusCode == 2) {
                // Reset Stage to 0 (waiting for ingredients) after pickup Item
                if (items.get(5).isEmpty()) {
                    statusCode = 0;
                    setChanged();
                }
            }
            // Error status reset
            else {
                remainingBrewTime = 0;
                statusCode = 0;
                setChanged();
            }
        }
    }


    private boolean canBrew(@Nullable JarBrewingRecipe recipe) {
        if (recipe != null) {
            return recipe.matches(this, this.level);
        } else {
            return false;
        }
    }

    private boolean hasEnoughEmptyCap(JarBrewingRecipe recipe) {
        return recipe.isCupQualified(this);
    }

    private void startBrewing(JarBrewingRecipe recipe) {
        // Pre-set bear to output Slot
        // This Step must be done first
        items.set(5, recipe.assemble(this));
        // Consume Ingredient & Cup;
        for (int i = 0; i < 4; i++) {
            ItemStack ingred = items.get(i);
            if (isBucket(ingred)) items.set(i, Items.BUCKET.getDefaultInstance());
            else ingred.shrink(1);
        }
        items.get(4).shrink(recipe.getRequiredCupCount());
        // Set Remaining Time;
        remainingBrewTime = recipe.getBrewingTime();
        // Change Status Code to 1 (brewing)
        statusCode = 1;

        setChanged();
    }

    private boolean isBucket(ItemStack itemStack) {
        return itemStack.getItem() instanceof BucketItem || itemStack.getItem() instanceof MilkBucketItem;
    }

    private void clearPreview() {
        items.set(5, ItemStack.EMPTY);
        remainingBrewTime = 0;
        setChanged();
    }

    private void showPreview(JarBrewingRecipe recipe) {
        items.set(5, recipe.assemble(this));
        remainingBrewTime = recipe.getBrewingTime();
        setChanged();
    }


    @Nonnull
    @Override
    public List<ItemStack> getIngredients() {
        NonNullList<ItemStack> sample = NonNullList.withSize(4, ItemStack.EMPTY);
        for (int i = 0; i < 4; i++) {
            sample.set(i, items.get(i).copy());
        }
        return sample;
    }

    @Nonnull
    @Override
    public ItemStack getCup() {
        return items.get(4).copy();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        ItemStackHelper.saveAllItems(tag, this.items);
        tag.putShort("RemainingBrewTime", (short) this.remainingBrewTime);
        tag.putShort("statusCode", (short) this.statusCode);
        super.save(tag);
        return tag;
    }

    @Override
    public void load(BlockState state, @Nonnull CompoundNBT tag) {
        super.load(state, tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, this.items);
        this.remainingBrewTime = tag.getShort("RemainingBrewTime");
        this.statusCode = tag.getShort("statusCode");
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.drinkbeer.liquor_jar");
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new LiquorJarContainer(id, this, syncData, inventory, this);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(level.getBlockState(pkt.getPos()), pkt.getTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = super.getUpdateTag();
        ItemStackHelper.saveAllItems(tag, this.items);
        tag.putShort("RemainingBrewTime", (short) this.remainingBrewTime);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        ItemStackHelper.loadAllItems(tag, this.items);
        this.remainingBrewTime = tag.getShort("RemainingBrewTime");
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int p_70301_1_) {
        return p_70301_1_ >= 0 && p_70301_1_ < this.items.size() ? this.items.get(p_70301_1_) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
        return ItemStackHelper.removeItem(this.items, p_70298_1_, p_70298_2_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_70304_1_) {
        return ItemStackHelper.takeItem(this.items, p_70304_1_);
    }

    @Override
    public void setItem(int p_70299_1_, ItemStack p_70299_2_) {
        if (p_70299_1_ >= 0 && p_70299_1_ < this.items.size()) {
            this.items.set(p_70299_1_, p_70299_2_);
        }
    }

    @Override
    public int getMaxStackSize() {
        return IBrewingInventory.super.getMaxStackSize();
    }

    @Override
    public boolean stillValid(PlayerEntity p_70300_1_) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(p_70300_1_.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }
}
