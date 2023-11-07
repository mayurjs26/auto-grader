/**
 * AutoGrader
*/
package csci_620_grading_software;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import org.bson.Document;
import csci_620_grading_software.RecordUtils.Configuration;

public class JavaGrader extends AutoGrader{

    
    public JavaGrader(Configuration config) {
	super(config);

    }

    
    public boolean runStudentProject(String[] processArgs, int waitInTime) {
	

	System.out.println("Running student java project.......");
	cleanProject();	
	String runCommands[] = new String[1 + processArgs.length];
	runCommands[0] =  "app/build/install/app/bin/app" + (configuration.windows()? ".bat" : "");
   
	try (Connection con = Utils.getDatabaseConnection(configuration)) {
	    Utils.deleteAllTables(con, configuration.databaseName());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	for (int i = 1; i < runCommands.length; i++) {
	    runCommands[i] = processArgs[i-1];
	}
	try {
	    ProcessBuilder builder = new ProcessBuilder(runCommands).directory(new File(configuration.projectPath())).redirectErrorStream(true);
	    builder.environment().put("JAVA_OPTS", "-Xmx" + configuration.maxSize() + "m");
	    boolean done =  Utils.runProcess(runCommands, configuration.projectPath(), waitInTime);

	} catch(Exception e) {
	    e.printStackTrace();
	    return false;

	}
	return true;
    

    }


    public void cleanProject() {

	System.out.println("Cleaning Gradle Project.....");
	String cleanCommands[] = new String[]{"gradle", "clean"};
	try {
	    ProcessBuilder builder = new ProcessBuilder(cleanCommands).directory(new File(configuration.projectPath())).redirectErrorStream(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }



    public boolean buildStudentProject() {

	System.out.println("Building Java Project ......");
		try {
		
			File gradleProject = new File(configuration.projectPath());
			if (gradleProject != null) {
				File buildFolder = new File(gradleProject, "app/build");
				
				// Delete all previous builds first.
				if (buildFolder.exists())
					MoreFiles.deleteRecursively(Paths.get(buildFolder.toURI()), RecursiveDeleteOption.ALLOW_INSECURE);
				
				// Get total size and make sure it is within the requirements.
				long size = Utils.getFolderSize(gradleProject);
				if (size/1024.0 > configuration.maxSize()) {
					System.out.println("The total size is " + size/1024.0 + " which is larger than expected.");
				}
				
				// Build and install Gradle.
				String gradlewFile = "gradle"+(configuration.windows()?".bat":"");
				File gradlewFileObj = new File(gradleProject.getAbsolutePath() + "/" + gradlewFile);
				if (!configuration.windows())
					Files.setPosixFilePermissions(Paths.get(gradlewFileObj.toURI()), PosixFilePermissions.fromString("rwxrwxr-x"));
				
				boolean error = Utils.runProcess(new String[]{gradlewFile, "build"}, configuration.projectPath(), 5);
				Utils.runProcess(new String[]{gradlewFile,"installDist"}, configuration.projectPath(), 5);

				if (error) {
					System.out.println("Could not compile/install project.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return true;
    }  

    public static void executeTestCases() {
	
	Assignment1TestCases assignment1TestCases = new Assignment1TestCases();
	try {
	    Document jsonDocObj = Utils.readJsonFile();
	    Document testDefinitions = jsonDocObj.get("tests", Document.class);
	    for (String testDefinition : testDefinitions.keySet()) {
		System.out.println(testDefinition);
		Document testDefinitionDoc = testDefinitions.get(testDefinition, Document.class);
		// String testCaseMethod = testDefinitionDoc.get("methodName", String.class);
		String outputFormat = testDefinitionDoc.get("outputFormat", String.class);
		String inputQuery = testDefinitionDoc.get("inputQuery", String.class);
		// Method testCaseMethodInstance = assignment1TestCases.getClass().getMethod(testCaseMethod, Document.class,Document.class);
		ArrayList<Document> cases = testDefinitionDoc.get("cases", ArrayList.class);
		System.out.println(testDefinitionDoc.get("cases").getClass());
		for (Document testCase : cases) {
		    if (outputFormat.equals("database")) {
			Connection connection = null;
			assignment1TestCases.executeDatabaseTestCase(connection, inputQuery,"", testCase.get("input", Document.class), testCase.get("output", Document.class));
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
