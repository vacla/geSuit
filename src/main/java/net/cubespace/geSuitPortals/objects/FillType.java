package net.cubespace.geSuitPortals.objects;

import org.bukkit.Material;

public enum FillType {
    AIR(Material.AIR),
    WATER(Material.STATIONARY_WATER),
    LAVA(Material.STATIONARY_LAVA),
    WEB(Material.WEB),
    SUGAR_CANE(Material.SUGAR_CANE_BLOCK),
    END_PORTAL(Material.ENDER_PORTAL),
    PORTAL(Material.PORTAL);

    private final Material BlockMaterial;

    FillType(Material blockId) {
        BlockMaterial = blockId;
    }

    public Material getBlockMaterial() {
        return BlockMaterial;
    }

}
