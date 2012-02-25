package de.uni.leipzig.asv.zitationsgraph.db;

/**
 * Simple config-reader for DB-connection
 * requires the file 'db.conf' in the working-dir'/config
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

	/*
	 * Constructor reading the config-file using properties
	 * simple mapping from file-properties to local variables
	 */
    public ConfigReader() {
		Properties prop = new Properties();
		String fileName = "db.conf";
		try{
			InputStream is = new FileInputStream("config/"+fileName);
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
	
	public String getServer() {
		return server;
	}


	public String getPort() {
		return port;
	}


	public String getUser() {
		return user;
	}


	public String getPassword() {
		return password;
	}



}