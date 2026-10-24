package com.geoda1;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod(Geoda1.MODID)
public class Geoda1 {
    public static final String MODID = "geoda1";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> HEART_JAR = BLOCKS.register("heart_jar", () ->
            new HeartJarBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.GLASS)
                    .lightLevel(s -> s.getValue(HeartJarBlock.FILLED) ? 10 : 0)));
    public static final RegistryObject<Item> HEART_JAR_ITEM = ITEMS.register("heart_jar",
            () -> new BlockItem(HEART_JAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_HEART = ITEMS.register("golden_heart",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(16)));

    public static final RegistryObject<Item> HEART_FOOD = ITEMS.register("heart_food",
            () -> new EdibleHeart(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Block> GEODA_BLOCK = BLOCKS.register("geoda_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(5f, 6f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> GEODA_PUNT = BLOCKS.register("geoda_punt",
            () -> new Block(BlockBehaviour.Properties.of().strength(50f, 1200f).sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Item> GEODA_BLOCK_ITEM = ITEMS.register("geoda_block",
            () -> new BlockItem(GEODA_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> GEODA_PUNT_ITEM = ITEMS.register("geoda_punt",
            () -> new BlockItem(GEODA_PUNT.get(), new Item.Properties()));

    public static final Map<String, RegistryObject<Item>> EMERALDS = new LinkedHashMap<>();
    static {
        for (String n : new String[]{"yellow", "red", "pink", "violet", "rainbow"}) {
            final boolean rb = n.equals("rainbow");
            EMERALDS.put(n, ITEMS.register("emerald_" + n, () -> rb
                    ? new Item(new Item.Properties().rarity(Rarity.EPIC)) {
                        @Override public boolean isFoil(ItemStack s) { return true; }
                    }
                    : new Item(new Item.Properties().rarity(Rarity.RARE))));
        }
    }

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("geoda1", () ->
            CreativeModeTab.builder().title(Component.translatable("itemGroup.geoda1"))
                    .icon(() -> new ItemStack(GOLDEN_HEART.get()))
                    .displayItems((p, o) -> { o.accept(GOLDEN_HEART.get()); o.accept(HEART_JAR_ITEM.get()); o.accept(HEART_FOOD.get());
                        o.accept(GEODA_BLOCK_ITEM.get()); o.accept(GEODA_PUNT_ITEM.get());
                        o.accept(Items.EMERALD); EMERALDS.values().forEach(r -> o.accept(r.get())); })
                    .build());

    public Geoda1() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus); ITEMS.register(bus); TABS.register(bus);
        bus.addListener((FMLCommonSetupEvent e) -> Net.register());
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }
}
