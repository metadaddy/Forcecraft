package us.forcecraft;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Teleporter;
import net.minecraftforge.common.DimensionManager;
import argo.jdom.JsonNode;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid=Forcecraft.FORCECRAFT, name=Forcecraft.FORCECRAFT, version="0.1.5")
@NetworkMod(clientSideRequired=true, 
	clientPacketHandlerSpec = @SidedPacketHandler(channels = { Forcecraft.CONTACT_CHANNEL }, packetHandler = ClientPacketHandler.class), 
	serverPacketHandlerSpec = @SidedPacketHandler(channels = { Forcecraft.CHATTER_CHANNEL }, packetHandler = ServerPacketHandler.class)) 
public class Forcecraft {
	// Mod constants
	public static final String FORCECRAFT = "Forcecraft";
	public static final int DIMENSION_ID_DEFAULT = 7;
	public static int dimensionId = DIMENSION_ID_DEFAULT;
	public static final int STAGE_BLOCK_ID_DEFAULT = 3500;
	public static int stageBlockId = STAGE_BLOCK_ID_DEFAULT;
	
	public static final String STAGE_BLOCK_NAME = "stage";
	public static final String DIMENSION_ID_NAME = "dimensionId";
	public static final String LOGIN_HOST_KEY = "loginHost";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String CONTACT_CHANNEL = "FC|Contact";
	public static final String CHATTER_CHANNEL = "FC|Chatter";

	public static int groundLevel = 8;
	
	private ForcecraftTeleporter teleporter = null;
	
	public JsonNode accounts = null;
	public List<JsonNode> stages = null;
	
	public static String loginHost;
	public static String username;
	public static String password;
	
	public ForceRestClient client = null;
	
	// Forge Mod instance
	@Instance(value = "Forcecraft")
	public static Forcecraft instance;

	@SidedProxy(clientSide="us.forcecraft.client.ClientProxy", serverSide="us.forcecraft.CommonProxy")
	public static CommonProxy proxy;
	
	static Block stageBlock;
	
	public ForcecraftTickHandler tickHandler = new ForcecraftTickHandler();
	public ForcecraftGenerator generator = new ForcecraftGenerator();
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer(); //Gets current server
		ICommandManager command = server.getCommandManager(); //Gets the command manager to use for server
		ServerCommandManager serverCommand = ((ServerCommandManager) command); //Turns it into another form to use
				
		serverCommand.registerCommand(new CommandLogin());
		serverCommand.registerCommand(new CommandLogout());
		
		teleporter = new ForcecraftTeleporter(MinecraftServer.getServer().worldServerForDimension(dimensionId));
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		
		// StageBlock is a special stone block associated with an opportunity stage
		stageBlock = new StageBlock(stageBlockId, Material.rock)
        	.setHardness(0.5F)
        	.setStepSound(Block.soundStoneFootstep)
        	.setUnlocalizedName(STAGE_BLOCK_NAME)
        	.setCreativeTab(CreativeTabs.tabBlock)
        	.setTextureName("stone");
		LanguageRegistry.instance().addStringLocalization(STAGE_BLOCK_NAME, "en_US",  "Stage");
        GameRegistry.registerBlock(stageBlock, STAGE_BLOCK_NAME);
        
        GameRegistry.registerTileEntity(TileEntityStageBlock.class, STAGE_BLOCK_NAME);
        
        TickRegistry.registerTickHandler(tickHandler, Side.SERVER);
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
		
		GameRegistry.registerWorldGenerator(generator);
		
		EntityRegistry.registerGlobalEntityID(EntityContact.class, "Contact", EntityRegistry.findGlobalUniqueEntityId());
		DimensionManager.registerProviderType(dimensionId, ForcecraftWorldProvider.class, false);
		DimensionManager.registerDimension(dimensionId, dimensionId);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method
	}
	
    public Teleporter getDefaultTeleporter()
    {
        return this.teleporter;
    }
    
	@EventHandler
	public void aboutToStart(FMLServerAboutToStartEvent event)
	{
		try {
			if (client == null) {
				client = new ForceRestClient();
				
				client.login(loginHost, username, password);

				client.getId();
				
				// TODO - loadingScreen.resetProgresAndWorkingMessage(I18n.getString(s2));
				
				accounts = client.getAccounts();
				stages = client.getStages();
				
				// TODO - refactor
				if (!client.streamingTopicExists(StreamingClient.OPPORTUNITY_TOPIC_NAME)) {
					client.createStreamingTopic(StreamingClient.OPPORTUNITY_TOPIC_NAME, StreamingClient.OPPORTUNITY_TOPIC_QUERY);
				}
									
				if (!client.streamingTopicExists(StreamingClient.ACCOUNT_TOPIC_NAME)) {
					client.createStreamingTopic(StreamingClient.ACCOUNT_TOPIC_NAME, StreamingClient.ACCOUNT_TOPIC_QUERY);
				}
									
				StreamingClient.subscribe(client.oauth.getStringValue("instance_url"), 
						client.oauth.getStringValue("access_token"));				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
