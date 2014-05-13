package us.forcecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketChatterRequest extends AbstractPacket {

	int windowId;
    String recordId;
    String post;

    public PacketChatterRequest()
    {

    }

    public PacketChatterRequest(int windowId, String recordId, String post)
    {
        this.windowId = windowId;
        this.recordId = recordId;
        this.post = post;
    }

    @Override
    public void encodeInto (ChannelHandlerContext ctx, ByteBuf buffer)
    {
        ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new ByteBufOutputStream(buffer));
	        os.writeInt(windowId);
	        os.writeObject(recordId);
	        os.writeObject(post);
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
		  	post = (String)inputStream.readObject();
		  	
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

    }

    @Override
    public void handleServerSide (EntityPlayer player)
    {
    	Forcecraft.instance.client.postToChatter(recordId, post);
    	
    	// Update chatter GUI
        GuiChatter.showChatter((EntityPlayerMP)player, windowId, recordId, "");
    }

}