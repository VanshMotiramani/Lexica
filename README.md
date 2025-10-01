# Lexica Regex Engine

A custom regular expression engine built from scratch in Java, supporting common regex features.

## Features

-  Basic string matching
-  Anchors (`^` start, `$` end)
-  Character classes (`\d`, `\w`, `[abc]`, `[^abc]`)
-  Quantifiers (`+` one or more, `?` zero or one)
-  Dot metacharacter (`.` matches any character)
-  Alternation with groups (`(cat|dog)`)
-  Backreferences (`\1`)

## Usage

```bash
# Compile the program
javac src/main/java/Main.java

# Run with a pattern
echo "cat" | java -cp src/main/java Main -E "cat"

# Examples
echo "hello123" | java -cp src/main/java Main -E "\d+"
echo "cat and cat" | java -cp src/main/java Main -E "(cat) and \1"
```
## Project Structure
``` 
├── src/
│   ├── main/
│       └── java/
│           └── Main.java
├── docs/
├── README.md
└── .gitignore
```

## Implementation 
This regex engine uses:
   - Recursive descent parsing for pattern matching
   - Backtracking for quantifiers
   - HashMap for storing captured groups
   - Token-based pattern parsing

# Author
Vansh Motiramani