package com.rinko1231.drinkmoon.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

public class LiquorJarContainerScreen extends ContainerScreen<LiquorJarContainer> {

    private final ResourceLocation LIQUOR_JAR_CONTAINER_RESOURCE = new ResourceLocation("drinkbeer", "textures/gui/container/liquor_jar.png");
    private final int textureWidth = 176;
    private final int textureHeight = 166;
    private PlayerInventory inventory;

    public LiquorJarContainerScreen(LiquorJarContainer screenContainer, PlayerInventory inv, ITextComponent title) {
        super(screenContainer, inv, title);
        this.imageWidth = textureWidth;
        this.imageHeight = textureHeight;

        this.inventory = inv;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        renderBackground(stack);
        this.minecraft.getTextureManager().bind(LIQUOR_JAR_CONTAINER_RESOURCE);
        int i = (this.width - this.getXSize()) / 2;
        int j = (this.height - this.getYSize()) / 2;
        blit(stack, i, j, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack stack, int x, int y) {
        drawCenteredString(stack, this.font, this.title, (int) this.textureWidth / 2, (int) this.titleLabelY, 4210752);
        this.font.draw(stack, this.inventory.getDisplayName(), (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
        String str = menu.getIsBrewing() ? convertTickToTime(menu.getRemainingBrewingTime()) : convertTickToTime(menu.getStandardBrewingTime());
        int i = (this.width - this.textureWidth) / 2;
        int j = (this.height - this.textureHeight) / 2;
        this.font.draw(stack, str, (float) 128, (float) 54, new Color(64, 64, 64, 255).getRGB());

        // this sound effect part has been move to BeerBarrelContainer.OutputSlot#onTake
        /* if (menu.pouring) {
            this.inventory.player.level.playSound(this.inventory.player, this.inventory.player.blockPosition(), SoundEventRegistry.POURING.get(), SoundCategory.BLOCKS, 1f, 1f);
            menu.stopPouring();
        }*/
    }

    public String convertTickToTime(int tick) {
        String result;
        if (tick > 0) {
            double time = tick / 20;
            int m = (int) (time / 60);
            int s = (int) (time % 60);
            result = m + ":" + s;
        } else result = "";
        return result;
    }
}
