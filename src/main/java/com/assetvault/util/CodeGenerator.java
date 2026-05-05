package com.assetvault.util;

import com.assetvault.model.enums.AssetType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

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

    private CodeGenerator() {
    }

    public static String assetCode(AssetType type, long sequence) {
        return "%s-%05d".formatted(PREFIXES.getOrDefault(type, "AST"), sequence);
    }

    public static String employeeCode(long sequence) {
        return "EMP-%05d".formatted(sequence);
    }

    public static String pendingAssetCode() {
        return "TMP-" + compactUuid(16);
    }

    public static String pendingEmployeeCode() {
        return "EMP-TMP-" + compactUuid(22);
    }

    private static String compactUuid(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
