package us.forcecraft;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

public class EntityContact extends EntityVillager {
	public static Map<String, EntityContact> contactMap = new HashMap<String, EntityContact>();
	private static Random rand = new Random();
	private static final Class[] unwantedAI = { 
		EntityAIVillagerMate.class,
		EntityAIFollowGolem.class
	};
	String id;
	long oppyCloseTime = 0;

	public EntityContact(World par1World) {
		this(par1World, null);
	}

	public EntityContact(World par1World, String id) {
		super(par1World, rand.nextInt(6)); // 6 villager professions
		
		overrideAI();
		
		this.tasks.addTask(0, new EntityAIWaitOnOppyClose(this));
		
		this.id = id;
		addToContactMap();
	}
	
	private EntityAIBase findTask(Class taskClass) {
		for (Object o : tasks.taskEntries) {
			EntityAITaskEntry taskEntry = (EntityAITaskEntry)o;
			if (taskEntry.action.getClass().equals(taskClass)) {
				return taskEntry.action;
			}
		}
		
		return null;
	}
	
	private void overrideAI() {
		for (Class taskClass : unwantedAI) {
			EntityAIBase task = findTask(taskClass);
			if (task != null) {
				tasks.removeTask(task);
			}			
		}
	}

	private void addToContactMap() {
		contactMap.put(this.id, this);		
	}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setString("Id", this.id);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.id = par1NBTTagCompound.getString("Id");
        addToContactMap();
    }
    
	private static int[] treasureItems = {
		Item.coal.itemID,		// 1
		Item.ingotIron.itemID,  // 10 
		Item.ingotGold.itemID,  // 100
		Item.redstone.itemID,   // 1000
		Item.glowstone.itemID,  // 10000
		Item.emerald.itemID,    // 100000
		Item.diamond.itemID     // 1000000
	};
	
	public static List<int[]> getTreasure(double amount) {
		int a = (int)amount, n;
		List<int[]> l = new ArrayList<int[]>();
		
		for (int i = 0; (i < (treasureItems.length - 1) && a > 0); i++) {
			n = a % 10;
			if (n > 0) {
				l.add(new int[]{treasureItems[i], n});
			}
			a /= 10;
		}
		
		if (a > 0) {
			l.add(new int[]{treasureItems[treasureItems.length - 1], a});
		}
		
		return l;
	}
	
    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        if (this.isEntityAlive() && !par1EntityPlayer.isSneaking())
        {
            if (!this.worldObj.isRemote)
            {
            	displayChatterGUI((EntityPlayerMP)par1EntityPlayer);
            }

            return true;
        }
        else
        {
        	return false;
        }
    }

	private void displayChatterGUI(EntityPlayerMP player) {
		player.incrementWindowID();
		
		showChatter(player, player.currentWindowId, id, getCustomNameTag());
	}
	
	public static void showChatter(EntityPlayerMP player, int windowId, String contactId, String contactName) {
        try
        {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bytearrayoutputstream);
            os.writeInt(windowId);
            os.writeObject(contactId);
            os.writeObject(contactName);
            os.writeObject(Forcecraft.instance.client.getFeed(contactId));
            player.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload(Forcecraft.CONTACT_CHANNEL, bytearrayoutputstream.toByteArray()));
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }		
	}
	
	public long getOppyCloseTime() {
		return this.oppyCloseTime;
	}
	
	public void setOppyCloseTime(long time) {
		this.oppyCloseTime = time;
	}
}
