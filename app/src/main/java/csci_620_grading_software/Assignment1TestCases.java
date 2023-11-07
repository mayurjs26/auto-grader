package csci_620_grading_software;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.checkerframework.checker.units.qual.s;

/**
 * Assignment1TestCases
 */

//TODO Junit parameterized tests
//TODO Implement 
public class Assignment1TestCases {

	 

	public void executeDatabaseTestCase(Connection connection, String inputQuery, String outputQuery, Document inputParameter, Document outputParameter) {
		
		
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

			System.out.println(statement);
		} catch (Exception exception) {
			exception.printStackTrace();	
		}
	}
	

	public void executeFileTestCase(String projectPath, String outputFile, String inputFormat, Document inputParameter, Document outputParameter) {
		outputFile = projectPath + "/" + outputFile;
		String commandPrefix = "/app/build/install/app/bin/app" ;
		List<String> commands = new ArrayList<>();
		commands.add(commandPrefix);
		for (String key : inputParameter.keySet()) {
			commands.add(inputParameter.getString(key));
		}
		commands.add(outputFile);
		String commandsArr[] = new String[commands.size()];

		for (int i=0; i < commands.size(); i++) {
			commandsArr[i] = commands.get(i);
		}
		
		boolean processComplete = false;/* Utils.runProcess(commandsArr, 4); */
		
		if (processComplete) {
			File o = new File(outputFile);
			if (!o.exists()) {
				System.out.println("Output File doesnt exist");
				//Penalize
			}
			else {

				try {
				Set<String> expected = new HashSet<>();
				Set<String> relationsDiscovered = new HashSet<>(Files.readAllLines(o.toPath()));

				Set<String> remainingDiscovered = new HashSet<>(relationsDiscovered);
				
				remainingDiscovered.removeAll(expected);
								
				Set<String> remainingExpected = new HashSet<>(expected);
				remainingExpected.removeAll(relationsDiscovered);
				
				if (!remainingDiscovered.isEmpty() || !remainingExpected.isEmpty()) {
/* 					System.out.println("Expected relations not found: " + getRelations(remainingExpected));
					System.out.println("Discovered relations not expected: " + getRelations(remainingDiscovered));
					System.out.println("\tPenalty: -10"); */
				}
				} catch(Exception e) {
				
				}
			}
		}

		
	}
	public void checkMoviesTable(String outputFormat, String inputFormat, Document inputParameters, Document expectedOutputParameters) { 
		System.out.println("In Check movies method");
		System.out.println(inputParameters);
		System.out.println(expectedOutputParameters);

	}

	public void checkActorsTable(String outputFormat, String inputFormat, Document inputParameters, Document expectedOutputParameters) {
		System.out.println("In check actor method");
		System.out.println(inputParameters);
		System.out.println(expectedOutputParameters);
	}
}
