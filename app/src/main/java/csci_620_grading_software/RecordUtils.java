package csci_620_grading_software;

public class RecordUtils {
    public static record Configuration(
	    String language,
	    String database,
	    String user,
	    String password,
	    String host,
	    String port,
	    String databaseName,
	    boolean windows,
	    int maxSize,
	    int maxMem,
	    String projectPath,
	    boolean gobbler,
	    String[] args
		){}
}
