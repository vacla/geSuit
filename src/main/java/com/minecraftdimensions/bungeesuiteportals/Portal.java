/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.Location;

/**
 *
 * @author ANNA
 */
public class Portal {
	private String name;
	private String toServer=null;
	private String toWarp=null;
	private Region portal;
	private FillType fillType;
	private boolean active;

	public Portal(String name, String type, String dest, Region portal, FillType fillType) {
		this.name = name;
		if(type.equalsIgnoreCase("server")){
		this.toServer = dest;
		}else {
			this.toWarp = dest;	
		}
		this.portal = portal;
		this.fillType = fillType;
		this.active = true;
		fillBlocks();
	}

	public FillType getFillType() {
		return fillType;
	}


	public Region getPortalArea() {
		return portal;
	}

	public String getTag() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

//	public boolean teleportPlayer(Player p)
//	{
//		return true;
//	}

	public boolean containsLocation(Location l, double offset)
	{
			if (portal.isIn(l, offset)){
				return true;
			}
		return false;
	}
	

	public boolean isIn(Location l) {
		return portal.isIn(l);
	}

	public void setActive(boolean active) {
		this.active = active;
		if (active)
		{
			this.fillBlocks();
		}
		else
		{
			this.defillBlocks();
		}
	}

	public void setFillType(FillType fillType) {
		if (this.isActive())
		{
			this.defillBlocks();
			this.fillType = fillType;
			this.fillBlocks();
		}
	}

	public void beforeDelete()
	{
		this.defillBlocks();
	}

	public void defillBlocks()
	{
			for(Location data:portal.getBlocks()){
				if(this.fillType.isAType(data.getBlock().getTypeId()))
				data.getBlock().setTypeId(0);
			}
	}

	public void fillBlocks()
	{
			this.fillType.fillBlocks(portal.getBlocks());
	}

	public void setFromPoints(Region fromPoints) {
		this.portal = fromPoints;
	}
	public String getToServer() {
		return toServer;
	}
	public boolean hasWarp() {
		return toWarp!=null;
	}
	
	public String getWarp() {
		return toWarp;
	}

	public void setToServer(String toServer) {
		this.toServer = toServer;
	}
	public void setToWarp(String toWarp) {
		this.toWarp = toWarp;
	}
}