package us.forcecraft;

import cpw.mods.fml.common.Loader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import argo.jdom.JdomParser;

public class LogoutCommand extends CommandBase {
	private static JdomParser parser = new JdomParser();
	
	@Override
	public String getCommandName()
	{
		return "logout";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		if(icommandsender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)icommandsender;
			
			if (player.dimension == Forcecraft.dimensionId){
				player.addChatMessage("Teleporting from Forcecraft dimension");
				player.mcServer.getConfigurationManager().transferPlayerToDimension(player,	0, 
						Forcecraft.instance.getDefaultTeleporter());
			}					
		}
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return "/logout";
	}
}
