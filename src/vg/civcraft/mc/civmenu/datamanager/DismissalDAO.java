package vg.civcraft.mc.civmenu.datamanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import vg.civcraft.mc.civmenu.CivMenu;
import vg.civcraft.mc.civmenu.database.Database;

public class DismissalDAO {

	private static DismissalDAO instance;
	
	private Database db;
	private String tableName;
	
	private DismissalDAO(String plugin) {
		tableName = plugin + "_dismissals";
	}
	
	private void initializeDatabase() {
		CivMenu plugin = CivMenu.getInstance();
		FileConfiguration config = plugin.getConfig();
		String username = config.getString("mysql.username");
		String password = config.getString("mysql.password");
		String host = config.getString("mysql.host");
		String dbname = config.getString("mysql.dbname");
		int port = config.getInt("mysql.port");
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		if (!db.connect()) {
			plugin.getLogger().log(Level.INFO, "Mysql could not connect, shutting down.");
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
		createTables();
	}
	
	private void createTables() {
		db.execute("create table if not exists " + tableName + " ("
					+ "event VARCHAR(40) not null,"
					+ "player VARCHAR(40) not null)");
	}
	
	public List<String> getDismissals(UUID id) {
		List<String> events = new ArrayList<String>();
		try {
			PreparedStatement getDismissals = db.prepareStatement("SELECT * FROM " + tableName + " WHERE player = ?");
			getDismissals.setString(1, id.toString());
			ResultSet result = getDismissals.executeQuery();
			while(result.next()) {
				events.add(result.getString("event"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return events;
	}
	
	public void dismissEvent(String event, UUID player) {
		try {
			PreparedStatement dismiss = db.prepareStatement("INSERT INTO " + tableName + " (event, player) VALUES (?,?)");
			dismiss.setString(1, event);
			dismiss.setString(2, player.toString());
			dismiss.execute();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static DismissalDAO getInstance(String plugin) {
		if(instance == null) {
			instance = new DismissalDAO(plugin);
			instance.initializeDatabase();
		}
		return instance;
	}
}
