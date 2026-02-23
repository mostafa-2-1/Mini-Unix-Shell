# Mini UNIX Shell

A small, structured UNIX-like shell implemented in Java. Designed with modularity, built-in commands, pipeline execution, and tab-completion support.

## Features

- Custom command parser and executor
- Built-in commands: `cd`, `pwd`, `echo`, `exit`, `type`, `history`
- Pipeline execution and external command support
- Tab-completion using a Trie
- Modular design with separation of parsing, execution, and built-ins

## How to run

Clone the repository:

```bash
git clone https://github.com/mostafa-2-1/Mini-Unix-Shell.git
cd Mini-Unix-Shell
```

Build with Maven:

```bash
mvn clean package
```

Run the shell:

```bash
java -cp target/classes shell.Shell
```

## License

MIT License
