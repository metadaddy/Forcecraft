package us.forcecraft;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class TileEntityStageBlock extends TileEntity
{
	private static Map<String, Map<String, TileEntityStageBlock>> oppyStageMap = new HashMap<String, Map<String, TileEntityStageBlock>>();
    public String stage = "";
    public String opportunityId = "";

    /**
     * Execute the command, called when the command block is powered.
     */
    public int setOpportunityStage(World par1World)
    {
        if (par1World.isRemote)
        {
            return 0;
        }
        else
        {
        	System.out.println("Setting "+opportunityId+" to "+stage);
        	Forcecraft.instance.client.setOpportunityStage(opportunityId, stage);
        	return 1;
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setString("Stage", this.stage);
        par1NBTTagCompound.setString("OpportunityId", this.opportunityId);
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        setOpportunityStage(par1NBTTagCompound.getString("OpportunityId"), 
        		par1NBTTagCompound.getString("Stage"));
    }

    /**
     * Return the position for this command sender.
     */
    public ChunkCoordinates getPlayerCoordinates()
    {
        return new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord);
    }

    public World getEntityWorld()
    {
        return this.getWorldObj();
    }

    /**
     * Overriden in a sign to provide the text.
     */
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 2, nbttagcompound);
    }

	public void setOpportunityStage(String oppyId, String stage) {
		this.opportunityId = oppyId;
		this.stage = stage;
		
		Map<String, TileEntityStageBlock> map = oppyStageMap.get(oppyId);
		if (map == null) {
			map = new HashMap<String, TileEntityStageBlock>();
			oppyStageMap.put(oppyId, map);
		}
		
		map.put(stage, this);
	}
	
	public static TileEntityStageBlock getStageBlock(String oppyId, String stage) {
		return oppyStageMap.get(oppyId).get(stage);
	}
}
