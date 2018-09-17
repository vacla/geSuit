package net.cubespace.geSuitPortals.objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public enum FillType {
    AIR("AIR"),
    WATER("WATER"),
    LAVA("LAVA"),
    WEB("COBWEB"),
    SUGAR_CANE("SUGAR_CANE"),
    END_PORTAL("END_PORTAL"),
    PORTAL("NETHER_PORTAL");
    
    private final String BlockMaterial;

    FillType(String material) {
        BlockMaterial = material;
    }

    public Material getBlockMaterial() {
        return Material.getMaterial(BlockMaterial);
    }

}
