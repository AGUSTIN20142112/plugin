package me.agusti.anarchyops.services;

import net.minecraft.util.math.BlockPos;

public record Marker(String name, String dimension, int x, int y, int z, long createdAt) {
    public BlockPos pos() { return new BlockPos(x, y, z); }
}
