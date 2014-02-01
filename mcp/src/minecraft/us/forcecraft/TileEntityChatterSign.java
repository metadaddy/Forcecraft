package us.forcecraft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class TileEntityChatterSign extends TileEntitySign
{
    public String accountId = "";
    public String accountName = "";

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setString("accountId", this.accountId);
        par1NBTTagCompound.setString("accountName", this.accountName);
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.accountId = par1NBTTagCompound.getString("accountId");
        this.accountName = par1NBTTagCompound.getString("accountName");
    }
}
