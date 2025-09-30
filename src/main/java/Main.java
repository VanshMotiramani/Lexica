
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
      int consumed = matchFrom(inputLine, 0, stripped);
      return consumed == inputLine.length();
    }else if (startsWithAnchor) {
      String stripped = pattern.substring(1);
      int consumed = matchFrom(inputLine, 0, stripped);
      return consumed != -1;
    } else if (endsWithAnchor) {
      String stripped = pattern.substring(0, pattern.length() - 1);
      for (int start = 0; start <= inputLine.length(); start++) {
        int consumed = matchFrom(inputLine, start, stripped);
        if (consumed == inputLine.length()) {
          return true;
        }
      }
      return false;
    } else {
      for (int start = 0; start <= inputLine.length(); start++) {
        int consumed = matchFrom(inputLine, start, pattern);

        if (consumed != -1) {
          return true;
        }
      }

      return false;
    }
  }
  
  // private static boolean matchEnding(String input, String pattern) {
  //   int start = input.length() - pattern.length();
  //   if (start < 0)
  //     return false;
  //   return matchFrom(input, start, pattern);
  // }
  
  private static String nextToken(String pattern, int index) {
    if (index >= pattern.length())
      return "";

    char c = pattern.charAt(index);
    String token;

    if (c == '\\') {
      // escape sequence \d or \w
      token = pattern.substring(index, index + 2);
    } else if (c == '[') {
      int close = pattern.indexOf(']', index);
      token = pattern.substring(index, close + 1);
    } else {
      //literal char
      token = Character.toString(c);
    }

    int nextIndex = index + token.length();
    if (nextIndex < pattern.length() && pattern.charAt(nextIndex) == '+')
      token += '+';
    
    return token;
  }

  private static int matchTokenAndConsume(String input, int pos, String token) {
    boolean oneOrMore = token.endsWith("+");
    String baseToken = oneOrMore ? token.substring(0, token.length() -1) : token;

    int i=pos;
    int count = 0;

    while(i < input.length() && matchSingle(input.charAt(i), baseToken)) {
      i++;
      count++;
      if (!oneOrMore) break;
    }

    if (count == 0)
      return -1;

    return i;
  }

  private static boolean matchSingle(char ch, String token) {
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

  private static int matchFrom(String input, int start, String pattern) {
    int i = start;
    int j = 0;

    while (j < pattern.length()) {
      String token = nextToken(pattern, j);
      if (token.isEmpty())
        break;

      System.err.println("Pattern pos " + j + ": token='" + token + "', input pos " + i + ", remaining input: '"
          + input.substring(i) + "'");

      int newPos = matchTokenAndConsume(input, i, token);
      if (newPos == -1) {
        System.err.println("Failed to match token '" + token + "' at input position " + i);
        return -1;
      }

      i = newPos;
      j += token.length();
    }
    
    System.err.println("Successfully matched, consumed " + (i - start) + " characters");

    return i;
  }

}
