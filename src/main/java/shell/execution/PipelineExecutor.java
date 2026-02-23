package shell.execution;

import shell.parser.PipelineCommand;
import shell.parser.SimpleCommand;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class PipelineExecutor {

    private final BuiltinExecutor builtinExecutor;
    private final ExternalCommandExecutor externalExecutor;

    public PipelineExecutor(BuiltinExecutor builtinExecutor,
                            ExternalCommandExecutor externalExecutor) {
        this.builtinExecutor = builtinExecutor;
        this.externalExecutor = externalExecutor;
    }

    public void execute(PipelineCommand pipeline,
                        InputStream stdin,
                        PrintStream stdout,
                        PrintStream stderr) throws Exception {

        List<Thread> stages = new ArrayList<>();
        InputStream currentIn = stdin;

        for (int i = 0; i < pipeline.stages().size(); i++) {
            SimpleCommand cmd = pipeline.stages().get(i);
            boolean last = (i == pipeline.stages().size() - 1);

            PipedOutputStream pipeOut = last ? null : new PipedOutputStream();
            PipedInputStream nextIn = last ? null : new PipedInputStream(pipeOut);

            InputStream in = currentIn;                  // capture per-iteration
            OutputStream out = last ? stdout : pipeOut;  // also capture per-iteration

            Thread t = new Thread(() -> {
                try {
                    if (builtinExecutor.supports(cmd)) {
                        PrintStream ps = new PrintStream(out, true);
                        builtinExecutor.execute(cmd, ps, stderr);
                        ps.flush();
                        // Don't close ps if it wraps System.out 
                    } else {
                        externalExecutor.execute(cmd, in, out, stderr);
                    }
                } catch (Exception e) {
                    e.printStackTrace(stderr);
                } finally {
                    //  close the pipe output so downstream gets EOF
                    if (!last) {
                        try { out.close(); } catch (IOException ignored) {}
                    }
                    //  close piped input streams we created
                    if (in instanceof PipedInputStream) {
                        try { in.close(); } catch (IOException ignored) {}
                    }
                }
            });

            t.start();
            stages.add(t);

            currentIn = nextIn; // next stage reads from our pipe
        }

        for (Thread t : stages) {
            t.join();
        }
    }
}