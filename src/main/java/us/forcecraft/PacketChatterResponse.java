package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import us.forcecraft.GuiChatter.ChatterEntry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketChatterResponse extends AbstractPacket {

	int windowId;
    String recordId;
    String name;
    List<ChatterEntry> chatterEntries;

    public PacketChatterResponse()
    {

    }

    public PacketChatterResponse(int windowId, String name, String recordId, List<ChatterEntry> chatterEntries)
    {
        this.windowId = windowId;
        this.recordId = recordId;
        this.name = name;
        this.chatterEntries = chatterEntries;
    }

    @Override
    public void encodeInto (ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new ByteBufOutputStream(buffer));
	        os.writeInt(windowId);
	        os.writeObject(recordId);
	        os.writeObject(name);
	        os.writeObject(chatterEntries);
	        os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void decodeInto (ChannelHandlerContext ctx, ByteBuf buffer)
    {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(new ByteBufInputStream(buffer));
		      
			windowId = inputStream.readInt();
			recordId = (String)inputStream.readObject();
			name = (String)inputStream.readObject();
			chatterEntries = (List<ChatterEntry>)inputStream.readObject();
			
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void handleClientSide (EntityPlayer player)
    {
      Minecraft mc = Minecraft.getMinecraft();
      EntityClientPlayerMP entityclientplayermp = (EntityClientPlayerMP)player;
      GuiScreen guiscreen = mc.currentScreen;

      if (guiscreen != null && guiscreen instanceof GuiChatter && windowId == entityclientplayermp.openContainer.windowId) {
      	GuiChatter guiContact = (GuiChatter)guiscreen;
      	guiContact.setFeed(chatterEntries);
      } else {
          Minecraft.getMinecraft().displayGuiScreen(new GuiChatter(windowId, recordId, name, chatterEntries));
          entityclientplayermp.openContainer.windowId = windowId;        	
      }        
    }

    @Override
    public void handleServerSide (EntityPlayer player)
    {
    }

}