package us.forcecraft;

import static argo.jdom.JsonNodeFactories.*;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;

import org.apache.logging.log4j.Level;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;

import cpw.mods.fml.common.FMLLog;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class VegetableListener implements MessageListener {
	private static final String FLOWER = "Flower";
	private static final String CARROT = "Carrot";
	private static final String POTATO = "Potato";
	public static boolean inMessage = false; // True if we're currently processing a message from Salesforce
	
	private static EntityItem dropItem(World worldObj, double posX, double posY, double posZ, Item item, int quantity) {
		ItemStack itemStack = new ItemStack(item, quantity, 0);
		EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, itemStack);
		entityitem.delayBeforeCanPickup = 10;
		worldObj.spawnEntityInWorld(entityitem);
		
		return entityitem;
	}
	
	@Override
	public void onMessage(ClientSessionChannel channel, Message message) {
		inMessage = true;
		
		try {
			FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Received Message: %s", message);
			
			JsonRootNode root = null;
			try {
				root = Forcecraft.instance.client.parser.parse(message.getJSON());
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
				return;
			}
			
			String eventType = root.getStringValue("data", "event", "type");
			if (eventType.equals("deleted")) {
				return;
			}
			
			JsonNode sobject = root.getNode("data", "sobject");
			String id = sobject.getStringValue("Id");
			String type = sobject.getStringValue("Type__c");
			int quantity;
			try {
				quantity = Double.valueOf(sobject.getNumberValue("Quantity__c")).intValue();
			} catch (IllegalArgumentException e) {
				quantity = 0;
			}
			
			if (quantity == 0) {
				FMLLog.log(Forcecraft.FORCECRAFT, Level.WARN, "Vegetable quantity is zero - nothing to create!");
				return;
			}
			
			if (eventType.equals("created")) {
				Item item = null;
				if (type.equalsIgnoreCase(FLOWER)) {
					item = Item.getItemFromBlock(Blocks.red_flower);
				} else if (type.equalsIgnoreCase(POTATO)) {
					item = Items.potato;
				} else if (type.equalsIgnoreCase(CARROT)) {
					item = Items.carrot;					
				} else {
					FMLLog.log(Forcecraft.FORCECRAFT, Level.WARN, "I don't recognize vegetable type %s", type);
					return;
				}
				
				// Basically assuming there is only one player for now
				EntityPlayerMP player = (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);
				
				Vec3 playerPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ); 
				Vec3 look = player.getLook(2.0F);
				Vec3 pos = playerPos.addVector(look.xCoord, 0, look.zCoord);
				
				// Limit quantity to inventory stack size (64)
				quantity = Math.min(quantity, player.inventory.getInventoryStackLimit());
				
				dropItem(player.worldObj, pos.xCoord, pos.yCoord, pos.zCoord, item, quantity);
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			inMessage = false;
		}				
	}
}
