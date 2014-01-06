package us.forcecraft;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class StageBlock extends BlockContainer
{
	public StageBlock (int id, Material material) 
	{
		super(id, material);
	}
	
    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityStageBlock();
    }

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: x, y, z, neighbor blockID
	 */
	public void onNeighborBlockChange(World world, int par2, int par3, int par4, int par5)
	{
		if (!world.isRemote)
		{
            boolean hasPower = (world.getBlockPowerInput(par2, par3, par4) == 15);
            int blockMetadata = world.getBlockMetadata(par2, par3, par4);
            boolean isAlreadyOn = (blockMetadata & 0x1) != 0;

            if (hasPower && !isAlreadyOn)
            {
            	// Turn on
                world.setBlockMetadataWithNotify(par2, par3, par4, blockMetadata | 0x1, 4);
                // Go along the row clearing the other levers - this is a bit dodgy -
                // it relies on knowledge of the lever layout!
                int x = par2 + 1, y = par3, z = par4 + 1;
                while (world.getBlockId(x, y, z) == Block.lever.blockID) {
                	// Turn off lever
                	int metadata = world.getBlockMetadata(x, y, z);
                	if ((metadata & 0x8) != 0) {
                		world.setBlockMetadataWithNotify(x, y, z, metadata & ~(0x8), 2);
                	}
                	// Turn off stage block
                	metadata = world.getBlockMetadata(x-1, y, z);
                	if ((metadata & 0x1) != 0) {
                		world.setBlockMetadataWithNotify(x-1, y, z, metadata & ~(0x1), 2);
                	}
                	z++;
                }
                z = par4 - 1;
                while (world.getBlockId(x, y, z) == Block.lever.blockID) {
                	// Turn off lever
                	int metadata = world.getBlockMetadata(x, y, z);
                	if ((metadata & 0x8) != 0) {
                		world.setBlockMetadataWithNotify(x, y, z, metadata & ~(0x8), 2);
                	}
                	// Turn off stage block
                	metadata = world.getBlockMetadata(x-1, y, z);
                	if ((metadata & 0x1) != 0) {
                		world.setBlockMetadataWithNotify(x-1, y, z, metadata & ~(0x1), 2);
                	}
                	z--;
                }
                world.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(world));
            }
            else if (!hasPower && isAlreadyOn)
            {
            	// Don't want user directly turning lever off!
            	System.out.println("Override!!!");
            	int x = par2 + 1, y = par3, z = par4;
            	int metadata = world.getBlockMetadata(x, y, z);
            	world.setBlockMetadataWithNotify(x, y, z, metadata | 0x8, 2);
            }
		}
	}
	
    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World world, int par2, int par3, int par4, Random par5Random)
    {
        TileEntity tileentity = world.getBlockTileEntity(par2, par3, par4);

        if (tileentity != null && tileentity instanceof TileEntityStageBlock)
        {
        	TileEntityStageBlock tileentitystageblock = (TileEntityStageBlock)tileentity;
            tileentitystageblock.setOpportunityStage(world);
            //par1World.func_96440_m(par2, par3, par4, this.blockID);
        }
    }
}