Forcecraft
==========

This Minecraft mod visualizes [Salesforce](https://www.salesforce.com/crm/) Accounts, Opportunities and Contacts. Forcecraft adds a new dimension to Minecraft, where each Account in your Salesforce environment (aka 'org') is represented by a building, comprising a level for each Opportunity associated with that Account.

![Forcecraft](http://metadaddy-sfdc.github.io/Forcecraft/Forcecraft.png)

Levers on each level represent and control the Opportunity Stage Name; updating an Opportunity's Stage Name outside Minecraft will cause the lever state to update within the Minecraft world, while throwing the lever in Minecraft will update the Opportunity's Stage Name.

![Opportunity Levers](http://metadaddy-sfdc.github.io/Forcecraft/OpportunityLevers.png)

Each Salesforce Contact is represented by a Villager-derived entity, with the Contact name shown as a custom name tag above the entity's head. 

![Contact](http://metadaddy-sfdc.github.io/Forcecraft/Contact.png)

Interacting with a Contact (right-click, by default) will show the Chatter feed for that Contact and allow you to post to the feed. Similarly, Account and Opportunity signs give access to the respective Chatter feeds.

![Contact Feed](http://metadaddy-sfdc.github.io/Forcecraft/ContactFeed.png)

If an Opportunity's Stage Name is updated to 'Closed Won', a Contact from the associated Account will teleport to the player and give the player items to the 'value' of the opportunity. If, on the other hand, an Opportunity's Stage Name is updated to 'Closed Lost', the weather will take a turn for the worse!

Take a look at the mod in action:

[![YouTube video](http://img.youtube.com/vi/eb3GgM1o_8I/0.jpg)](http://www.youtube.com/watch?v=eb3GgM1o_8I)

Running the mod
---------------

### Pre-requisites

* [Minecraft](https://minecraft.net/) 1.6.4
* [Minecraft Forge](http://files.minecraftforge.net/) - I've been using 9.11.1.953 - later versions *may* also work. Download the [Forge Installer](http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.6.4-9.11.1.953/forge-1.6.4-9.11.1.953-installer.jar) and run it to create a Forge profile in the Minecraft launcher.
* [The mod binary](http://metadaddy-sfdc.github.io/Forcecraft/Forcecraft-v0.1.7.jar) - Download it and drop it into the Minecraft mods directory. On a Mac, this is `~/Library/Application Support/minecraft/mods`; on Windows it is `%appdata%/.minecraft/mods`.
* A Salesforce org. [Create a free Force.com Developer Edition](http://developer.force.com/join) if you are new to Salesforce and want to try out Forcecraft.

**IMPORTANT** You MUST create a configuration file with your account credentials so that Forcecraft can authenticate to Salesforce. Use this as a template:

```
# Configuration file

####################
# block
####################

block {
    I:stage=3500
    I:chatterSign=3501
}


####################
# general
####################

general {
    I:dimensionId=7
    S:loginHost=login.salesforce.com
    S:username=user@example.com    
    S:password=p455w0rd
}
```

Edit this, and save it as

* Mac: `~/Library/Application Support/minecraft/config/Forcecraft.cfg`
* Windows: `%appdata%/.minecraft/config/Forcecraft.cfg`

You MUST edit the username and password entries. If you are using a Salesforce sandbox, change the loginHost to test.salesforce.com. If you are using another mod that has already taken the 3500+ block IDs, you can also modify that.

Run Minecraft and select the Forge profile. If all is well, you should see Forcecraft listed on the Mods screen. Start Minecraft as a single player and create a new world, in creative mode.

![Create New World](http://metadaddy-sfdc.github.io/Forcecraft/CreateNewWorld.png)

Once the game starts, you can type `/login` to teleport to the Forcecraft dimension. Type `/logout` to return to the default 'Overworld' dimension.

Building the mod
----------------

### Pre-requisites

Minecraft, etc, as listed above, under 'Running the mod'. You will need the [Minecraft Forge 9.11.1.953 source distribution](http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.6.4-9.11.1.953/forge-1.6.4-9.11.1.953-src.zip). Follow the [installation process](http://www.minecraftforge.net/w/index.php?title=Installation/Source&oldid=2082).

### Getting the source

Fetch the Forcecraft repo into the Forge root directory (the `forge/` directory created during zip extraction) like this:

	cd forge
	git init
	git remote add origin https://github.com/metadaddy-sfdc/Forcecraft.git
	git fetch
	git checkout -t origin/master

### Building the mod

The easiest way to do this is to point an IDE at Forge, as documented in the [Forge installation process](http://www.minecraftforge.net/w/index.php?title=Installation/Source&oldid=2082). I'll give instructions here for working with Eclipse.

In the Eclipse Project Explorer, open the Minecraft/lib folder, select all the listed jars, right click, and select 'Build Path | Add to Build Path'. Eclipse should now build the project with no errors (but possibly a handful of warnings).

![Add jars to build path](http://metadaddy-sfdc.github.io/Forcecraft/AddJarsToBuildPath.png)

### Debugging the mod

Follow the instructions in the 'Running the mod' section above to configure the mod, except that the configuration file needs to be at `/forge/mcp/jars/config/Forcecraft.cfg`.

In Eclipse, you can use the 'Run' or 'Debug' button as appropriate.

### Packaging the mod as a jar file

In a Terminal window, navigate to `/forge/mcp` and type `./makemodjar.sh`. This will create Forcecraft-vx.x.x.jar in the /forge directory. You should be able to drop this into the mods directory to play the mod with Minecraft + Forge.