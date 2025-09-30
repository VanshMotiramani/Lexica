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

    System.err.println("Logs from your program will appear here!");
    
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
      return matchFromRecursive(inputLine, 0, stripped, 0) == inputLine.length();
    } else if (startsWithAnchor) {
      String stripped = pattern.substring(1);
      return matchFromRecursive(inputLine, 0, stripped, 0) != -1;
    } else if (endsWithAnchor) {
      String stripped = pattern.substring(0, pattern.length() - 1);
      for (int start = 0; start <= inputLine.length(); start++) {
        if (matchFromRecursive(inputLine, start, stripped, 0) == inputLine.length()) {
          return true;
        }
      }
      return false;
    } else {
      for (int start = 0; start <= inputLine.length(); start++) {
        if (matchFromRecursive(inputLine, start, pattern, 0) != -1) {
          return true;
        }
      }
      return false;
    }
  }
  
  private static String nextToken(String pattern, int index) {
    if (index >= pattern.length())
      return "";

    char c = pattern.charAt(index);
    String token;

    if (c == '\\') {
      token = pattern.substring(index, index + 2);
    } else if (c == '[') {
      int close = pattern.indexOf(']', index);
      token = pattern.substring(index, close + 1);
    } else {
      token = Character.toString(c);
    }

    int nextIndex = index + token.length();
    if (nextIndex < pattern.length() && pattern.charAt(nextIndex) == '+')
      token += '+';
    
    return token;
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

  private static int matchFromRecursive(String input, int inputPos, String pattern, int patternPos) {
    // Base case: consumed entire pattern
    if (patternPos >= pattern.length()) {
      return inputPos;
    }
    
    String token = nextToken(pattern, patternPos);
    if (token.isEmpty()) {
      return inputPos;
    }
    
    if (token.endsWith("+")) {
      // Handle + quantifier with backtracking
      String baseToken = token.substring(0, token.length() - 1);
      
      // Must match at least once
      if (inputPos >= input.length() || !matchSingle(input.charAt(inputPos), baseToken)) {
        return -1;
      }
      
      // Find maximum possible matches
      int maxPos = inputPos + 1;
      while (maxPos < input.length() && matchSingle(input.charAt(maxPos), baseToken)) {
        maxPos++;
      }
      
      // Try from longest match to shortest (greedy with backtracking)
      for (int endPos = maxPos; endPos > inputPos; endPos--) {
        int result = matchFromRecursive(input, endPos, pattern, patternPos + token.length());
        if (result != -1) {
          return result;
        }
      }
      return -1;
      
    } else {
      // Regular token - must match exactly once
      if (inputPos >= input.length() || !matchSingle(input.charAt(inputPos), token)) {
        return -1;
      }
      return matchFromRecursive(input, inputPos + 1, pattern, patternPos + token.length());
    }
  }
}