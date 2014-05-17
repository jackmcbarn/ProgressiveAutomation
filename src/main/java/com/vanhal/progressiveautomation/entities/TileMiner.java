package com.vanhal.progressiveautomation.entities;

import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;

import com.vanhal.progressiveautomation.ProgressiveAutomation;
import com.vanhal.progressiveautomation.ref.ToolInfo;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;

public class TileMiner extends BaseTileEntity {
	protected int totalMineBlocks = -1;
	protected int currentMineBlocks = 0;

	public TileMiner() {
		super(14);
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("MineBlocks", totalMineBlocks);
		nbt.setInteger("MinedBlocks", currentMineBlocks);
	}
	
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		totalMineBlocks = nbt.getInteger("MineBlocks");
		currentMineBlocks = nbt.getInteger("MinedBlocks");
	}
	
	public void updateEntity() {
		checkForChanges();
	}
	
	public void scanBlocks() {
		ProgressiveAutomation.logger.info("Range: "+getRange());
		for (int i = 0; i < getRange(); i++) {
			Point block = spiral(i, 0, 0);
			ProgressiveAutomation.logger.info("Mine Block: "+block.getX()+", "+block.getY());
		}
		totalMineBlocks = currentMineBlocks = 0;
		boolean bedrock = false;
		int newY = this.yCoord - 1;
		while (!bedrock) {
			Block tryBlock = worldObj.getBlock(this.xCoord, newY, this.zCoord);
			if (tryBlock != null) {
				if (
					(tryBlock.getBlockHardness(worldObj, xCoord, newY, zCoord)>=0) &&
					(tryBlock.getHarvestLevel(0)>=0)
					) {
					boolean mine = false;
					if (tryBlock == Blocks.cobblestone) {
						currentMineBlocks++;
					} if (tryBlock.getHarvestTool(0)=="pickaxe") {
						if (getToolMineLevel(2)>=tryBlock.getHarvestLevel(0)) {
							totalMineBlocks++;
							mine = true;
						}
					} else if (tryBlock.getHarvestTool(0)=="shovel") {
						if (getToolMineLevel(3)>=tryBlock.getHarvestLevel(0)) {
							totalMineBlocks++;
							mine = true;
						}
					} else {
						totalMineBlocks++;
						mine = true;
					}
					/*ProgressiveAutomation.logger.info("Block: "+newY+", Harvest Tool: "+
							tryBlock.getHarvestTool(0)+", Harvest Level: "+tryBlock.getHarvestLevel(0)+
							". Mine: "+mine);*/
				}
			}
			newY--;
			if (newY<0) bedrock = true;
		}
	}

	public int getRange() {
		if (this.getStackInSlot(4)==null) {
			return 1;
		} else {
			return this.getStackInSlot(4).stackSize + 1;
		}
	}
	
	public int getToolMineLevel(int slot) {
		if (getStackInSlot(slot) != null) {
			if (getStackInSlot(slot).getItem() instanceof ItemTool) {
				ItemTool tool = (ItemTool) getStackInSlot(slot).getItem();
				return ToolInfo.getHarvestLevel(tool);
			}
		}
		return -1;
	}
	
	public int getMinedBlocks() {
		return currentMineBlocks;
	}
	
	public int getMineBlocks() {
		if (totalMineBlocks==-1) {
			scanBlocks();
		}
		return totalMineBlocks;
	}
	
	
	/* Check for changes to tools and upgrades */
	protected int lastPick = -1;
	protected int lastShovel = -1;
	protected int lastUpgrades = 0;
	
	public void checkForChanges() {
		boolean update = false;
		//check pickaxe
		if ( (slots[2] == null) && (lastPick>=0) ) {
			lastPick = -1;
			update = true;
		} else if (slots[2] != null) {
			if (ToolInfo.getLevel(slots[2].getItem()) != lastPick) {
				lastPick = ToolInfo.getLevel(slots[2].getItem());
				update = true;
			}
		}
		
		//check shovel
		if ( (slots[3] == null) && (lastShovel>=0) ) {
			lastShovel = -1;
			update = true;
		} else if (slots[3] != null) {
			if (ToolInfo.getLevel(slots[3].getItem()) != lastShovel) {
				lastShovel = ToolInfo.getLevel(slots[3].getItem());
				update = true;
			}
		}
		
		//check upgrades
		if (getRange() != lastUpgrades) {
			lastUpgrades = getRange();
			update = true;
		}
		
		//update
		if (update) {
			scanBlocks();
		}
	}

	public static Point spiral(int n, int x, int y) {
		n = n-1;
		int dx, dy;
		
		int k = (int)Math.ceil( (Math.sqrt(n)-1)/2);
		int t = 2*k + 1;
		int m = t^2;
		t = t-1;
		
		if (n>=(m-t)) {
			dx = k-(m-n);
			dy = -k;
		} else {
			m = m-t;
			if (n>=(m-t)) {
				dx = -k;
				dy = -k + (m-n);
			} else {
				m = m-t;
				if (n>=(m-t)) {
					dx = -k + (m-n);
					dy = k;
				} else {
					dx = k;
					dy = k - (m-n-t);
				}
			}
		}
		
		return new Point(x + dx, y + dy);
	}
}
