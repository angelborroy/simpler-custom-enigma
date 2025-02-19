# Simple Enigma Machine (Educational)

This repository contains a simplified **Enigma Machine** implementation in Java. 
The original Enigma Machine was used in World War II for encryption and decryption of secret messages. 
This educational version demonstrates the core concepts: rotors, notches, ring settings, a reflector, and a plugboard.

## Overview

This Java class, **SimpleEnigmaMachine**, represents a minimal and readable version of how an Enigma Machine operates:

- **Rotors** that transform letters in a cyclic manner
- A **reflector** that bounces signals back through the rotors
- A **plugboard** to swap letters before and after passing through the rotors
- A **notch system** that triggers rotation of other rotors in a stepping fashion

>> This code is intended to help students or anyone curious about basic Enigma mechanics. 
>> It is not cryptographically secure and should not be used for sensitive encryption.

## Key Components

### Rotor

Defined by the `Rotor` inner class:

```java
static class Rotor {
    String sequence;
    char notchPosition;
    char ringPosition;
    ...
}
```
- **sequence**: A permutation of the alphabet indicating how each position maps an input letter to an output letter
- **notchPosition**: When the rotor’s front-facing letter hits this notch, it triggers the next rotor to rotate (mimicking the stepping system in a real Enigma)
- **ringPosition**: The rotor’s initial setting. The constructor rotates the rotor’s sequence until the front-facing character matches this position

**Methods**:
- `rotate()`: Shifts the rotor sequence by one position (mimicking a physical rotor stepping)
- `encrypt(char c)`: Performs a forward pass lookup (i.e., input letter index → output letter from `sequence`)
- `encryptBackward(char c)`: Performs the backward pass (i.e., finds the index of the letter in the sequence → maps back to the standard alphabet)

### Plugboard

In the Enigma, a plugboard swaps pairs of letters before and after the rotor steps. In this code, the plugboard is a `Map<Character, Character>`:

```java
private Map<Character, Character> createPlugboard() {
    Map<Character, Character> board = new HashMap<>();
    connectPlugs(board, "IR", "HQ", ...);
    ...
    return board;
}
```
- **connectPlugs**: Helper method that pairs two letters. For instance, if `A` is connected to `K`, whenever an `A` is encountered, it is first mapped to `K` (and vice versa).

### Reflector

In a real Enigma, the reflector bounces the current inside the machine back through the rotors.
Here, it is another `Map<Character, Character>`. Each letter has exactly one partner:

```java
private Map<Character, Character> createReflector() {
    Map<Character, Character> ref = new HashMap<>();
    connectPlugs(ref, "LE", "YJ", "VC", ...);
    return ref;
}
```

So if the character `L` enters the reflector, it becomes `E`, and if `E` enters, it becomes `L`.

## Encryption Process

When `encrypt(String message)` is called:

1. **Plugboard**: Each character is swapped according to the plugboard
2. **Rotor Stepping**: The rotors rotate according to the notch positions
3. **Rotors (forward pass)**:
    - The letter goes through `rightRotor`, then `middleRotor`, then `leftRotor`
4. **Reflector**: The letter is swapped with its paired letter
5. **Rotors (backward pass)**:
    - The letter goes back through `leftRotor`, `middleRotor`, and `rightRotor` in reverse order
6. **Plugboard (again)**: The resulting letter is once again swapped through the plugboard
7. The resulting character is appended to the output

## Rotation Mechanism

Enigma rotors step in a cascading fashion:

- **Left rotor** always rotates each time you encrypt a character
- If the left rotor’s top-facing letter matches its `notchPosition`, it **triggers** the middle rotor to rotate
- If the middle rotor’s top-facing letter matches its `notchPosition`, it **triggers** the right rotor to rotate

In this code:

```java
private void rotateRotors() {
    leftRotor.rotate();
    if (leftRotor.sequence.charAt(0) == leftRotor.notchPosition) {
        middleRotor.rotate();
    }
    if (middleRotor.sequence.charAt(0) == middleRotor.notchPosition) {
        rightRotor.rotate();
    }
}
```

## Example Usage

Inside the `main` method:

```java
public static void main(String[] args) {
    SimpleEnigmaMachine enigma = new SimpleEnigmaMachine();

    String message = "SUBMARINE";
    String encrypted = enigma.encrypt(message);

    System.out.println("Original message: " + message);
    System.out.println("Encrypted message: " + encrypted);
}
```

**Output** (example):

```
Original message: SUBMARINE
Encrypted message: YPCVGXZDA
```
Every run with the same machine settings and initial positions should yield the same result.

## How to Run

1. **Clone or Download** this repository
2. **Compile**:
    ```bash
    javac es/usj/crypto/SimpleEnigmaMachine.java
    ```
3. **Run**:
    ```bash
    java es.usj.crypto.SimpleEnigmaMachine
    ```
4. You should see the original and encrypted messages in the console
