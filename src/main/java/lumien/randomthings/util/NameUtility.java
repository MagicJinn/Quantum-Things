package lumien.randomthings.util;

public class NameUtility {
    /**
     * Separates words in a camelCase or PascalCase string by inserting spaces before capital
     * letters. Examples: - "SnowBiome" -> "Snow Biome" - "RainShieldRange" -> "Rain Shield Range" -
     * "MaxBiomeCapsuleCapacity" -> "Max Biome Capsule Capacity"
     * 
     * @param name The name to format
     * @return The formatted name with spaces inserted before capital letters (except the first
     *         character)
     */
    public static String separateWordsByCapital(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = name.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Insert a space before a capital letter, except if it's the first character
            if (Character.isUpperCase(c) && i > 0) {
                result.append(' ');
            }

            result.append(c);
        }

        return result.toString();
    }
}

