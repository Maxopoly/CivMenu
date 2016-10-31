package vg.civcraft.mc.civmenu.datamanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmenu.CivMenu;
import vg.civcraft.mc.civmenu.TermObject;
import vg.civcraft.mc.civmenu.database.Database;

public class MysqlManager implements ISaveLoad{

	private CivMenu plugin;
	private FileConfiguration config;
	private Database db;
	
	private String insertData, getAllData;
	
	private Map<UUID, TermObject> registeredPlayers = new HashMap<UUID, TermObject>();
	
	public MysqlManager(CivMenu plugin) {
		this.plugin = plugin;
		config = plugin.getConfig();
		initializeStrings();
	}
	
	private void initializeStrings() {
		insertData = "insert into civ_menu_data(uuid, term) values (?,?);";
		getAllData = "select * from civ_menu_data;";
	}
	
	private void loadDB() {
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
		db.execute("create table if not exists civ_menu_data("
				+ "uuid varchar(36) not null,"
				+ "term varchar(255) not null,"
				+ "primary key uuid_info(uuid, term));");
	}

	@Override
	public void load() {
		loadDB();
		PreparedStatement playerData = db.prepareStatement(getAllData);
		ResultSet set;
		try {
			set = playerData.executeQuery();
			while (set.next()) {
				UUID uuid = UUID.fromString(set.getString("uuid"));
				if (!registeredPlayers.containsKey(uuid)) 
					registeredPlayers.put(uuid, new TermObject(uuid));
				registeredPlayers.get(uuid).addTerm(set.getString("term"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		// No need to save, all done on playeradd.
	}

	@Override
	public void addPlayer(Player p, final String term) {
		final UUID uuid = p.getUniqueId();
		if (!registeredPlayers.containsKey(uuid)){
			registeredPlayers.put(uuid, new TermObject(uuid));
			registeredPlayers.get(uuid).addTerm(term);
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

				@Override
				public void run() {
					final PreparedStatement addPlayer = db.prepareStatement(insertData);
					try {
						addPlayer.setString(1, uuid.toString());
						addPlayer.setString(2, term);
						addPlayer.execute();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				
			});
		}
	}
	
	@Override
	public void setUUID(UUID uuid, String term){
		if (!registeredPlayers.containsKey(uuid))
			registeredPlayers.put(uuid, new TermObject(uuid));
		registeredPlayers.get(uuid).addTerm(term);
	}

	@Override
	public boolean isAddedPlayer(Player p, String term) {
		return registeredPlayers.get(p.getUniqueId()) != null && registeredPlayers.get(p.getUniqueId()).hasTerm(term);
	}
}
