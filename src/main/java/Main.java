import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    Map<Integer, String> capturedGroups = new HashMap<>();

    if (startsWithAnchor && endsWithAnchor) {
      String stripped = pattern.substring(1, pattern.length() - 1);
      return matchFromRecursive(inputLine, 0, stripped, 0, capturedGroups) == inputLine.length();
    } else if (startsWithAnchor) {
      String stripped = pattern.substring(1);
      return matchFromRecursive(inputLine, 0, stripped, 0, capturedGroups) != -1;
    } else if (endsWithAnchor) {
      String stripped = pattern.substring(0, pattern.length() - 1);
      for (int start = 0; start <= inputLine.length(); start++) {
        if (matchFromRecursive(inputLine, start, stripped, 0, capturedGroups) == inputLine.length()) {
          return true;
        }
      }
      return false;
    } else {
      for (int start = 0; start <= inputLine.length(); start++) {
        if (matchFromRecursive(inputLine, start, pattern, 0, capturedGroups) != -1) {
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
      if (index + 1 < pattern.length() && Character.isDigit(pattern.charAt(index + 1))) {
        token = pattern.substring(index, index + 2);
      } else {
        token = pattern.substring(index, index + 2);
      } 
    } else if (c == '[') {
      int close = pattern.indexOf(']', index);
      token = pattern.substring(index, close + 1);
    } else if (c == '(') {
      // group handling 
      int depth = 1;
      int close = index + 1;
      while (close < pattern.length() && depth > 0) {
        if (pattern.charAt(close) == '(')
          depth++;
        else if (pattern.charAt(close) == ')')
          depth--;
        close++;
      }
      token = pattern.substring(index, close);
    } else {
      token = Character.toString(c);
    }

    int nextIndex = index + token.length();
    if (nextIndex < pattern.length()) {
      char nextChar = pattern.charAt(nextIndex);
      if (nextChar == '+' || nextChar == '?')
        token += nextChar;
    }
    
    return token;
  }

  private static boolean matchSingle(char ch, String token) {
    if (token.equals(".")) {
      return true;
    } else if (token.equals("\\d")) {
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

  private static int matchFromRecursive(String input, int inputPos, String pattern, int patternPos, Map<Integer, String> capturedGroups) {
    // Base case consumed entire pattern
    if (patternPos >= pattern.length()) {
      return inputPos;
    }

    String token = nextToken(pattern, patternPos);
    if (token.isEmpty()) {
      return inputPos;
    }

    if (token.matches("\\\\\\d")) {
      int groupNum = Integer.parseInt(token.substring(1));
      String capturedText = capturedGroups.get(groupNum);

      if (capturedText == null) {
        return -1;
      }

      if (inputPos + capturedText.length() > input.length()) {
        return -1;
      }

      for (int i = 0; i < capturedText.length(); i++) {
        if (input.charAt(inputPos + i) != capturedText.charAt(i)) {
          return -1;
        }
      }

      return matchFromRecursive(input, inputPos + capturedText.length(), pattern, patternPos + token.length(),
          capturedGroups);
    }

    if (token.startsWith("(") && token.endsWith(")")) {
      String groupContent = token.substring(1, token.length() - 1);
      String[] alternatives = splitAlternatives(groupContent);

      //each alt
      for (String alt : alternatives) {
        int startCapture = inputPos;

        Map<Integer, String> tempGroups = new HashMap<>(capturedGroups);

        int tempResult = matchFromRecursive(input, inputPos, alt, 0, tempGroups);
        if (tempResult != -1) {
          String capturedText = input.substring(startCapture, tempResult);
          tempGroups.put(1, capturedText);

          int result = matchFromRecursive(input, tempResult, pattern, patternPos + token.length(), tempGroups);
          if (result != -1) {
            capturedGroups.putAll(tempGroups);
            return result;
          }
        }

      }
      return -1;

    } else if (token.startsWith("(") && (token.endsWith("+") || token.endsWith("?"))) {
      String quantifier = token.substring(token.length() - 1);
      String baseGroup = token.substring(0, token.length() - 1);
      String groupContent = baseGroup.substring(1, baseGroup.length() - 1);
      String[] alternatives = splitAlternatives(groupContent);

      if (quantifier.equals("+")) {
        
        for (String alt : alternatives) {
          int startCapture = inputPos;
          Map<Integer, String> tempGroups = new HashMap<>(capturedGroups);

          int tempResult = matchFromRecursive(input, inputPos, alt, 0, tempGroups);
          if (tempResult != -1) {
            int currentPos = tempResult;
            List<Integer> matchPositions = new ArrayList<>();
            List<String> capturedTexts = new ArrayList<>();
            matchPositions.add(tempResult);
            capturedTexts.add(input.substring(startCapture, tempResult));

            while (true) {
              boolean foundMatch = false;
              int nextStart = currentPos;
              for (String alt2 : alternatives) {
                int nextResult = matchFromRecursive(input, currentPos, alt2, 0, tempGroups);
                if (nextResult != -1) {
                  currentPos = nextResult;
                  matchPositions.add(currentPos);
                  capturedTexts.add(input.substring(nextStart, nextResult));
                  foundMatch = true;
                  break;
                }
              }

              if (!foundMatch)
                break;
            }

            for (int i = matchPositions.size() - 1; i >= 0; i--) {
              Map<Integer, String> tryGroups = new HashMap<>(capturedGroups);
              tryGroups.put(1, input.substring(startCapture, matchPositions.get(i)));

              int result = matchFromRecursive(input, matchPositions.get(i), pattern, patternPos + token.length(), tryGroups);
              if (result != -1) {
                capturedGroups.putAll(tryGroups);
                return result;
              }
            }
          }
        }
        return -1;

      } else if (quantifier.equals("?")) {
        for (String alt : alternatives) {
          int startCapture = inputPos;
          Map<Integer, String> tempGroups = new HashMap<>(capturedGroups);

          int tempResult = matchFromRecursive(input, inputPos, alt, 0, tempGroups);
          if (tempResult != -1) {
            tempGroups.put(1, input.substring(startCapture, tempResult));
            int result = matchFromRecursive(input, tempResult, pattern, patternPos + token.length(), tempGroups);
            if (result != -1) {
              capturedGroups.putAll(tempGroups);
              return result;
            }              
          }
        }
        return matchFromRecursive(input, inputPos, pattern, patternPos + token.length(), capturedGroups);
      }
      return -1;

    } else if (token.endsWith("+")) {
      // Handle + quantifier with backtracking
      String baseToken = token.substring(0, token.length() - 1);

      // Must match at least once
      if (inputPos >= input.length() || !matchSingle(input.charAt(inputPos), baseToken)) {
        return -1;
      }

      // maximum possible matches
      int maxPos = inputPos + 1;
      while (maxPos < input.length() && matchSingle(input.charAt(maxPos), baseToken)) {
        maxPos++;
      }

      // from longest match to shortest (greedy with backtracking)
      for (int endPos = maxPos; endPos > inputPos; endPos--) {
        int result = matchFromRecursive(input, endPos, pattern, patternPos + token.length(), capturedGroups);
        if (result != -1) {
          return result;
        }
      }
      return -1;

    } else if (token.endsWith("?")) {
      String baseToken = token.substring(0, token.length() - 1);
      if (inputPos < input.length() && matchSingle(input.charAt(inputPos), baseToken)) {
        int result = matchFromRecursive(input, inputPos + 1, pattern, patternPos + token.length(), capturedGroups);
        if (result != -1) {
          return result;
        }
      }

      return matchFromRecursive(input, inputPos, pattern, patternPos + token.length(), capturedGroups);

    } else {
      // Regular token - must match exactly once
      if (inputPos >= input.length() || !matchSingle(input.charAt(inputPos), token)) {
        return -1;
      }
      return matchFromRecursive(input, inputPos + 1, pattern, patternPos + token.length(), capturedGroups);

    }
  }
  
  private static String[] splitAlternatives(String groupContent) {
    java.util.List<String> alternatives = new java.util.ArrayList<>();
    int depth = 0;
    int start = 0;

    for (int i = 0; i < groupContent.length(); i++) {
      char c = groupContent.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
      } else if(c == '|' && depth == 0) {
        alternatives.add(groupContent.substring(start, i));
        start = i + 1;
      }
    }
    alternatives.add(groupContent.substring(start));

    return alternatives.toArray(new String[0]);
  }
}