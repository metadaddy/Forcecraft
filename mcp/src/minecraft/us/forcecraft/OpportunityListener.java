package us.forcecraft;

import static argo.jdom.JsonNodeFactories.field;
import static argo.jdom.JsonNodeFactories.object;
import static argo.jdom.JsonNodeFactories.string;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class OpportunityListener implements MessageListener {
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
			
			String eventType = root.getStringValue("data", "event", "type");
			if (eventType.equals("deleted")) {
				// TODO...
				return;
			}
			
			JsonNode sobject = root.getNode("data", "sobject");
			String oppyId = sobject.getStringValue("Id");
			String oppyName = sobject.getStringValue("Name");
			String stage = sobject.getStringValue("StageName");
			String accountId = sobject.getStringValue("AccountId");
			
			if (eventType.equals("created")) {
				// Regenerate the chunk, to add levers or build a new floor...
				int n = 0;
				for (JsonNode acct : Forcecraft.instance.accounts.getNode("records").getElements()) {
					String acctId = acct.getStringValue("Id");
					if (acct.getStringValue("Id").substring(0,15).equals(accountId.substring(0,15))) {
						int[] chunkCoords = ForcecraftGenerator.getPointDiscreteSpiral(n);
						WorldServer worldserver = MinecraftServer.getServer().worldServerForDimension(Forcecraft.dimensionId);
						ChunkProviderServer cps = worldserver.theChunkProviderServer;
						if (cps.chunkExists(chunkCoords[0], chunkCoords[1])) {
							System.out.println("Reloading accounts");
							Forcecraft.instance.accounts = Forcecraft.instance.client.getAccounts();
							System.out.println("Repopulating chunk at ("+chunkCoords[0]+", "+chunkCoords[1]+")");
							Chunk chunk = cps.loadChunk(chunkCoords[0], chunkCoords[1]);
							
							// The opportunity doesn't seem to be on the Account yet...
							List<JsonNode> oppys = acct.getNode("Opportunities", "records").getElements();
							int index = oppys.size();
							JsonNode oppy = oppys.get(index - 1);
							if (!oppy.getStringValue("Id").equals("oppyId")) {
								oppy = object(
					                field("Id", string(oppyId)),
					                field("Name", string(oppyName)),
					                field("StageName", string(stage))
					            );								
							}
							Forcecraft.instance.generator.addNewLevel(worldserver, acct, oppy, index, chunkCoords[0], chunkCoords[1]);
						} // If the chunk doesn't exist, it will be current when it is created later
						break;
					}
					n++;
				}
			} else if (eventType.equals("updated")) {
				// Throw the appropriate lever
				TileEntityStageBlock t = TileEntityStageBlock.getStageBlock(oppyId, stage);
				// If the lever for this oppy/stage is 'off'...
				int leverX = t.xCoord+1;
				if ((t.worldObj.getBlockMetadata(leverX, t.yCoord, t.zCoord) & 0x8) == 0) {
					// Throw the lever to 'on', setting the current 'on' lever off...
			        Block block = Block.blocksList[t.worldObj.getBlockId(leverX, t.yCoord, t.zCoord)];
					block.onBlockActivated(t.worldObj, leverX, t.yCoord, t.zCoord, null, 0, 0.0F, 0.0F, 0.0F);
				}
				
				// Basically assuming there is only one player for now
				EntityPlayerMP player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);
				
				if (stage.equals("Closed Won")) {					
					String amount = root.getNumberValue("data", "sobject", "Amount");							
					String post = "Congratulations! Opportunity closed: " + oppyName + ", for $"+amount;
					player.addChatMessage(post);
					
					List<JsonNode> records = Forcecraft.instance.accounts.getNode("records").getElements();
					for (int i = 0; i < records.size(); i++) {
						String id = records.get(i).getStringValue("Id");
						if (id.equals(accountId)) {
							List<JsonNode> contacts = records.get(i).getNode("Contacts", "records").getElements();
							EntityContact entityContact = null;
							
							for (int j = 0; entityContact == null && j < contacts.size(); j++) {
								entityContact = EntityContact.contactMap.get(contacts.get(j).getStringValue("Id"));								
							}
							
							if (entityContact != null) {
								Vec3 playerPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ); 
								Vec3 look = player.getLook(1.0F);
								Vec3 pos = playerPos.addVector(look.xCoord, 0, look.zCoord);

								entityContact.setOppyCloseTime(MinecraftServer.getSystemTimeMillis());
								entityContact.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord);
																								
								List<int[]> items = EntityContact.getTreasure(Double.valueOf(amount));
								for (int j = 0; j < items.size(); j++) {
									entityContact.dropItem(items.get(j)[0], items.get(j)[1]);								
								}
								
								Forcecraft.instance.client.postToChatter(entityContact.id, post, false);
							}														
						}
						break;
					}
				} else if (stage.equals("Closed Lost")) {
					player.addChatMessage("Opportunity lost: " + oppyName);
					
		            WorldServer worldserver = MinecraftServer.getServer().worldServers[0];		            
		            WorldInfo worldinfo = worldserver.getWorldInfo();
		            int weatherTime = 60 * 20; // Number of ticks in one minute
		            worldinfo.setRainTime(weatherTime);
		            worldinfo.setThunderTime(weatherTime);
	                worldinfo.setRaining(true);
	                worldinfo.setThundering(true);					
				}					
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			inMessage = false;
		}				
	}
}
