package com.geoda1;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EdibleHeart extends Item {
    public EdibleHeart(Properties props) {
        super(props.food(new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).alwaysEat().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) ServerEvents.addHeart(sp);
        return super.finishUsingItem(stack, level, entity);
    }
}
