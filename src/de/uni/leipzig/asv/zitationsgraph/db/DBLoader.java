package de.uni.leipzig.asv.zitationsgraph.db;

import java.io.*;
import java.util.Date;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.uni.leipzig.asv.zitationsgraph.data.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DBLoader {

	//the used mysql-driver
	static final String JDBC_DRIVER = "org.gjt.mm.mysql.Driver";
	
	//db-organization queries
	static final String DB_CREATE = "CREATE DATABASE GRAPH";
	static final String DB_DROP = "DROP DATABASE GRAPH";
	//static final String DB_USE = "USE graph01";
	static final String DB_USE = "USE GRAPH";
	
	//queries to create tables
	static final String VENUE_TABLE_CREATE = "CREATE TABLE Venue (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(500) UNIQUE, year VARCHAR(20))";
	static final String AUTHOR_TABLE_CREATE = "CREATE TABLE Author (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(200) UNIQUE, department VARCHAR(500))";
	static final String PUBLICATION_TABLE_CREATE = "CREATE TABLE Publication (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id),title VARCHAR(500), match_title VARCHAR(500) UNIQUE," +
			" venue_id INT, Foreign Key (venue_id) references Venue(id))";
	static final String PUBLISHED_TABLE_CREATE = "CREATE TABLE Published (pub_id INT references Publication(id), author_id INT references Author(id), PRIMARY KEY(pub_id, author_id))";
	static final String CITED_TABLE_CREATE = "CREATE TABLE Cited (source_id INT references Publication(id), target_id INT references Publication(id), textphrase VARCHAR(500), PRIMARY KEY(source_id, target_id))";
	
	//queries to drop tables
	static final String VENUE_TABLE_DROP = "DROP TABLE Venue";
	static final String AUTHOR_TABLE_DROP = "DROP TABLE Author";
	static final String PUBLICATION_TABLE_DROP = "DROP TABLE Publication";
	static final String PUBLISHED_TABLE_DROP = "DROP TABLE Published";
	static final String CITED_TABLE_DROP = "DROP TABLE Cited";
	
	//queries to insert data
	static final String INSERT_AUTHOR = "INSERT INTO Author(name, department) VALUES(?, ?)";
	static final String INSERT_VENUE = "INSERT INTO Venue(name, year) VALUES(?, ?)";
	static final String INSERT_PUBLICATION ="INSERT INTO Publication(title, match_title, venue_id) VALUES(?, ?, ?)";
	static final String INSERT_PUBLISHED = "INSERT INTO Published(pub_id, author_id) VALUES(?, ?)";
	static final String INSERT_CITED = "INSERT INTO Cited(source_id, target_id, textphrase) VALUES(?, ?, ?)";
	
	static final String GET_PUB_ID = "SELECT id FROM Publication WHERE match_title like ?";
	static final String GET_VENUE_ID = "SELECT id FROM Venue WHERE name like ?";
	static final String GET_AUTHOR_ID = "SELECT id FROM Author WHERE name like ?";
	/*
	//better to change mysql-delimiter to create functions in one statement
	static final String CHANGE_DELIMITER = "delimiter //";
	static final String RESTORE_DELIMITER = "delimiter ;";
	*/
	
	//queries 
	//MYSQL Levenshteinfunction from "Extending Chapter 9 of Get it Done with MySQL 5&6" by Jason Rust
	static final String CREATE_LEVENSHTEIN_FUNCTION = 
			"CREATE FUNCTION levenshtein( s1 VARCHAR(255), s2 VARCHAR(255) ) " 
		   +"RETURNS INT "
	       +"DETERMINISTIC " 
	       +"BEGIN " 
	       		+"DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT; " 
	       		+"DECLARE s1_char CHAR; " 
	       		//+"-- max strlen=255 " 
	       		+"DECLARE cv0, cv1 VARBINARY(256); " 
	       		+"SET s1_len = CHAR_LENGTH(s1), s2_len = CHAR_LENGTH(s2), cv1 = 0x00, j = 1, i = 1, c = 0; " 
	       		+"IF s1 = s2 THEN " 
	       			+"RETURN 0; " 
	       		+"ELSEIF s1_len = 0 THEN " 
	       			+"RETURN s2_len; " 
	       		+"ELSEIF s2_len = 0 THEN "
	       			+"RETURN s1_len; " 
	       		+"ELSE " 
	       			+"WHILE j <= s2_len DO " 
	       				+"SET cv1 = CONCAT(cv1, UNHEX(HEX(j))), j = j + 1; " 
	       			+"END WHILE; " 
	       			+"WHILE i <= s1_len DO " 
	       				+"SET s1_char = SUBSTRING(s1, i, 1), c = i, cv0 = UNHEX(HEX(i)), j = 1; " 
	       				+"WHILE j <= s2_len DO " 
	       					+"SET c = c + 1; " 
	       					+"IF s1_char = SUBSTRING(s2, j, 1) THEN "  
	       						+"SET cost = 0; ELSE SET cost = 1; " 
	       					+"END IF; " 
	       					+"SET c_temp = CONV(HEX(SUBSTRING(cv1, j, 1)), 16, 10) + cost; "
	       					+"IF c > c_temp THEN SET c = c_temp; END IF; " 
	       					+"SET c_temp = CONV(HEX(SUBSTRING(cv1, j+1, 1)), 16, 10) + 1; " 
	       					+"IF c > c_temp THEN "  
	       						+"SET c = c_temp; "  
	       					+"END IF; " 
	       					+"SET cv0 = CONCAT(cv0, UNHEX(HEX(c))), j = j + 1; " 
	       				+"END WHILE; " 
	       				+"SET cv1 = cv0, i = i + 1; " 
	       			+"END WHILE; " 
	       		+"END IF; " 
	       		+"RETURN c; "
	       	+"END";
	
	static final String CREATE_LEVENSHTEIN_HELPER_FUNCTION = 
			"CREATE FUNCTION levenshtein_ratio( s1 VARCHAR(255), s2 VARCHAR(255) ) " 
		   +"RETURNS INT "
		   +"DETERMINISTIC "
		   +"BEGIN "
		   		+"DECLARE s1_len, s2_len, max_len INT; " 
		   		+"SET s1_len = LENGTH(s1), s2_len = LENGTH(s2); " 
		   		+"IF s1_len > s2_len THEN "
		   			+"SET max_len = s1_len; "
		   		+"ELSE "
		   			+"SET max_len = s2_len; "
		   		+"END IF; "
		   		+"RETURN ROUND((1 - LEVENSHTEIN(s1, s2) / max_len) * 100); " 
		   	+"END";

	static final String DROP_LEVENSHTEIN_HELPER_FUNCTION = "DROP FUNCTION levenshtein_ratio";
	static final String DROP_LEVENSHTEIN_FUNCTION = "DROP FUNCTION levenshtein";
	
	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet generatedKeys  =null;
	

	/**
	 * basic constructor initializing DB-connection and selecting DB
	 */
	public DBLoader() {
		loadDriver();
		dbConnect();
		db_use();
//		drop();
//		create();
		
//		System.out.println(createMatchTitle("Hi  +#+#+#+? & sxajf;.-"));
		
//		Author a= new Author("hi");
//		Author a2= new Author("hu");
//		Author a3= new Author("hi");
//		System.out.println(saveAuthor(a));
//		System.out.println(saveAuthor(a2));
//		System.out.println(saveAuthor(a3));
//		System.out.println(saveAuthor(a));
//		
//		Date d = new Date();
//		System.out.println(saveVenue(d, "hallo"));
//		System.out.println(saveVenue(d, "hallo1"));
//		System.out.println(saveVenue(d, "hall2"));
//		System.out.println(saveVenue(d, "hallo"));
//		
		//Publication test = new Publication(null, "test");
		//System.out.println("id: "+ savePublication(test));
		//closeConnection();
	}
	
	/**
	 * Method to load the JDBC-driver
	 */
	private void loadDriver(){
		
		try {
			Class.forName(JDBC_DRIVER).newInstance();
		}
		catch(Exception e){
			System.out.println("Unable to load the driver class!");
			System.out.println(e);
		}
	}
	
	/**
	 * Method to read the config-file and connect with the DB
	 */
	private void dbConnect(){
		
		ConfigReader configReader = new ConfigReader();
		String url = "jdbc:mysql://"+configReader.getServer()+":"+configReader.getPort();
		establishConnection(url, configReader.getUser(), configReader.getPassword());	
	}
	
	/**
	 * Method to create the Connection
	 * @param url 		MySQL-server URL
	 * @param user		MySQL-user
	 * @param password	MySQL user-password (unsave handling)
	 */
	private void establishConnection(String url, String user, String password){
		
		try{
			 connection=DriverManager.getConnection(url,user,password);
		}catch( SQLException e ){
				System.out.println( "Couldn’t establish DB-connection!" );
				System.out.println(e);
		}
	}
	
	/**
	 * Method to close the DB-connection
	 * (should be used after DB-work is done)
	 */
	public void closeConnection(){
		
		try{
			 connection.close();
		}catch( SQLException e ){
				System.out.println( "Couldn't close DB-Connection!" );
				System.out.println(e);
		}
	}
	
	
	/**
	 * Method to create the DB named 'GRAPH'
	 */
	public void dbCreate(){
		System.out.println("try to created DB...");
		String errorMessage = "Couldn't create DB";
		executeStatement(DB_CREATE, errorMessage);
	}
	
	/**
	 * Method to drop the DB 'GRAPH'
	 */
	public void dbDrop(){
		System.out.println("try to drop DB...");
		String errorMessage = "Couldn't drop DB";
		executeStatement(DB_DROP, errorMessage);
	}
	
	/**
	 * Method to activate the MySQL-DB
	 */
	public void db_use(){
		System.out.println( "try to select Graph-DB..." );
		String errorMessage = "Couldn't select Graph-DB";
		executeStatement(DB_USE, errorMessage);
	}
	
	
	
	/**
	 * Method to create tables and functions for DB
	 */
	public void create(){
		createTables();
		createLevenshteinFunction();
	}
	
	/**
	 * Method to Drop all Tables
	 */
	private void createTables(){
		createVenueTable();
		createAuthorTable();
		createPublicationTable();
		createPublishedTable();
		createCitedTable();
	}
	
	/**
	 * Method to create the Venue-table
	 */
	private void createVenueTable(){
		System.out.println("try to created Venue-Table...");
		String errorMessage = "Couldn't create Venue-Table";
		executeStatement(VENUE_TABLE_CREATE, errorMessage);
	}
	
	/**
	 * Method to create the Author-table
	 */
	private void createAuthorTable(){
		System.out.println("try to created Author-Table...");
		String errorMessage = "Couldn't create Author-Table";
		executeStatement(AUTHOR_TABLE_CREATE, errorMessage);
	}
	
	/**
	 * Method to create the Publication-table
	 */
	private void createPublicationTable(){
		System.out.println("try to created Publication-Table...");
		String errorMessage = "Couldn't create Publication-Table";
		executeStatement(PUBLICATION_TABLE_CREATE, errorMessage);
	}
	
	/**
	 * Method to create the Published-table
	 */
	private void createPublishedTable(){
		System.out.println("try to created Published-Table...");
		String errorMessage = "Couldn't create Published-Table";
		executeStatement(PUBLISHED_TABLE_CREATE, errorMessage);
	}
	
	/**
	 * Methode to create the Cited-Table
	 */
	private void createCitedTable(){
		System.out.println("try to created Cited-Table...");
		String errorMessage = "Couldn't create Cited-Table";
		executeStatement(CITED_TABLE_CREATE, errorMessage);
	}
	
	//create Functions
	/**
	 * Method to create LevensheinFunction for usage in MySQL
	 */
	private void createLevenshteinFunction(){
		System.out.println("try to create Levenshtein-Function...");
		String errorMessage = "Couldn't create Levenshtein-Function";
		//executeStatement(CHANGE_DELIMITER, "chouldn'n change Delimiter");
		executeStatement(CREATE_LEVENSHTEIN_HELPER_FUNCTION, errorMessage);
		executeStatement(CREATE_LEVENSHTEIN_FUNCTION, errorMessage);
		//executeStatement(RESTORE_DELIMITER, "chouldn'n restore Delimiter");		
	}
	
	/**
	 * Method to drop tables & functions
	 */
	public void drop(){
		dropTables();
	    dropLevenshteinFunction();
	}
	
	/**
	 * Method to drop all tables
	 */
	private void dropTables(){
		dropCitedTable();
		dropPublishedTable();
		dropPublicationTable();
		dropAuthorTable();
		dropVenueTable();
	}
	

	/**
	 * Method to drop Cited-table
	 */
	private void dropCitedTable(){
		System.out.println("try to drop Cited-Table...");
		String errorMessage = "Couldn't drop Cited-Table";
		executeStatement(CITED_TABLE_DROP, errorMessage);
	}
	
	/**
	 * Method to drop Venue-table
	 */
	private void dropVenueTable(){
		System.out.println("try to drop Venue-Table...");
		String errorMessage = "Couldn't drop Venue-Table";
		executeStatement(VENUE_TABLE_DROP, errorMessage);
	}
	
	/**
	 * Method to drop Author-table
	 */
	private void dropAuthorTable(){
		System.out.println("try to drop Author-Table...");
		String errorMessage = "Couldn't drop Author-Table";
		executeStatement(AUTHOR_TABLE_DROP, errorMessage);
	}
	
	/**
	 * Method to drop Publication-table
	 */
	private void dropPublicationTable(){
		System.out.println("try to drop Publication-Table...");
		String errorMessage = "Couldn't drop Publication-Table";
		executeStatement(PUBLICATION_TABLE_DROP, errorMessage);
	}
	
	/**
	 * Method to drop Published-table
	 */
	private void dropPublishedTable(){
		System.out.println("try to drop Published-Table...");
		String errorMessage = "Couldn't drop Published-Table";
		executeStatement(PUBLISHED_TABLE_DROP, errorMessage);
	}

	
	/**
	 * Method to drop Levenshein-function
	 */
	public void dropLevenshteinFunction(){
		System.out.println("try to drop Levenshtein-Function...");
		String errorMessage = "Couldn't drop Levenshtein-Function";
		executeStatement(DROP_LEVENSHTEIN_FUNCTION, errorMessage);
		executeStatement(DROP_LEVENSHTEIN_HELPER_FUNCTION, errorMessage);
	}
	
	/**
	 * Standard method to execute an SQL-statement 
	 * @param String query 			SQL-Query
	 * @param String errorMessage	understandable message appearing in console after fail
	 */
	private void executeStatement(String query, String errorMessage){
		try{
			statement = connection.createStatement();
			statement.execute(query);
			System.out.println( "done..." );
		}catch( SQLException e ){
			System.out.println( errorMessage );
			System.out.println(e);
		}finally{
			closeStatement();
		}
	}
	
	/**
	 * Method to close a statement after use
	 */
	private void closeStatement(){
		try{
			if(statement!=null)
	        	 statement.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close Statement!" );
	    	System.out.println(e);
	    }
	}
	
	/**
	 * Method to close a used ResultSet
	 * @param resultSet
	 */
	private void closeResultSet(ResultSet resultSet){
		try{
			if(resultSet!=null)
	        	 resultSet.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close ResultSet!" );
	    	System.out.println(e);
	    }
	}
	
	/**
	 * Method to close the prepared statement
	 */
	private void closePreparedStatement(){
		try{
			if(preparedStatement!=null)
	        	 preparedStatement.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close PreparedStatement!" );
	    	System.out.println(e);
	    }
	}
	
	
	/**
	 * Method to write a author into the DB
	 * @param author	author-object, with the extraced data
	 * @return			inserted id, -1 on failure
	 */
	private int saveAuthor(Author author){
		int id =getID(GET_AUTHOR_ID,author.getName());
		if (id!=-1) return id;
		
		try {
			preparedStatement = connection.prepareStatement(INSERT_AUTHOR, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, author.getName());
			preparedStatement.setString(2, author.getAffiliation());
			execute("Author");
		    id =  getAutoID();
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Author" );
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		      closeResultSet(generatedKeys);
		} 
		return id;
	}
	
	/**
	 * Method to execute the insert Statement
	 * @param type				name of the table, to track by failure
	 * @throws SQLException		insert-failure with the name of the table
	 */
	private void execute(String type) throws SQLException{
		int affectedRows = preparedStatement.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("Insert "+type+", failed");
		}
	}
	
	/**
	 * Method to get the autoincID assigned by the DB
	 * @return	id, -1 by failure
	 * @throws SQLException
	 */
	private int getAutoID() throws SQLException{
		int id=-1;
		generatedKeys = preparedStatement.getGeneratedKeys();
	    if (generatedKeys.next()) {
	        id = generatedKeys.getInt(1);
	    } else {
	        throw new SQLException("Failed, no generated key obtained.");
	    }
	    return id;
	}
    
	/**
	 * Method to write a venue into the DB
	 * @param year		parsed year of the venue
	 * @param name		prased name of the venue
	 * @return id of the MySQL-entry  , -1 
	 */
	private int saveVenue(String year, String name){
		//test if its already in the DB
		int id =getID(GET_VENUE_ID,name);
		if (id!=-1) return id;
		
		try {
			preparedStatement = connection.prepareStatement(INSERT_VENUE,PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, year);
			execute("Venue");
		    id =  getAutoID();
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Venue" );
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		      closeResultSet(generatedKeys);
		}
		return id;
	}
	
	/**
	 * Method to clear the title from additional characters, whitespaces and stuff
	 * @param title		extracted title
	 * @return			cleared title
	 */
	private String createMatchTitle(String title){
		return title.toLowerCase().replaceAll("\\W+", "");
	}
	
	/**
	 * Method to get the DB-id of a given unique name
	 * @param name 	unique name
	 * @return		DB-id, -1 if no match
	 */
	private int getID(String query, String name){
		int id = -1;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, name);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()){
				id = rs.getInt(1);
			}
		}catch( SQLException e ){
			System.out.println( "Couldn't exectue Query:" + query);
		   	System.out.println(e.getErrorCode());System.out.println(e);
		} finally {
			closePreparedStatement();
	    }
		return id;
	}
	
	/**
	 * Method to save a Publication
	 * @param publication
	 * @return
	 */
	private int savePublication(Publication publication){
		String matchTitle = createMatchTitle(publication.getTitle());
		
		int id = getID(GET_PUB_ID, matchTitle);
		if (id!=-1) return id;
		
		int venueID= saveVenue(publication.getYearString(), publication.getVenue());

		try {
			preparedStatement = connection.prepareStatement(INSERT_PUBLICATION,PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, publication.getTitle());
			preparedStatement.setString(2, matchTitle);
			preparedStatement.setInt(3, venueID);
			execute("Publication");
		    id =  getAutoID();
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Publication" );
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		      closeResultSet(generatedKeys);
		}
		for (Author author: publication.getAuthors())
		{
			int authorID = saveAuthor(author);
			if (authorID != -1 || id!=-1)
				savePublished(id, authorID);
		}
		return id;
	}
	
	/**
	 * Method to save the publication-author-relation
	 */
	private void savePublished(int pubID, int authorID){
		try {
			preparedStatement = connection.prepareStatement(INSERT_PUBLISHED);
			preparedStatement.setInt(1, pubID);
			preparedStatement.setInt(2, authorID);
			execute("Published");
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Published");
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		}
	}
	
	/**
	 * Method to save the publication<->publication (Citation) realtion
	 * @param source	id of the publication, that cites
	 * @param target	id of the publication, that is cited
	 */
	private void saveCitation(int source, int target){
		try {
			preparedStatement = connection.prepareStatement(INSERT_CITED);
			preparedStatement.setInt(1, source);
			preparedStatement.setInt(2, target);
			preparedStatement.setString(3, "");
			execute("Citation");
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Citation");
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		}
	}
	
	
	/**
	 * Method for writing a Document into the DB
	 * @param document	document-object with all extracted data
	 */
	public void saveDocument(Document document){
		Publication publication = document.getPublication();
		int sourceID = savePublication(publication);
		/*for (Author author : publication.getAuthors())
		{
		    saveAuthor(author);
		}*/
		for (Citation citation: document.getCitations())
		{
			int targetID  = savePublication(citation.getPublication());
			if (targetID != -1 && sourceID!=-1)
				saveCitation(sourceID, targetID);
		}
	}	
	
	/**
	 * Method to create a graph-object from the cited-table
	 * @return 	graphobject
	 */
	public DirectedGraph<String, DefaultEdge> createGraph(){
		DirectedGraph<String, DefaultEdge> g =
	            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT title FROM Publication");
			while (rs.next()){
				g.addVertex(rs.getString(1));
			}
			rs = stmt.executeQuery("SELECT P.title as source_title, PP.title as target_title FROM Cited C join Publication P on (C.source_id=P.id) join Publication PP  on (C.target_id=PP.id)");
			while (rs.next()){
				g.addEdge(rs.getString(1), rs.getString(2));
			}
	
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return g;
	}
	
	/**
	 * Method to create a graphfile from the cited_table
	 * @param graph
	 */
	public void writeGraphToFile(DirectedGraph<String, DefaultEdge> graph){
		try{
			FileWriter f0 = new FileWriter("graph1.gml");
			GmlExporter<String, DefaultEdge> ge = new GmlExporter<String, DefaultEdge>();
		    ge.setPrintLabels(3);
			ge.export(f0, graph);
		}catch (Exception e){
			System.out.println(e);
		}
	}
	
	
}