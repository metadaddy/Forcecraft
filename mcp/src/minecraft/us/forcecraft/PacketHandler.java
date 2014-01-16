package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import argo.jdom.JsonNode;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
public class PacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(Forcecraft.CONTACT_CHANNEL)) {
            handleContact(packet, player);
		}
	}

	private void handleContact(Packet250CustomPayload packet, Player player) {
        int windowId;
        String contactName;
        String feedJson;
        
        try {
    		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.data));
            
        	windowId = inputStream.readInt();
        	contactName = (String)inputStream.readObject();
        	feedJson = (String)inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        EntityClientPlayerMP entityclientplayermp = (EntityClientPlayerMP)player;
        
        Minecraft.getMinecraft().displayGuiScreen(new GuiContact(contactName, feedJson));
        entityclientplayermp.openContainer.windowId = windowId;
        
        System.out.println(windowId + " " + contactName);
	}

}
