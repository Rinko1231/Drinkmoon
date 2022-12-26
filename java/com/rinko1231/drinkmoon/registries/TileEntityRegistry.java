package com.rinko1231.drinkmoon.registries;


import com.rinko1231.drinkmoon.tileentities.LiquorJarTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegistry {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "drinkbeer");
    public static final RegistryObject<TileEntityType<LiquorJarTileEntity>> LIQUOR_JAR_TILEENTITY = TILE_ENTITY.register("liquor_jar_blockentity", () -> TileEntityType.Builder.of(LiquorJarTileEntity::new, BlockRegistry.LIQUOR_JAR.get()).build(null));
}
