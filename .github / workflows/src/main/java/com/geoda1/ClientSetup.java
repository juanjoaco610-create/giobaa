package com.geoda1;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Geoda1.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent ev) {
        ev.registerAboveAll("vidas", (gui, g, pt, w, h) -> {
            if (Minecraft.getInstance().options.hideGui) return;
            int x = w / 2 - 91 - 72;
            int y = h - 18;
            g.drawString(gui.getFont(), "Vidas:", x, y, 0xFFFFFF);
            for (int i = 0; i < LifeData.MAX_LIVES; i++) {
                int hx = x + 38 + i * 10;
                g.blit(ICONS, hx, y - 1, 16, 45, 9, 9);          // contenedor hardcore
                if (i < ClientState.lives) g.blit(ICONS, hx, y - 1, 52, 45, 9, 9); // corazón hardcore lleno
            }
        });
    }
}
