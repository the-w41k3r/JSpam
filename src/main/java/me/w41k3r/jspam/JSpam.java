package me.w41k3r.jspam;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static me.w41k3r.jspam.Utils.getStr;
import static me.w41k3r.jspam.Utils.msg;


public class JSpam extends Plugin implements Listener {
    private static JSpam instance;


    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Configuration config;

    private String prefix;


    @Override
    public void onEnable() {

        //If Jpremium not fou
//        if (getProxy().getPluginManager().getPlugin("JPremium") == null) {
//            getLogger().severe("JPremium not found! Disabling plugin...");
//            return;
//        }

        // Load configuration from config.yml
        loadConfig();

        // Register listener
        getProxy().getPluginManager().registerListener(this, this);

        getProxy().getPluginManager().registerCommand(this, new AddEmailCommand(this));

        instance = this;


        getLogger().info("");
        getLogger().info("     ██╗███████╗██████╗  █████╗ ███╗   ███╗");
        getLogger().info("     ██║██╔════╝██╔══██╗██╔══██╗████╗ ████║");
        getLogger().info("     ██║███████╗██████╔╝███████║██╔████╔██║");
        getLogger().info("██   ██║╚════██║██╔═══╝ ██╔══██║██║╚██╔╝██║");
        getLogger().info("╚█████╔╝███████║██║     ██║  ██║██║ ╚═╝ ██║");
        getLogger().info(" ╚════╝ ╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝     ╚═╝");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("Enabling JSpam!");

    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ignored) {}


        // Get MySQL credentials from configuration file
        host = config.getString("mysql.host", "localhost");
        port = config.getInt("mysql.port", 3306);
        database = config.getString("mysql.database", "mydatabase");
        username = config.getString("mysql.username", "myusername");
        password = config.getString("mysql.password", "mypassword");

    }

    private boolean hasValue(ProxiedPlayer player, String value) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Get a connection to the MySQL database
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

            // Prepare a statement to check if the user has the specified value in the database
            statement = connection.prepareStatement("SELECT " + value + " FROM user_profiles WHERE lastNickname=?");
            statement.setString(1, player.getName());

            // Execute the query and get the result set
            resultSet = statement.executeQuery();

            // Check if the result set contains a value for the specified column
            return resultSet.next() && resultSet.getString(value) != null;
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error checking value for player " + player.getName(), e);
            return false;
        } finally {
            // Close the result set, statement, and connection
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }



    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!(hasValue(player, "mailAddress"  ))) {
                player.sendMessage(getStr("no-email"));

                if(getConfig().getBoolean("spam-to-add-email")){

                    getProxy().getScheduler().schedule(this, new Runnable() {
                        @Override
                        public void run() {
                            player.sendMessage(getStr("no-email"));
                        }
                    }, getConfig().getInt("spam-interval"), getConfig().getInt("spam-interval"), TimeUnit.SECONDS);

                }
            }
    }

    public Configuration getConfig() {
        return config;
    }


    public static JSpam getInstance() {
        return instance;
    }
}
