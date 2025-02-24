# Simpler Custom Enigma

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A Java implementation of a simplified Enigma Machine encryption system, along with cryptanalysis tools for educational purposes. This project helps students understand the basic principles of the historical Enigma Machine and common cryptanalysis techniques.

## Overview

This project consists of two main components:

1. `SimpleEnigmaMachine`: A simplified implementation of the Enigma Machine encryption system
2. `SimpleEnigmaMachineAttack`: A collection of cryptanalysis tools to analyze and attempt to break Enigma-encrypted messages

The implementation focuses on educational value and clarity rather than historical accuracy or cryptographic security.

## Implementation Details

### SimpleEnigmaMachine

#### Core Components

1. **Rotors**
   - Implemented as String arrays containing scrambled alphabets
   - Each rotor represents a unique permutation of the 26 letters
   - Example: `"EKMFLGDQVZNTOWYHXUSPAIBRCJ"` (Rotor I)
   - The implementation includes the five historical rotor configurations (I-V)

2. **Notch Positions**
   - Stored as character array
   - Determines when adjacent rotors should rotate
   - Each rotor has a specific notch position (Q, E, V, J, Z for rotors I-V)

3. **Ring Settings**
   - Initial offset for each rotor
   - Affects the internal wiring connections
   - Represented as characters ('A' to 'Z')

4. **Rotor Positions**
   - Tracks current position of each rotor
   - Updated during encryption/decryption process
   - Stored as integer array (0-25)

5. **Plugboard**
   - Implemented as character array of paired letters
   - Swaps letters before and after rotor encryption
   - Example: `"AZBYCXDWEVFU"` swaps A↔Z, B↔Y, C↔X, etc.

6. **Reflector**
   - Fixed substitution table implemented as character array
   - Ensures encryption is reversible
   - Example: `"YRUHQSLDPXNGOKMIEBFZCWVJAT"`

#### Encryption Process

The encryption/decryption process is implemented in the `processChar` method:

```java
private char processChar(char c) {
    // Step 1: Apply plugboard substitutions
    c = applyPairs(c, plugboardPairs);
    
    // Step 2: Rotate rotors
    rotateRotors();
    
    // Step 3: Forward pass through rotors
    for (int i = 0; i < rotors.length; i++) {
        int pos = (ALPHABET.indexOf(c) + rotorPositions[i]) % 26;
        c = rotors[i].charAt(pos);
    }
    
    // Step 4: Apply reflector
    c = applyPairs(c, reflectorPairs);
    
    // Step 5: Reverse pass through rotors
    for (int i = rotors.length - 1; i >= 0; i--) {
        int pos = (rotors[i].indexOf(c) - rotorPositions[i] + 26) % 26;
        c = ALPHABET.charAt(pos);
    }
    
    // Step 6: Apply plugboard again
    return applyPairs(c, plugboardPairs);
}
```

### SimpleEnigmaMachineAttack

#### Cryptanalysis Implementation

1. **Index of Coincidence (IoC)**
   ```java
   public static double calculateIoC(String text) {
       // Count letter frequencies
       int[] frequencies = new int[26];
       int totalChars = 0;
       for (char c : text.toUpperCase().toCharArray()) {
           if (Character.isLetter(c)) {
               frequencies[c - 'A']++;
               totalChars++;
           }
       }
       
       // Calculate IoC
       double sum = 0.0;
       for (int freq : frequencies) {
           sum += freq * (freq - 1);
       }
       return totalChars > 1 ? sum / (totalChars * (totalChars - 1)) : 0;
   }
   ```

2. **Frequency Analysis**
   - Compares observed letter frequencies with expected English frequencies
   - Uses chi-square test to measure the difference
   - Lower scores indicate better matches to English text

3. **Hill Climbing Algorithm**
   ```java
   public static EnigmaConfig hillClimb(String ciphertext, int maxIterations) {
       // Start with random configuration
       EnigmaConfig bestConfig = generateRandomConfig();
       double bestScore = evaluateConfig(bestConfig);
       
       for (int i = 0; i < maxIterations; i++) {
           // Generate and test neighbor configuration
           EnigmaConfig neighbor = bestConfig.mutate();
           double score = evaluateConfig(neighbor);
           
           // Keep better configurations
           if (score > bestScore) {
               bestConfig = neighbor;
               bestScore = score;
           }
       }
       return bestConfig;
   }
   ```

4. **Configuration Management**
   - `EnigmaConfig` class manages rotor selection and positions
   - Implements mutation methods for hill climbing
   - Tracks best configurations during attack

#### Fitness Scoring

The attack implementation uses a combined fitness score that weighs multiple factors:

```java
public static double calculatePlaintextFitness(String text) {
    double iocScore = calculateIoC(text);
    double freqScore = calculateFrequencyScore(text);
    int trigramCount = countCommonTrigrams(text);

    // Normalize and combine scores
    double iocFitness = 1.0 - Math.abs(0.067 - iocScore);
    double freqFitness = 1.0 / (1.0 + freqScore);
    double trigramFitness = text.isEmpty() ? 0 : 
        (trigramCount / (double) text.length());

    // Weighted combination
    return (0.4 * iocFitness) + (0.4 * freqFitness) + 
           (0.2 * trigramFitness);
}
```

## Building and Running

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher

### Building the Project

1. Clone the repository:
```bash
git clone https://github.com/angelborroy/simpler-custom-enigma.git
cd simpler-custom-enigma
```

2. Build with Maven:
```bash
mvn clean package
```

### Running Examples

The project includes example usage in both main classes. To run them:

```bash
# Run Enigma Machine example
java -cp target/classes es.usj.crypto.SimpleEnigmaMachine

# Run Cryptanalysis example
java -cp target/classes es.usj.crypto.SimpleEnigmaMachineAttack
```

### Using in Your Own Code

To use the Enigma Machine:

```java
// Create rotor configurations
String[] rotors = {
    "BDFHJLCPRTXVZNYEIWGAKMUSQO",  // Rotor III
    "VZBRGITYUPSDNHLXAWMJQOFECK",  // Rotor V
    "AJDKSIRUXBLHWTMCQGZNPYFVOE"   // Rotor II
};
char[] notches = {'V', 'Z', 'E'};
char[] rings = {'A', 'A', 'A'};

// Initialize the machine
SimpleEnigmaMachine enigma = new SimpleEnigmaMachine(
    rotors,
    notches,
    rings,
    "AZBYCXDWEVFU",                 // Plugboard configuration
    "YRUHQSLDPXNGOKMIEBFZCWVJAT"   // Reflector configuration
);

// Encrypt a message
String encrypted = enigma.encrypt("HELLO WORLD");

// Decrypt a message (using the same settings)
String decrypted = enigma.decrypt(encrypted);
```

To use the cryptanalysis tools:

```java
// Calculate text statistics
double ioc = SimpleEnigmaMachineAttack.calculateIoC(text);
double freqScore = SimpleEnigmaMachineAttack.calculateFrequencyScore(text);
int trigrams = SimpleEnigmaMachineAttack.countCommonTrigrams(text);

// Attempt to break encryption
EnigmaConfig bestConfig = SimpleEnigmaMachineAttack.hillClimb(ciphertext, 1000);
```

## Project Structure

```
src/main/java/es/usj/crypto/
├── SimpleEnigmaMachine.java        # Enigma implementation
└── SimpleEnigmaMachineAttack.java  # Cryptanalysis tools
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This implementation is inspired by the historical Enigma Machine used during World War II, simplified for educational purposes. The cryptanalysis techniques are based on common approaches used by cryptanalysts during the war.