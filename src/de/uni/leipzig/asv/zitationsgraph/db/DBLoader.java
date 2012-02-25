package de.uni.leipzig.asv.zitationsgraph.db;

import java.sql.Connection;
import java.io.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;


import de.uni.leipzig.asv.zitationsgraph.data.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.ext.*;
import java.lang.Integer;
public class DBLoader {

	static final String JDBC_DRIVER = "org.gjt.mm.mysql.Driver";
	
	static final String DB_CREATE = "CREATE DATABASE GRAPH";
	static final String DB_DROP = "DROP DATABASE GRAPH";
	static final String DB_USE = "USE GRAPH";
	static final String VENUE_TABLE_CREATE = "CREATE TABLE Venue (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(200), year INT)";
	static final String AUTHOR_TABLE_CREATE = "CREATE TABLE Author (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(500), department VARCHAR(200))";
	static final String PUBLICATION_TABLE_CREATE = "CREATE TABLE Publication (id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id),title VARCHAR(1000) UNIQUE ," +
			" venue VARCHAR(500), Foreign Key (venue) references Venue(id))";
	static final String PUBLISHED_TABLE_CREATE = "CREATE TABLE Published (pub_id INT references Publication(id), author_id INT references Author(id), PRIMARY KEY(pub_id, author_id))";
	static final String CITED_TABLE_CREATE = "CREATE TABLE Cited (source_id INT references Publication(id), target_id INT references Publication(id), textphrase VARCHAR(1000), PRIMARY KEY(source_id, target_id))";
	static final String VENUE_TABLE_DROP = "DROP TABLE Venue";
	static final String AUTHOR_TABLE_DROP = "DROP TABLE Author";
	static final String PUBLICATION_TABLE_DROP = "DROP TABLE Publication";
	static final String PUBLISHED_TABLE_DROP = "DROP TABLE Published";
	static final String CITED_TABLE_DROP = "DROP TABLE Cited";
	
	static final String INSERT_AUTHOR = "INSERT INTO Author(name, department) VALUES(?, ?)";
	static final String INSERT_VENUE = "INSERT INTO Venue(name, year) VALUES(?, ?)";
	static final String INSERT_PUBLICATION ="INSERT INTO Publication(title, venue) VALUES(?, ?)";
	static final String INSERT_PUBLISHED = "INSERT INTO Published(pub_id, author_id) VALUES(?, ?)";
	static final String INSERT_CITED = "INSERT INTO Cited(source_id, target_id, textphrase) VALUES(?, ?, ?)";
	static final String GET_PUBID = "SELECT id FROM Publication WHERE title like ?";

	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet generatedKeys  =null;
	
	
	public DBLoader() {
		
		loadDriver();
		dbConnect();
		db_use();
		//dropTables();
		//createTables();
		//Publication test = new Publication(null, "test");
		//System.out.println("id: "+ savePublication(test));
		//closeConnection();
		
	}
	
	private void loadDriver(){
		
		try {
			Class.forName(JDBC_DRIVER).newInstance();
		}
		catch(Exception e){
			System.out.println("Unable to load the driver class!");
			System.out.println(e);
		}
	}
	
	private void dbConnect(){
		
		ConfigReader configReader = new ConfigReader();
		String url = "jdbc:mysql://"+configReader.getServer()+":"+configReader.getPort();
		establishConnection(url, configReader.getUser(), configReader.getPassword());	
	}
	
	private void establishConnection(String url, String user, String password){
		
		try{
			 connection=DriverManager.getConnection(url,user,password);
		}catch( SQLException e ){
				System.out.println( "Couldn’t establish DB-connection!" );
				System.out.println(e);
		}
	}
	
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
	
	
	//create Tables
	
	/**
	 * Method to Drop all Tables
	 */
	public void createTables(){
		createVenueTable();
		createAuthorTable();
		createPublicationTable();
		createPublishedTable();
		createCitedTable();
	}
	
	private void createVenueTable(){
		System.out.println("fu");
		System.out.println("try to created Venue-Table...");
		String errorMessage = "Couldn't create Venue-Table";
		executeStatement(VENUE_TABLE_CREATE, errorMessage);
	}
	
	private void createAuthorTable(){
		System.out.println("try to created Author-Table...");
		String errorMessage = "Couldn't create Author-Table";
		executeStatement(AUTHOR_TABLE_CREATE, errorMessage);
	}
	
	private void createPublicationTable(){
		System.out.println("try to created Publication-Table...");
		String errorMessage = "Couldn't create Publication-Table";
		executeStatement(PUBLICATION_TABLE_CREATE, errorMessage);
	}
	
	private void createPublishedTable(){
		System.out.println("try to created Published-Table...");
		String errorMessage = "Couldn't create Published-Table";
		executeStatement(PUBLISHED_TABLE_CREATE, errorMessage);
	}
	
