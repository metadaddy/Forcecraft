package us.forcecraft;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
	public static void init(File configFile) {
		Configuration config = new Configuration(configFile);
		
		config.load();
		
		Forcecraft.loginHost = config.get(Configuration.CATEGORY_GENERAL, Forcecraft.LOGIN_HOST_KEY, "").getString();
		Forcecraft.username = config.get(Configuration.CATEGORY_GENERAL, Forcecraft.USERNAME_KEY, "").getString();
		Forcecraft.password = config.get(Configuration.CATEGORY_GENERAL, Forcecraft.PASSWORD_KEY, "").getString();
		
		Forcecraft.dimensionId = config.get(Configuration.CATEGORY_GENERAL, Forcecraft.DIMENSION_ID_NAME, Forcecraft.DIMENSION_ID_DEFAULT).getInt();

		config.save();
	}
}