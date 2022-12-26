package com.rinko1231.drinkmoon.gui;

import com.rinko1231.drinkmoon.registries.ContainerTypeRegistry;
import lekavar.lma.drinkbeer.registries.ItemRegistry;
import lekavar.lma.drinkbeer.registries.SoundEventRegistry;

import com.rinko1231.drinkmoon.tileentities.LiquorJarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class LiquorJarContainer extends Container {
    private static final int STATUS_CODE = 1;
    private static final int BREWING_REMAINING_TIME = 0;
    private final IInventory brewingSpace;
    private final IIntArray syncData;

    public LiquorJarContainer(int id, IInventory brewingSpace, IIntArray syncData, PlayerInventory playerInventory, LiquorJarTileEntity liquorJarTileEntity) {
        super(ContainerTypeRegistry.liquorJarContainer.get(), id);
        this.brewingSpace = brewingSpace;
        this.syncData = syncData;

        // Layout Slot
        // Plauer Inventory
        layoutPlayerInventorySlots(8, 84, new InvWrapper(playerInventory));
        // Input Ingredients
        addSlot(new Slot(brewingSpace, 0, 28, 26));
        addSlot(new Slot(brewingSpace, 1, 46, 26));
        addSlot(new Slot(brewingSpace, 2, 28, 44));
        addSlot(new Slot(brewingSpace, 3, 46, 44));
        // Empty Cup
        addSlot(new CupSlot(brewingSpace, 4, 73, 50));
        // Output
        addSlot(new OutputSlot(brewingSpace, 5, 128, 34, syncData, liquorJarTileEntity));

        //Tracking Data
        addDataSlots(syncData);
    }

    public LiquorJarContainer(int id, PlayerInventory playerInventory, PacketBuffer data) {
        this(id, playerInventory, data.readBlockPos());
    }

    public LiquorJarContainer(int id, PlayerInventory playerInventory, BlockPos pos) {
        this(id, ((LiquorJarTileEntity) Minecraft.getInstance().level.getBlockEntity(pos)), ((LiquorJarTileEntity) Minecraft.getInstance().level.getBlockEntity(pos)).syncData, playerInventory, ((LiquorJarTileEntity) Minecraft.getInstance().level.getBlockEntity(pos)));
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow, IItemHandler playerInventory) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity p_82846_1_, int p_82846_2_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_82846_2_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Try quick-pickup output
            if (p_82846_2_ == 41) {
                if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            // Try quick-move item in player inv.
            else if (p_82846_2_ < 36) {
                // Try to fill cup slot first.
                if (this.isEmptyCup(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 40, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Try to fill ingredient slot.
                if (!this.moveItemStackTo(itemstack1, 36, 40, false)) {
                    return ItemStack.EMPTY;
                }
            }

            // Try quick-move item to player inv.
            else if (!this.moveItemStackTo(itemstack1, 0, 36, false)) {
                return ItemStack.EMPTY;
            }

            // Detect weather the quick-move is successful or not
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            // Detect weather the quick-move is successful or not
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_82846_1_, itemstack1);
        }

        return itemstack;
    }

    public boolean isEmptyCup(ItemStack itemStack) {
        return itemStack.getItem() == ItemRegistry.EMPTY_BEER_MUG.get();
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return this.brewingSpace.stillValid(p_75145_1_);
    }

    public boolean getIsBrewing() {
        return syncData.get(STATUS_CODE) == 1;
    }

    public int getStandardBrewingTime() {
        return syncData.get(BREWING_REMAINING_TIME);
    }

    public int getRemainingBrewingTime() {
        return syncData.get(BREWING_REMAINING_TIME);
    }

    @Override
    public void removed(PlayerEntity player) {
        if (!player.level.isClientSide()) {
            // Return Item to Player;
            for (int i = 0; i < 5; i++) {
                if (!brewingSpace.getItem(i).isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, brewingSpace.removeItem(i, brewingSpace.getItem(i).getCount()));
                }
            }
        } else {
            // Play Closing Barrel Sound
            player.level.playSound(player, player.blockPosition(), SoundEvents.BARREL_CLOSE, SoundCategory.BLOCKS, 1f, 1f);
        }
        super.removed(player);
    }

    static class CupSlot extends Slot {
        public CupSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        }

        // Only Empty Cup is Allowed.
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return p_75214_1_.getItem() == ItemRegistry.EMPTY_BEER_MUG.get();
        }
    }

    static class OutputSlot extends Slot {
        private final IIntArray syncData;
        private final LiquorJarTileEntity liquorJarTileEntity;

        public OutputSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_, IIntArray syncData, LiquorJarTileEntity liquorJarTileEntity) {
            super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
            this.syncData = syncData;
            this.liquorJarTileEntity = liquorJarTileEntity;
        }

        // After player picking up product, play pour sound effect
        // statusCode reset is handled by TileEntity#tick
        @Override
        public ItemStack onTake(PlayerEntity p_190901_1_, ItemStack p_190901_2_) {
            /*if(p_190901_2_.getItem() == ItemRegistry.BEER_MUG_FROTHY_PINK_EGGNOG.get()){
                p_190901_1_.level.playSound((PlayerEntity)null, beerBarrelTileEntity.getBlockPos(), SoundEventRegistry.POURING_CHRISTMAS_VER.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                //p_190901_1_.level.playSound(p_190901_1_, p_190901_1_.blockPosition(), SoundEventRegistry.POURING_CHRISTMAS_VER.get(), SoundCategory.BLOCKS, 1f, 1f);

            } else {*/
            p_190901_1_.level.playSound((PlayerEntity) null, liquorJarTileEntity.getBlockPos(), SoundEventRegistry.POURING.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            //p_190901_1_.level.playSound(p_190901_1_, p_190901_1_.blockPosition(), SoundEventRegistry.POURING.get(), SoundCategory.BLOCKS, 1f, 1f);
            //}
            return super.onTake(p_190901_1_, p_190901_2_);
        }

        // Placing item on output slot is prohibited.
        @Override
        public boolean mayPlace(ItemStack p_75214_1_) {
            return false;
        }

        // Only statusCode is 2 (waiting for pickup), pickup is allowed.
        @Override
        public boolean mayPickup(PlayerEntity p_82869_1_) {
            return syncData.get(STATUS_CODE) == 2;
        }
    }
}
