package shell.builtin;

import shell.parser.SimpleCommand;

import java.io.PrintStream;
import java.util.List;

public class Echo implements Builtin {

	@Override
	public String name() {
	    return "echo";
	}

    @Override
    public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
        List<String> argv = cmd.argv();
        for (int i = 1; i < argv.size(); i++) {
            out.print(argv.get(i));
            if (i + 1 < argv.size()) out.print(" ");
        }
        out.println();
    }
}