package us.forcecraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.util.Vec3;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import cpw.mods.fml.common.Loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * This example demonstrates how a streaming client works
 * against the Salesforce Streaming API with generic notifications.
 **/
public class StreamingClient {
	private static final String CHANNEL = "/topic/OpportunitiesChannel";
	private static final String STREAMING_ENDPOINT_URI = "/cometd/29.0";
	private static final int CONNECTION_TIMEOUT = 20 * 1000;  // milliseconds
	private static final int READ_TIMEOUT = 120 * 1000; // milliseconds

	public static void subscribe(final String endpoint, final String sessionid) throws Exception {
		System.out.println("Running streaming client example....");
		final BayeuxClient client = makeClient(endpoint, sessionid);
		client.getChannel(Channel.META_HANDSHAKE).addListener
		(new ClientSessionChannel.MessageListener() {
			public void onMessage(ClientSessionChannel channel, Message message) {
				System.out.println("[CHANNEL:META_HANDSHAKE]: " + message);
				boolean success = message.isSuccessful();
				if (!success) {
					String error = (String) message.get("error");
					if (error != null) {
						System.out.println("Error during HANDSHAKE: " + error);
//						System.out.println("Exiting...");
//						System.exit(1);
					}
					Exception exception = (Exception) message.get("exception");
					if (exception != null) {
						System.out.println("Exception during HANDSHAKE: ");
						exception.printStackTrace();
//						System.out.println("Exiting...");
//						System.exit(1);
					} 
				}
			}
		});

		client.getChannel(Channel.META_CONNECT).addListener(
				new ClientSessionChannel.MessageListener() {
					public void onMessage(ClientSessionChannel channel, Message message) {
						System.out.println("[CHANNEL:META_CONNECT]: " + message);
						boolean success = message.isSuccessful();
						if (!success) {
							String error = (String) message.get("error");
							if (error != null) {
								System.out.println("Error during CONNECT: " + error);
//								System.out.println("Exiting...");
//								System.exit(1);
							}
						}
					}
				});

		client.getChannel(Channel.META_SUBSCRIBE).addListener(
				new ClientSessionChannel.MessageListener() {
					public void onMessage(ClientSessionChannel channel, Message message) {
						System.out.println("[CHANNEL:META_SUBSCRIBE]: " + message);
						boolean success = message.isSuccessful();
						if (!success) {
							String error = (String) message.get("error");
							if (error != null) {
								System.out.println("Error during SUBSCRIBE: " + error);
//								System.out.println("Exiting...");
//								System.exit(1);
							} 
						}
					}
				});

		client.handshake();
		System.out.println("Waiting for handshake");

		boolean handshaken = client.waitFor(10 * 1000, BayeuxClient.State.CONNECTED);
		if (!handshaken) {
			System.out.println("Failed to handshake: " + client);
//			System.exit(1);
		}

		System.out.println("Subscribing for channel: " + CHANNEL);
		client.getChannel(CHANNEL).subscribe(new MessageListener() {
			@Override
			public void onMessage(ClientSessionChannel channel, Message message) {
				System.out.println("Received Message: " + message);
				
				JsonRootNode root = null;
				try {
					root = Forcecraft.instance.client.parser.parse(message.getJSON());
				} catch (InvalidSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// and do... something
				}
				
				JsonNode sobject = root.getNode("data", "sobject");
				String oppyId = sobject.getStringValue("Id");
				String oppyName = sobject.getStringValue("Name");
				String stage = sobject.getStringValue("StageName");
				String accountId = sobject.getStringValue("AccountId");
				
				TileEntityStageBlock t = TileEntityStageBlock.getStageBlock(oppyId, stage);
				// If the lever for this oppy/stage is 'off'...
				int leverX = t.xCoord+1;
				if ((t.worldObj.getBlockMetadata(leverX, t.yCoord, t.zCoord) & 0x8) == 0) {
					// Throw the lever to 'on', setting the current 'on' lever off...
			        Block block = Block.blocksList[t.worldObj.getBlockId(leverX, t.yCoord, t.zCoord)];
					block.onBlockActivated(t.worldObj, leverX, t.yCoord, t.zCoord, null, 0, 0.0F, 0.0F, 0.0F);
				}
				
				if (stage.equals("Closed Won")) {
					EntityPlayerMP player = null;
					ArrayList<EntityPlayerMP> allp = new ArrayList<EntityPlayerMP>();
					ListIterator itl;
	
					for(int i = 0; i<MinecraftServer.getServer().worldServers.length; i++) {
						itl = MinecraftServer.getServer().worldServers[i].playerEntities.listIterator();
						while(itl.hasNext()) {
							// Basically assuming there is only one player :-/
							player = (EntityPlayerMP)itl.next();
						}
					}
					
					List<JsonNode> records = Forcecraft.instance.accounts.getNode("records").getElements();
					for (int i = 0; i < records.size(); i++) {
						String id = records.get(i).getStringValue("Id");
						if (id.equals(accountId)) {
							List<JsonNode> contacts = records.get(i).getNode("Contacts", "records").getElements();
							EntityContact entityContact = EntityContact.contactMap.get(contacts.get(0).getStringValue("Id"));
							
							Vec3 playerPos = player.getPosition(1.0F);
							Vec3 look = player.getLook(1.0F);
							Vec3 pos = playerPos.addVector(look.xCoord, 0, look.zCoord);

							entityContact.setPositionAndUpdate(pos.xCoord, pos.yCoord, pos.zCoord);
							
							String amount = root.getNumberValue("data", "sobject", "Amount");
							
							player.addChatMessage("Opportunity closed: " + oppyName + 
									", for $"+amount);
							
							List<int[]> items = EntityContact.getTreasure(Double.valueOf(amount));
							for (int j = 0; j < items.size(); j++) {
								entityContact.dropItem(items.get(j)[0], items.get(j)[1]);								
							}
						}
					}
				}
			}	
		});

		System.out.println("Waiting for streamed data from your organization ...");
	}

	private static BayeuxClient makeClient(final String endpoint, final String sessionid) throws Exception {
		HttpClient httpClient = new HttpClient();
		httpClient.setConnectTimeout(CONNECTION_TIMEOUT);
		httpClient.setTimeout(READ_TIMEOUT);
		httpClient.start();
		System.out.println("Login successful!\nEndpoint: " + endpoint
				+ "\nSessionid=" + sessionid);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(ClientTransport.TIMEOUT_OPTION, READ_TIMEOUT);
		LongPollingTransport transport = new LongPollingTransport(
				options, httpClient) {
			@Override
			protected void customize(ContentExchange exchange) {
				super.customize(exchange);
				exchange.addRequestHeader("Authorization", "OAuth " + sessionid);
			} };
			BayeuxClient client = new BayeuxClient(salesforceStreamingEndpoint(
					endpoint), transport);
			return client;
	}
	
	private static String salesforceStreamingEndpoint(String endpoint)
			throws MalformedURLException {
		return new URL(endpoint + STREAMING_ENDPOINT_URI).toExternalForm();
	} 
}
