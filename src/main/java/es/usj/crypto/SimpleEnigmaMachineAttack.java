package es.usj.crypto;

import java.util.*;

/**
 * Educational implementation of cryptanalysis techniques for the Enigma Machine.
 * This class includes statistical analysis, hill climbing, and exhaustive search to test rotor configurations.
 * <p>
 * The cryptanalysis techniques used:
 * - Index of Coincidence (IoC): Measures letter repetition probability.
 * - Frequency Analysis: Compares letter frequencies to English language norms.
 * - Common Trigram Search: Detects common three-letter sequences in decrypted text.
 * - Hill Climbing: Iteratively refines rotor settings for better decryption.
 * - Exhaustive Search: Brute-force approach to find the best possible rotor configuration.
 * </p>
 */
public class SimpleEnigmaMachineAttack {

    // English letter frequencies (A-Z) in percentage
    private static final double[] ENGLISH_FREQUENCIES = {
            8.2, 1.5, 2.8, 4.3, 13, 2.2, 2.0, 6.1, 7.0, 0.15,
            0.77, 4.0, 2.4, 6.7, 7.5, 1.9, 0.095, 6.0, 6.3, 9.1,
            2.8, 0.98, 2.4, 0.15, 2.0, 0.074
    };

    // Common English trigrams (three-letter sequences frequently appearing in text)
    private static final String[] COMMON_TRIGRAMS = {
            "THE", "AND", "ING", "ENT", "ION", "HER", "FOR", "THA", "NTH", "INT"
    };

    // Historical Enigma rotor wirings (Rotors I-V)
    public static final String[] ROTOR_WIRINGS = {
            "EKMFLGDQVZNTOWYHXUSPAIBRCJ",  // Rotor I
            "AJDKSIRUXBLHWTMCQGZNPYFVOE",  // Rotor II
            "BDFHJLCPRTXVZNYEIWGAKMUSQO",  // Rotor III
            "ESOVPZJAYQUIRHXLNFTGKDCMWB",  // Rotor IV
            "VZBRGITYUPSDNHLXAWMJQOFECK"   // Rotor V
    };

    // Historical notch positions for each rotor, which determine when the next rotor advances
    public static final char[] NOTCH_POSITIONS = {'Q', 'E', 'V', 'J', 'Z'};

    /**
     * Represents a potential Enigma machine configuration.
     */
    static class EnigmaConfig {
        int leftRotor;
        int middleRotor;
        int rightRotor;
        char leftRingPosition;
        char middleRingPosition;
        char rightRingPosition;

        /**
         * Creates an Enigma machine configuration with specified rotors and positions.
         */
        public EnigmaConfig(int left, int middle, int right,
                            char leftPos, char middlePos, char rightPos) {
            this.leftRotor = left;
            this.middleRotor = middle;
            this.rightRotor = right;
            this.leftRingPosition = leftPos;
            this.middleRingPosition = middlePos;
            this.rightRingPosition = rightPos;
        }

        /**
         * Creates a SimpleEnigmaMachine with this configuration
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
            char[] rings = {
                    leftRingPosition,
                    middleRingPosition,
                    rightRingPosition
            };

            return new SimpleEnigmaMachine(
                    rotors,
                    notches,
                    rings,
                    "AZBYCXDWEVFU",      // Default plugboard
                    "YRUHQSLDPXNGOKMIEBFZCWVJAT"   // Reflector B
            );

        }

        /**
         * Mutates a configuration to explore new possibilities for breaking the encryption.
         */
        public EnigmaConfig mutate() {
            Random rand = new Random();
            EnigmaConfig newConfig = new EnigmaConfig(
                    leftRotor, middleRotor, rightRotor,
                    leftRingPosition, middleRingPosition, rightRingPosition
            );

            switch (rand.nextInt(6)) {
                case 0 -> {
                    // Select a unique left rotor
                    int newLeftRotor;
                    do {
                        newLeftRotor = rand.nextInt(ROTOR_WIRINGS.length);
                    } while (newLeftRotor == middleRotor || newLeftRotor == rightRotor);
                    newConfig.leftRotor = newLeftRotor;
                }
                case 1 -> {
                    // Select a unique middle rotor
                    int newMiddleRotor;
                    do {
                        newMiddleRotor = rand.nextInt(ROTOR_WIRINGS.length);
                    } while (newMiddleRotor == leftRotor || newMiddleRotor == rightRotor);
                    newConfig.middleRotor = newMiddleRotor;
                }
                case 2 -> {
                    // Select a unique right rotor
                    int newRightRotor;
                    do {
                        newRightRotor = rand.nextInt(ROTOR_WIRINGS.length);
                    } while (newRightRotor == leftRotor || newRightRotor == middleRotor);
                    newConfig.rightRotor = newRightRotor;
                }
                case 3 -> newConfig.leftRingPosition = (char) ('A' + rand.nextInt(26));
                case 4 -> newConfig.middleRingPosition = (char) ('A' + rand.nextInt(26));
                case 5 -> newConfig.rightRingPosition = (char) ('A' + rand.nextInt(26));
            }

            return newConfig;
        }


