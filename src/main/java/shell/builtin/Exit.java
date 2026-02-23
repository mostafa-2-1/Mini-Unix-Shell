package shell.builtin;

import shell.parser.SimpleCommand;

import java.io.PrintStream;



public class Exit implements Builtin {


	@Override
	public String name() {
		return "exit";
	}

	@Override
	public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
	
		int code = 0;
		if (cmd.argv().size() >= 2) {
			try {
				code = Integer.parseInt(cmd.argv().get(1));
			} catch (NumberFormatException ignored) {
				code = 0; 
			}
		}
		throw new ShellExitException(code);
	}

}