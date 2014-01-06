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
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid="Forcecraft", name="Forcecraft", version="0.1.0")
@NetworkMod(clientSideRequired=true)
public class Forcecraft {
	// Mod constants
	public static int dimensionId = 7;
	public static int groundLevel = 8;
	
	private ForcecraftTeleporter teleporter = null;
	
	public JsonNode accounts = null;
	public List<JsonNode> stages = null;
	
	public ForceRestClient client = new ForceRestClient();
	
	// Forge Mod instance
	@Instance(value = "Forcecraft")
	public static Forcecraft instance;

	@SidedProxy(clientSide="us.forcecraft.client.ClientProxy", serverSide="us.forcecraft.CommonProxy")
	public static CommonProxy proxy;
	
	static Block stageBlock;
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer(); //Gets current server
		ICommandManager command = server.getCommandManager(); //Gets the command manager to use for server
		ServerCommandManager serverCommand = ((ServerCommandManager) command); //Turns it into another form to use
				
		serverCommand.registerCommand(new LoginCommand());
		serverCommand.registerCommand(new LogoutCommand());
		
		teleporter = new ForcecraftTeleporter(MinecraftServer.getServer().worldServerForDimension(dimensionId));
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Stub Method
		try {
			client.login(System.getenv("SF_USERNAME"), System.getenv("SF_PASSWORD"));

			client.getId();
			
			this.accounts = client.getAccounts();
			this.stages = client.getStages();
								
			StreamingClient.subscribe(client.oauth.getStringValue("instance_url"), client.oauth.getStringValue("access_token"));
		} catch (Exception e) {
			e.printStackTrace();
			//player.addChatMessage("Exception: "+e.getMessage());
		}
		
		// StageBlock is a special stone block associated with an opportunity stage
		stageBlock = new StageBlock(500, Material.rock)
        	.setHardness(0.5F)
        	.setStepSound(Block.soundStoneFootstep)
        	.setUnlocalizedName("stage")
        	.setCreativeTab(CreativeTabs.tabBlock)
        	.setTextureName("stone");
		LanguageRegistry.instance().addStringLocalization("stage", "en_US",  "Stage");
        GameRegistry.registerBlock(stageBlock, "stage");
        
        GameRegistry.registerTileEntity(TileEntityStageBlock.class, "Stage");
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
		
		GameRegistry.registerWorldGenerator(new ForcecraftGenerator());
		
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
}
