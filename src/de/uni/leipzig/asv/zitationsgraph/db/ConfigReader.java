package de.uni.leipzig.asv.zitationsgraph.db;

/**
* Simple config-reader for DB-connection
 * requires the file 'db.conf' in the working-dir
 * db.conf must be structured like: (without brackets)
 * |                                         |
 * | mysql_server=[your_mysql_server_adress] |
 * | port=[your_mysql_server_port]           |
 * | user=[your_mysql_username]              |
 * | password=[your_mysql_user_password]     |
 * |                                         |
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class ConfigReader {
	
	
	private String server;
	private String port;
	private String user;
	// this is unsafe! - no password-encryption used
	private String password;

	/**
	 * Constructor reading the config-file using properties
	 * simple mapping from file-properties to local variables
	 */
    public ConfigReader() {
		Properties prop = new Properties();
		String fileName = "db.conf";
		try{
			InputStream is = new FileInputStream(fileName);
			prop.load(is);
			this.server=prop.getProperty("mysql_server");
			this.port=prop.getProperty("port");
			this.user=prop.getProperty("user");
			this.password=prop.getProperty("password");
		} catch(Exception e){
			System.out.println("Failed to read Config-File");
			System.out.println(e);
		}
	}
	
    /**
     * Method to get the MySQL-Server URL as String
     * @return server-URL
     */
	public String getServer() {
		return server;
	}

	/**
     * Method to get the DB
     * @return server-URL
     */
	public String getPort() {
		return port;
	}

	/**
     * Method to get the username
     * @return username
     */
	public String getUser() {
		return user;
	}

	/**
     * Method to get the username's passwort (unsave!)
     * @return username's passwort
     */
	public String getPassword() {
		return password;
	}



}