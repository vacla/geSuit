package com.minecraftdimensions.bungeesuiteportals;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Region {
	private Location first;
	private Location end;
	
	private BlockCoord cacheCoord = new BlockCoord(0, 0, 0);
	private HashSet<BlockCoord> blocks = new HashSet<BlockCoord>();

	public Region(Location f, Location e)
	{
		this.first = f;
		this.end = e;
		
		if (f == null || e == null) {
			return;
		}
		
		int maxx = Math.max(f.getBlockX(), e.getBlockX());
		int minx = Math.min(f.getBlockX(), e.getBlockX());
		int maxy = Math.max(f.getBlockY(), e.getBlockY());
		int miny = Math.min(f.getBlockY(), e.getBlockY());
		int maxz = Math.max(f.getBlockZ(), e.getBlockZ());
		int minz = Math.min(f.getBlockZ(), e.getBlockZ());
		
		for (int x = minx; x <= maxx; ++x) {
			for (int z = minz; z <= maxz; ++z) {
				for (int y = miny; y <= maxy; ++y) {
					blocks.add(new BlockCoord(x, y, z));
				}
			}
		}
	}
	
	public Region(String world,int maxx,int maxy,int maxz,int minx, int miny, int minz)
	{
		this.first = new Location(Bukkit.getWorld(world), minx, miny, minz);
		this.end = new Location(Bukkit.getWorld(world), maxx, maxy, maxz);
	}

	public ArrayList<Location> getBlocks()
	{
		int maxx = Math.max(first.getBlockX(), end.getBlockX());
		int minx = Math.min(first.getBlockX(), end.getBlockX());
		int maxy = Math.max(first.getBlockY(), end.getBlockY());
		int miny = Math.min(first.getBlockY(), end.getBlockY());
		int maxz = Math.max(first.getBlockZ(), end.getBlockZ());
		int minz = Math.min(first.getBlockZ(), end.getBlockZ());
		
		ArrayList<Location> locs = new ArrayList<Location>(Math.abs(maxx - minx) * Math.abs(maxy - miny) * Math.abs(maxz - minz));
		
		for (int fy = miny;fy <= maxy;fy++)
		{
			for (int fx = minx;fx <= maxx;fx++)
			{
				for (int fz = minz;fz <= maxz;fz++)
				{
					locs.add(new Location(first.getWorld(), fx, fy, fz));
				}
			}
		}
		return locs;
	}

	public boolean isIn(Location l, double offset)
	{
		if (offset == 0.0) {
			return isIn(l);
		}
		if ((l.getX() >= Math.min(first.getBlockX(), end.getBlockX()) - offset && l.getX() < Math.max(first.getBlockX(), end.getBlockX()) + offset + 1) 
				&& (l.getY() >= Math.min(first.getBlockY(), end.getBlockY()) - offset && l.getY() < Math.max(first.getBlockY(), end.getBlockY()) + offset + 1) 
				&& (l.getZ() >= Math.max(first.getBlockZ(), end.getBlockZ()) + offset && l.getZ() < Math.min(first.getBlockZ(), end.getBlockZ()) - offset + 1))
			return true;

		/*for (Location lC : getBlocks())
		{
			if (l.getWorld() == lC.getWorld() && l.getBlockX() == lC.getBlockX() && lC.getBlockY() == l.getBlockY() && lC.getBlockZ() == l.getBlockZ())
				return true;
		}*/
		return false;
	}
	
	public boolean isIn(Location l) {
		cacheCoord.copy(l);
		
		return blocks.contains(cacheCoord);
	}

	public Location getFirst() {
		return first;
	}

	public void setFirst(Location first) {
		this.first = first;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}
}

class BlockCoord {
	public int x, y, z;
	
	public BlockCoord(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void copy(Location l) {
		x = l.getBlockX();
		y = l.getBlockY();
		z = l.getBlockZ();
	}

	@Override
	public int hashCode() {
		return y + (x << 5) + (z << 15);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		if (this == o)
			return true;
		
		if (getClass() != o.getClass())
			return false;
		
		BlockCoord c = (BlockCoord) o;
		
		return x == c.x && y == c.y && z == c.z;
	}
}