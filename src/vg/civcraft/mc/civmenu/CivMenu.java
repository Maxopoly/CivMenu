package vg.civcraft.mc.civmenu;

import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmenu.database.TOSManager;
import vg.civcraft.mc.civmenu.donators.DonatorDataFileLoader;
import vg.civcraft.mc.civmenu.guides.DismissalCacheListener;
import vg.civcraft.mc.civmenu.guides.ResponseManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.mercury.MercuryAPI;

public class CivMenu extends ACivMod {
	
	private TOSManager tosManager;
	private static CivMenu plugin;
	
	public void onEnable() {
		super.onEnable();
		plugin = this;
		tosManager = new TOSManager(this);
		DonatorDataFileLoader.loadDataAndInitEverything();
		ResponseManager.initWildcardDismissals();
		getServer().getPluginManager().registerEvents(new TOSListener(), this);
		getServer().getPluginManager().registerEvents(new DismissalCacheListener(), this);
		if (getServer().getPluginManager().isPluginEnabled("Mercury")){
			getServer().getPluginManager().registerEvents(new MercuryListener(), plugin);
			MercuryAPI.registerPluginMessageChannel("civmenu");
		}
		CommandHandler commandHandler = new CommandHandler(this);
		for (String command : getDescription().getCommands().keySet()) {
			getCommand(command).setExecutor(commandHandler);
		}
		
	}
	
    public void onDisable() { 
    	tosManager.save();
    }

    
    public void SendHelpMenu(Player player, JavaPlugin plugin){
		Menu menu = new Menu();
		FileConfiguration config = getConfig();

		if (plugin == null) {
			TextComponent title = new TextComponent("Civcraft Help Menu");
			title.setColor(ChatColor.RED);
			menu.setTitle(title);
			menu.setSubTitle(new TextComponent(config.getString("helpMenu.message")));
			String[] plugins = config.getString("helpMenu.plugins").split(", ");
			for(String pluginName:plugins){
				TextComponent part = new TextComponent(pluginName);
				part.setColor(ChatColor.YELLOW);
				part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + pluginName));
				menu.addPart(part);
			}
			menu.sendPlayer(player);
			return;
		}
		
		menu.setTitle(new TextComponent(plugin.getName()));
		
		if (plugin.getDescription().getDescription() != null) {
			menu.setSubTitle(new TextComponent(plugin.getDescription()
					.getDescription()));
		}
		if (plugin.getDescription().getCommands() != null) {
			for (String commandName : plugin.getDescription().getCommands()
					.keySet()) {
				Command command = plugin.getCommand(commandName);
				if (command.getPermission() != null
						&& !player.hasPermission(command.getPermission())) {
					continue;
				}
				TextComponent part = new TextComponent(command.getLabel());
				part.setColor(ChatColor.YELLOW);
				part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(command.getUsage() + " \n"
								+ command.getDescription()).create()));
				part.setClickEvent(new ClickEvent(
						ClickEvent.Action.SUGGEST_COMMAND, "/" + command.getLabel()));
	
				menu.addPart(part);
			}
		}
		
		menu.sendPlayer(player);
    	
    }
    
	public TOSManager getTosManager() {
		return tosManager;
	}

	@Override
	protected String getPluginName() {
		return "CivMenu";
	}
	
	public static CivMenu getInstance() {
		return plugin;
	}
    
}
