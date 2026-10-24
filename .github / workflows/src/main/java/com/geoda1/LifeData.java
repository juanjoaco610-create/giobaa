package com.geoda1;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifeData extends SavedData {
    public static final int ALIVE = 0, WAITING = 1, ELIMINATED = 2;
    public static final int MAX_LIVES = 2;

    public static class Entry {
        public String name = "?";
        public int lives = MAX_LIVES;
        public int kills = 0;
        public boolean autoHeart = true;
        public int bonus = 0; // corazones extra (max 15 => 25 corazones en total)
        public int state = ALIVE;
        public long deadline;
        public double x, y, z;
    }

    public long yellowUntil, redUntil, pinkUntil, violetUntil, rainbowUntil;
    public boolean rainbowUsed;

    private final Map<UUID, Entry> map = new HashMap<>();

    public static LifeData get(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(LifeData::load, LifeData::new, "geoda1_lives");
    }

    public Entry entry(ServerPlayer p) {
        Entry e = map.computeIfAbsent(p.getUUID(), k -> new Entry());
        e.name = p.getGameProfile().getName();
        setDirty();
        return e;
    }

    public Entry peek(UUID id) { return map.get(id); }
    public Map<UUID, Entry> all() { return map; }

    public static LifeData load(CompoundTag tag) {
        LifeData d = new LifeData();
        d.yellowUntil = tag.getLong("yellowUntil"); d.redUntil = tag.getLong("redUntil");
        d.pinkUntil = tag.getLong("pinkUntil"); d.violetUntil = tag.getLong("violetUntil");
        d.rainbowUntil = tag.getLong("rainbowUntil"); d.rainbowUsed = tag.getBoolean("rainbowUsed");
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            Entry e = new Entry();
            e.name = t.getString("name");
            e.lives = t.getInt("lives");
            e.bonus = t.getInt("bonus");
            e.kills = t.getInt("kills");
            e.autoHeart = !t.contains("auto") || t.getBoolean("auto");
            e.state = t.getInt("state");
            e.deadline = t.getLong("deadline");
            e.x = t.getDouble("x"); e.y = t.getDouble("y"); e.z = t.getDouble("z");
            d.map.put(t.getUUID("id"), e);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        map.forEach((id, e) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", id);
            t.putString("name", e.name);
            t.putInt("lives", e.lives);
            t.putInt("bonus", e.bonus);
            t.putInt("kills", e.kills);
            t.putBoolean("auto", e.autoHeart);
            t.putInt("state", e.state);
            t.putLong("deadline", e.deadline);
            t.putDouble("x", e.x); t.putDouble("y", e.y); t.putDouble("z", e.z);
            list.add(t);
        });
        tag.put("players", list);
        tag.putLong("yellowUntil", yellowUntil); tag.putLong("redUntil", redUntil);
        tag.putLong("pinkUntil", pinkUntil); tag.putLong("violetUntil", violetUntil);
        tag.putLong("rainbowUntil", rainbowUntil); tag.putBoolean("rainbowUsed", rainbowUsed);
        return tag;
    }
}
