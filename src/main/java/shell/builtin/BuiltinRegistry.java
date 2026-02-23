package shell.builtin;

import shell.parser.SimpleCommand;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinRegistry {
	private final Map<String, Builtin> builtins = new HashMap<>();

	public BuiltinRegistry(List<Builtin> allBuiltins) {
		for (Builtin b : allBuiltins) {
			builtins.put(b.name(), b);
		}
	}

	public boolean isBuiltin(String name) {
		return builtins.containsKey(name);
	}

	public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
		Builtin builtin = builtins.get(cmd.commandName());
		if (builtin == null) {
			throw new IllegalStateException("Builtin not found: " + cmd.commandName());
		}
		builtin.execute(cmd, out, err);
	}
	public void register(Builtin builtin) {
		String name = builtin.name();
		if (builtins.containsKey(name)) {
			throw new IllegalStateException("Duplicate builtin registered: " + name);
		}
		builtins.put(name, builtin);
	}
	
	// in BuiltinRegistry
	public java.util.Set<String> names() {
	    return java.util.Collections.unmodifiableSet(builtins.keySet());
	}
}
