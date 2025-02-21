package es.usj.crypto;

/**
 * A simplified simulation of the Enigma Machine used during World War II.
 * This class provides basic encryption and decryption using rotors, a plugboard, and a reflector.
 * <p>
 * The Enigma machine works by scrambling letters through multiple stages:
 * - Plugboard swaps characters.
 * - Rotors shift and modify letters.
 * - A reflector ensures bidirectional encryption.
 * - The same settings can both encrypt and decrypt messages.
 * </p>
 */
public class SimpleEnigmaMachine {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Rotors and settings
    private String[] rotors; // Each rotor contains a scrambled version of the alphabet
    private char[] notchPositions; // Notch positions determine rotor advancement
    private char[] ringSettings; // Initial ring settings for each rotor
    private int[] rotorPositions; // Current positions of the rotors

    // Plugboard and reflector settings
    private char[] plugboardPairs; // Swaps letters before entering rotors
    private char[] reflectorPairs; // Reflects letters to ensure reversibility

    /**
     * Constructs an Enigma machine with custom rotor and wiring configurations.
     *
     * @param rotorSettings Array of scrambled alphabets representing rotor wirings.
     * @param notches Positions on each rotor that trigger rotation of the next rotor.
     * @param rings Initial ring settings for each rotor (adjusts the starting offset).
     * @param plugboard String defining character swaps before entering the rotors.
     * @param reflector String defining character swaps in the reflector.
     */
    public SimpleEnigmaMachine(String[] rotorSettings, char[] notches, char[] rings,
                               String plugboard, String reflector) {
        this.rotors = rotorSettings;
        this.notchPositions = notches;
        this.ringSettings = rings;
        this.rotorPositions = new int[rotorSettings.length];
        this.plugboardPairs = plugboard.toCharArray();
        this.reflectorPairs = reflector.toCharArray();

        // Apply initial ring settings to adjust rotor wirings.
        applyRingSettings();
    }

    /**
     * Constructs an Enigma machine with predefined settings mimicking historical models.
     * Uses three rotors, a predefined plugboard, and a standard reflector.
     * <p/>
     * Historical rotors:
     *   - Rotor I:   EKMFLGDQVZNTOWYHXUSPAIBRCJ, notch: Q
     *   - Rotor II:  AJDKSIRUXBLHWTMCQGZNPYFVOE, notch: E
     *   - Rotor III: BDFHJLCPRTXVZNYEIWGAKMUSQO, notch: V
     *   - Rotor IV:  ESOVPZJAYQUIRHXLNFTGKDCMWB, notch: J
     *   - Rotor V:   VZBRGITYUPSDNHLXAWMJQOFECK, notch: Z
     * <p/>
     * Historical reflectors:
     *   - Reflector B: YRUHQSLDPXNGOKMIEBFZCWVJAT
     *   - Reflector C: FVPJIAOYEDRZXWGCTKUQSBNMHL
     */
    public SimpleEnigmaMachine() {
        this.rotors = new String[]{
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ",  // Rotor I
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",  // Rotor II
                "BDFHJLCPRTXVZNYEIWGAKMUSQO"   // Rotor III
        };
        this.notchPositions = new char[]{'Q', 'E', 'V'};
        this.ringSettings = new char[]{'A', 'A', 'A'};
        this.rotorPositions = new int[3]; // Initial position for each rotor is 0 = A
        this.reflectorPairs = "YRUHQSLDPXNGOKMIEBFZCWVJAT".toCharArray(); // Reflector B
        this.plugboardPairs = "AZBYCXDWEVFU".toCharArray();
        applyRingSettings();
    }

    /**
     * Adjusts rotor wirings based on the initial ring settings.
     * This effectively shifts the internal wiring of each rotor.
     */
    private void applyRingSettings() {
        for (int i = 0; i < rotors.length; i++) {
            int shift = ALPHABET.indexOf(ringSettings[i]);
            if (shift > 0) {
                rotors[i] = rotors[i].substring(shift) + rotors[i].substring(0, shift);
            }
        }
    }