        @Override
        public String toString() {
            return String.format("Rotors: %d-%d-%d, Positions: %c-%c-%c",
                    leftRotor + 1, middleRotor + 1, rightRotor + 1,
                    leftRingPosition, middleRingPosition, rightRingPosition);
        }
    }


    /**
     * Calculates the Index of Coincidence (IoC) for a given text.
     * Measures the probability that two randomly selected letters from the text are the same.
     *
     * @param text The text to analyze.
     * @return The Index of Coincidence.
     */
    public static double calculateIoC(String text) {
        text = text.toUpperCase();
        int[] frequencies = new int[26];
        int totalChars = 0;

        // Count frequencies
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                frequencies[c - 'A']++;
                totalChars++;
            }
        }

        // Compute IoC
        double sum = 0.0;
        for (int freq : frequencies) {
            sum += freq * (freq - 1);
        }
        return totalChars > 1 ? sum / (totalChars * (totalChars - 1)) : 0;
    }

    /**
     * Analyzes letter frequencies and compares them to expected English frequencies.
     * Returns a chi-square statistic (lower is better, indicating closer match to English).
     *
     * @param text The text to analyze.
     * @return Chi-square statistic.
     */
    public static double calculateFrequencyScore(String text) {
        text = text.toUpperCase();
        int[] frequencies = new int[26];
        int totalChars = 0;

        // Count frequencies
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                frequencies[c - 'A']++;
                totalChars++;
            }
        }

        // Calculate chi-square
        double chiSquare = 0;
        for (int i = 0; i < 26; i++) {
            if (totalChars == 0) {
                break;
            }
            double observed = frequencies[i] / (double) totalChars * 100;
            double expected = ENGLISH_FREQUENCIES[i];
            chiSquare += Math.pow(observed - expected, 2) / expected;
        }
        return chiSquare;
    }

    /**
     * Counts occurrences of common English trigrams in the text.
     *
     * @param text The text to analyze.
     * @return Number of occurrences of common trigrams.
     */
    public static int countCommonTrigrams(String text) {
        text = text.toUpperCase();
        int count = 0;

        for (String trigram : COMMON_TRIGRAMS) {
            int index = 0;
            while ((index = text.indexOf(trigram, index)) != -1) {
                count++;
                index++;
            }
        }
        return count;
    }

    /**
     * Calculates an overall fitness score for potential plaintext.
     * Combines IoC, frequency analysis, and trigram hits.
     *
     * @param text The candidate plaintext.
     * @return A combined fitness score (higher is better).
     */
    public static double calculatePlaintextFitness(String text) {
        double iocScore = calculateIoC(text);
        double freqScore = calculateFrequencyScore(text);
        int trigramCount = countCommonTrigrams(text);

        // Normalize and combine
        // IoC for standard English is ~0.067; let's see how close we are
        double iocFitness = 1.0 - Math.abs(0.067 - iocScore);
        // Lower chi-square is better, so use an inverse scale
        double freqFitness = 1.0 / (1.0 + freqScore);
        // Basic measure for common trigram frequency
        double trigramFitness = text.isEmpty() ? 0 : (trigramCount / (double) text.length());

        // Weighted combination of these three metrics
        // Adjust weights as desired.
        return (0.4 * iocFitness) + (0.4 * freqFitness) + (0.2 * trigramFitness);
    }

    /**
     * Performs a hill-climbing attack to find a good Enigma configuration.
     */
    public static EnigmaConfig hillClimb(String ciphertext, int maxIterations) {
        Random rand = new Random();

        // Create a list of rotor indices and shuffle it
        List<Integer> rotorIndices = new ArrayList<>();
        for (int i = 0; i < ROTOR_WIRINGS.length; i++) {
            rotorIndices.add(i);
        }
        Collections.shuffle(rotorIndices, rand);

        // Start from a random configuration
        EnigmaConfig bestConfig = new EnigmaConfig(
                rotorIndices.get(0),
                rotorIndices.get(1),
                rotorIndices.get(2),
                (char)('A' + rand.nextInt(26)),
                (char)('A' + rand.nextInt(26)),
                (char)('A' + rand.nextInt(26))
        );

        // Initialize machine and get initial decryption
        SimpleEnigmaMachine enigma = bestConfig.createEnigma();
        String bestDecryption = enigma.decrypt(ciphertext);
        double bestScore = calculatePlaintextFitness(bestDecryption);

        System.out.printf("Initial configuration: %s%n", bestConfig);

        int iterationsWithoutImprovement = 0;

        for (int i = 0; i < maxIterations; i++) {
            EnigmaConfig neighbor = bestConfig.mutate();
            enigma = neighbor.createEnigma();

            String decryption = enigma.decrypt(ciphertext);
            double score = calculatePlaintextFitness(decryption);

            if (score > bestScore) {
                bestConfig = neighbor;
                bestScore = score;
                bestDecryption = decryption;
                iterationsWithoutImprovement = 0;

                System.out.printf("Iteration %d: New best configuration found%n", i);
            } else {
                iterationsWithoutImprovement++;

                Collections.shuffle(rotorIndices, rand);

                if (iterationsWithoutImprovement > 100) {
                    bestConfig = new EnigmaConfig(
                            rotorIndices.get(0),
                            rotorIndices.get(1),
                            rotorIndices.get(2),
                            (char)('A' + rand.nextInt(26)),
                            (char)('A' + rand.nextInt(26)),
                            (char)('A' + rand.nextInt(26))
                    );
                    enigma = bestConfig.createEnigma();
                    bestDecryption = enigma.decrypt(ciphertext);
                    bestScore = calculatePlaintextFitness(bestDecryption);
                    iterationsWithoutImprovement = 0;

                    System.out.println("Restarting search with a new random configuration...\n");
                }
            }
        }
        return bestConfig;
    }

    /**
     * Performs an exhaustive search through all possible rotor and position combinations
     * to find the best Enigma machine configuration that maximizes plaintext fitness.
     *
     * <p>The method iterates through all unique rotor orderings and their initial positions,
     * decrypting the given ciphertext with each configuration. It evaluates each decryption
     * using a fitness function and returns the configuration that produces the highest score.
     *
     * <p><b>Note:</b> This method performs a brute-force search and may take considerable
     * time depending on the input size.
     *
     * @param ciphertext The encrypted text to be deciphered.
     * @return The {@link EnigmaConfig} with the highest-scoring plaintext.
     */
    public static EnigmaConfig exhaustiveSearch(String ciphertext) {
        double bestScore = -Double.MAX_VALUE;
        EnigmaConfig bestConfig = null;
        int totalConfigurations = ROTOR_WIRINGS.length * (ROTOR_WIRINGS.length - 1) * (ROTOR_WIRINGS.length - 2) * 26 * 26 * 26;
        int checkedConfigurations = 0;

        System.out.println("Starting exhaustive search. Total configurations to check: " + totalConfigurations);

        for (int left = 0; left < ROTOR_WIRINGS.length; left++) {
            for (int middle = 0; middle < ROTOR_WIRINGS.length; middle++) {
                if (middle == left) continue; // Ensure uniqueness

                for (int right = 0; right < ROTOR_WIRINGS.length; right++) {
                    if (right == left || right == middle) continue; // Ensure uniqueness

                    for (char leftPos = 'A'; leftPos <= 'Z'; leftPos++) {
                        for (char middlePos = 'A'; middlePos <= 'Z'; middlePos++) {
                            for (char rightPos = 'A'; rightPos <= 'Z'; rightPos++) {
                                checkedConfigurations++;

                                if (checkedConfigurations % 100000 == 0) {
                                    System.out.println("Checked " + checkedConfigurations + " / " + totalConfigurations + " configurations...");
                                }

                                EnigmaConfig config = new EnigmaConfig(
                                        left, middle, right,
                                        leftPos, middlePos, rightPos
                                );
                                SimpleEnigmaMachine enigma = config.createEnigma();
                                String candidatePlaintext = enigma.decrypt(ciphertext);
                                double score = calculatePlaintextFitness(candidatePlaintext);

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestConfig = config;

                                    System.out.println("New best configuration found: " + config + " with score " + bestScore);
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Exhaustive search completed. Best configuration: " + bestConfig);
        return bestConfig;
    }

    /**
     * Example usage demonstrating cryptanalysis techniques.
     */
    public static void main(String[] args) {

        // Sample ciphertext
        String ciphertext = "WW PBM XVNIN BFZ AIUCHE OQVPI MZG TJ AVLVWD NDXEEMN DPKPRXIVFXVJYD OUQXU ZKTKDK NTBLX GQ " +
                "MKCVFES APTANMT IQUBN ZEJZDCF UOU JCC EUHXN HAFN YEVJ XPI RNWC AYVZT YKCUGMV OB YSBH LGPI AQFG SIF DBRC" +
                "UCY WLDOQIUT TFFXZ XVY ADJ XI GZKP CA OPN FSMMAMB ZRAAM YAH LWKPZA UJQGUY WG BLJFA OHIMI LJHBH RCV " +
                "ISIRGQ WNPGZ CS HKLEX LLE LIHYKEIFUEW";
        // Remove spaces to avoid impacting analysis results
        ciphertext = ciphertext.replace(" ", "");

        System.out.println("Starting cryptanalysis...\n");

        System.out.println("Initial analysis:");
        System.out.printf("IoC: %.4f%n", calculateIoC(ciphertext));
        System.out.printf("Frequency Score: %.4f%n", calculateFrequencyScore(ciphertext));
        System.out.printf("Trigram Count: %d%n%n", countCommonTrigrams(ciphertext));

        // Hill climbing attack
        System.out.println("Starting hill climbing attack...");
        EnigmaConfig bestConfigHC = hillClimb(ciphertext, 1000);
        SimpleEnigmaMachine enigmaHC = bestConfigHC.createEnigma();
        String decryptedHC = enigmaHC.decrypt(ciphertext);

        System.out.println("\nHill Climb Best Configuration:");
        System.out.println(bestConfigHC);
        System.out.println("Hill Climb Decryption: " + decryptedHC);
        System.out.printf("Hill Climb Fitness: %.4f%n\n", calculatePlaintextFitness(decryptedHC));

        // Exhaustive search
        System.out.println("Starting exhaustive search... (1054560 combinations)");
        long startTime = System.currentTimeMillis();
        EnigmaConfig bestConfigES = exhaustiveSearch(ciphertext);
        long endTime = System.currentTimeMillis();

        System.out.printf("Exhaustive search took %.2f seconds.%n", (endTime - startTime) / 1000.0);
        SimpleEnigmaMachine enigmaES = bestConfigES.createEnigma();
        String decryptedES = enigmaES.decrypt(ciphertext);

        System.out.println("\nExhaustive Search Best Configuration:");
        System.out.println(bestConfigES);
        System.out.println("Exhaustive Search Decryption: " + decryptedES);
        System.out.printf("Exhaustive Search Fitness: %.4f%n", calculatePlaintextFitness(decryptedES));

    }
}
