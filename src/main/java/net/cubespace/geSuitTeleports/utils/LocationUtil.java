package net.cubespace.geSuitTeleports.utils;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.cubespace.geSuitTeleports.geSuitTeleports;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static net.cubespace.geSuitTeleports.geSuitTeleports.logDebugMessages;


public class LocationUtil {
    // The player can stand inside these materials
    public static final Set<Material> HOLLOW_MATERIALS = new HashSet<>();

    public static class Vector3D
    {
        public int x;
        public int y;
        public int z;

        public Vector3D(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static final int RADIUS = 16;
    public static final Vector3D[] VOLUME;

    static
    {
        List<Vector3D> pos = new ArrayList<>();
        for (int x = -RADIUS; x <= RADIUS; x++)
        {
            for (int y = -RADIUS; y <= RADIUS; y++)
            {
                for (int z = -RADIUS; z <= RADIUS; z++)
                {
                    pos.add(new Vector3D(x, y, z));
                }
            }
        }
        Collections.sort(
                pos, new Comparator<Vector3D>() {
                    @Override
                    public int compare(Vector3D a, Vector3D b) {
                        return (a.x * a.x + a.y * a.y + a.z * a.z) - (b.x * b.x + b.y * b.y + b.z * b.z);
                    }
                }
        );
        VOLUME = pos.toArray(new Vector3D[0]);
    }


    static {
        HOLLOW_MATERIALS.add(Material.AIR);
        HOLLOW_MATERIALS.add(Material.SAPLING);
        HOLLOW_MATERIALS.add(Material.POWERED_RAIL);
        HOLLOW_MATERIALS.add(Material.DETECTOR_RAIL);
        HOLLOW_MATERIALS.add(Material.LONG_GRASS);
        HOLLOW_MATERIALS.add(Material.DEAD_BUSH);
        HOLLOW_MATERIALS.add(Material.YELLOW_FLOWER);
        HOLLOW_MATERIALS.add(Material.RED_ROSE);
        HOLLOW_MATERIALS.add(Material.BROWN_MUSHROOM);
        HOLLOW_MATERIALS.add(Material.RED_MUSHROOM);
        HOLLOW_MATERIALS.add(Material.TORCH);
        HOLLOW_MATERIALS.add(Material.REDSTONE_WIRE);
        HOLLOW_MATERIALS.add(Material.SEEDS);
        HOLLOW_MATERIALS.add(Material.SIGN_POST);
        HOLLOW_MATERIALS.add(Material.WOODEN_DOOR);
        HOLLOW_MATERIALS.add(Material.LADDER);
        HOLLOW_MATERIALS.add(Material.RAILS);
        HOLLOW_MATERIALS.add(Material.WALL_SIGN);
        HOLLOW_MATERIALS.add(Material.LEVER);
        HOLLOW_MATERIALS.add(Material.STONE_PLATE);
        HOLLOW_MATERIALS.add(Material.IRON_DOOR_BLOCK);
        HOLLOW_MATERIALS.add(Material.WOOD_PLATE);
        HOLLOW_MATERIALS.add(Material.REDSTONE_TORCH_OFF);
        HOLLOW_MATERIALS.add(Material.REDSTONE_TORCH_ON);
        HOLLOW_MATERIALS.add(Material.STONE_BUTTON);
        HOLLOW_MATERIALS.add(Material.SNOW);
        HOLLOW_MATERIALS.add(Material.SUGAR_CANE_BLOCK);
        HOLLOW_MATERIALS.add(Material.DIODE_BLOCK_OFF);
        HOLLOW_MATERIALS.add(Material.DIODE_BLOCK_ON);
        HOLLOW_MATERIALS.add(Material.PUMPKIN_STEM);
        HOLLOW_MATERIALS.add(Material.MELON_STEM);
        HOLLOW_MATERIALS.add(Material.VINE);
        HOLLOW_MATERIALS.add(Material.FENCE_GATE);
        HOLLOW_MATERIALS.add(Material.WATER_LILY);
        HOLLOW_MATERIALS.add(Material.NETHER_WARTS);

        try // 1.6 update
        {
            HOLLOW_MATERIALS.add(Material.CARPET);
        } catch (java.lang.NoSuchFieldError e) {

        }
    }

    static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
        return y > world.getMaxHeight() || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
    }

