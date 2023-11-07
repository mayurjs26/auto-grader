package csci_620_grading_software;

import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptEngineFactory;

/*
 * Issue
 */
public class Issue {

	private	String condition;
	private String message;
	
	public Issue(String condition, String message) {
	    this.condition = condition;
	    this.message = message;
	}

	public boolean evaluateCondition(String console, String output, int maxMem) {
	    
	    ScriptEngine jScriptEngine = new ScriptEngineManager().getEngineByName("js");
	    Bindings bindings = jScriptEngine.createBindings();
	    if (condition.contains("console"))
		bindings.put("console", console);
	    if (condition.contains("output"))
		bindings.put("output", output);
	    if (condition.contains("maxMem"))
		bindings.put("maxMem", maxMem);

	    try {
		Object bindingsResult = jScriptEngine.eval(condition, bindings);
		System.out.println("Issues output :  ");
		System.out.println((boolean) bindingsResult);
	    } catch(ScriptException e) {
		e.printStackTrace();

	    }
	    return false;

	}

	public static void main(String[] args) {
		Issue issue = new Issue("console.includes('error') && !output.includes('some id')",
			"some text");

		issue.evaluateCondition("error", "no", 0);
	}
    
}
