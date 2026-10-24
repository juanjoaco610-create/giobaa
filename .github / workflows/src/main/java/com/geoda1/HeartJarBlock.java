package com.geoda1;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeartJarBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public HeartJarBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FILLED); }

    @Override
    public InteractionResult use(BlockState s, Level l, BlockPos p, Player pl, InteractionHand h, BlockHitResult hit) {
        ItemStack st = pl.getItemInHand(h);
        if (!s.getValue(FILLED)) {
            if (st.is(Geoda1.GOLDEN_HEART.get())) {
                if (!l.isClientSide) {
                    l.setBlock(p, s.setValue(FILLED, true), 3);
                    if (!pl.getAbilities().instabuild) st.shrink(1);
                    l.playSound(null, p, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.2f);
                }
                return InteractionResult.sidedSuccess(l.isClientSide);
            }
            if (!l.isClientSide) pl.displayClientMessage(Component.literal("Poné un corazón dorado en el frasco."), true);
            return InteractionResult.sidedSuccess(l.isClientSide);
        }
        if (!l.isClientSide && pl instanceof ServerPlayer sp) {
            LifeData d = LifeData.get(sp.getServer());
            long now = System.currentTimeMillis();
            List<Net.Who> list = new ArrayList<>();
            for (Map.Entry<UUID, LifeData.Entry> en : d.all().entrySet()) {
                LifeData.Entry e = en.getValue();
                if (e.state == LifeData.WAITING && e.deadline > now
                        && sp.getServer().getPlayerList().getPlayer(en.getKey()) != null) {
                    list.add(new Net.Who(en.getKey(), e.name, e.deadline - now));
                }
            }
            Net.CH.send(PacketDistributor.PLAYER.with(() -> sp), new Net.OpenScreen(p, list));
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
