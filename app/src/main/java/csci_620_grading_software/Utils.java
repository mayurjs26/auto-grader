package csci_620_grading_software;


import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import csci_620_grading_software.RecordUtils.Configuration;
import org.bson.Document;


public class Utils {


	public static void executePoetryCommands(Configuration config) {
		
		/* try {
			System.out.println("Executing poetry commands");	
			String poetryBuildCommands[] = new String[]{"poetry", "build"};
			URL url = Utils.class.getClassLoader().getResource("jsonFiles/testCases/assignment_1.json");
			String jsonFilePath = url.toURI().getPath();

			String poetryRunCommands[] = new String[]{"poetry", "run", "python3", "main.py", config.user(), config.password(), config.host(), config.database(), jsonFilePath};
			
			boolean buildSuccess = runProcess(poetryBuildCommands, 1);
			System.out.println(buildSuccess);
			if (buildSuccess) {
			    boolean runSuccess = runProcess(poetryRunCommands, 5);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} */

	}

	public static boolean runProcess(String[] command, String projectPath, int waitInMin) {
			
		boolean ret = false;
		ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true).directory(new File(projectPath));
		Process process = null;
		try {
			process = builder.start();
			StreamGobbler gobbler = new StreamGobbler(true);
			gobbler.setInputStream(process.getInputStream());	
			gobbler.start();
			ret = process.waitFor(waitInMin, TimeUnit.MINUTES);
		} catch (Exception oops) {
			System.out.println("A major problem happened: " + oops.getMessage() + "; ");
			ret = true;
			oops.printStackTrace();
		} finally {
			builder = null;
			if (process != null)
				process.destroy();
		}
		return ret;
	}    

	public static Document readJsonFile() {
		
		try {
			URL url = Utils.class.getClassLoader().getResource("jsonFiles/testCases/assignment_2.json");
			String jsonFilePath = url.toURI().getPath();
			String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
			Document doc = Document.parse(jsonString);
			return doc;
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public static Configuration readConfigFile() {
			
		InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream("jsonFiles/config.json");
		Reader inputStreamReader = new InputStreamReader(inputStream); 
		Gson gson = new Gson();
		Configuration config = gson.fromJson(inputStreamReader, Configuration.class);
		
		return config;
	}

	public static Connection getDatabaseConnection(Configuration config) {
		
		Connection con = null;
		try {
			String jdbcUrl = getJDBCURL(config.host(), config.port(), config.database());
			con = DriverManager.getConnection(jdbcUrl, config.user(), config.password());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	public static long getFolderSize(File folder) throws Exception {
		AtomicLong size = new AtomicLong();
		Files.walk(folder.toPath()).forEach(f -> {
			File file = f.toFile();
			if (file.isFile()) {
				size.addAndGet(file.length());
			}
		});
		return size.get();
		}

	public static String getJDBCURL(String host, String port, String database) {
		String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC&useSSL=false";
		return jdbcUrl;
	}
	public static void executeDatabaseTestCase(Connection connection, String inputQuery, String outputQuery, Document inputParameter, Document outputParameter) {
		
		
		for(String key : inputParameter.keySet()) {
			inputQuery +=  " " + key + " = ? and ";
		}
		inputQuery = inputQuery.substring(0, inputQuery.length() - 5);
		System.out.println(inputQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(inputQuery);
			int index = 1;
			for (String key : inputParameter.keySet()) {
				Object paramValue = inputParameter.get(key);
				Class paramClass = paramValue.getClass();
				if (paramClass == Integer.class) 
					statement.setInt(index, (Integer) paramValue);
				else if (paramClass == String.class)
					statement.setString(index, (String) paramValue);
				else if (paramClass == Boolean.class)
					statement.setBoolean(index, (Boolean) paramValue);
				else if (paramClass == Double.class)
					statement.setDouble(index, (Double) paramValue);
				index++;
			}

			ResultSet resultSet = statement.executeQuery();
			index = 1;
			boolean result = true;
			for (String key : outputParameter.keySet()) {
				if (resultSet.next()) {
					Object paramValue = outputParameter.get(key);
					Class paramClass = paramValue.getClass();
					if (paramClass == Integer.class && !(resultSet.getInt(key) == (Integer) paramValue))
						result = false;
					else if (paramClass == String.class && !(resultSet.getString(key) == (String) paramValue))
						result = false;
					else if (paramClass == Boolean.class && !(resultSet.getBoolean(key) == (Boolean) paramValue))
						result = false;	
					else if (paramClass == Double.class)
						result = false;
					index++;
				}
			}

			System.out.println("Result : " + result);

			System.out.println(statement);
		} catch (Exception exception) {
			exception.printStackTrace();	
		}
	}


	public static String getRelations(Collection<String> col) {
		StringBuffer buf = new StringBuffer();
		for (String str : col) {
			buf.append(str);
			buf.append("; ");
		}
		if (buf.length() > 2)
			buf.delete(buf.length()-2, buf.length());
		return buf.toString();
	}
	
	public static void executeFileTestCase(String outputFile, String expectedOutput) {
				
		File o = new File(outputFile);
		if (!o.exists()) {
			System.out.println("Output File doesnt exist");
		}
		else {
			try {
				Set<String> relationsDiscovered = new HashSet<String>(Files.readAllLines(o.toPath()));
				Set<String> remainingDiscovered = new HashSet<>(relationsDiscovered);
				Set<String> expected = new HashSet<>(Arrays.asList(expectedOutput.split(";")));
				remainingDiscovered.removeAll(expected);
				Set<String> remainingExpected = new HashSet<>(expected);
				remainingExpected.removeAll(relationsDiscovered);
				
				if (!remainingDiscovered.isEmpty() || !remainingExpected.isEmpty()) {
					System.out.println("Expected output not found: " + getRelations(remainingExpected));
					System.out.println("Discovered output not expected: " + getRelations(remainingDiscovered));
					System.out.println("\tPenalty: -10");
				}

			} catch(Exception e) {
				e.printStackTrace();	
			}
		}

		
	} 

	public static void deleteAllTables(Connection connection, String schema) {
		String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			while (true) {
				System.out.println("Deleting existing tables");	
				statement.setString(1, schema);
				ResultSet rs = statement.executeQuery();
				if (!rs.first())
					break;
				while (rs.next()) {

					try {
						System.out.println("Table name : " + rs.getString(1));
						connection.createStatement().execute("DROP TABLE IF EXISTS " + rs.getString(1)); 
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error deleting table: " + rs.getString(1));
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();	
		}

	}

}