    public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
        return isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z);

    }

    public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
        final Block below = world.getBlockAt(x, y - 1, z);
        if (below.getType() == Material.LAVA || below.getType() == Material.STATIONARY_LAVA) {
            return true;
        }
        if (below.getType() == Material.FIRE) {
            return true;
        }
        return below.getType() == Material.BED_BLOCK || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).getType())) || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType()));
    }

    // Not needed if using getSafeDestination(loc)
    public static Location getRoundedDestination(final Location loc) {
        final World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static Location getSafeDestination(final Player player, final Location loc) {
        if (loc.getWorld().equals(player.getWorld())
                && ((player.getGameMode() == GameMode.CREATIVE) || (player.isFlying()))) {
            return getRoundedDestination(loc);
        }

        return getSafeDestination(loc);
    }

    public static Location getSafeDestination(final Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return null;
        }

        final World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        final int origX = x;
        final int origY = y;
        final int origZ = z;
        while (isBlockAboveAir(world, x, y, z)) {
            y -= 1;
            if (y < 0) {
                y = origY;
                break;
            }
        }
        if (isBlockUnsafe(world, x, y, z)) {
            x = Math.round(loc.getX()) == origX ? x - 1 : x + 1;
            z = Math.round(loc.getZ()) == origZ ? z - 1 : z + 1;
        }
        int i = 0;
        while (isBlockUnsafe(world, x, y, z)) {
            i++;
            if (i >= VOLUME.length) {
                x = origX;
                y = origY + RADIUS;
                z = origZ;
                break;
            }
            x = origX + VOLUME[i].x;
            y = origY + VOLUME[i].y;
            z = origZ + VOLUME[i].z;
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y += 1;
            if (y >= world.getMaxHeight()) {
                x += 1;
                break;
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y -= 1;
            if (y <= 1) {
                x += 1;
                y = world.getHighestBlockYAt(x, z);
                if (x - 48 > loc.getBlockX()) {
                    return null;
                }
            }
        }
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static boolean shouldFly(Location loc) {
        final World world = loc.getWorld();
        final int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        final int z = loc.getBlockZ();
        int count = 0;
        while (LocationUtil.isBlockUnsafe(world, x, y, z) && y > -1) {
            y--;
            count++;
            if (count > 2) {
                return true;
            }
        }

        return y < 0 ? true : false;
    }

    public static boolean worldGuardTpAllowed(Location l, Player p) {
        Boolean result = true;
        Logger log = geSuitTeleports.instance.getLogger();
        if(logDebugMessages) log.info("Checking if WG allows TP. Status of Plugin:"+geSuitTeleports.worldGuarded);//Todo remove after debug
        if (geSuitTeleports.worldGuarded) {
            RegionContainer container = geSuitTeleports.getWorldGuard().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(l);
            if (!set.isVirtual()) {//VirtualSet indicates that there is no region protection to check
                for (ProtectedRegion region : set) {
                    Set<String> flags = region.getFlag(DefaultFlag.BLOCKED_CMDS);
                    if (flags != null) {
                        if(logDebugMessages) log.info("Blocked Commands Found:" + flags.toString());
                        for (String cmd : flags) {
                            if (geSuitTeleports.deny_Teleport.contains(cmd)) {
                                if(logDebugMessages) log.info("Test for " + cmd + " was true.");
                                if (p.hasPermission("worldgaurd.teleports.allregions")|| TeleportsManager.administrativeTeleport.contains(p)) {
                                    p.sendMessage(geSuitTeleports.tp_admin_bypass);
                                    if(logDebugMessages) log.info("Player:"+ p.getDisplayName()+":" + geSuitTeleports.tp_admin_bypass + "Location: Region=" + region.getId());
                                    TeleportsManager.administrativeTeleport.remove(p);
                                    result = true;
                                } else {
                                    p.sendMessage(geSuitTeleports.location_blocked);
                                    result = false;
                                }
                            }
                        }
                        if(logDebugMessages)
                        log.info("Tests on List:"+geSuitTeleports.deny_Teleport.toString() + " completed" );
                    }else{
                        if(logDebugMessages)
                        log.info("FLAGS was null");
                    }
                }
            }else{
                if(logDebugMessages)
                    log.info("Region set was virtual");
            }
        }
        log.info("World gaurd check for TP completed: Player=" + p.getDisplayName() + " Location=(" + l.toString() + ") Region TP Allowed=" + result);
        return result;
    }
}
