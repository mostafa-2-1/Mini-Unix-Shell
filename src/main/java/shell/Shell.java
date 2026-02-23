package shell;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import shell.builtin.*;
import shell.completion.ShellCompleter;
import shell.execution.*;
import shell.history.HistoryManager;
import shell.parser.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Shell {

	public static void main(String[] args) throws Exception {
		new Shell().run();
	}

	private final HistoryManager historyManager = new HistoryManager();
	private final CommandParser commandParser = new CommandParser();

	public void run() throws Exception {
		Terminal terminal = buildTerminal();

		// shared shell state
		ShellContext ctx = new ShellContext(Paths.get("").toAbsolutePath());

		// builtins + registry
		BuiltinRegistry registry = buildBuiltinRegistry(ctx);

		// executors
		BuiltinExecutor builtinExecutor = new BuiltinExecutor(registry, ctx);
		ExternalCommandExecutor externalExecutor = new ExternalCommandExecutor(ctx);
		PipelineExecutor pipelineExecutor = new PipelineExecutor(builtinExecutor, externalExecutor);
		Executor executor = new Executor(builtinExecutor, externalExecutor, pipelineExecutor);

		LineReader reader = buildReader(terminal, registry);
		//DefaultHistory jlineHistory = new DefaultHistory(reader);

		Path histFile = resolveHistFile();
		//preloadHistory(histFile, jlineHistory);
		preloadHistory(histFile, reader);

		try {
			//replLoop(reader, jlineHistory, histFile, executor, builtinExecutor);
			replLoop(reader, histFile, executor, builtinExecutor);
		} catch (ShellExitException e) {
			// Controlled shutdown
		} finally {
			// persist history on any exit path (exit builtin, ctrl-d, exceptions)
			try {
				historyManager.onExit(histFile);
			} catch (IOException ignored) {}
		}
	}

	// ---------------- bootstrapping ----------------

	//    private Terminal buildTerminal() throws IOException {
	//        return TerminalBuilder.builder().system(true).build();
	//    }
	private Terminal buildTerminal() throws IOException {
		return TerminalBuilder.builder()
				.system(true)
				.jna(true)
				.build();
	}

	private LineReader buildReader(Terminal terminal, BuiltinRegistry registry) {
		DefaultParser parser = new DefaultParser();
		parser.setEscapeChars(null);
		parser.setQuoteChars(new char[0]);
		parser.setEofOnUnclosedQuote(false);

		return LineReaderBuilder.builder()
				.terminal(terminal)
				.parser(parser)
				.history(new DefaultHistory())
				.completer(new ShellCompleter(registry))
				.build();
	}

	private BuiltinRegistry buildBuiltinRegistry(ShellContext ctx) {
		BuiltinRegistry registry = new BuiltinRegistry(List.of(
				new Cd(ctx),
				new Pwd(ctx),
				new Echo(),
				new History(historyManager),
				new Exit()
				));

		// type needs registry (and ctx for PATH-relative resolution)
		registry.register(new Type(registry, ctx));
		return registry;
	}

	private Path resolveHistFile() {
		String histFilePath = System.getenv("HISTFILE");
		return (histFilePath == null || histFilePath.isBlank()) ? null : Paths.get(histFilePath);
	}

	//private void preloadHistory(Path histFile, DefaultHistory jlineHistory) {
	private void preloadHistory(Path histFile, LineReader reader)    {
	if (histFile == null) return;
	try {
		historyManager.preload(histFile, reader.getHistory());
		historyManager.markWritten();
	} catch (IOException ignored) {}
}

// ---------------- repl loop ----------------

private void replLoop(LineReader reader,
		//DefaultHistory jlineHistory,
		Path histFile,
		Executor executor,
		BuiltinExecutor builtinExecutor) throws Exception {

	while (true) {
		String line;
		try {
			line = reader.readLine("$ ");
			System.out.flush();
		} catch (UserInterruptException e) {
			continue; // ctrl-c
		} catch (EndOfFileException e) {
			return;   // ctrl-d
		}

		if (line == null) return;
		line = line.trim();
		if (line.isEmpty()) continue;

		//historyManager.record(line, jlineHistory);
		historyManager.record(line, reader.getHistory());

		Command cmd = commandParser.parse(line);
		if (cmd == null) continue;

		// Builtins need shell-managed redirection streams.
		if (cmd instanceof SimpleCommand sc && builtinExecutor.supports(sc)) {
			try (RedirectedStreams rs = RedirectedStreams.forCommand(sc, System.out, System.err)) {		
				try {
					executor.execute(sc, System.in, rs.out(), rs.err());
				} catch (IOException e) {
				    System.err.println(e.getMessage());
				}
			}
		} else {
			// External commands + pipelines: executor + external executor handle their own redirection.
			try {
			    executor.execute(cmd, System.in, System.out, System.err);
			} catch (IOException e) {
			    System.err.println(e.getMessage());
			}
		}
	}
}

// ---------------- redirection helper for builtins ----------------

private static final class RedirectedStreams implements AutoCloseable {
	private final PrintStream out;
	private final PrintStream err;
	private final boolean closeOut;
	private final boolean closeErr;

	private RedirectedStreams(PrintStream out, boolean closeOut, PrintStream err, boolean closeErr) {
		this.out = out;
		this.err = err;
		this.closeOut = closeOut;
		this.closeErr = closeErr;
	}

	public PrintStream out() { return out; }
	public PrintStream err() { return err; }

	static RedirectedStreams forCommand(SimpleCommand sc, PrintStream fallbackOut, PrintStream fallbackErr)
			throws IOException {

		Redirection r = sc.redirection();

		PrintStream out = fallbackOut;
		boolean closeOut = false;

		if (r.stdout().isPresent()) {
			Path p = r.stdout().get();
			if (p.getParent() != null) java.nio.file.Files.createDirectories(p.getParent());
			out = new PrintStream(new FileOutputStream(p.toFile(), r.stdoutAppend()));
			closeOut = true;
		}

		PrintStream err = fallbackErr;
		boolean closeErr = false;

		if (r.stderr().isPresent()) {
			Path p = r.stderr().get();
			if (p.getParent() != null) java.nio.file.Files.createDirectories(p.getParent());
			err = new PrintStream(new FileOutputStream(p.toFile(), r.stderrAppend()));
			closeErr = true;
		}

		return new RedirectedStreams(out, closeOut, err, closeErr);
	}

	@Override
	public void close() {
		if (closeOut) out.close(); else out.flush();
		if (closeErr) err.close(); else err.flush();
	}
}
}