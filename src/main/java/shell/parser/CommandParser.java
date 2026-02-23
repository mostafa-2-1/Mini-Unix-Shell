package shell.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CommandParser {

	public Command parse(String input) throws ParseException {
		List<String> tokens = tokenize(input);

		if (tokens.isEmpty()) {
			throw new ParseException("empty command");
		}

		List<List<String>> segments = splitByPipe(tokens);

		List<SimpleCommand> commands = new ArrayList<>();
		for (List<String> segment : segments) {
			commands.add(parseSimple(segment));
		}

		if (commands.size() == 1) {
			return commands.get(0);
		}
		return new PipelineCommand(commands);
	}

	private SimpleCommand parseSimple(List<String> tokens) throws ParseException {
		List<String> argv = new ArrayList<>();
		Redirection redirection = Redirection.none();

		for (int i = 0; i < tokens.size(); i++) {
			String tok = tokens.get(i);

			switch (tok) {
			case ">", "1>" -> {
				redirection = redirection.withStdout(
						Path.of(next(tokens, ++i)), false);
			}
			case ">>", "1>>" -> {
				redirection = redirection.withStdout(
						Path.of(next(tokens, ++i)), true);
			}
			case "2>" -> {
				redirection = redirection.withStderr(
						Path.of(next(tokens, ++i)), false);
			}
			case "2>>" -> {
				redirection = redirection.withStderr(
						Path.of(next(tokens, ++i)), true);
			}
			default -> argv.add(tok);
			}
		}

		if (argv.isEmpty()) {
			throw new ParseException("missing command");
		}

		return new SimpleCommand(argv, redirection);
	}

	private String next(List<String> tokens, int idx) throws ParseException {
		if (idx >= tokens.size()) {
			throw new ParseException("missing redirection target");
		}
		return tokens.get(idx);
	}

	// reuse your existing logic
	private List<String> tokenize(String input) throws ParseException {
		List<String> tokens = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				} else {
					current.append(c);
				}
			} else if (inDoubleQuote) {

				if (c == '\\') {
					if (i + 1 < input.length()) {
						char next = input.charAt(i + 1);

						if (next == '"' || next == '\\') {
							current.append(next);
							i++;
						} else {
							current.append('\\');
						}
					} else {
						current.append('\\');
					}
				} else if (c == '"') {
					inDoubleQuote = false;
				} else {
					current.append(c);
				}
			} else if (c == '\'') {
				inSingleQuote = true;
			} else if (c == '"') {
				inDoubleQuote = true;
			} else if (c == '\\') {

				if (i + 1 < input.length()) {
					current.append(input.charAt(i + 1));
					i++;
				}
			} else if (Character.isWhitespace(c)) {
				if (current.length() > 0) {
					tokens.add(current.toString());
					current.setLength(0);
				}
			} else {
				current.append(c);
			}
		}

		if (current.length() > 0) {
			tokens.add(current.toString());
		}
		if (inSingleQuote || inDoubleQuote) {
		    throw new ParseException("unclosed quote");
		}

		return tokens;
	}

	private List<List<String>> splitByPipe(List<String> tokens) throws ParseException {
		List<List<String>> segments = new ArrayList<>();
		List<String> current = new ArrayList<>();

		for (String token : tokens) {
			if (token.equals("|")) {
				if (current.isEmpty()) {
					throw new ParseException("empty pipeline stage");
				}
				segments.add(current);
				current = new ArrayList<>();
			} else {
				current.add(token);
			}
		}

		if (current.isEmpty()) {
			throw new ParseException("empty pipeline stage");
		}

		segments.add(current);
		return segments;
	}
}