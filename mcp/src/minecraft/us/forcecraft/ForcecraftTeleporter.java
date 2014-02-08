package us.forcecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

// Stub, for the benefit of ServerConfigurationManager.transferEntityToWorld
public class ForcecraftTeleporter extends Teleporter
{
	private final WorldServer worldServerInstance;
	
    public ForcecraftTeleporter(WorldServer par1WorldServer)
    {
    	super(par1WorldServer);
    	worldServerInstance = par1WorldServer;
    }

    /**
     * Place an entity in a nearby portal, creating one if necessary.
     */
    public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
    {
    	// Do almost nothing - we don't actually want to create a portal!
    	if (par1Entity.dimension == Forcecraft.dimensionId) {
    		ChunkCoordinates spawnPoint = worldServerInstance.getSpawnPoint();
    		
    		// Ensure player spawns above ground
    		spawnPoint.posY++;
    		
	    	par1Entity.setLocationAndAngles(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ, 
	    			par1Entity.rotationYaw, par1Entity.rotationPitch);
    	} else {
    		EntityPlayerMP player = (EntityPlayerMP)par1Entity;
    		WorldServer worldServer = player.mcServer.worldServerForDimension(par1Entity.dimension);
    		ChunkCoordinates spawnpoint = worldServer.getSpawnPoint();
    		spawnpoint.posY = worldServer.getTopSolidOrLiquidBlock(spawnpoint.posX, spawnpoint.posZ);
	    	par1Entity.setLocationAndAngles(spawnpoint.posX, spawnpoint.posY, spawnpoint.posZ, par1Entity.rotationYaw, par1Entity.rotationPitch);    		
    	}
    }
}
