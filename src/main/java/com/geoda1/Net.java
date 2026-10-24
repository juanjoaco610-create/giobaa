package com.geoda1;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class Net {
    private static final String V = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Geoda1.MODID, "main"), () -> V, V::equals, V::equals);

    public record SyncLives(int lives, int state) {
        static void enc(SyncLives m, FriendlyByteBuf b) { b.writeVarInt(m.lives); b.writeVarInt(m.state); }
        static SyncLives dec(FriendlyByteBuf b) { return new SyncLives(b.readVarInt(), b.readVarInt()); }
        static void handle(SyncLives m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.sync(m.lives, m.state)));
            c.get().setPacketHandled(true);
        }
    }

    public record PlayEmerald(String item) {
        static void enc(PlayEmerald m, FriendlyByteBuf b) { b.writeUtf(m.item); }
        static PlayEmerald dec(FriendlyByteBuf b) { return new PlayEmerald(b.readUtf()); }
        static void handle(PlayEmerald m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.emerald(m.item)));
            c.get().setPacketHandled(true);
        }
    }

    public record Who(UUID id, String name, long left) {}

    public record OpenScreen(BlockPos pos, List<Who> list) {
        static void enc(OpenScreen m, FriendlyByteBuf b) {
            b.writeBlockPos(m.pos);
            b.writeVarInt(m.list.size());
            for (Who w : m.list) { b.writeUUID(w.id()); b.writeUtf(w.name()); b.writeLong(w.left()); }
        }
        static OpenScreen dec(FriendlyByteBuf b) {
            BlockPos p = b.readBlockPos();
            int n = b.readVarInt();
            List<Who> l = new ArrayList<>();
            for (int i = 0; i < n; i++) l.add(new Who(b.readUUID(), b.readUtf(), b.readLong()));
            return new OpenScreen(p, l);
        }
        static void handle(OpenScreen m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openScreen(m.pos, m.list)));
            c.get().setPacketHandled(true);
        }
    }

    public record Revive(BlockPos pos, UUID target) {
        static void enc(Revive m, FriendlyByteBuf b) { b.writeBlockPos(m.pos); b.writeUUID(m.target); }
        static Revive dec(FriendlyByteBuf b) { return new Revive(b.readBlockPos(), b.readUUID()); }
        static void handle(Revive m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> {
                ServerPlayer sender = c.get().getSender();
                if (sender == null) return;
                BlockState st = sender.level().getBlockState(m.pos);
                if (!st.is(Geoda1.HEART_JAR.get()) || !st.getValue(HeartJarBlock.FILLED)) return;
                if (sender.distanceToSqr(m.pos.getX() + 0.5, m.pos.getY() + 0.5, m.pos.getZ() + 0.5) > 64) return;
                ServerPlayer target = sender.getServer().getPlayerList().getPlayer(m.target);
                if (target == null) return;
                if (Revival.revive(sender, target, m.pos)) {
                    sender.level().setBlock(m.pos, st.setValue(HeartJarBlock.FILLED, false), 3);
                }
            });
            c.get().setPacketHandled(true);
        }
    }

    public static void register() {
        int id = 0;
        CH.registerMessage(id++, SyncLives.class, SyncLives::enc, SyncLives::dec, SyncLives::handle);
        CH.registerMessage(id++, OpenScreen.class, OpenScreen::enc, OpenScreen::dec, OpenScreen::handle);
        CH.registerMessage(id++, PlayEmerald.class, PlayEmerald::enc, PlayEmerald::dec, PlayEmerald::handle);
        CH.registerMessage(id++, Revive.class, Revive::enc, Revive::dec, Revive::handle);
    }

    public static void sync(ServerPlayer p, LifeData.Entry e) {
        CH.send(PacketDistributor.PLAYER.with(() -> p), new SyncLives(e.lives, e.state));
    }
}
