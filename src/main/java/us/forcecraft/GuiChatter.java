package us.forcecraft;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayerMP;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChatter extends GuiScreen {
	public static class ChatterEntry implements Serializable {
		String name;
		String text;
		String date;
		
		public ChatterEntry(String name, String text, String date) {
			this.name = name;
			this.text = text;
			this.date = date;
		}
		
		public static List<ChatterEntry> makeEntries(String feedJson) throws InvalidSyntaxException {
            JsonRootNode feed = Forcecraft.instance.client.parser.parse(feedJson);
        	List<JsonNode> items = feed.getNode("items").getElements();
        	List<ChatterEntry> chatterEntries = new ArrayList<ChatterEntry>();
        	int count = Math.min(items.size(), 20);
			for (int i = 0; i < count; i++) {
				JsonNode item = items.get(i);
				String text;
				String name;
				try {
					text = item.getStringValue("body", "text");
					name = item.getStringValue("actor", "name");
				} catch (IllegalArgumentException iae) {
					// no body.text - use preamble with no name
					text = item.getStringValue("preamble", "text");
					name = "";
				}
				String date = item.getStringValue("relativeCreatedDate");
				chatterEntries.add(new ChatterEntry(name, text, date));
			}
			return chatterEntries;
		}
	}
	
	private int windowId;
	private String id;
	private String name;
	private List<ChatterEntry> feed = null;
	private static final int POST_HEIGHT = 22;
	private static final int MAX_CHATTER_CHARS = 80;
	private GuiTextField textfield;
	private int yInput;
	private FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

	public GuiChatter(int windowId, String id, String name, List<ChatterEntry> feed) {
		this.windowId = windowId;
		this.id = id;
		this.name = name;
		this.feed = feed;
	}
	
	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        
        String shareText = "Share";
        yInput = this.height - 35;
        int buttonWidth = fontRenderer.getStringWidth(shareText) + 30;
        
        buttonList.add(new GuiButton(0, this.width - (15 + buttonWidth), yInput, buttonWidth, 20, shareText));
		textfield = new GuiTextField(fontRenderer, 15, yInput, this.width - (45 + buttonWidth), 20);
		textfield.setMaxStringLength(MAX_CHATTER_CHARS);
		textfield.setFocused(true);
		
		((GuiButton)this.buttonList.get(0)).enabled = false;
	}
	
    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
	public void keyTyped(char c, int i){
		super.keyTyped(c, i);
		textfield.textboxKeyTyped(c, i);
        ((GuiButton)this.buttonList.get(0)).enabled = textfield.getText().trim().length() > 0;

        if (i == 28 || i == 156)
        {
            actionPerformed((GuiButton)this.buttonList.get(0));
        }
	}
	
	@Override
	public void mouseClicked(int i, int j, int k){
		super.mouseClicked(i, j, k);
		textfield.mouseClicked(i, j, k);
	}

	@Override
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
        	String post = textfield.getText().trim();
        	
        	if (post.length() > 0) { 
        		FMLLog.log(Forcecraft.FORCECRAFT, Level.INFO, "Posting to Chatter: %s", post);
	        	
	        	PacketChatterRequest packet = new PacketChatterRequest(windowId, id, post);
	        	Forcecraft.instance.packetPipeline.sendToServer(packet);
	        	
	        	textfield.setText("");
	        	((GuiButton)this.buttonList.get(0)).enabled = false;
        	}
        }
    }

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void drawScreen(int x, int y, float f)
	{
		drawDefaultBackground();
			
		drawCenteredString(fontRenderer, name, width / 2, 15, 0xFFFFFF);
		
		if (feed != null) {
			int i = 0;
			for (ChatterEntry item: feed) {
				drawString(fontRenderer, item.name, 15, 33 + (POST_HEIGHT * i), 0xFFFF00);
				drawString(fontRenderer, ((item.name.length() > 0) ? ": " : "") + item.text, 15 + fontRenderer.getStringWidth(item.name), 33 + (POST_HEIGHT * i), 0xC0C0C0);
				drawString(fontRenderer, item.date, 15, 42 + (POST_HEIGHT * i), 0xC0C0C0);
				
				i++;
				
				if (33 + (POST_HEIGHT * i) > yInput) {
					break;
				}
			}
		}
		
		super.drawScreen(x, y, f);
		textfield.drawTextBox();
	}

	public void setFeed(List<ChatterEntry> chatterEntries) {
		this.feed = chatterEntries;
	}
	
	// These should be on the server!
	public static void displayChatterGUI(EntityPlayerMP player, String id, String name) {
		player.getNextWindowId();
		
		showChatter(player, player.currentWindowId, id, name);
	}	
	
	public static void showChatter(EntityPlayerMP player, int windowId, String id, String name) {
			try {
				List<ChatterEntry> chatterEntries = ChatterEntry.makeEntries(Forcecraft.instance.client.getFeed(id));
	            PacketChatterResponse packet = new PacketChatterResponse(windowId, name, id, chatterEntries);
	            Forcecraft.instance.packetPipeline.sendTo(packet, player);
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
	}	
}
