package com.geoda1;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class Revival {
    /** Revive a un jugador en WAITING cuyo contador no terminó. reviver puede ser null (comando). */
    public static boolean revive(ServerPlayer reviver, ServerPlayer target, BlockPos pos) {
        LifeData d = LifeData.get(target.getServer());
        LifeData.Entry e = d.entry(target);
        if (e.state != LifeData.WAITING || e.deadline <= System.currentTimeMillis()) return false;

        ServerLevel lvl = reviver != null ? reviver.serverLevel() : target.serverLevel();
        Vec3 at = pos != null ? Vec3.atBottomCenterOf(pos) : (reviver != null ? reviver.position() : target.position());

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lvl);
        if (bolt != null) {
            bolt.moveTo(at);
            bolt.setVisualOnly(true);
            lvl.addFreshEntity(bolt);
        }

        e.state = LifeData.ALIVE;
        e.lives = LifeData.MAX_LIVES;
        d.setDirty();

        target.teleportTo(lvl, at.x + 0.5, at.y + (pos != null ? 1 : 0), at.z + 0.5, target.getYRot(), target.getXRot());
        target.setGameMode(GameType.SURVIVAL);
        target.setHealth(target.getMaxHealth());
        target.getFoodData().setFoodLevel(20);
        Net.sync(target, e);

        String who = reviver != null ? reviver.getGameProfile().getName() : "Un administrador";
        target.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("✚ " + who + " revivió a " + e.name + ".").withStyle(ChatFormatting.GREEN), false);
        target.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal("¡Has revivido!").withStyle(ChatFormatting.GOLD)));
        return true;
    }
}
