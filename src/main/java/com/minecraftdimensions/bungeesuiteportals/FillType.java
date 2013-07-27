package com.minecraftdimensions.bungeesuiteportals;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;

public class FillType {
	protected String name;
	protected int[] materials;

	public FillType(String name, int[] materials) {
		this.name = name;
		this.materials = materials;
	}

	public void fillBlocks(ArrayList<Location> blocks)
	{
		for (Location l : blocks)
			if (l.getBlock().getType() == Material.AIR)
				l.getBlock().setTypeId(materials[0]);
	}

	public void defillBlocks(ArrayList<Location> blocks)
	{
		for (Location  l : blocks)
		{
			if (l.getBlock() != null && this.isAType(l.getBlock().getTypeId()))
				l.getBlock().setTypeId(materials[0]);
		}
	}

	public boolean isAType(int id)
	{
		for (int i : materials)
			if (id == i)
				return true;
		return false;
	}

	public int[] getMaterials() {
		return materials;
	}

	public String getName() {
		return name;
	}
	public static ArrayList<FillType> fillTypes = new ArrayList<FillType>();
	static
	{
		fillTypes.add(new FillType("AIR", new int[]{0}));
		fillTypes.add(new FillType("WATER", new int[]{8, 9}));
		fillTypes.add(new FillType("LAVA", new int[]{10, 11}));
		fillTypes.add(new FillType("WEB", new int[]{30}));
		fillTypes.add(new PortalFillType("PORTAL", new int[]{90}));
		fillTypes.add(new PortalFillType("END_PORTAL", new int[]{119}));
	}

	public static FillType getFillType(String tag)
	{
		for (FillType ft : fillTypes)
		{
			if (ft.getName().equalsIgnoreCase(tag))
				return ft;
		}
		return null;
	}
}