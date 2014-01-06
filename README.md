Forcecraft
==========

This Minecraft mod visualizes Salesforce Accounts, Opportunities and Contacts. Forcecraft adds a new dimension, accessed via the `/login` command, where each Account in your Salesforce org is represented by a building, with a level for each Opportunity.

![Forcecraft](http://metadaddy-sfdc.github.io/Forcecraft/Forcecraft.png)

Levers on each level represent and control the Opportunity Stage Name; updating an Opportunity's Stage Name outside Minecraft will cause the lever state to update within the Minecraft world, while throwing the lever in Minecraft will update the Opportunity's Stage Name.

![Opportunity Levers](http://metadaddy-sfdc.github.io/Forcecraft/OpportunityLevers.png)

Each Salesforce Contact is represented by a Villager-derived entity, with the Contact name shown as a custom name tag above the entity's head. 

![Contact](http://metadaddy-sfdc.github.io/Forcecraft/Contact.png)

If an Opportunity's Stage Name is updated to 'Closed Won', a Contact from the associated Account will teleport to the player and give the player items to the 'value' of the opportunity.

You may return to the default 'Overworld' dimension via the '/logout' command.

Pre-requisites
--------------

* [Minecraft](https://minecraft.net/) 1.6.4
* [Minecraft Forge](http://files.minecraftforge.net/) 1.6.4. Follow the [installation process](http://www.minecraftforge.net/wiki/Installation/Source).

Installation
------------

Clone the Forcecraft repo into the Forge root directory.

Configuration
-------------

Set the following environment variables:

* `SF_LOGINHOST` Set this to `login.salesforce.com` for a Developer Edition, or `test.salesforce.com` for a sandbox. It should go without saying that you should not run this on a production org!
* `SF_USERNAME` Salesforce username
* `SF_PASSWORD` Salesforce password

In Eclipse, you can set these in Project | Properties | Run/Debug Settings | Client | Environment.

Running the Mod
---------------

The easiest way to do this is to point an IDE at Forge, as documented in the [installation process](http://www.minecraftforge.net/wiki/Installation/Source). If all is well, you should see Forcecraft listed on the Mods screen. Start Minecraft as a single player and create a new world, in creative mode.

![Create New World](http://metadaddy-sfdc.github.io/Forcecraft/CreateNewWorld.png)

Once the game starts, you can type `/login` to teleport to the Forcecraft dimension. Type `/logout` to return to the default 'Overworld' dimension.