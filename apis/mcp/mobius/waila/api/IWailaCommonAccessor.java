package mcp.mobius.waila.api;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * The Accessor is used to get some basic data out of the game without having to request direct access to the game engine.<br>
 * It will also return things that are unmodified by the overriding systems (like getWailaStack).<br> 
 * Common accessor for both Entity and Block/TileEntity.<br>
 * Available data depends on what it is called upon (ie : getEntity() will return null if looking at a block, etc).<br>
 *
 */
public interface IWailaCommonAccessor {
	World        		 getWorld();
	EntityPlayer 		 getPlayer();
	Block        		 getBlock();
	int          		 getBlockID();
	String               getBlockQualifiedName();
	int          		 getMetadata();
	TileEntity           getTileEntity();
	Entity               getEntity();
	MovingObjectPosition getPosition();
	Vec3                 getRenderingPosition();
	NBTTagCompound       getNBTData();
	int                  getNBTInteger(NBTTagCompound tag, String keyname);
	double               getPartialFrame();
	ForgeDirection       getSide();
	ItemStack            getStack();
}
