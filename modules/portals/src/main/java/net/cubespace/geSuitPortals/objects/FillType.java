package net.cubespace.geSuitPortals.objects;

import org.bukkit.Material;

public enum FillType {
    AIR(Material.AIR),
    WATER(Material.WATER),
    LAVA(Material.LAVA),
    WEB(Material.COBWEB),
    SUGAR_CANE(Material.SUGAR_CANE),
    END_PORTAL(Material.END_PORTAL),
    PORTAL(Material.NETHER_PORTAL);

    private final Material BlockMaterial;

    FillType(Material blockId) {
        BlockMaterial = blockId;
    }

    public Material getBlockMaterial() {
        return BlockMaterial;
    }

}
