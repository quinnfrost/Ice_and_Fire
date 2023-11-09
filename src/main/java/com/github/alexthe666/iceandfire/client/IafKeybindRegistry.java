package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.entity.debug.quinnfrost.ExtendedEntityDebugger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;

public class IafKeybindRegistry {
    public static KeyMapping dragon_fireAttack;
    public static KeyMapping dragon_strike;
    public static KeyMapping dragon_down;
    public static KeyMapping dragon_change_view;
    public static KeyMapping extended_debug;

    public static void init() {
        // Minecraft instance is null during data gen
        if (Minecraft.getInstance() == null)
            return;
        dragon_fireAttack = new KeyMapping("key.dragon_fireAttack", 82, "key.categories.gameplay");
        dragon_strike = new KeyMapping("key.dragon_strike", 71, "key.categories.gameplay");
        dragon_down = new KeyMapping("key.dragon_down", 88, "key.categories.gameplay");
        dragon_change_view = new KeyMapping("key.dragon_change_view", 296, "key.categories.misc");
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, dragon_fireAttack);
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, dragon_strike);
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, dragon_down);
        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, dragon_change_view);

        if (ExtendedEntityDebugger.EXTENDED_DEBUG) {
            extended_debug = new KeyMapping("key.extended_debug", -1, "key.categories.misc");
            Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, extended_debug);
        }
    }
}
