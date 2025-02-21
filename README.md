# Simple Enigma Machine (Educational)

This repository contains a simplified **Enigma Machine** implementation in Java, designed for educational purposes.
The project now consists of two main components:

1. **SimpleEnigmaMachine** – A simulation of the Enigma machine that can encrypt and decrypt messages.
2. **SimpleEnigmaMachineAttack** – A set of cryptanalysis tools to analyze and break Enigma-encrypted messages using statistical techniques.

## Overview

This educational project demonstrates:
- **Rotor-based encryption** that mimics the Enigma machine's letter substitution and rotation process.
- **Plugboard swapping** to add a layer of encryption.
- **Reflector-based signal bouncing** to ensure the reversibility of encryption and decryption.
- **Automated cryptanalysis** techniques such as **frequency analysis**, **index of coincidence**, **hill climbing**, and **exhaustive search** to break encrypted messages.

## Key Components

### 1. SimpleEnigmaMachine

This class implements the core Enigma encryption process, including:

#### **Rotors**
- Modeled using an array of character mappings.
- Each rotor steps forward upon processing a character.
- A **notch system** ensures cascading movement between rotors.

#### **Plugboard**
- Implemented as a character swap system using key-value mappings.
- Each letter has an optional substitution before and after passing through the rotors.

#### **Reflector**
- A fixed letter-swapping system that ensures encryption reversibility.
- Implemented as a predefined character mapping.

#### **Encryption Process**
1. Input letter goes through **plugboard substitution**.
2. Rotors advance based on their **notch positions**.
3. Letter is transformed as it passes through **each rotor (forward pass)**.
4. Letter is swapped by the **reflector**.
5. Letter passes back through **rotors in reverse order (backward pass)**.
6. Output is swapped again via the **plugboard**.

#### **Example Usage**

```java
SimpleEnigmaMachine enigma = new SimpleEnigmaMachine();
enigma.setRotorPositions(0, 0, 0);

String message = "HELLO WORLD";
String encrypted = enigma.encrypt(message);

// Reset rotor positions before decrypting
enigma.setRotorPositions(0, 0, 0);
String decrypted = enigma.decrypt(encrypted);

System.out.println("Encrypted: " + encrypted);
System.out.println("Decrypted: " + decrypted);
```

### 2. SimpleEnigmaMachineAttack

This class provides cryptanalysis techniques to break Enigma-encrypted messages. It includes:

#### **Letter Frequency Analysis**
- Compares letter distributions in the ciphertext to typical English letter frequencies.
- Uses a **chi-square** test to score the likelihood of a given decryption being valid English.

#### **Index of Coincidence (IoC)**
- Measures the probability that two randomly chosen letters are the same.
- Helps distinguish encrypted text from random noise.

#### **Trigram Analysis**
- Counts occurrences of common English three-letter sequences (e.g., "THE", "AND").
- Higher counts indicate more probable plaintext.

#### **Hill Climbing Attack**
- Starts with a random Enigma configuration.
- Iteratively mutates settings to maximize a fitness score.
- Uses **letter frequencies, IoC, and trigrams** to refine guesses.

#### **Exhaustive Search**
- Tries **all possible rotor combinations and positions**.
- Computationally expensive but guarantees finding the correct key.

#### **Example Cryptanalysis Execution**

```java
String ciphertext = "YPCVGXZDA";

// Hill climbing attack
EnigmaConfig bestConfigHC = SimpleEnigmaMachineAttack.hillClimb(ciphertext, 1000);
SimpleEnigmaMachine enigmaHC = bestConfigHC.createEnigma();
String decryptedHC = enigmaHC.decrypt(ciphertext);

System.out.println("Best Configuration: " + bestConfigHC);
System.out.println("Decrypted Message: " + decryptedHC);
```

## How to Run

### **Compiling**
```bash
javac es/usj/crypto/SimpleEnigmaMachine.java es/usj/crypto/SimpleEnigmaMachineAttack.java
```

### **Running Encryption**
```bash
java es.usj.crypto.SimpleEnigmaMachine
```

### **Running Cryptanalysis**
```bash
java es.usj.crypto.SimpleEnigmaMachineAttack
```

## Summary

This project provides an educational look at **Enigma encryption** and **basic cryptanalysis techniques**. While it is not secure by modern standards, it serves as an excellent introduction to classical encryption methods and cryptanalysis strategies.