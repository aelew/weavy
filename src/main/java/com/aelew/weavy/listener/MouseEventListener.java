package com.aelew.weavy.listener;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.weavemc.api.event.MouseEvent;
import net.weavemc.api.event.SubscribeEvent;
import net.weavemc.api.event.TickEvent;

import java.util.Random;

import static com.aelew.weavy.util.Constants.mc;

// clickassist/doubleclicker (vibe-coded slop, but it works!)
public final class MouseEventListener {

    private final Random random = new Random();

    private int queuedButton = -1;
    private int queuedClicks = 0;
    private int tickDelay = 0;

    private int recentRightClickTicks = 0;
    private int rightClickStreak = 0;

    private static final int LEFT_EXTRA_CLICKS = 1;
    private static final int RIGHT_EXTRA_CLICKS = 2;

    private static final double RIGHT_CLICK_CHANCE = 1.0;

    // How many ticks count as "recent"
    // 4 ticks = about 0.2 seconds at 20 TPS
    private static final int RECENT_RIGHT_CLICK_WINDOW = 6;

    // How many recent clicks before blocks get boosted
    private static final int REQUIRED_BLOCK_CLICK_STREAK = 2;

    @SubscribeEvent
    public void onMouse(final MouseEvent event) {
        if (!event.getButtonState()) {
            return;
        }

        if (event.getButton() != 0 && event.getButton() != 1) {
            return;
        }

        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        final int button = event.getButton();

        if (button == 0) {
            if (isLookingAtBlock()) {
                return;
            }

            queueExtraClicks(0, LEFT_EXTRA_CLICKS);
            return;
        }

        if (button == 1) {
            final ItemStack held = mc.thePlayer.getHeldItem();

            if (!shouldDoubleRightClick(held)) {
                resetRightClickStreak();
                return;
            }

            updateRightClickStreak();

            final Item item = held.getItem();

            if (item instanceof ItemBlock) {
                // blocks only get extra clicks if you have clicked repeatedly recently
                if (rightClickStreak < REQUIRED_BLOCK_CLICK_STREAK) {
                    return;
                }
            }

            // XP bottles still get boosted normally
            if (random.nextDouble() < RIGHT_CLICK_CHANCE) {
                queueExtraClicks(1, RIGHT_EXTRA_CLICKS);
            }
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent event) {
        if (recentRightClickTicks > 0) {
            recentRightClickTicks--;

            if (recentRightClickTicks == 0) {
                rightClickStreak = 0;
            }
        }

        if (queuedButton == -1 || queuedClicks <= 0) {
            return;
        }

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            clearQueue();
            return;
        }

        if (queuedButton == 0) {
            clickKey(mc.gameSettings.keyBindAttack.getKeyCode());
        } else if (queuedButton == 1) {
            final ItemStack held = mc.thePlayer.getHeldItem();

            if (shouldDoubleRightClick(held)) {
                clickKey(mc.gameSettings.keyBindUseItem.getKeyCode());
            }
        }

        queuedClicks--;

        if (queuedClicks <= 0) {
            clearQueue();
        } else {
            tickDelay = 0;
        }
    }

    private void updateRightClickStreak() {
        if (recentRightClickTicks > 0) {
            rightClickStreak++;
        } else {
            rightClickStreak = 1;
        }

        recentRightClickTicks = RECENT_RIGHT_CLICK_WINDOW;
    }

    private void resetRightClickStreak() {
        rightClickStreak = 0;
        recentRightClickTicks = 0;
    }

    private void queueExtraClicks(final int button, final int clicks) {
        queuedButton = button;
        queuedClicks = clicks;
        tickDelay = 0;
    }

    private void clearQueue() {
        queuedButton = -1;
        queuedClicks = 0;
        tickDelay = 0;
    }

    private void clickKey(final int keyCode) {
        KeyBinding.setKeyBindState(keyCode, true);
        KeyBinding.onTick(keyCode);
        KeyBinding.setKeyBindState(keyCode, false);
    }

    private boolean shouldDoubleRightClick(final ItemStack held) {
        if (held == null) {
            return false;
        }

        final Item item = held.getItem();
        if (item instanceof ItemBlock) {
            return true;
        }

        return item == Items.experience_bottle;
    }

    private boolean isLookingAtBlock() {
        return mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

}
