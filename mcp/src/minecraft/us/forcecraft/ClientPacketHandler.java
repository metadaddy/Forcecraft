package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import us.forcecraft.GuiContact.ChatterEntry;
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
		if (packet.channel.equals(Forcecraft.CHATTER_CHANNEL)) {
            handleChatter(packet, player);
		}
	}

	private void handleChatter(Packet250CustomPayload packet, Player player) {
        int windowId;
        String contactId;
        String contactName;
        List<GuiContact.ChatterEntry> chatterEntries;
        
        try {
    		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.data));
            
        	windowId = inputStream.readInt();
        	contactId = (String)inputStream.readObject();
        	contactName = (String)inputStream.readObject();
        	chatterEntries = (List<ChatterEntry>)inputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP entityclientplayermp = (EntityClientPlayerMP)player;
        GuiScreen guiscreen = mc.currentScreen;

        if (guiscreen != null && guiscreen instanceof GuiContact && windowId == entityclientplayermp.openContainer.windowId) {
        	GuiContact guiContact = (GuiContact)guiscreen;
        	guiContact.setFeed(chatterEntries);
        } else {
            Minecraft.getMinecraft().displayGuiScreen(new GuiContact(windowId, contactId, contactName, chatterEntries));
            entityclientplayermp.openContainer.windowId = windowId;        	
        }        
	}

}
