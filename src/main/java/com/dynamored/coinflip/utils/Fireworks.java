package com.dynamored.coinflip.utils;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class Fireworks {
	public static void summon(Location location) {
        Firework firework = location.getWorld().spawn(location.add(0.5, 0.5, 0.5), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withColor(Arrays.asList(Color.RED, Color.ORANGE, Color.YELLOW))
                .withFade(Color.WHITE, Color.BLACK, Color.SILVER)
                .withFlicker()
                .withTrail()
                .build();

        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(2);

        firework.setFireworkMeta(fireworkMeta);
    }
}
