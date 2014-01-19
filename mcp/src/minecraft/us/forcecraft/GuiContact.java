package us.forcecraft;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.packet.Packet250CustomPayload;

import org.lwjgl.input.Keyboard;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContact extends GuiScreen {
	private int windowId;
	private String contactId;
	private String contactName;
	private JsonRootNode feed = null;
	private static final int POST_HEIGHT = 22;
	private static final int MAX_CHATTER_CHARS = 80;
	private GuiTextField textfield;
	private int yInput;

	public GuiContact(int windowId, String contactId, String contactName, String feedJson) {
		this.windowId = windowId;
		this.contactId = contactId;
		this.contactName = contactName;
        try {
			this.feed = Forcecraft.instance.client.parser.parse(feedJson);
	        System.out.println("----------------------------------------");
	        System.out.println(Forcecraft.instance.client.formatter.format(this.feed));
	        System.out.println("----------------------------------------");
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	        	// TODO: Post to Chatter
	        	System.out.println("POST!!! "+post);
	        	
	            try
	            {
	                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
	                ObjectOutputStream os = new ObjectOutputStream(bytearrayoutputstream);
	                os.writeInt(windowId);
	                os.writeObject(contactId);
	                os.writeObject(post);
	                mc.getNetHandler().addToSendQueue(new Packet250CustomPayload(Forcecraft.CHATTER_CHANNEL, bytearrayoutputstream.toByteArray()));
	            }
	            catch (Exception exception)
	            {
	                exception.printStackTrace();
	            }
	        	
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
			
		drawCenteredString(fontRenderer, contactName, width / 2, 15, 0xFFFFFF);
		
		if (feed != null) {
			int i = 0;
			for (JsonNode item: feed.getNode("items").getElements()) {
				try {
					String name = item.getStringValue("actor", "name");
					String text = item.getStringValue("body", "text");
					String date = item.getStringValue("relativeCreatedDate");
					
					drawString(fontRenderer, name, 15, 33 + (POST_HEIGHT * i), 0xFFFF00);
					drawString(fontRenderer, ": " + text, 15 + fontRenderer.getStringWidth(name), 33 + (POST_HEIGHT * i), 0xC0C0C0);
					drawString(fontRenderer, date, 15, 42 + (POST_HEIGHT * i), 0xC0C0C0);
					
					i++;
					
					if (33 + (POST_HEIGHT * i) > yInput) {
						break;
					}
				} catch (IllegalArgumentException iae) {
					// Probably no body.text - just don't render it
				}
			}
		}
		
		super.drawScreen(x, y, f);
		textfield.drawTextBox();
	}

	public void setFeed(String feedJson) {
        try {
			this.feed = Forcecraft.instance.client.parser.parse(feedJson);
	        System.out.println("----------------------------------------");
	        System.out.println(Forcecraft.instance.client.formatter.format(this.feed));
	        System.out.println("----------------------------------------");
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
