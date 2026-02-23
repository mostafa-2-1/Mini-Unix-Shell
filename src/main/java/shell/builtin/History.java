package shell.builtin;

import shell.parser.SimpleCommand;
import shell.history.HistoryManager;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class History implements Builtin {

	private final HistoryManager historyManager;

	public History(HistoryManager historyManager) {
		this.historyManager = historyManager;
	}

	@Override
	public String name() {
	    return "history";
	}

	@Override
	public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
		List<String> argv = cmd.argv();
		Integer n = null;
		if (argv.size() >= 2) {
			try {
				n = Integer.parseInt(argv.get(1));
			} catch (NumberFormatException ignored) {}
		}
		PrintWriter pw = new PrintWriter(out, true);
		historyManager.print(pw, n);
		pw.flush();
	}
}