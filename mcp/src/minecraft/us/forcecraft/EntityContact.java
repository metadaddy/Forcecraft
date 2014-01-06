package us.forcecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class EntityContact extends EntityVillager {
	public static Map<String, EntityContact> contactMap = new HashMap<String, EntityContact>();
	private static Random rand = new Random();
	String id;

	public EntityContact(World par1World) {
		super(par1World);
		
		this.id = null;
	}

	public EntityContact(World par1World, String id) {
		super(par1World, rand.nextInt(6)); // 6 villager professions
		
		this.id = id;
		addToContactMap();
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
}
