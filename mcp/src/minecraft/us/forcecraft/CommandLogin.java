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
				transferPlayerToDimension(player, Forcecraft.dimensionId, Forcecraft.instance.getDefaultTeleporter());
			}
		}
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return "/login";
	}
	
	// This is essentially a clone of ServerConfigurationManager.transferPlayerToDimension, 
	// with a call to MinecraftServer.tick() added to avoid a race condition. 
    public void transferPlayerToDimension(EntityPlayerMP player, int dimension, Teleporter teleporter)
    {
    	MinecraftServer mcServer = player.mcServer;
    	ServerConfigurationManager scm = mcServer.getConfigurationManager();
        int j = player.dimension;
        WorldServer worldserver = mcServer.worldServerForDimension(player.dimension);
        player.dimension = dimension;
        WorldServer worldserver1 = mcServer.worldServerForDimension(player.dimension);
        player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension, (byte)player.worldObj.difficultySetting, worldserver1.getWorldInfo().getTerrainType(), worldserver1.getHeight(), player.theItemInWorldManager.getGameType()));
        worldserver.removePlayerEntityDangerously(player);
        player.isDead = false;
        scm.transferEntityToWorld(player, j, worldserver, worldserver1, teleporter);
        scm.func_72375_a(player, worldserver);
        
        // This is here because, otherwise, on the next tick, the client is unpaused, but the server
        // doesn't know it yet, so the server saves the world while the client is waiting for blocks,
        // resulting in the player falling out of the world.
        mcServer.tick();
        
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(worldserver1);
        scm.updateTimeAndWeatherForPlayer(player, worldserver1);
        scm.syncPlayerInventory(player);
        Iterator iterator = player.getActivePotionEffects().iterator();

        while (iterator.hasNext())
        {
            PotionEffect potioneffect = (PotionEffect)iterator.next();
            player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, potioneffect));
        }

        GameRegistry.onPlayerChangedDimension(player);
    }
}
