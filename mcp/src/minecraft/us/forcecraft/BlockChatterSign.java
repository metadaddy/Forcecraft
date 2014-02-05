package us.forcecraft;

import net.minecraft.block.BlockSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockChatterSign extends BlockSign {
	public BlockChatterSign(int blockID, Class signEntityClass, boolean isFreestanding) {
		super(blockID, signEntityClass, isFreestanding);
	}
	
    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityChatterSign();
    }

    public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
        if (!world.isRemote)
        {
            TileEntity tileentity = world.getBlockTileEntity(par2, par3, par4);

            if (tileentity != null && tileentity instanceof TileEntityChatterSign)
            {
            	TileEntityChatterSign tileentityaccountblock = (TileEntityChatterSign)tileentity;
            	GuiChatter.displayChatterGUI((EntityPlayerMP)par5EntityPlayer, tileentityaccountblock.accountId, tileentityaccountblock.accountName);
            }        	
        }
        
        return true;
    }	
}
