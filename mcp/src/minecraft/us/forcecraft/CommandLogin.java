package us.forcecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommandLogin extends CommandBase {
	@Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public String getCommandName()
	{
		return "login";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		if(icommandsender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)icommandsender;
			
			if (player.dimension != Forcecraft.dimensionId){
				player.addChatMessage("Teleporting to Forcecraft dimension");
				player.mcServer.getConfigurationManager().transferPlayerToDimension(player, 
						Forcecraft.dimensionId, Forcecraft.instance.getDefaultTeleporter());
			}
		}
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return "/login";
	}
}
