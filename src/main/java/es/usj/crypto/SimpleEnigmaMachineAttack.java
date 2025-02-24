package es.usj.crypto;

import java.util.*;

/**
 * Educational implementation of cryptanalysis techniques for the Enigma Machine.
 * This class includes statistical analysis, hill climbing, and an exhaustive search
 * to test rotor configurations.
 */
public class SimpleEnigmaMachineAttack {

    // English letter frequencies (A-Z) in percentage
    private static final double[] ENGLISH_FREQUENCIES = {
            8.2, 1.5, 2.8, 4.3, 13.0, 2.2, 2.0, 6.1, 7.0, 0.15,
            0.77, 4.0, 2.4, 6.7, 7.5, 1.9, 0.095, 6.0, 6.3, 9.1,
            2.8, 0.98, 2.4, 0.15, 2.0, 0.074
    };

    // Common English trigrams
    private static final String[] COMMON_TRIGRAMS = {
            "THE", "AND", "ING", "ENT", "ION", "HER", "FOR", "THA", "NTH", "INT"
    };

    // Historical Enigma rotor wirings (I-V)
    public static final String[] ROTOR_WIRINGS = {
            "EKMFLGDQVZNTOWYHXUSPAIBRCJ",  // Rotor I
            "AJDKSIRUXBLHWTMCQGZNPYFVOE",  // Rotor II
            "BDFHJLCPRTXVZNYEIWGAKMUSQO",  // Rotor III
            "ESOVPZJAYQUIRHXLNFTGKDCMWB",  // Rotor IV
            "VZBRGITYUPSDNHLXAWMJQOFECK"   // Rotor V
    };

    // Historical notch positions for each rotor
    public static final char[] NOTCH_POSITIONS = {'Q', 'E', 'V', 'J', 'Z'};

    /**
     * Represents a potential Enigma machine configuration.
     * <p/>
     * NOTE: In a real Enigma, “ring settings” (Ringstellung) and
     * “rotor start positions” (Grundstellung) are separate.
     */
    static class EnigmaConfig {
        int leftRotor, middleRotor, rightRotor;
        char leftStart, middleStart, rightStart;      // rotor start positions

        /**
         * Constructor with explicit ring and start positions.
         */
        public EnigmaConfig(int left, int middle, int right,
                            char leftStart, char middleStart, char rightStart) {
            this.leftRotor = left;
            this.middleRotor = middle;
            this.rightRotor = right;
            this.leftStart = leftStart;
            this.middleStart = middleStart;
            this.rightStart = rightStart;
        }

        /**
         * Creates a SimpleEnigmaMachine with this configuration (rotor wirings, start positions)
         */
        public SimpleEnigmaMachine createEnigma() {
            String[] rotors = {
                    ROTOR_WIRINGS[leftRotor],
                    ROTOR_WIRINGS[middleRotor],
                    ROTOR_WIRINGS[rightRotor]
            };
            char[] notches = {
                    NOTCH_POSITIONS[leftRotor],
                    NOTCH_POSITIONS[middleRotor],
                    NOTCH_POSITIONS[rightRotor]
            };

            return new SimpleEnigmaMachine(
                    rotors,
                    notches,
                    new char[]{leftStart, middleStart, rightStart},
                    "AZBYCXDWEVFU",
                    "YRUHQSLDPXNGOKMIEBFZCWVJAT"
            );

        }

        @Override
        public String toString() {
            return String.format("Rotors: [%d-%d-%d], StartPos: [%c-%c-%c]",
                    leftRotor + 1, middleRotor + 1, rightRotor + 1,
                    leftStart, middleStart, rightStart);
        }
    }

