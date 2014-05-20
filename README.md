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

* [Minecraft](https://minecraft.net/) 1.7.2. [Download the Minecraft Launcher](https://minecraft.net/download), edit your profile, and select version 1.7.2.
* [Minecraft Forge](http://files.minecraftforge.net/) - I've been using 10.12.1.1060 - later versions *may* also work. Download the [Forge Installer](http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.7.2-10.12.1.1060/forge-1.7.2-10.12.1.1060-installer.jar) and run it (select 'Install Client') to create a Forge profile in the Minecraft launcher. Select the Forge profile.
* [The mod binary](http://metadaddy-sfdc.github.io/Forcecraft/Forcecraft-v0.2.0.jar) - Download it and drop it into the Minecraft mods directory. On a Mac, this is `~/Library/Application Support/minecraft/mods`; on Windows it is `%appdata%/.minecraft/mods`.
* A Salesforce org. [Create a free Force.com Developer Edition](http://developer.force.com/join) if you are new to Salesforce and want to try out Forcecraft.

**IMPORTANT** You MUST create a configuration file with your account credentials so that Forcecraft can authenticate to Salesforce. Use this as a template:

```
# Configuration file

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

You MUST edit the username and password entries. If you are using a Salesforce sandbox, change the loginHost to test.salesforce.com.

Run Minecraft and select the Forge profile. If all is well, you should see Forcecraft listed on the Mods screen. Start Minecraft as a single player and create a new world, in creative mode.

![Create New World](http://metadaddy-sfdc.github.io/Forcecraft/CreateNewWorld.png)

Once the game starts, you can type `/login` to teleport to the Forcecraft dimension. Type `/logout` to return to the default 'Overworld' dimension.

**Note** - if you're demoing the mod, you probably want to stop the game from pausing when you switch away to the browser or another app. To do this, edit options.txt

* If you're running via Eclipse (see below), it's at `<forge dir>/eclipse/options.txt`
* If you're running 'standalone' on a Mac, it's at `~/Library/Application Support/minecraft/options.txt`. On Windows, it's at `%appdata%\.minecraft`.

Look for the line that reads

    pauseOnLostFocus:true

Change true to false.

Building the mod
----------------

### Pre-requisites

Minecraft, etc, as listed above, under 'Running the mod'. You will need the [Minecraft Forge 10.12.1.1060 source distribution](http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.7.2-10.12.1.1060/forge-1.7.2-10.12.1.1060-src.zip). Follow the [installation process](http://www.minecraftforge.net/wiki/Installation/Source).

### Getting the source

Fetch the Forcecraft repo into the Forge root directory (the `forge/` directory created during zip extraction) like this:

	cd forge
	git init
	git remote add origin https://github.com/metadaddy-sfdc/Forcecraft.git
	git fetch
	git checkout -t origin/master

### Building the mod

    gradle build

The build process creates `build/distributions/Forcecraft-1.7.2-0.2.0-mod.jar`. This is an [uber-jar](http://stackoverflow.com/a/11947093/33905) including all the mod's dependencies, packaged so that Forge will load it correctly.

You should be able to drop this into the mods directory to play the mod with Minecraft + Forge.

### Debugging the mod

Create the Eclipse project:

    gradle eclipse

Build the mod jar (see above). In the Eclipse Project Explorer, open the `build/libs` folder, select `Forcecraft-1.7.2-0.2.0-dep.jar`, right click, and select 'Build Path | Add to Build Path'. This jar file contains all the dependencies that Eclipse needs.

Follow the instructions in the 'Running the mod' section above to configure the mod, except that the configuration file needs to be at `/<forge dir>/eclipse/config/Forcecraft.cfg`.

In Eclipse, you can use the 'Run' or 'Debug' button as appropriate.
