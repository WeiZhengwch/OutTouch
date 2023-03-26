package me.banendy.client.utils;

import net.minecraft.util.EnumFacing;

import java.util.Set;

public abstract class Chunk {
    public abstract void addEdges(int pos, Set<EnumFacing> p_178610_2_);
    protected abstract int getNeighborIndexAtFace(int pos, EnumFacing facing);
}
