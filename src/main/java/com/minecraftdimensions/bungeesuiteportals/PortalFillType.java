/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.minecraftdimensions.bungeesuiteportals;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author ANNA
 */
public class PortalFillType extends FillType {

	public PortalFillType(String name, int[] ids) {
		super(name, ids);
	}

	@Override
	public void fillBlocks(ArrayList<Location> blocks) {
		ArrayList<Location> changed = new ArrayList<Location>();
		for (Location l : blocks)
		{
			if (l.getBlock() != null && l.getBlock().getType() == Material.AIR)
			{
				l.getBlock().setType(Material.AIR);
				changed.add(l);
			}
		}

		for (Location b : changed)
		{
			b.getBlock().setTypeId(this.materials[0]);
		}
	}

}