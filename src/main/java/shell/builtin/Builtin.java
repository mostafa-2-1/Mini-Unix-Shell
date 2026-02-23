package shell.builtin;

import shell.parser.SimpleCommand;

import java.io.PrintStream;

public interface Builtin {
    String name();
    void execute(SimpleCommand cmd, PrintStream out, PrintStream err);
}