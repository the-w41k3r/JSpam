package me.w41k3r.jspam;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import static me.w41k3r.jspam.Utils.getStr;
import static me.w41k3r.jspam.Utils.msg;


public class AddEmailCommand extends Command {

    private final JSpam plugin;
    private final String database;
    private final String username;
    private final String password;
    private final String hostname;
    private final int port;



    public AddEmailCommand(JSpam plugin) {
        super("addemail");
        this.plugin = plugin;
        // Load configuration from plugin config file
        this.database = plugin.getConfig().getString("mysql.database");
        this.username = plugin.getConfig().getString("mysql.username");
        this.password = plugin.getConfig().getString("mysql.password");
        this.hostname = plugin.getConfig().getString("mysql.host");
        this.port = plugin.getConfig().getInt("mysql.port");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage( msg(plugin.getConfig().getString("prefix")) );
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(getStr("invalid-command"));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String email = args[0];
        String confirmEmail = args[1];

        if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            sender.sendMessage(getStr("invalid-email"));
            return;
        }

        if (!email.equals(confirmEmail)) {
            player.sendMessage(getStr("mismatch-email"));
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            // Get a connection to the MySQL database
            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);

            // Prepare a statement to update the user's email address in the database
            statement = connection.prepareStatement("UPDATE user_profiles SET mailAddress=? WHERE lastNickname=?");
            statement.setString(1, email);
            statement.setString(2, player.getName());

            // Execute the update
            statement.executeUpdate();

            player.sendMessage(getStr("success"));
        } catch (SQLException e) {
            player.sendMessage(getStr("faliure"));
            plugin.getLogger().log(Level.SEVERE, "Error updating email address for player " + player.getName(), e);
            //plugin.getLogger().info("jdbc:mysql://" + hostname + ":" + port + "/" + database + username+ password);
        } finally {
            // Close the statement and connection
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
    }
}
