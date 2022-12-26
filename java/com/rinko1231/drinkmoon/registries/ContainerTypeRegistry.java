package com.rinko1231.drinkmoon.registries;


import com.rinko1231.drinkmoon.gui.LiquorJarContainer;
import com.rinko1231.drinkmoon.gui.LiquorJarContainerScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

// Register Container & ContainerScreen in one class.
// Automatically Registering Static Event Handlers, see https://mcforge.readthedocs.io/en/1.16.x/events/intro/#automatically-registering-static-event-handlers
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypeRegistry {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, "drinkbeer");
    public static final RegistryObject<ContainerType<LiquorJarContainer>> liquorJarContainer = CONTAINERS.register("liquor_jar_container", () -> IForgeContainerType.create(LiquorJarContainer::new));


    @SubscribeEvent
    public static void registerContainerScreen(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

            ScreenManager.register(ContainerTypeRegistry.liquorJarContainer.get(), LiquorJarContainerScreen::new);
        });
    }
}