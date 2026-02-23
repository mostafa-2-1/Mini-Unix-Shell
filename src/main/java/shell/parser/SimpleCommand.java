package shell.parser;

import java.util.List;
import java.util.Objects;

public final class SimpleCommand implements Command{
	private final List<String> argv;
	private final Redirection redirection;

	public SimpleCommand(List<String> argv, Redirection redirection) {
		if (argv == null || argv.isEmpty()) {
			throw new IllegalArgumentException("argv must not be empty");
		}
		this.argv = List.copyOf(argv);
		this.redirection = Objects.requireNonNull(redirection);
	}

	public List<String> argv() {
		return argv;
	}

	public String commandName() {
		return argv.get(0);
	}

	public Redirection redirection() {
		return redirection;
	}
}