    /**
     * Encrypts or decrypts a single character by passing it through the Enigma machine's stages.
     *
     * @param c The character to process.
     * @return The transformed character after passing through the machine.
     */
    private char processChar(char c) {
        c = applyPairs(c, plugboardPairs); // Step 1: Apply plugboard swaps
        rotateRotors(); // Step 2: Rotate rotors before encoding

        // Step 3: Forward pass through rotors
        for (int i = 0; i < rotors.length; i++) {
            int pos = (ALPHABET.indexOf(c) + rotorPositions[i]) % 26;
            c = rotors[i].charAt(pos);
        }

        c = applyPairs(c, reflectorPairs); // Step 4: Reflector swaps the character

        // Step 5: Reverse pass through rotors
        for (int i = rotors.length - 1; i >= 0; i--) {
            int pos = (rotors[i].indexOf(c) - rotorPositions[i] + 26) % 26;
            c = ALPHABET.charAt(pos);
        }

        return applyPairs(c, plugboardPairs); // Step 6: Apply plugboard swaps again
    }

    /**
     * Swaps characters according to a provided mapping (used for both plugboard and reflector).
     *
     * @param c The character to transform.
     * @param pairs The substitution rules.
     * @return The transformed character.
     */
    private char applyPairs(char c, char[] pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            if (c == pairs[i]) return pairs[i + 1];
            if (c == pairs[i + 1]) return pairs[i];
        }
        return c;
    }

    /**
     * Advances rotors, taking notch positions into account.
     * The rightmost rotor always moves, and middle/left rotors advance when required.
     */
    private void rotateRotors() {
        boolean[] shouldRotate = new boolean[rotors.length];
        shouldRotate[0] = true;

        if (ALPHABET.charAt(rotorPositions[1]) == notchPositions[1]) {
            shouldRotate[1] = true;
            shouldRotate[2] = true;
        }
        if (ALPHABET.charAt(rotorPositions[0]) == notchPositions[0]) {
            shouldRotate[1] = true;
        }

        for (int i = 0; i < rotorPositions.length; i++) {
            if (shouldRotate[i]) {
                rotorPositions[i] = (rotorPositions[i] + 1) % 26;
            }
        }
    }

    /**
     * Encrypts a message by processing each character through the Enigma machine.
     */
    public String encrypt(String message) {
        return process(message);
    }

    /**
     * Decrypts a message using the same method as encryption (Enigma is reciprocal).
     */
    public String decrypt(String message) {
        return process(message);
    }

    /**
     * Processes a message, converting each character individually.
     * When a character is not a letter (like a space), it's appended to the result unencrypted.
     */
    private String process(String message) {
        StringBuilder result = new StringBuilder();
        for (char c : message.toUpperCase().toCharArray()) {
            result.append(Character.isLetter(c) ? processChar(c) : c);
        }
        return result.toString();
    }

    /**
     * Sets the initial position of the rotors before encryption/decryption.
     */
    public void setRotorPositions(int... positions) {
        System.arraycopy(positions, 0, rotorPositions, 0, positions.length);
    }

    /**
     * Example usage demonstrating encryption and decryption
     */
    public static void main(String[] args) {

        // Create machine with default settings:
        // - Rotors I, II and III
        // - Ring settings A, A, A
        // - Reflector B
        // - Plugboard AZBYCXDWEVFU
        SimpleEnigmaMachine enigma = new SimpleEnigmaMachine();

        // Plaintext should be long enough to apply cryptanalysis techniques
        String message = "IN THE HEART THE FOREST THERE WAS AN HIDDEN VILLAGE SYSTEMATICALLY WHERE PEOPLE LIVED "+
                "IN PERFECT HARMONY EVERY MORNING THE SUN WOULD RISE OVER THE TALL TREES CASTING AN WARM GLOW OVER " +
                "THE LAND THE CHILDREN WOULD RUN OUT TO PLAY IN THE MEADOWS WHILE THE ADULTS TENDED TO THEIR TASKS " +
                "THERE WAS ALWAYS SENSE OF PEACE AND CONTENTMENT";

        // Encrypt
        enigma.setRotorPositions(0, 0, 0);
        String encrypted = enigma.encrypt(message);

        // Decrypt (reset positions first!)
        enigma.setRotorPositions(0, 0, 0);
        String decrypted = enigma.decrypt(encrypted);

        System.out.println("Original: " + message);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);

    }
}