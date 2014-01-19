package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientPacketHandler implements IPacketHandler {
	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(Forcecraft.CONTACT_CHANNEL)) {
            handleContact(packet, player);
		}
	}

	private void handleContact(Packet250CustomPayload packet, Player player) {
        int windowId;
        String contactId;
        String contactName;
        String feedJson;
        
        try {
    		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.data));
            
        	windowId = inputStream.readInt();
        	contactId = (String)inputStream.readObject();
        	contactName = (String)inputStream.readObject();
        	feedJson = (String)inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP entityclientplayermp = (EntityClientPlayerMP)player;
        GuiScreen guiscreen = mc.currentScreen;

        if (guiscreen != null && guiscreen instanceof GuiContact && windowId == entityclientplayermp.openContainer.windowId) {
        	GuiContact guiContact = (GuiContact)guiscreen;
        	guiContact.setFeed(feedJson);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiContact(windowId, contactId, contactName, feedJson));
            entityclientplayermp.openContainer.windowId = windowId;        	
        }        
	}

}
