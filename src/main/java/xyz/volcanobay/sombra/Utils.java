package xyz.volcanobay.sombra;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;


public class Utils {
    public static boolean isInvisible(Player player) {
        return player.isHolding(Items.ECHO_SHARD);
    }
    public static boolean isInvisible(Player target, Player observer) {
        return isInvisible(target) && !isInvisible(observer);
    }
}
