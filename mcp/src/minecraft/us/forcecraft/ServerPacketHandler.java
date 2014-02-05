package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(Forcecraft.CHATTER_CHANNEL)) {
            handleChatter(packet, player);
		}
	}

	private void handleChatter(Packet250CustomPayload packet, Player player) {
		int windowId;
        String contactId;
        String post;
        
        try {
    		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.data));
            
        	windowId = inputStream.readInt();
        	contactId = (String)inputStream.readObject();
        	post = (String)inputStream.readObject();
        	
        	Forcecraft.instance.client.postToChatter(contactId, post);
        	
        	// Update chatter GUI
            GuiChatter.showChatter((EntityPlayerMP)player, windowId, contactId, "");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
                
        System.out.println(contactId + " " + post);
	}

}