    /**
     * Calculate Index of Coincidence.
     */
    public static double calculateIoC(String text) {
        text = text.toUpperCase();
        int[] freq = new int[26];
        int totalChars = 0;

        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                freq[c - 'A']++;
                totalChars++;
            }
        }

        double sum = 0;
        for (int f : freq) {
            sum += f * (f - 1);
        }
        return (totalChars > 1) ? sum / (totalChars * (totalChars - 1)) : 0.0;
    }

    /**
     * Calculate how well letter frequencies match typical English frequencies (chi-square).
     * Lower chi-square means a closer match to English.
     */
    public static double calculateFrequencyScore(String text) {
        text = text.toUpperCase();
        int[] freq = new int[26];
        int totalChars = 0;

        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                freq[c - 'A']++;
                totalChars++;
            }
        }

        if (totalChars == 0) return Double.MAX_VALUE;

        double chiSq = 0.0;
        for (int i = 0; i < 26; i++) {
            double observed = (freq[i] * 100.0) / totalChars;
            double expected = ENGLISH_FREQUENCIES[i];
            chiSq += Math.pow(observed - expected, 2) / (expected + 0.000001);
        }
        return chiSq;
    }

    /**
     * Count occurrences of known common English trigrams.
     */
    public static int countCommonTrigrams(String text) {
        text = text.toUpperCase();
        int count = 0;
        for (String trigram : COMMON_TRIGRAMS) {
            int idx = 0;
            while ((idx = text.indexOf(trigram, idx)) != -1) {
                count++;
                idx++;
            }
        }
        return count;
    }

    /**
     * Combine IoC, frequency analysis, and trigram count into a single fitness value (higher = better).
     */
    public static double calculatePlaintextFitness(String text) {
        // Index of Coincidence
        double ioc = calculateIoC(text);           // typical ~ 0.0667 for English
        double iocFitness = 1.0 - Math.abs(0.067 - ioc);

        // Frequency score (lower is better) => invert
        double freqScore = calculateFrequencyScore(text);
        double freqFitness = 1.0 / (1.0 + freqScore);

        // Common trigram hits
        int trigramCount = countCommonTrigrams(text);
        // trivially scaled by length to avoid punishing short texts
        double trigramFitness = (text.length() > 0) ? (trigramCount / (double) text.length()) : 0;

        // Weighted combination
        return 0.4 * iocFitness + 0.4 * freqFitness + 0.2 * trigramFitness;
    }

    /**
     * Simple hill climb method that mutates ring or rotor selections,
     * but does NOT separately handle rotor start positions in this example.
     * You can adapt this if you want to hill-climb the start positions as well.
     */
    public static EnigmaConfig hillClimb(String ciphertext, int maxIterations) {
        Random rand = new Random();
        List<Integer> rotorList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));
        Collections.shuffle(rotorList, rand);

        EnigmaConfig bestConfig = new EnigmaConfig(rotorList.get(0), rotorList.get(1), rotorList.get(2), 'A', 'A', 'A');
        SimpleEnigmaMachine enigma = bestConfig.createEnigma();

        String bestDecryption = enigma.decrypt(ciphertext);
        double bestScore = calculatePlaintextFitness(bestDecryption);

        for (int i = 0; i < maxIterations; i++) {
            EnigmaConfig neighbor = mutateConfig(bestConfig);
            enigma = neighbor.createEnigma();

            String dec = enigma.decrypt(ciphertext);
            double score = calculatePlaintextFitness(dec);

            if (score > bestScore) {
                bestConfig = neighbor;
                bestScore = score;
                System.out.printf("Iteration %d: New best config => %.4f%n", i, score);
            }
        }
        return bestConfig;
    }

    /**
     * A simple mutation for demonstration — randomly change one field.
     */
    private static EnigmaConfig mutateConfig(EnigmaConfig config) {
        Random rand = new Random();
        int mutationType = rand.nextInt(4);
        int newRotor;

        switch (mutationType) {
            case 0:
                do { newRotor = rand.nextInt(5); } while (newRotor == config.leftRotor);
                return new EnigmaConfig(newRotor, config.middleRotor, config.rightRotor, config.leftStart, config.middleStart, config.rightStart);
            case 1:
                do { newRotor = rand.nextInt(5); } while (newRotor == config.middleRotor);
                return new EnigmaConfig(config.leftRotor, newRotor, config.rightRotor, config.leftStart, config.middleStart, config.rightStart);
            case 2:
                do { newRotor = rand.nextInt(5); } while (newRotor == config.rightRotor);
                return new EnigmaConfig(config.leftRotor, config.middleRotor, newRotor, config.leftStart, config.middleStart, config.rightStart);
            case 3:
                return new EnigmaConfig(config.leftRotor, config.middleRotor, config.rightRotor,
                        (char) ('A' + rand.nextInt(26)),
                        (char) ('A' + rand.nextInt(26)),
                        (char) ('A' + rand.nextInt(26)));
            default:
                return config;
        }
    }

    /**
     * Performs an exhaustive search to determine the best Enigma configuration
     * that maximizes the plaintext fitness score.
     *
     * - Tests all permutations of three rotors out of five.
     * - For each permutation, tests all 26^3 rotor start positions (AAA - ZZZ).
     * - Uses a fitness function to determine the most likely correct decryption.
     *
     * @param ciphertext The encrypted message to decrypt.
     * @return The best Enigma configuration found.
     */
    public static EnigmaConfig exhaustiveSearch(String ciphertext) {
        double bestScore = -Double.MAX_VALUE;
        EnigmaConfig bestConfig = null;

        int rotorCount = ROTOR_WIRINGS.length;
        long totalCombinations = (rotorCount * (rotorCount - 1) * (rotorCount - 2)) * 26L * 26L * 26L;
        long checked = 0;

        System.out.println("Starting exhaustive search. Total combinations: " + totalCombinations);

        // Iterate over all permutations of three rotors
        for (int left = 0; left < rotorCount; left++) {
            for (int middle = 0; middle < rotorCount; middle++) {
                if (middle == left) continue;
                for (int right = 0; right < rotorCount; right++) {
                    if (right == left || right == middle) continue;

                    // Iterate over all possible rotor start positions (AAA to ZZZ)
                    for (char leftStart = 'A'; leftStart <= 'Z'; leftStart++) {
                        for (char middleStart = 'A'; middleStart <= 'Z'; middleStart++) {
                            for (char rightStart = 'A'; rightStart <= 'Z'; rightStart++) {
                                checked++;

                                if (checked % 100000 == 0) {
                                    System.out.printf("Checked %d / %d...%n", checked, totalCombinations);
                                }

                                // Build the current Enigma configuration
                                EnigmaConfig config = new EnigmaConfig(left, middle, right, leftStart, middleStart, rightStart);
                                SimpleEnigmaMachine enigma = config.createEnigma();

                                // Decrypt using this configuration
                                String candidatePlaintext = enigma.decrypt(ciphertext);
                                double score = calculatePlaintextFitness(candidatePlaintext);

                                // Keep track of the best configuration found
                                if (score > bestScore) {
                                    bestScore = score;
                                    bestConfig = config;
                                    System.out.printf("New best (score=%.4f): %s%n", score, bestConfig);
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Exhaustive search completed. Best configuration found: " + bestConfig);
        return bestConfig;
    }

    /**
     * Example usage demonstrating cryptanalysis.
     */
    public static void main(String[] args) {

        // Some sample ciphertext (spaces removed to not confuse analysis)
        String ciphertext = "ZE FCA WIZHK DNH AEIABS MYBPH GNF GI BTRWMC MRDYTZX COJYQTWYATGKJE YMUWO UUUWWM KRJSJ YL " +
                "JDSYZJQ QULPULL ODDTS LDKDTHD WBH JAU UXGUV PMDQ BQYG LTQ XITO YZLNB HQALSZP CX JFVA XNRX WKMO IEI OOPO  " +
                "GCL SZEZKLKQ ZVKNI ZII TQQ XC NBYZ EF IVS PZUNLCN HRJPT JYN DKNSRR BBYHLM YC GFYLP DTZVA UBDZP NIF FBQTSZ " +
                "IDVQO JD ODULS FWV LCLFPFZIKUP";
        ciphertext = ciphertext.replace(" ", "").toUpperCase();

        System.out.println("Analyzing ciphertext: " + ciphertext.substring(0, Math.min(ciphertext.length(), 100)) + "...\n");

        double ioc = calculateIoC(ciphertext);
        double freq = calculateFrequencyScore(ciphertext);
        int trigrams = countCommonTrigrams(ciphertext);
        System.out.printf("IoC: %.4f, FrequencyScore: %.4f, CommonTrigrams: %d%n", ioc, freq, trigrams);

        // Hill Climb example
        System.out.println("\n--- Hill Climb Attack ---");
        EnigmaConfig bestHill = hillClimb(ciphertext, 1000);
        SimpleEnigmaMachine enigmaHC = bestHill.createEnigma();
        enigmaHC.setRotorPositions(new char[]{bestHill.leftStart, bestHill.middleStart, bestHill.rightStart});
        String plainHill = enigmaHC.decrypt(ciphertext);
        System.out.println("Best from Hill Climb: " + bestHill);
        System.out.printf("Fitness: %.4f%nDecryption: %s%n%n", calculatePlaintextFitness(plainHill), plainHill);

        // Exhaustive search
        System.out.println("--- Exhaustive Search ---");
        long start = System.currentTimeMillis();
        EnigmaConfig bestExhaustive = exhaustiveSearch(ciphertext);
        long end = System.currentTimeMillis();
        SimpleEnigmaMachine enigmaES = bestExhaustive.createEnigma();
        enigmaES.setRotorPositions(new char[]{
                bestExhaustive.leftStart, bestExhaustive.middleStart, bestExhaustive.rightStart
        });
        String plainExhaustive = enigmaES.decrypt(ciphertext);

        System.out.printf("Exhaustive search took %.2f seconds.%n", (end - start) / 1000.0);
        System.out.println("Best from Exhaustive: " + bestExhaustive);
        System.out.printf("Fitness: %.4f%nDecryption: %s%n", calculatePlaintextFitness(plainExhaustive), plainExhaustive);
    }
}
