package us.forcecraft;

import net.minecraft.client.gui.GuiScreen;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class GuiContact extends GuiScreen {

	private String contactName;
	private JsonRootNode feed = null;
	private static final int POST_HEIGHT = 22;

	public GuiContact(String contactName, String feedJson) {
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
	public void drawScreen(int x, int y, float f)
	{
		drawDefaultBackground();
	
		super.drawScreen(x, y, f);
		
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
					
					if (33 + (POST_HEIGHT * i) > height) {
						break;
					}
				} catch (IllegalArgumentException iae) {
					// Probably no body.text - just don't render it
				}
			}
		}
	}
}
