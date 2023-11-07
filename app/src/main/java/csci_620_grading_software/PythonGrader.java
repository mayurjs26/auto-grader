package csci_620_grading_software;

import csci_620_grading_software.RecordUtils.Configuration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;


/**
 * PythonGrader
 */
public class PythonGrader extends AutoGrader{

    public PythonGrader(Configuration config) {
	super(config); 
    }

    public boolean runStudentProject(String[] args, int waitInTime) {

	System.out.println("Running Python project .....");
	String runCommands[] = new String[5 + args.length]; 
	try {

	    File poetryProject = new File(configuration.projectPath());
	    if (poetryProject != null) {
		runCommands[0] = "poetry";
		runCommands[1] = "python3";
		runCommands[2] = "run";
		runCommands[3] = "main.py";
		runCommands[4] = poetryProject.getAbsolutePath();

		int index = 5;
		for(String arg : args) {
		    runCommands[index++] = arg;
		}
		System.out.println("Run Commands..........");
		for (String command : runCommands) {
		    System.out.print(command + " ");
		}
		System.out.println();
		
		ProcessBuilder builder = new ProcessBuilder(runCommands).redirectErrorStream(true);

		Process process = null;

		long before = System.nanoTime();
		
		process = builder.start();
		this.gobbler.setInputStream(process.getInputStream());
		this.gobbler.start();
		boolean processComplete = process.waitFor(waitInTime, TimeUnit.MINUTES);
		long after = System.nanoTime();
		double timeTaken = (after - before) / (1e9 * 3600);
		System.out.println("The process took " + timeTaken + " hours.");

		if (!processComplete) {
		    System.out.println("The process did not run in the expected time.");
		    System.out.println("\tPenalty : -5");
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} 

	return true;

    }
    
    public boolean buildStudentProject() {

	System.out.println("Building Python Project .......");

	try {
	    File poetryProject = new File(configuration.projectPath());
	    if (poetryProject != null) {
		boolean error = Utils.runProcess(new String[]{"poetry" , "build"}, configuration.projectPath() , 5);
		error = error || Utils.runProcess(new String[]{"poetry", "install"}, configuration.projectPath() , 5);
		if (!error) {
		    System.out.println("Could not install or compile");
		    return false;
		}

	    }
	} catch(Exception e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
	
    }


}
