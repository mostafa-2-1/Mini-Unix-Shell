package shell.builtin;

import shell.execution.ShellContext;
import shell.parser.SimpleCommand;

import java.io.PrintStream;

public class Pwd implements Builtin {

    private final ShellContext ctx;

    public Pwd(ShellContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String name() {
        return "pwd";
    }

    @Override
    public void execute(SimpleCommand cmd, PrintStream out, PrintStream err) {
        out.println(ctx.getCurrentDirectory().toString());
    }
}