	private void createCitedTable(){
		System.out.println("try to created Cited-Table...");
		String errorMessage = "Couldn't create Cited-Table";
		executeStatement(CITED_TABLE_CREATE, errorMessage);
	}
	
	
	public void dropTables(){
		dropCitedTable();
		dropPublishedTable();
		dropPublicationTable();
		dropAuthorTable();
		dropVenueTable();
	}
	
	//drop Tables
	private void dropCitedTable(){
		System.out.println("try to drop Cited-Table...");
		String errorMessage = "Couldn't drop Cited-Table";
		executeStatement(CITED_TABLE_DROP, errorMessage);
	}
	
	private void dropVenueTable(){
		System.out.println("try to drop Venue-Table...");
		String errorMessage = "Couldn't drop Venue-Table";
		executeStatement(VENUE_TABLE_DROP, errorMessage);
	}
	
	private void dropAuthorTable(){
		System.out.println("try to drop Author-Table...");
		String errorMessage = "Couldn't drop Author-Table";
		executeStatement(AUTHOR_TABLE_DROP, errorMessage);
	}
	
	private void dropPublicationTable(){
		System.out.println("try to drop Publication-Table...");
		String errorMessage = "Couldn't drop Publication-Table";
		executeStatement(PUBLICATION_TABLE_DROP, errorMessage);
	}
	
	private void dropPublishedTable(){
		System.out.println("try to drop Published-Table...");
		String errorMessage = "Couldn't drop Published-Table";
		executeStatement(PUBLISHED_TABLE_DROP, errorMessage);
	}
	


	/**
	 * Standard method to execute an SQL-statement 
	 * @param String Query, String errorMessage
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
	
	
	private void closeStatement(){
		try{
			if(statement!=null)
	        	 statement.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close Statement!" );
	    	System.out.println(e);
	    }
	}
	
	private void closeResultSet(ResultSet resultSet){
		try{
			if(resultSet!=null)
	        	 resultSet.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close ResultSet!" );
	    	System.out.println(e);
	    }
	}
	
	private void closePreparedStatement(){
		try{
			if(preparedStatement!=null)
	        	 preparedStatement.close();
	    }catch(SQLException e){
	    	System.out.println( "Couldn't close PreparedStatement!" );
	    	System.out.println(e);
	    }
	}
	
	
	private int saveAuthor(Author author){
		int id =-1;
		try {
			preparedStatement = connection.prepareStatement(INSERT_AUTHOR, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, author.getName());
			preparedStatement.setString(2, author.getAffiliation());
			execute("Author");
		    id =  getID();
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Author" );
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		      closeResultSet(generatedKeys);
		} 
		return id;
	}
	
	private void execute(String type) throws SQLException{
		int affectedRows = preparedStatement.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("Insert type, failed");
		}
	}
	
	private int getID() throws SQLException{
		int id=-1;
		generatedKeys = preparedStatement.getGeneratedKeys();
	    if (generatedKeys.next()) {
	        id = generatedKeys.getInt(1);
	    } else {
	        throw new SQLException("Failed, no generated key obtained.");
	    }
	    return id;
	}
    
	
	private int saveVenue(Date year, String name){
		int id =-1;
		try {
			preparedStatement = connection.prepareStatement(INSERT_VENUE);
			preparedStatement.setString(1, name);
			preparedStatement.setInt(2, year.getYear()); //TODO: change Date
			execute("Venue");
		    id =  getID();
		}catch( SQLException e ){
			System.out.println( "Couldn't insert Author" );
	    	System.out.println(e);
		} finally {
		      closePreparedStatement();
		}
		return id;
	}
	
	
	private int savePublication(Publication publication){
		int id =-1;
		try {
			preparedStatement = connection.prepareStatement(INSERT_PUBLICATION,Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, publication.getTitle());
			preparedStatement.setInt(2, 0);
			execute("Publication");
		    id =  getID();
		}catch( SQLException e ){
			if (e.getErrorCode()==1062){
				System.out.println( "Duplikate found" );
		    	id = getDuplicateID(publication.getTitle());
			}
			System.out.println( "Couldn't insert Publication" );
	    	System.out.println(e.getErrorCode());System.out.println(e);
		} finally {
		      closePreparedStatement();
		}
		return id;
	}
	
	private int getDuplicateID(String title){
		int id =-1;
		try {
			PreparedStatement preparedStatement2 = connection.prepareStatement(GET_PUBID);
			preparedStatement2.setString(1, title);
			ResultSet rs = preparedStatement2.executeQuery();
			while (rs.next()){
				id = rs.getInt(1);
			}
		}catch( SQLException e ){
			System.out.println( "Couldn't exectue Query:" + GET_PUBID);
	    	System.out.println(e.getErrorCode());System.out.println(e);
		}
		return id;
	}
	
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
	 * @param publication
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