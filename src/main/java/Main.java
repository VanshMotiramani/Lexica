
import java.util.Scanner;

public class Main {
  public static void main(String[] args){
    if (args.length != 2 || !args[0].equals("-E")) {
      System.out.println("Usage: ./your_program.sh -E <pattern>");
      System.exit(1);
    }

    String pattern = args[1];  
    Scanner scanner = new Scanner(System.in);
    String inputLine = scanner.nextLine();

    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    //Uncomment this block to pass the first stage
    
    if (matchPattern(inputLine, pattern)) {
        System.exit(0);
    } else {
        System.exit(1);
    }
  }

  public static boolean matchPattern(String inputLine, String pattern) {
    boolean startsWithAnchor = pattern.startsWith("^");
    boolean endsWithAnchor = pattern.endsWith("$");

    if (startsWithAnchor && endsWithAnchor) {
      String stripped = pattern.substring(1, pattern.length() - 1);
      return matchFrom(inputLine, 0, stripped) && stripped.length() == inputLine.length();
    }else if (startsWithAnchor) {
      String stripped = pattern.substring(1);
      return matchFrom(inputLine, 0, stripped);
    } else if (endsWithAnchor) {
      String stripped = pattern.substring(0, pattern.length() - 1);
      return matchEnding(inputLine, stripped);
    } else {
      for (int start = 0; start <= inputLine.length(); start++) {
        if (matchFrom(inputLine, start, pattern)) {
          return true;
        }
      }

      return false;
    }
  }
  
  private static boolean matchEnding(String input, String pattern) {
    int start = input.length() - pattern.length();
    if (start < 0)
      return false;
    return matchFrom(input, start, pattern);
  }
  
  private static String nextToken(String pattern, int index) {
    char c = pattern.charAt(index);
    if (c == '\\') {
      // escape sequence \d or \w
      return pattern.substring(index, index + 2);
    } else if (c == '[') {
      int close = pattern.indexOf(']', index);
      return pattern.substring(index, close + 1);
    } else {
      //literal char
      return Character.toString(c);
    }
  }

  private static boolean matchToken(char ch, String token) {
    if (token.equals("\\d")) {
      return Character.isDigit(ch);
    } else if (token.equals("\\w")) {
      return Character.isLetterOrDigit(ch) || ch == '_';
    } else if (token.startsWith("[^") && token.endsWith("]")) {
      String chars = token.substring(2, token.length() - 1);
      return chars.indexOf(ch) == -1;
    } else if (token.startsWith("[") && token.endsWith("]")) {
      String chars = token.substring(1, token.length() - 1);
      return chars.indexOf(ch) >= 0;
    } else {
      return ch == token.charAt(0);
    }
  }

  private static boolean matchFrom(String input, int start, String pattern) {
    int i = start;
    int j = 0;
    while (i < input.length() && j < pattern.length()) {
      String token = nextToken(pattern, j);
      if (!matchToken(input.charAt(i), token)) {
        return false;
      }

      i++;
      j += token.length();
    }
    
    return j == pattern.length();
  }

}
