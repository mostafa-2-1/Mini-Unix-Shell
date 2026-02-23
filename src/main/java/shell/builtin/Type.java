package shell.builtin;

import shell.parser.SimpleCommand;
import shell.execution.ShellContext;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Type implements Builtin {

    private final BuiltinRegistry registry;
    private final ShellContext ctx;

    public Type(BuiltinRegistry registry, ShellContext ctx) {
        this.registry = registry;
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "type";
    }

    @Override
    public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
        List<String> argv = cmd.argv();
        if (argv.size() < 2) {
            err.println("type: missing operand");
            return;
        }
        String target = argv.get(1);

        if (registry.isBuiltin(target)) {
            out.println(target + " is a shell builtin");
            return;
        }

        Path exec = findExecutable(target);
        if (exec != null) {
            out.println(target + " is " + exec);
        } else {
            err.println(target + ": not found");
        }
    }

    private Path findExecutable(String command) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) return null;

        for (String dir : pathEnv.split(System.getProperty("path.separator"))) {
            Path candidate = ctx.resolve(dir).resolve(command);
            if (Files.exists(candidate) && Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}