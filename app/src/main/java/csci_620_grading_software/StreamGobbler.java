package csci_620_grading_software;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * StreamGobbler
 */
public class StreamGobbler extends Thread {

	private InputStream is;
	private boolean print;

	public StreamGobbler(boolean print) {
		this.print = print;
	}

	public void setInputStream(InputStream inputStream) {
	    this.is = inputStream;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				if (print)
					System.out.println(line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
