package com.geoda1;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ClientHooks {
    public static void sync(int lives, int state) { ClientState.lives = lives; ClientState.state = state; }
    public static void emerald(String id) {
        Item it = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (it != null) Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(it));
    }
    public static void openScreen(BlockPos pos, List<Net.Who> list) {
        Minecraft.getInstance().setScreen(new ReviveScreen(pos, list));
    }
}
