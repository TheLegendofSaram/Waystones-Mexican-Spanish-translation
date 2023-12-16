package net.blay09.mods.waystones.item;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.menu.BalmMenuProvider;
import net.blay09.mods.waystones.api.IResetUseOnDamage;
import net.blay09.mods.waystones.compat.Compat;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.blay09.mods.waystones.core.WarpMode;
import net.blay09.mods.waystones.core.Waystone;
import net.blay09.mods.waystones.menu.ModMenus;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WarpScrollItem extends ScrollItemBase implements IResetUseOnDamage {

    public WarpScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return WaystonesConfig.getActive().cooldowns.scrollUseTime;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof ServerPlayer player) {
            final var waystones = PlayerWaystoneManager.getTargetsForItem(player, itemStack);
            PlayerWaystoneManager.ensureSortingIndex(player, waystones);
            Balm.getNetworking().openGui(((ServerPlayer) entity), new BalmMenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("container.waystones.waystone_selection");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new WaystoneSelectionMenu(ModMenus.warpScrollSelection.get(), WarpMode.WARP_SCROLL, null, windowId, waystones);
                }

                @Override
                public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                    Waystone.writeList(buf, waystones);
                }
            });
        }
        return itemStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!player.isUsingItem() && !world.isClientSide) {
            world.playSound(null, player, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.1f, 2f);
        }
        if (getUseDuration(itemStack) <= 0 || Compat.isVivecraftInstalled) {
            finishUsingItem(itemStack, world, player);
        } else {
            player.startUsingItem(hand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }


}
