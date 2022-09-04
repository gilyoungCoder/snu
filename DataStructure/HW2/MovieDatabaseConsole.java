import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MovieDatabaseConsole {

	public static void main(String args[]) {
		// 표준 입력을 읽을 준비를 한다.
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// 프로그램에서 사용할 MovieDB 객체를 생성한다.
		MovieDB db = new MovieDB();

		String input = null;
		while (true) {
			try {
				// 표준 입력으로부터 한 줄을 입력받는다.
				input = br.readLine().trim();

				if (input.isEmpty())
					continue;

				if (input.toUpperCase().equals("QUIT"))
					break;

				// 입력을 해석한다.
				ConsoleCommand command = parse(input);
				
				// 입력을 처리하는 과제의 복잡한 스펙을 아래의 한 줄로 묘사함으로써
				// 자세한 내용을 생략하고 다른 클래스에 위임한다. 
				command.apply(db);
				
				// 위와 같은 코드를 통해, 과제 프로그램의 큰 흐름이
				// 입력 - 해석 - 처리 과정을 반복하는 것으로 구성되어 있음을
				// 명확하게 드러낼 수 있다.
			} catch (CommandParseException e) {
				System.err.printf("command parse failure: %s [cmd=%s, input=%s]\n",
						e.getMessage(), e.getCommand(), e.getInput());
				e.printStackTrace(System.err);
			} catch (CommandNotFoundException e) {
				System.err.printf("command not found: %s\n", e.getCommand());
				e.printStackTrace(System.err);
			} catch (Exception e) {
				System.err.printf("unexpected exception with input: [%s]\n", input);
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * {@code input}을 해석(parse)하여 ConsoleCommand 객체를 생성해 반환한다.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	private static ConsoleCommand parse(String input) throws Exception {
		// 우선 어떤 종류의 ConsoleCommand 를 생성할 것인지 결정한다.
		ConsoleCommand command = null;
		if (input.startsWith("INSERT")) {
			command = new InsertCmd();
		} else if (input.startsWith("DELETE")) {
			command = new DeleteCmd();
		} else if (input.startsWith("SEARCH")) {
			command = new SearchCmd();
		} else if (input.startsWith("PRINT")) {
			command = new PrintCmd();
		} else {
			throw new CommandNotFoundException(input);
		}

		/*
		 * ConsoleCommand의 종류가 결정되었으니 입력을 각 ConsoleCommand 의 방식에 맞춰
		 * 해석(parse)한다.
		 */
		// command variable should not be null here by throwing exception.
		// TIP: eclipse 에서 parse 위에 커서를 올리고 Ctrl+T 를 누르면 해당 인터페이스를 실제로
		//      구현하고 있는 클래스들의 목록을 확인할 수 있고, 바로 이동할 수 있다.
		command.parse(input);

		// command variable should always be valid here
		// because parse method above throws CommandParseException when arguments are invalid.
		return command;
	}

}
