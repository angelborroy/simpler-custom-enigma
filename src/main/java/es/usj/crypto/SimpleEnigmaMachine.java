package es.usj.crypto;

public class SimpleEnigmaMachine {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Rotor settings
    private String[] rotors;
    private char[] notchPositions;
    private char[] ringSettings;
    private int[] rotorPositions;

    // Plugboard and reflector
    private char[] plugboardPairs;
    private char[] reflectorPairs;

    /**
     * Creates an Enigma machine with custom settings
     * @param rotorSettings Array of strings representing rotor wirings
     * @param notches Notch positions for each rotor
     * @param rings Ring settings for each rotor
     * @param plugboard String of character pairs for plugboard connections
     * @param reflector String of character pairs for reflector connections
     */
    public SimpleEnigmaMachine(String[] rotorSettings, char[] notches, char[] rings,
                               String plugboard, String reflector) {
        this.rotors = rotorSettings;
        this.notchPositions = notches;
        this.ringSettings = rings;
        this.rotorPositions = new int[rotorSettings.length];
        this.plugboardPairs = plugboard.toCharArray();
        this.reflectorPairs = reflector.toCharArray();

        // Apply initial ring settings
        applyRingSettings();
    }

    /**
     * Creates an Enigma machine with default settings
     */
    public SimpleEnigmaMachine() {
        // Default rotor settings
        this.rotors = new String[]{
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ",  // Rotor I
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",  // Rotor II
                "BDFHJLCPRTXVZNYEIWGAKMUSQO"   // Rotor III
        };

        // Default notch positions (Q, E, V for rotors I, II, III)
        this.notchPositions = new char[]{'Q', 'E', 'V'};

        // Default ring settings (A for all rotors)
        this.ringSettings = new char[]{'A', 'A', 'A'};

        this.rotorPositions = new int[3];

        // Default plugboard and reflector pairs
        this.plugboardPairs = "AZBYCXDWEVFU".toCharArray();
        this.reflectorPairs = "AYBNCDEXFWGV".toCharArray();

        // Apply initial ring settings
        applyRingSettings();
    }

    /**
     * Applies ring settings to rotor wirings
     */
    private void applyRingSettings() {
        for (int i = 0; i < rotors.length; i++) {
            int shift = ALPHABET.indexOf(ringSettings[i]);
            if (shift > 0) {
                // Shift rotor wiring according to ring setting
                rotors[i] = rotors[i].substring(shift) + rotors[i].substring(0, shift);
            }
        }
    }

    /**
     * Encrypts a single character through the Enigma machine
     */
    private char processChar(char c) {
        // Step 1: Apply plugboard
        c = applyPairs(c, plugboardPairs);

        // Step 2: Rotate rotors
        rotateRotors();

        // Step 3: Pass through rotors forward
        for (int i = 0; i < rotors.length; i++) {
            int pos = ALPHABET.indexOf(c);
            pos = (pos + rotorPositions[i]) % 26;
            c = rotors[i].charAt(pos);
        }

        // Step 4: Apply reflector
        c = applyPairs(c, reflectorPairs);

        // Step 5: Pass through rotors backward
        for (int i = rotors.length - 1; i >= 0; i--) {
            int pos = rotors[i].indexOf(c);
            pos = (pos - rotorPositions[i] + 26) % 26;
            c = ALPHABET.charAt(pos);
        }

        // Step 6: Apply plugboard again
        return applyPairs(c, plugboardPairs);
    }

    /**
     * Applies character pair substitutions (used for both plugboard and reflector)
     */
    private char applyPairs(char c, char[] pairs) {
        for (int i = 0; i < pairs.length; i += 2) {
            if (c == pairs[i]) return pairs[i + 1];
            if (c == pairs[i + 1]) return pairs[i];
        }
        return c;
    }

    /**
     * Rotates the rotors considering notch positions
     */
    private void rotateRotors() {
        // Check for notch positions and rotate accordingly
        boolean[] shouldRotate = new boolean[rotors.length];
        shouldRotate[0] = true; // Rightmost rotor always rotates

        // Check middle rotor notch
        if (ALPHABET.charAt(rotorPositions[1]) == notchPositions[1]) {
            shouldRotate[1] = true;
            shouldRotate[2] = true;
        }

        // Check right rotor notch
        if (ALPHABET.charAt(rotorPositions[0]) == notchPositions[0]) {
            shouldRotate[1] = true;
        }

        // Apply rotations
        for (int i = 0; i < rotorPositions.length; i++) {
            if (shouldRotate[i]) {
                rotorPositions[i] = (rotorPositions[i] + 1) % 26;
            }
        }
    }

    /**
     * Encrypts a message
     */
    public String encrypt(String message) {
        return process(message);
    }

    /**
     * Decrypts a message (same as encrypt due to Enigma's reciprocal nature)
     */
    public String decrypt(String message) {
        // Due to the reciprocal nature of the Enigma machine,
        // decryption is the same as encryption with the same settings
        return process(message);
    }

    /**
     * Processes a message (used for both encryption and decryption)
     */
    private String process(String message) {
        StringBuilder result = new StringBuilder();

        for (char c : message.toUpperCase().toCharArray()) {
            if (Character.isLetter(c)) {
                result.append(processChar(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Sets the initial position of the rotors
     */
    public void setRotorPositions(int... positions) {
        if (positions.length != rotorPositions.length) {
            throw new IllegalArgumentException("Must provide position for each rotor");
        }
        System.arraycopy(positions, 0, rotorPositions, 0, positions.length);
    }

    /**
     * Example usage demonstrating encryption and decryption
     */
    public static void main(String[] args) {

        // Create machine with default settings
        SimpleEnigmaMachine enigma = new SimpleEnigmaMachine();

        // Example with default settings
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