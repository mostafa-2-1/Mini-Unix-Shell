package shell.execution;

import shell.parser.Command;
import shell.parser.PipelineCommand;
import shell.parser.SimpleCommand;

import java.io.InputStream;
import java.io.PrintStream;

public final class Executor {
    private final BuiltinExecutor builtinExecutor;
    private final ExternalCommandExecutor externalExecutor;
    private final PipelineExecutor pipelineExecutor;

    public Executor(BuiltinExecutor builtinExecutor,
                    ExternalCommandExecutor externalExecutor,
                    PipelineExecutor pipelineExecutor) {
        this.builtinExecutor = builtinExecutor;
        this.externalExecutor = externalExecutor;
        this.pipelineExecutor = pipelineExecutor;
    }

    public void execute(Command cmd,
                        InputStream stdin,
                        PrintStream stdout,
                        PrintStream stderr) throws Exception {

        if (cmd instanceof PipelineCommand pc) {
            pipelineExecutor.execute(pc, stdin, stdout, stderr);
            return;
        }

        if (cmd instanceof SimpleCommand sc) {
            if (builtinExecutor.supports(sc)) {
                builtinExecutor.execute(sc, stdout, stderr);
            } else {
                externalExecutor.execute(sc, stdin, stdout, stderr);
            }
        }
    }
}