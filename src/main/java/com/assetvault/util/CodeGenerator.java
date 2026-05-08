package com.assetvault.util;

import com.assetvault.model.enums.AssetType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility methods for code generator.
 */
public final class CodeGenerator {
    private static final Map<AssetType, String> PREFIXES = new EnumMap<>(AssetType.class);

    static {
        PREFIXES.put(AssetType.LAPTOP, "LPT");
        PREFIXES.put(AssetType.DESKTOP, "DSK");
        PREFIXES.put(AssetType.MONITOR, "MON");
        PREFIXES.put(AssetType.KEYBOARD, "KEY");
        PREFIXES.put(AssetType.MOUSE, "MOU");
        PREFIXES.put(AssetType.HEADSET, "HDS");
        PREFIXES.put(AssetType.PHONE, "PHN");
        PREFIXES.put(AssetType.PRINTER, "PRN");
//        PREFIXES.put(AssetType.OTHER, "AST");
    }

    /**
     * Prevents instantiation of this utility class.
     */
    private CodeGenerator() {
    }

    /**
     * Generates a stable asset code from the supplied type and sequence.
     *
     * @param type the requested type value
     * @param sequence the sequence value
     * @return the generated code value
     */
    public static String assetCode(AssetType type, long sequence) {
        return "%s-%05d".formatted(PREFIXES.getOrDefault(type, "AST"), sequence);
    }

    /**
     * Generates a stable employee code from the supplied sequence.
     *
     * @param sequence the sequence value
     * @return the generated code value
     */
    public static String employeeCode(long sequence) {
        return "EMP-%05d".formatted(sequence);
    }

    /**
     * Returns the placeholder asset code used before persistence assigns an id.
     *
     * @return the generated code value
     */
    public static String pendingAssetCode() {
        return "TMP-" + compactUuid(16);
    }

    /**
     * Returns the placeholder employee code used before persistence assigns an id.
     *
     * @return the generated code value
     */
    public static String pendingEmployeeCode() {
        return "EMP-TMP-" + compactUuid(22);
    }

    /**
     * Executes the compact uuid operation.
     *
     * @param length the length value
     * @return the resulting code generator
     */
    private static String compactUuid(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
