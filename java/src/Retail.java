/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Retail {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   //keeps userID
   private static int loggeduserID;

   /**
    * Creates a new instance of Retail shop
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Retail(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Retail

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Retail.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Retail esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Retail object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Retail (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println(" OOO  N   N L    III N   N EEEE     RRRR  EEEE TTTTTT  AA  III L         SSS  TTTTTT  OOO  RRRR  EEEE ");
            System.out.println("O   O NN  N L     I  NN  N E        R   R E      TT   A  A  I  L        S       TT   O   O R   R E    ");
            System.out.println("O   O N N N L     I  N N N EEE      RRRR  EEE    TT   AAAA  I  L         SSS    TT   O   O RRRR  EEE  ");
            System.out.println("O   O N  NN L     I  N  NN E        R R   E      TT   A  A  I  L            S   TT   O   O R R   E    ");
            System.out.println(" OOO  N   N LLLL III N   N EEEE     R  RR EEEE   TT   A  A III LLLL     SSSS    TT    OOO  R  RR EEEE  ");
            System.out.println("------------");                                                                                         
            System.out.println("--Welcome!--");                                                                                                                                                                                               
            System.out.println("------------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("\nMAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 Recent Orders");

                //the following functionalities basically used by managers
                System.out.println("5. Update Product");
                System.out.println("6. View 5 Recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");
                System.out.println("10. Administrator User Update");
                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewStores(esql); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql); break;
                   case 4: viewRecentOrders(esql); break;
                   case 5: updateProduct(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewPopularProducts(esql); break;
                   case 8: viewPopularCustomers(esql); break;
                   case 9: placeProductSupplyRequests(esql); break;
                   case 10: updateUser(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Retail esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         String type = "";
         System.out.print("\tAre you an Admin? If yes, please enter the access password or press enter if not: ");
         String accessCode = in.readLine();

         if (accessCode.equals("admin")) {
            type = "admin";
            System.out.println("Welcome Admin!");
         }
         else {
            type = "customer";
         }

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Retail esql){
      loggeduserID = -1;

      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);

         //update the logged userID for access in other functions
         query = String.format("SELECT userID FROM Users  WHERE name = '%s' AND password = '%s'", name, password);
         List<List<String>> a = esql.executeQueryAndReturnResult(query);
         loggeduserID = Integer.parseInt(a.get(0).get(0));
         if (userNum > 0)
		      return name;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static String getProduct() {
      int product_num;
      while(true) {
         try {
            System.out.println("\n\t1. 7up");
            System.out.println("\t2. Brisk");
            System.out.println("\t3. Donuts");
            System.out.println("\t4. Egg");
            System.out.println("\t5. Hot and Sour Soup");
            System.out.println("\t6. Ice Cream");
            System.out.println("\t7. Lemonade");
            System.out.println("\t8. Orange Juice");
            System.out.println("\t9. Pepsi");
            System.out.println("\t10. Pudding");
            System.out.print("Enter the number of the product: ");
            product_num = Integer.parseInt(in.readLine());

            if (product_num == 1) {
               return "7up";
            }
            else if (product_num == 2) {
               return "Brisk";
            }
            else if (product_num == 3) {
               return "Donuts";
            }
            else if (product_num == 4) {
               return "Egg";
            }
            else if (product_num == 5) {
               return "Hot and Sour Soup";
            }
            else if (product_num == 6) {
               return "Ice Cream";
            }
            else if (product_num == 7) {
               return "Lemonade";
            }
            else if (product_num == 8) {
               return "Orange Juice";
            }
            else if (product_num == 9) {
               return "Pepsi";
            }
            else if (product_num == 10) {
               return "Pudding";
            }
            else {
               System.out.println("No such product.\n");
               continue;
            }
         }
      
         catch(Exception e) {
            System.out.println(e);
            continue;
         }
      }
   }

   public static void viewStores(Retail esql) {
      try {
         String Query = String.format("SELECT S.storeID as Store_ID, S.name as Store_Name FROM Store S, Users U WHERE U.userID = " + loggeduserID + " AND calculate_distance(U.latitude, u.longitude, s.latitude, s.longitude) < 30;");
         System.out.println("\nStores within 30 miles:");
         esql.executeQueryAndPrintResult(Query);
      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }

   public static void viewProducts(Retail esql) {
      int storeID;
      try {
         System.out.print("Enter Store ID: ");
         storeID = Integer.parseInt(in.readLine());
         if(storeID > 20 || storeID <= 0) {
            System.out.println("\nInvalid Store ID\n");
            return;
         }   
			String Query = ("SELECT P.productName, P.numberOfUnits, P.pricePerUnit FROM Product P, Store S WHERE S.storeID = " + storeID + " AND P.storeID = " + storeID + ";");
		   System.out.println("\nProducts available at Store " + storeID + ":");
         esql.executeQueryAndPrintResult(Query);
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }

   public static void placeOrder(Retail esql) {
      int storeID;
      String productName;
      int numberOfUnits;

      //get the store id
      while(true) {
         System.out.print("Enter Store ID: ");
         try {
            //gets the id
				storeID = Integer.parseInt(in.readLine());
            //checks if id is within user radius
            String Query = String.format("SELECT S.storeID FROM Store S, Users U WHERE U.userID = " + loggeduserID + " AND calculate_distance(U.latitude, u.longitude, s.latitude, s.longitude) < 30 AND S.storeID = " + storeID + ";");
            //throws error if out of range because the list is empty
            List<List<String>> storeList = esql.executeQueryAndReturnResult(Query); 
            if (storeList.size() <= 0) {
               System.out.println("That store is too far or does not exist. Please select a store within 30 miles.");
               continue;
            }
            break;
			}
			catch(Exception e) {
				System.out.println("Not a valid Store ID");
				System.out.println(e);
				continue;
			}
      }
      //get name of product
      while(true) {
         try {
				productName = getProduct();
            System.out.println(productName);
				break;
			}
			catch(Exception e) {
				System.out.println("Not a valid product");
				System.out.println(e);
				continue;
			}
      }  

      //get number of units
      while(true) {
         System.out.print("Enter the amount of product you wish to order: ");
         try {
            String query = String.format("SELECT P.numberOfUnits FROM Product P, Store S WHERE S.storeID = " + storeID + " AND P.storeID = " + storeID + " AND P.productName = '" + productName + "';");
            List<List<String>> productAmntList = esql.executeQueryAndReturnResult(query);
            numberOfUnits = Integer.parseInt(in.readLine());
            int productAmnt = Integer.parseInt(productAmntList.get(0).get(0));
            if(productAmnt - numberOfUnits < 0) {
               System.out.println("The amount of product you wish to order exceeds the amount of product left, please re-enter the product amount. Amount of product left: " + productAmnt);
               continue;
            }
            break;
         }
			catch(Exception e) {
				System.out.println("Not a valid amount");
				System.out.println(e);
				continue;
			}
      }

      try {
         //inserting the new order
         String query = String.format("INSERT INTO ORDERS (customerID, storeID, productName, unitsOrdered, orderTime) VALUES (%s, %s, '%s', %s, DATE_TRUNC('second', CURRENT_TIMESTAMP::timestamp))", loggeduserID, storeID, productName, numberOfUnits);
         esql.executeUpdate(query);
         //subtracting the number of units from specific store
         query = String.format("SELECT P.numberOfUnits FROM Product P WHERE P.storeID = " + storeID + " AND P.productName = '" + productName + "';");
         List<List<String>> productAmntList = esql.executeQueryAndReturnResult(query);
         int productAmnt = Integer.parseInt(productAmntList.get(0).get(0));
         productAmnt -= numberOfUnits;

         query = String.format("UPDATE Product SET numberOfUnits = " + productAmnt + " WHERE storeID = " + storeID + " AND productName = '" + productName + "';");
         esql.executeUpdate(query);
         System.out.println ("Order successfully placed!");
      }
      catch(Exception e){
         System.err.println(e.getMessage ());
      }
   }

   public static void viewRecentOrders(Retail esql) {
      try {
         String query = String.format("SELECT O.storeID, U.name AS username, S.name AS storename, O.productName, O.unitsOrdered, O.orderTime FROM Orders O, Store S, user U WHERE O.customerID = " + loggeduserID + " AND O.storeID = S.storeID ORDER BY orderTime DESC LIMIT 5;");
         esql.executeQueryAndPrintResult(query);
      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }

   public static void updateProduct(Retail esql) {
      int storeID;
      String product_to_update = "";
      int updated_num_units;
      int updated_price_per_unit;
      try {
         String query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");

         if (userType.equals("manager")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\n Invalid Store ID.\n");
               return;
            }

            query = String.format("SELECT storeID, name, managerID FROM store WHERE managerID = " + loggeduserID + " AND storeID = " + storeID + ";");
            List<List<String>> storeIDList = esql.executeQueryAndReturnResult(query);
            int enteredstoreID = storeIDList.size();
            if (enteredstoreID <= 0) {
               System.out.println("\nYou are not the manager of Store " + storeID + "\n");
               return;
            }

            product_to_update = getProduct();
            System.out.printf("Update the number of units of %s at Store %d: ", product_to_update, storeID);
            updated_num_units = Integer.parseInt(in.readLine());
            System.out.print("Update the price of " + product_to_update + ": ");
            updated_price_per_unit = Integer.parseInt(in.readLine()); 

            query = String.format("UPDATE product P SET numberofUnits = " + updated_num_units + ", pricePerUnit = " + updated_price_per_unit + " FROM users U JOIN store S ON S.managerID = U.userID WHERE U.userID = " + loggeduserID + " AND S.storeID = " + storeID + " AND S.storeID = P.storeID AND P.productName = \'" + product_to_update + "\';");
            esql.executeUpdate(query);
            System.out.printf("\nSuccessfully updated %s at Store %d", product_to_update, storeID);
            query = String.format("INSERT INTO productupdates (managerID, storeID, productName, updatedOn) VALUES (%s, %s,'%s', CURRENT_TIMESTAMP(0))",  loggeduserID, storeID, product_to_update);
            esql.executeUpdate(query);   
            System.out.println("\nSuccessfully updated productUpdates table\n");
         }        

         else if (userType.equals("admin")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\n Invalid Store ID.\n");
               return;
            }

            product_to_update = getProduct();
            System.out.printf("Update the number of units of %s at Store %d: ", product_to_update, storeID);
            updated_num_units = Integer.parseInt(in.readLine());
            System.out.print("Update the price of " + product_to_update + ": ");
            updated_price_per_unit = Integer.parseInt(in.readLine()); 

            query = String.format("UPDATE product P SET numberofUnits = " + updated_num_units + ", pricePerUnit = " + updated_price_per_unit + " WHERE P.storeID = " + storeID + "  AND P.productName = \'" + product_to_update + "\';");
            esql.executeUpdate(query);
            System.out.printf("\nSuccessfully updated %s at Store %d", product_to_update, storeID);
            query = String.format("INSERT INTO productupdates (managerID, storeID, productName, updatedOn) VALUES (%s, %s,'%s', CURRENT_TIMESTAMP(0))",  loggeduserID, storeID, product_to_update);
            esql.executeUpdate(query);   
            System.out.println("\nSuccessfully updated productUpdates table\n");
         }      

         else {
            System.out.println("You do not have access to this.\n");
            return;            
         }  
      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }
   public static void viewRecentUpdates(Retail esql) {
      //check to make sure if type is manager or admin
      //need to implement two queries where manager only sees their stores and admin sees all
      try {
         String query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");
         if(userType.equals("manager")) {
            int storeID;
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\nInvalid Store ID.\n");
               return;
            }
            query = String.format("SELECT storeID, name, managerID FROM store WHERE managerID = " + loggeduserID + " AND storeID = " + storeID + ";");
            List<List<String>> storeIDList = esql.executeQueryAndReturnResult(query);
            int enteredstoreID = storeIDList.size();
            if (enteredstoreID <= 0) {
               System.out.println("\nYou are not the manager of Store " + storeID + "\n");
               return;
            }
            
            System.out.println("The most recent updates to the products of Store " + storeID + " are: ");
            query = String.format("SELECT P.updatenumber, P.managerId, P.storeID, P.productName, P.updatedOn FROM productUpdates P WHERE P.managerID = " + loggeduserID + " AND P.storeID = " + storeID  + " ORDER BY P.updateNumber DESC LIMIT 5;");
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n");
         }
         else if (userType.equals("admin")) {
            int storeID;
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\nInvalid Store ID.\n");
               return;
            }            
            System.out.println("The most recent updates to the products of Store " + storeID + " are: ");
            query = String.format("SELECT P.updatenumber, P.managerId, P.storeID, P.productName, P.updatedOn FROM productUpdates P WHERE P.storeID = " + storeID  + " ORDER BY P.updateNumber DESC LIMIT 5;");
            esql.executeQueryAndPrintResult(query);            
         }
         else {
            System.out.println("You do not have access to this.");
            return;
         }
      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }
   public static void viewPopularProducts(Retail esql) {
      int storeID;
      try {
         String query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");

         if (userType.equals("manager")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\n Store ID.\n");
               return;
            }
            query = String.format("SELECT storeID, name, managerID FROM store WHERE managerID = " + loggeduserID + " AND storeID = " + storeID + ";");
            List<List<String>> storeIDList = esql.executeQueryAndReturnResult(query);
            int enteredstoreID = storeIDList.size();
            if (enteredstoreID <= 0) {
               System.out.println("\nYou are not the manager of Store " + storeID + "\n");
               return;
            }
            else {
               System.out.println("\nTop 5 products from Store " + storeID + ": ");
               query = String.format("SELECT P.productname, COUNT(O.unitsOrdered) AS Number_of_Times_Ordered FROM product P, users U, store S, orders O WHERE U.userID = " + loggeduserID + " AND U.userID = S.managerID AND S.storeID = " + storeID + " AND S.storeID = P.storeID AND O.storeID = P.storeID AND P.productName = O.productname GROUP BY P.productname ORDER BY Number_of_Times_Ordered DESC LIMIT 5;");
               esql.executeQueryAndPrintResult(query);
               System.out.println("\n"); 
            }
         }

         else if (userType.equals("admin")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\n Store ID.\n");
               return;
            }
            System.out.println("\nTop 5 products from Store " + storeID + ": ");
            query = String.format("SELECT O.productname, COUNT(O.unitsOrdered) AS Number_of_Times_Ordered FROM orders O WHERE O.storeID = " + storeID + " GROUP BY O.productname ORDER BY Number_of_Times_Ordered DESC LIMIT 5;");
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n"); 
            }
         else {
            System.out.println("You do not have access to this.\n");
            return;            
         }

      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }

   public static void viewPopularCustomers(Retail esql) {
      int storeID;
      try {
         String query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");

         if (userType.equals("manager")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\nInvalid Store ID.\n");
               return;
            }
            query = String.format("SELECT storeID, name, managerID FROM store WHERE managerID = " + loggeduserID + " AND storeID = " + storeID + ";");
            List<List<String>> storeIDList = esql.executeQueryAndReturnResult(query);
            int enteredstoreID = storeIDList.size();
            if (enteredstoreID <= 0) {
               System.out.println("\nYou are not the manager of Store " + storeID + "\n");
               return;
            }
            System.out.println("\nYour top 5 customers from Store " + storeID + ": ");
            query = String.format("SELECT * FROM users U INNER JOIN (SELECT O.customerID, COUNT(O.customerID) as Number_of_Orders_Placed FROM orders O, users U, store S WHERE  U.userID = " + loggeduserID +" AND U.userID = S.managerID AND S.storeID = " + storeID + " AND S.storeID = O.storeID GROUP BY O.customerID ORDER BY Number_of_Orders_Placed DESC) AS x ON U.userID = x.CustomerID ORDER BY Number_of_Orders_Placed DESC LIMIT 5;");
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n");
         }

         else if (userType.equals("admin")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\nInvalid Store ID.\n");
               return;
            }
            
            System.out.println("\nYour top 5 customers from Store " + storeID + ": ");
            query = String.format("SELECT * FROM users U INNER JOIN (SELECT O.customerID, COUNT(O.customerID) as Number_of_Orders_Placed FROM orders O WHERE O.storeID = " + storeID + " GROUP BY O.customerID ORDER BY Number_of_Orders_Placed DESC) AS x ON U.userID = x.CustomerID ORDER BY Number_of_Orders_Placed DESC LIMIT 5;");
            esql.executeQueryAndPrintResult(query);
            System.out.println("\n");
         }
         else {
            System.out.println("You do not have access to this.\n");
            return;            
         }
      }
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }
   public static void placeProductSupplyRequests(Retail esql) {
      int storeID;
      String product_to_order = "";
      int num_units_needed;
      int warehouse_num;
      int productAmnt;
      try {
         String query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");

         if (userType.equals("manager")) {
            System.out.print("Enter Store ID: ");
            storeID = Integer.parseInt(in.readLine());
            if(storeID > 20 || storeID <= 0) {
               System.out.println("\nInvalid Store ID.\n");
               return;
            }
            query = String.format("SELECT storeID, name, managerID FROM store WHERE managerID = " + loggeduserID + " AND storeID = " + storeID + ";");
            List<List<String>> storeIDList = esql.executeQueryAndReturnResult(query);
            int enteredstoreID = storeIDList.size();
            if (enteredstoreID <= 0) {
               System.out.println("\nYou are not the manager of Store " + storeID + "\n");
               return;
            }

            else {
               product_to_order = getProduct();
               System.out.printf("Number of units of %s needed: ", product_to_order);
               num_units_needed = Integer.parseInt(in.readLine());
               System.out.print("Enter the warehouse ID you'd like to place an order for " + product_to_order + " from: ");
               warehouse_num = Integer.parseInt(in.readLine()); 
               if (warehouse_num < 0 || warehouse_num > 5) {
                  System.out.println("No such warehouse.");
                  return;
               }
               else {
                  System.out.println ("\nOrder from Warehouse " + warehouse_num + " successfully placed! \n");
                  query = String.format("SELECT P.numberOfUnits FROM Product P WHERE P.storeID = " + storeID + " AND P.productName = '" + product_to_order + "';");
                  esql.executeQuery(query);
                  List<List<String>> productAmntList = esql.executeQueryAndReturnResult(query);
                  productAmnt = Integer.parseInt(productAmntList.get(0).get(0));
                  productAmnt += num_units_needed;
                  query = String.format("UPDATE product P SET numberofUnits = " + productAmnt + " FROM store S WHERE S.managerID = " + loggeduserID + " AND S.storeID = " + storeID + " AND P.storeID = S.storeID AND P.productName = '" + product_to_order + "';");
                  esql.executeUpdate(query);
                  query = String.format("INSERT INTO productSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) VALUES ( %s, %s, %s, '%s', %s )", loggeduserID, warehouse_num, storeID, product_to_order, num_units_needed);
                  esql.executeUpdate(query);
               }
            }
         }
         else {
            System.out.println("You do not have access to this.\n");
            return;            
         }
      }
      catch(Exception e) {
			System.err.println(e.getMessage());
		}
   }

   public static void updateUser(Retail esql) {
      int userID;
      int updateNumber;
      String name;
      String password;
      String latitude;
      String longitude;
      String query;
      //check if user is an Admin
      try {
         query = String.format("SELECT type FROM Users WHERE userID = " + loggeduserID + ";");
         List<List<String>> userTypeList = esql.executeQueryAndReturnResult(query);
         String userType = userTypeList.get(0).get(0).replaceAll("\\s+", "");
         if(!userType.equals("admin")) {
            System.out.println("You are not an administrator.");
            return;
         }
      }
      catch(Exception e) {
         System.err.println(e.getMessage());
      }

      //Ask admin what user they want to update
      while(true) {
         //check if valid userID
         try {
            System.out.println("\nEnter userID that you would like to update: ");
            userID = Integer.parseInt(in.readLine());
            query = String.format("SELECT name FROM Users WHERE userID = " + userID + ";");
            List<List<String>> userExistsList = esql.executeQueryAndReturnResult(query);
            if(userExistsList.size() <= 0) {
               System.out.println("This user does not exist, please enter a valid userID");
               continue;
            }
            break;
         }
         catch(Exception e) {
            System.err.println(e.getMessage());
         }
      }

      while(true) {
         try {
            System.out.println("1. Name");
            System.out.println("2. Password");
            System.out.println("3. Latitude");
            System.out.println("4. Longitude");
            System.out.println("\nWhat would you like to update: ");
            updateNumber = Integer.parseInt(in.readLine());
            if(updateNumber == 1) {
               System.out.println("\nEnter the new name: ");
               name = in.readLine();
               query = String.format("UPDATE Users U SET name = '" + name + "' WHERE userID = " + userID + ";");
               esql.executeUpdate(query);
               System.out.println("Name successfully updated!");
               break;
            }
            else if(updateNumber == 2) {
               System.out.println("\nEnter the new password: ");
               password = in.readLine();
               query = String.format("UPDATE Users U SET password = '" + password + "' WHERE userID = " + userID + ";");
               esql.executeUpdate(query);
               System.out.println("Password successfully updated!");
               break;
            }
            else if(updateNumber == 3) {
               System.out.println("\nEnter the new latitude: ");
               latitude = in.readLine();
               query = String.format("UPDATE Users U SET latitude = " + latitude + " WHERE userID = " + userID + ";");
               esql.executeUpdate(query);
               System.out.println("Latitude successfully updated!");
               break;
            }
            else if(updateNumber == 4) {
               System.out.println("\nEnter the new latitude: ");
               longitude = in.readLine();
               query = String.format("UPDATE Users U SET latitude = " + longitude + " WHERE userID = " + userID + ";");
               esql.executeUpdate(query);
               System.out.println("Longitude successfully updated!");
               break;
            }
         }
         catch(Exception e) {
			   System.err.println(e.getMessage());
		   }
      }
   }
}//end Retail

