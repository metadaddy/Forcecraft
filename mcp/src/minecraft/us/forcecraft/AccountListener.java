package us.forcecraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class AccountListener implements MessageListener {
	public static boolean inMessage = false; // True if we're currently processing a message from Salesforce
	
	@Override
	public void onMessage(ClientSessionChannel channel, Message message) {
		inMessage = true;
		
		try {
			System.out.println("Received Message: " + message);
			
			JsonRootNode root = null;
			try {
				root = Forcecraft.instance.client.parser.parse(message.getJSON());
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
				return;
			}
			
			JsonNode sobject = root.getNode("data", "sobject");
			if (root.getStringValue("data", "event", "type").equals("created")) {
				String id = sobject.getStringValue("Id");
				String name = sobject.getStringValue("Name");
				System.out.println("Account "+name+" created");
				
				System.out.println("Reloading accounts");
				Forcecraft.instance.accounts = Forcecraft.instance.client.getAccounts();
				
				int n = 0;
				for (JsonNode acct : Forcecraft.instance.accounts.getNode("records").getElements()) {
					String acctId = acct.getStringValue("Id");
					if (acct.getStringValue("Id").substring(0,15).equals(id.substring(0,15))) {
						int[] chunkCoords = ForcecraftGenerator.getPointDiscreteSpiral(n);
						ChunkProviderServer cps = MinecraftServer.getServer().worldServerForDimension(Forcecraft.dimensionId).theChunkProviderServer;
						if (cps.chunkExists(chunkCoords[0], chunkCoords[1])) {
							System.out.println("Repopulating chunk at ("+chunkCoords[0]+", "+chunkCoords[1]+")");
							cps.loadChunk(chunkCoords[0], chunkCoords[1]).isTerrainPopulated = false;
							cps.populate(cps, chunkCoords[0], chunkCoords[1]);
						} // If the chunk doesn't exist, it will be current when it is created later
						break;
					}
					n++;
				}
			}
			
			// TODO
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			inMessage = false;
		}				
	}
}
