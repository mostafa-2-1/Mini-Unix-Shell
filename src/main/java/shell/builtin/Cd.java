package shell.builtin;

import shell.execution.ShellContext;
import shell.parser.SimpleCommand;

import java.io.PrintStream;

public class Cd implements Builtin {

    private final ShellContext ctx;

    public Cd(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "cd";
    }

    @Override
    public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
        var argv = cmd.argv();

 
        if (argv.size() < 2) {
            return;
            // err.println("cd: missing operand"); return;
        }

        String raw = argv.get(1);
        try {
            ctx.changeDirectory(raw); // handles ~, relative paths, canonical resolution
        } catch (IllegalArgumentException e) {
            // ShellContext already formats the message 
            err.println(e.getMessage());
        }
    }
}