import java.util.Scanner;

import java.util.Set;
import java.util.HashSet;

public class Main {
  public static void main(String[] args) {
    if (args.length != 2 || !args[0].equals("-E")) {
      System.out.println("Usage: ./your_program.sh -E <pattern>");
      System.exit(1);
    }

    String pattern = args[1];
    Scanner scanner = new Scanner(System.in);
    String inputLine = scanner.nextLine();

    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.err.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
    if (matchPattern(inputLine, pattern)) {
      System.out.println("Match Pattern!");
      System.exit(0);
    } else {
      System.out.println("No Mathch Pattern");
      System.exit(1);
    }
  }

  public static boolean matchPattern(String inputLine, String pattern) {
    if (pattern.length() == 1) {
      return inputLine.contains(pattern);
    } else if (pattern.startsWith("[") && pattern.endsWith("]")) {
      pattern = pattern.substring(1, pattern.indexOf("]"));
      if (pattern.charAt(0) == '^') {
        pattern = pattern.substring(1);
        return findContainsChar(inputLine, pattern, false);
      } else {
        return findContainsChar(inputLine, pattern, true);
      }
    } else {
      // throw new RuntimeException("Unhandled pattern: " + pattern);
      return matchEach(inputLine, pattern);
    }
  }

  public static boolean matchEach(String text, String pattern) {
    if (pattern.charAt(0) == '^') {
      pattern = pattern.substring(1);
      if (pattern.endsWith("$")) {
        pattern = pattern.substring(0, pattern.indexOf("$"));
        return text.equals(pattern);
      }
      return pattern.equals(text.substring(0, pattern.length()));
    }
    if (pattern.endsWith("$")) {
      pattern = pattern.substring(0, pattern.indexOf("$"));
      return pattern.equals(text.substring(text.length() - pattern.length()));
    }

    int len = pattern.length();
    int idx = 0;
    for (int i = 0; i < text.length(); i++) {
      if (idx + 1 < len && pattern.charAt(idx) == '\\' && pattern.charAt(idx + 1) == 'd'
          && isDigit(text.charAt(i))) {
        idx += 2;
      } else if (idx + 1 < len && pattern.charAt(idx) == '\\' && pattern.charAt(idx + 1) == 'w'
          && (isDigit(text.charAt(i)) || isLetter(text.charAt(i)) || text.charAt(i) == '_')) {
        idx += 2;
      } else if (pattern.charAt(idx) == text.charAt(i)) {
        idx++;
      } else if (pattern.charAt(idx) == '+') {
        int j = idx + 1;
        while (j < len && pattern.charAt(j) == pattern.charAt(idx - 1))
          j++;
        int dt = j - idx - 1;
        int m = 0;
        while (i < text.length() && text.charAt(i) == pattern.charAt(idx - 1)) {
          i++;
          m++;
        }
        i--;
        if (m < dt)
          return false;
        i -= dt;
        idx++;
      } else {
        idx = 0;
      }
      if (idx == len) {
        return true;
      }
    }
    return false;
  }

  public static boolean matchSufEach(String inputString, String pattern) {
    return pattern.equals(inputString.substring(inputString.length() - pattern.length()));
  }

  public static boolean matchPreEach(String inputString, String pattern) {
    return pattern.equals(inputString.substring(0, pattern.length()));
  }

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isLetter(char c) {
    return ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'));
  }

  public static boolean isContainsDigit(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (isDigit(s.charAt(i)))
        return true;
    }
    return false;
  }

  public static boolean isContainsLetter(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (isDigit(s.charAt(i)) || isLetter(s.charAt(i)) || c == '_')
        return true;
    }
    return false;
  }

  /**
   * 
   * @param inputString
   * @param pattern
   * @param is          true: contain，false: not contain
   * @return
   */
  public static boolean findContainsChar(String inputString, String pattern, boolean is) {
    Set<Character> set = new HashSet<>();
    for (char c : pattern.toCharArray()) {
      set.add(c);
    }
    for (int i = 0; i < inputString.length(); i++) {
      if (set.contains(inputString.charAt(i)) == is) {
        set.clear();
        return true;
      }
    }
    set.clear();
    return false;
  }
}