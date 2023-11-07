package csci_620_grading_software;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import java.sql.Connection;
import csci_620_grading_software.RecordUtils.Configuration;

/**
 * AutoGrader
 */
public abstract class AutoGrader {

	public Configuration configuration;

	public StreamGobbler gobbler;

	public List<Issue> issues;
	
	public List<TestCase> testCaseList;

	public AutoGrader(Configuration config) {
		this.configuration = config;
		this.gobbler = new StreamGobbler(config.gobbler());
		this.issues = new ArrayList<>();
		this.testCaseList = new ArrayList<>();
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	public abstract boolean buildStudentProject();

	public abstract boolean runStudentProject(String[] args, int waitInTime);


	public void startGrading() {
		System.out.println("Grading project at path : " + configuration.projectPath()); 
		Document assignmentTestCaseDocument =  Utils.readJsonFile();		
		String outputFormat = assignmentTestCaseDocument.get("outputFormat", String.class);

		buildStudentProject();
		Document testDefinitions = assignmentTestCaseDocument.get("tests", Document.class);
		String[] args = configuration.args();
		String commonDbArgs[] = new String[3];
		commonDbArgs[0] = Utils.getJDBCURL(configuration.host(), configuration.port(), configuration.database());
		commonDbArgs[1] = configuration.user();
		commonDbArgs[2] = configuration.password();

		for (String testCaseName : testDefinitions.keySet()) {

			Document testCaseDefinition = testDefinitions.get(testCaseName, Document.class);
			ArrayList<Document> testCases = testCaseDefinition.get("cases", ArrayList.class);
			int penalty = testCaseDefinition.get("penalty", Integer.class);
			int waitInTime = testCaseDefinition.get("waitInTime", Integer.class); 
			
			for (Document testCase : testCases) {
				Document testCaseInput = testCase.get("input", Document.class);
				Document testCaseOutput = testCase.get("output", Document.class);
				TestCase testCaseObj = new TestCase(outputFormat, penalty, waitInTime, testCaseInput, testCaseOutput, commonDbArgs);
		
				if (outputFormat == "database")
					testCaseObj.inputQuery = testCase.getString("inputQuery");
				testCaseList.add(testCaseObj);
			}
		}
		
		


		if (outputFormat.equals("file")) {

			for(TestCase testCase : testCaseList) {
				

				String runCommands[] = new String[3 + testCase.input.keySet().size() + args.length];
				runCommands[0] = Utils.getJDBCURL(configuration.host(), configuration.port(), configuration.database());
				runCommands[1] = configuration.user();
				runCommands[2] = configuration.password();
				int index = 3;
				for(String key : testCase.input.keySet()) {
					runCommands[index++] = testCase.input.get(key, String.class);
				}
				for (String arg : args) {
					runCommands[index++] = arg;
				}
				runStudentProject(runCommands, testCase.waitInTime);
				testCase.executeFileTestCase("");



			}
		} else {

			String runCommands[] = new String[3 + args.length];
			runCommands[0] = Utils.getJDBCURL(configuration.host(), configuration.port(), configuration.database());
			runCommands[1] = configuration.user();
			runCommands[2] = configuration.password();
			int index = 3;
			for (String arg : args) {
				runCommands[index++] = arg;
			}
			int runTimeWait = assignmentTestCaseDocument.getInteger("runWaitInTime");
			runStudentProject(runCommands, runTimeWait);

			for (TestCase testCase : testCaseList) {

				try {
					Connection connection = Utils.getDatabaseConnection(configuration);
					testCase.executeDatabaseTestCase(connection, testCase.inputQuery);
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				

			}

		}
		
		for (TestCase testCase : testCaseList) {

			for (Issue issue : issues) {

				testCase.evaluateIssues(issue);	


			}

		}
	}
	
}
