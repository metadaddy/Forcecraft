package us.forcecraft;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.server.MinecraftServer;

public class EntityAIWaitOnOppyClose extends EntityAIBase {
	EntityContact contact;
	private static final long WAIT_TIME = 30000; // milliseconds 

	EntityAIWaitOnOppyClose(EntityContact contact) {
		this.contact = contact;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {		
		return (MinecraftServer.getSystemTimeMillis() - contact.getOppyCloseTime()) < WAIT_TIME;
	}

	public void startExecuting()
	{
		FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "EntityAIWaitOnOppyClose.startExecuting");
		this.contact.getNavigator().clearPathEntity();
	}
}
