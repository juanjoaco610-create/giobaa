package com.geoda1;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReviveScreen extends Screen {
    private final BlockPos pos;
    private final List<Net.Who> list;

    public ReviveScreen(BlockPos pos, List<Net.Who> list) {
        super(Component.literal("Frasco de corazón"));
        this.pos = pos;
        this.list = list;
    }

    @Override
    protected void init() {
        int y = this.height / 2 - 40;
        for (Net.Who w : list) {
            Button b = Button.builder(Component.literal("Revivir a " + w.name() + "  (" + ServerEvents.fmt(w.left()) + ")"), btn -> {
                Net.CH.sendToServer(new Net.Revive(pos, w.id()));
                this.onClose();
            }).bounds(this.width / 2 - 110, y, 220, 20).build();
            addRenderableWidget(b);
            y += 24;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 0xFFD700);
        if (list.isEmpty()) g.drawCenteredString(this.font, "Nadie para revivir ahora.", this.width / 2, this.height / 2 - 20, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }
}
