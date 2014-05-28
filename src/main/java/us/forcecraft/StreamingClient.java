package us.forcecraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;

import org.apache.logging.log4j.Level;
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
import cpw.mods.fml.common.FMLLog;
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
	public static final String API_VERSION = "29.0";
	public static final String OPPORTUNITY_TOPIC_NAME = "ForcecraftOpportunities";
	public static final String OPPORTUNITY_TOPIC_QUERY = "SELECT Id, Name, Amount, StageName, AccountId FROM Opportunity";
	private static final String OPPORTUNITY_CHANNEL = "/topic/"+OPPORTUNITY_TOPIC_NAME;
	public static final String ACCOUNT_TOPIC_NAME = "ForcecraftAccounts";
	public static final String ACCOUNT_TOPIC_QUERY = "SELECT Id, Name FROM Account";
	private static final String ACCOUNT_CHANNEL = "/topic/"+ACCOUNT_TOPIC_NAME;
	public static final String VEGETABLE_TOPIC_NAME = "ForcecraftVegetables";
	public static final String VEGETABLE_TOPIC_QUERY = "SELECT Id, Type__c, Quantity__c FROM Vegetable__c";
	private static final String VEGETABLE_CHANNEL = "/topic/"+VEGETABLE_TOPIC_NAME;
	private static final String STREAMING_ENDPOINT_URI = "/cometd/"+API_VERSION;
	private static final int CONNECTION_TIMEOUT = 20 * 1000;  // milliseconds
	private static final int READ_TIMEOUT = 120 * 1000; // milliseconds
	
	public static void subscribe(final String endpoint, final String sessionid) throws Exception {
		FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Connecting to Streaming API....");
		final BayeuxClient client = makeClient(endpoint, sessionid);
		client.getChannel(Channel.META_HANDSHAKE).addListener
		(new ClientSessionChannel.MessageListener() {
			public void onMessage(ClientSessionChannel channel, Message message) {
				FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "[CHANNEL:META_HANDSHAKE]: %s", message);
				boolean success = message.isSuccessful();
				if (success) {
					FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Subscribing for channel: %s", ACCOUNT_CHANNEL);
					client.getChannel(ACCOUNT_CHANNEL).subscribe(new AccountListener());
					
					FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Subscribing for channel: %s", OPPORTUNITY_CHANNEL);
					client.getChannel(OPPORTUNITY_CHANNEL).subscribe(new OpportunityListener());

					FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Subscribing for channel: %s", VEGETABLE_CHANNEL);
					client.getChannel(VEGETABLE_CHANNEL).subscribe(new VegetableListener());
					
					FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Waiting for streamed data from your organization ...");
				} else {
					String error = (String) message.get("error");
					if (error != null) {
						FMLLog.log(Forcecraft.FORCECRAFT, Level.ERROR, "Error during HANDSHAKE: %s", error);
						// Yeah - we really need some error handling...
//						System.out.println("Exiting...");
//						System.exit(1);
					}
					Exception exception = (Exception) message.get("exception");
					if (exception != null) {
						FMLLog.log(Forcecraft.FORCECRAFT, Level.ERROR, "Exception during HANDSHAKE:");
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
						FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "[CHANNEL:META_CONNECT]: %s", message);
						boolean success = message.isSuccessful();
						if (!success) {
							String error = (String) message.get("error");
							if (error != null) {
								FMLLog.log(Forcecraft.FORCECRAFT, Level.ERROR, "Error during CONNECT: %s", error);
//								System.out.println("Exiting...");
//								System.exit(1);
							}
						}
					}
				});

		client.getChannel(Channel.META_SUBSCRIBE).addListener(
				new ClientSessionChannel.MessageListener() {
					public void onMessage(ClientSessionChannel channel, Message message) {
						FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "[CHANNEL:META_SUBSCRIBE]: %s", message);
						boolean success = message.isSuccessful();
						if (!success) {
							String error = (String) message.get("error");
							if (error != null) {
								FMLLog.log(Forcecraft.FORCECRAFT, Level.ERROR, "Error during SUBSCRIBE: %s", error);
//								System.out.println("Exiting...");
//								System.exit(1);
							} 
						}
					}
				});

		client.handshake();
		FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Waiting for handshake");

		boolean handshaken = client.waitFor(10 * 1000, BayeuxClient.State.CONNECTED);
		if (!handshaken) {
			FMLLog.log(Forcecraft.FORCECRAFT, Level.ERROR, "Failed to handshake: %s", client.toString());
//			System.exit(1);
		}
	}

	private static BayeuxClient makeClient(final String endpoint, final String sessionid) throws Exception {
		HttpClient httpClient = new HttpClient();
		httpClient.setConnectTimeout(CONNECTION_TIMEOUT);
		httpClient.setTimeout(READ_TIMEOUT);
		httpClient.start();

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
