package es.usj.crypto;

import java.util.HashMap;
import java.util.Map;

/**
 * A simplified implementation of the Enigma Machine for educational purposes.
 * This machine encrypts text using a system of rotors, a reflector, and a plugboard.
 */
public class SimpleEnigmaMachine {

    // The alphabet used for encryption
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Components of the Enigma Machine
    private Map<Character, Character> plugboard;
    private Map<Character, Character> reflector;
    private Rotor leftRotor;
    private Rotor middleRotor;
    private Rotor rightRotor;

    /**
     * Represents a rotor in the Enigma Machine.
     * Each rotor has a sequence of letters, a current position, and a notch position.
     */
    static class Rotor {
        String sequence;     // The sequence of letters in the rotor
        char notchPosition;  // When the rotor reaches this position, it triggers the next rotor to rotate
        char ringPosition;   // Initial position of the rotor

        public Rotor(String sequence, char notchPosition, char ringPosition) {
            this.sequence = sequence;
            this.notchPosition = notchPosition;
            this.ringPosition = ringPosition;
            // Set initial position
            while (this.sequence.charAt(0) != this.ringPosition) {
                rotate();
            }
        }

        /**
         * Rotates the rotor by one position
         */
        void rotate() {
            // Move the last character to the front
            sequence = sequence.substring(sequence.length() - 1) +
                    sequence.substring(0, sequence.length() - 1);
        }

        /**
         * Encrypts a character going forward through the rotor
         */
        char encrypt(char c) {
            int position = ALPHABET.indexOf(c);
            return sequence.charAt(position);
        }

        /**
         * Encrypts a character going backward through the rotor
         */
        char encryptBackward(char c) {
            int position = sequence.indexOf(c);
            return ALPHABET.charAt(position);
        }
    }

    /**
     * Creates a new Enigma Machine with predefined settings.
     * Initializes all components during construction.
     */
    public SimpleEnigmaMachine() {
        initializeComponents();
    }

    /**
     * Initializes all components of the Enigma Machine
     */
    private void initializeComponents() {
        // Initialize the rotors with their sequences, notch positions, and initial positions
        leftRotor = new Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'Q', 'E');
        middleRotor = new Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'E', 'A');
        rightRotor = new Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'V', 'B');

        // Initialize the plugboard and reflector
        plugboard = createPlugboard();
        reflector = createReflector();
    }

    /**
     * Helper method to connect pairs of letters in the plugboard
     */
    private void connectPlugs(Map<Character, Character> board, String... pairs) {
        for (String pair : pairs) {
            char first = pair.charAt(0);
            char second = pair.charAt(1);
            board.put(first, second);
            board.put(second, first);
        }
    }

    /**
     * Creates the plugboard with predefined wire connections (10 wiring pairs)
     */
    private Map<Character, Character> createPlugboard() {
        Map<Character, Character> board = new HashMap<>();
        connectPlugs(board, "IR", "HQ", "NT", "WZ", "VC", "OY", "GP", "LF", "BX", "AK");
        // Add remaining letters as self-mapping
        connectPlugs(board, "DD", "EE", "JJ", "MM", "SS", "UU");
        return board;
    }

    /**
     * Creates the reflector with predefined connections
     */
    private Map<Character, Character> createReflector() {
        Map<Character, Character> ref = new HashMap<>();
        connectPlugs(ref, "LE", "YJ", "VC", "NI", "XW", "PB", "QM", "DR", "TA", "KZ", "GF", "UH", "OS");
        return ref;
    }

    /**
     * Encrypts a single character through the Enigma Machine
     */
    private char encryptChar(char c) {

        // Step 1: Pass through plugboard
        char result = plugboard.get(c);

        // Step 2: Rotate the rotors
        rotateRotors();

        // Step 3: Pass through rotors (forward)
        result = rightRotor.encrypt(result);
        result = middleRotor.encrypt(result);
        result = leftRotor.encrypt(result);

        // Step 4: Pass through reflector
        result = reflector.get(result);

        // Step 5: Pass through rotors (backward)
        result = leftRotor.encryptBackward(result);
        result = middleRotor.encryptBackward(result);
        result = rightRotor.encryptBackward(result);

        // Step 6: Pass through plugboard again
        return plugboard.get(result);
    }

    /**
     * Handles the rotation of all rotors
     */
    private void rotateRotors() {
        // Always rotate the left rotor first
        leftRotor.rotate();

        // Check if middle rotor should rotate
        if (leftRotor.sequence.charAt(0) == leftRotor.notchPosition) {
            middleRotor.rotate();

        }

        // Check if right rotor should rotate
        if (middleRotor.sequence.charAt(0) == middleRotor.notchPosition) {
            rightRotor.rotate();
        }
    }

    /**
     * Encrypts a message using the Enigma Machine
     */
    public String encrypt(String message) {
        StringBuilder result = new StringBuilder();
        for (char c : message.toUpperCase().toCharArray()) {
            result.append(encryptChar(c));
        }
        return result.toString();
    }

    /**
     * Example usage of the Enigma Machine
     */
    public static void main(String[] args) {
        SimpleEnigmaMachine enigma = new SimpleEnigmaMachine();

        String message = "SUBMARINE";
        String encrypted = enigma.encrypt(message);

        System.out.println("Original message: " + message);
        System.out.println("Encrypted message: " + encrypted);
    }
}