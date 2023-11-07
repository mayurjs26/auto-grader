package csci_620_grading_software;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import csci_620_grading_software.RecordUtils.Configuration;
import org.bson.Document;

/**
 * TestCase
 */
public class TestCase {

    
    public String[] commonArgs;
    public String type;
    public Document input;
    public Document output;
    private String console;
    private int penalty;
    private int memoryUsed;
    public int waitInTime;
    public String inputQuery;
    public String finalOutput;
    public boolean success;


    public TestCase(String type, int penalty, int waitInTime, Document input, Document output, String commonArgs[]) {
	this.commonArgs = commonArgs;
	this.type = type;
	this.penalty = penalty;
	this.waitInTime = waitInTime;
	this.input = input;
	this.output = output;
    }
    

    public boolean executeFileTestCase(String outputFile) {

	File o = new File(outputFile);
	if (!o.exists()) {
	    System.out.println("Output File doesnt exist");
	}
	else {
		try {
			Set<String> relationsDiscovered = new HashSet<String>(Files.readAllLines(o.toPath()));
			this.finalOutput = Utils.getOutputString(Files.readAllLines(o.toPath()));
			Set<String> remainingDiscovered = new HashSet<>(relationsDiscovered);
			Set<String> expected = new HashSet<>(Arrays.asList(output.getString("expected")));
			remainingDiscovered.removeAll(expected);
			Set<String> remainingExpected = new HashSet<>(expected);
			remainingExpected.removeAll(relationsDiscovered);
			
			if (!remainingDiscovered.isEmpty() || !remainingExpected.isEmpty()) {
				System.out.println("Expected output not found: " + Utils.getOutputString(remainingExpected));
				System.out.println("Discovered output not expected: " + Utils.getOutputString(remainingDiscovered));
				System.out.println("\tPenalty: -10");
				this.success = false;
			} else {
			    System.out.println("TestCase passed successfully");
			    this.success = true;
			}

		} catch(Exception e) {
			e.printStackTrace();	
		}
	}

	return true;
    }
    
    public void executeDatabaseTestCase(Connection connection, String inputQuery) {
		
		
		for(String key : input.keySet()) {
			inputQuery +=  " " + key + " = ? and ";
		}
		inputQuery = inputQuery.substring(0, inputQuery.length() - 5);
		System.out.println(inputQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(inputQuery);
			int index = 1;
			for (String key : input.keySet()) {
				Object paramValue = input.get(key);
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
			StringBuffer outputStr = new StringBuffer();
			for (String key : output.keySet()) {
				if (resultSet.next()) {
					Object paramValue = output.get(key);
					Class paramClass = paramValue.getClass();
					if (paramClass == Integer.class && !(resultSet.getInt(key) == (Integer) paramValue)) {
					    outputStr.append((Integer) paramValue + ";");
					    result = false;
					}
					else if (paramClass == String.class && !(resultSet.getString(key) == (String) paramValue)) {
					    outputStr.append((String) paramValue + ";");	
					    result = false;
					}
					else if (paramClass == Boolean.class && !(resultSet.getBoolean(key) == (Boolean) paramValue)) {
					    outputStr.append((Boolean) paramValue + ";");
					    result = false;
					}
					else if (paramClass == Double.class && !(resultSet.getDouble(key) == (Double) paramValue)) {
					    outputStr.append((Double) paramValue + ";");
					    result = false;
					}
					index++;
				}
			}
			this.finalOutput = outputStr.toString();
			this.success = result;

			System.out.println("Result : " + result);

			System.out.println(statement);
		} catch (Exception exception) {
			exception.printStackTrace();	
		}
	}


	public void evaluateIssues(Issue issue) {

	    System.out.println("Evaluating issue feedback for testcase...");

	}


}
