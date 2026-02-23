package shell.execution;

import shell.builtin.BuiltinRegistry;
import shell.parser.SimpleCommand;

import java.io.PrintStream;

public final class BuiltinExecutor {
    private final BuiltinRegistry registry;
    private final ShellContext ctx;

    public BuiltinExecutor(BuiltinRegistry registry, ShellContext ctx) {
        this.registry = registry;
        this.ctx = ctx;
    }

    public boolean supports(SimpleCommand cmd) {
        return registry.isBuiltin(cmd.commandName());
    }

    public boolean supportsName(String name) {
        return registry.isBuiltin(name);
    }

    // Builtins interface does not take stdin, so neither does this.
    public void execute(SimpleCommand cmd, PrintStream stdout, PrintStream stderr) {
        registry.execute(cmd, stdout, stderr);
    }

    public ShellContext context() {
        return ctx;
    }
}