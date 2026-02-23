package shell.execution;

import shell.parser.Redirection;
import shell.parser.SimpleCommand;

import java.io.*;
import java.util.Objects;

public final class ExternalCommandExecutor {

	private final ShellContext ctx;

	public ExternalCommandExecutor(ShellContext ctx) {
		this.ctx = Objects.requireNonNull(ctx);
	}

	public void execute(SimpleCommand cmd,
			InputStream stdin,
			OutputStream stdout,
			OutputStream stderr) throws Exception {

		ProcessBuilder pb = new ProcessBuilder(cmd.argv());
		pb.directory(ctx.getCurrentDirectory().toFile());

		applyRedirection(cmd.redirection(), pb);

		// If we're connected to the real console input, inherit it.
	 
		if (stdin == System.in) {
			pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
		}

		Process process = pb.start();

		// ---- stdin -> process stdin  
		Thread stdinThread = null;
		if (pb.redirectInput() == ProcessBuilder.Redirect.PIPE) {
			if (stdin == null) {
				// No input => immediate EOF
				process.getOutputStream().close();
			} else {
				stdinThread = new Thread(() -> {
					try {
						stdin.transferTo(process.getOutputStream());
					} catch (IOException ignored) {
					} finally {
						try {
							process.getOutputStream().close(); // deliver EOF to child
						} catch (IOException ignored) {}
					}
				});
				stdinThread.start();
			}
		}

		// ---- process stdout/stderr -> our streams  ----
		Thread stdoutThread = null;
		if (pb.redirectOutput() == ProcessBuilder.Redirect.PIPE) {
			stdoutThread = pipe(process.getInputStream(), stdout);
		}

		Thread stderrThread = null;
		if (pb.redirectError() == ProcessBuilder.Redirect.PIPE) {
			stderrThread = pipe(process.getErrorStream(), stderr);
		}

		process.waitFor();

		if (stdinThread != null) stdinThread.join();
		if (stdoutThread != null) stdoutThread.join();
		if (stderrThread != null) stderrThread.join();
	}

	private void applyRedirection(Redirection redir, ProcessBuilder pb) {

		redir.stdout().ifPresent(path -> {
			createParentDirectories(path);
			pb.redirectOutput(redir.stdoutAppend()
					? ProcessBuilder.Redirect.appendTo(path.toFile())
							: ProcessBuilder.Redirect.to(path.toFile()));
		});

		redir.stderr().ifPresent(path -> {
			createParentDirectories(path);
			pb.redirectError(redir.stderrAppend()
					? ProcessBuilder.Redirect.appendTo(path.toFile())
							: ProcessBuilder.Redirect.to(path.toFile()));
		});
	}

	private Thread pipe(InputStream in, OutputStream out) {
		Thread t = new Thread(() -> {
			try (in) { // we DO own the process stream
				in.transferTo(out);
				out.flush();
			} catch (IOException ignored) {}
		});
		t.start();
		return t;
	}

	private void createParentDirectories(java.nio.file.Path path) {
		try {
			java.nio.file.Path parent = path.getParent();
			if (parent != null) {
				java.nio.file.Files.createDirectories(parent);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}