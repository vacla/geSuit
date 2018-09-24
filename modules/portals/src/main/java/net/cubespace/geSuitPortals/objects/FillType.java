package net.cubespace.geSuitPortals.objects;

import org.bukkit.Material;

public enum FillType {
    AIR("AIR", "AIR"),
    WATER("WATER", "WATER"),
    LAVA("LAVA", "LAVA"),
    WEB("WEB", "COBWEB"),
    SUGAR_CANE("SUGAR_CANE_BLOCK", "SUGAR_CANE"),
    END_PORTAL("ENDER_PORTAL", "END_PORTAL"),
    PORTAL("PORTAL", "NETHER_PORTAL");

    private final String legacyMaterial;
    private final String material;

    FillType(String material) {
        this(material, null);
    }

    FillType(String legacy, String newMaterial) {
        legacyMaterial = legacy;
        this.material = newMaterial;
    }

    public Material getBlockMaterial() {
        Material mat = Material.getMaterial(legacyMaterial);
        if (mat == null) mat = Material.getMaterial(material);
        return mat;
    }

}
