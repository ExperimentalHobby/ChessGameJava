/*
 * MIT License
 *
 * Copyright (c) 2026 ChessGame Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.chessgame.ui.shared;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * PGN ファイルの保存パス解決を行う。Swing版・JavaFX版の両フレームで同一の実装が
 * 重複していたため共通化した（Issue #174）。
 */
public final class PgnPaths {

    private PgnPaths() {
    }

    /** 付与・判定に使う PGN の拡張子。 */
    private static final String PGN_EXTENSION = ".pgn";

    /**
     * 保存すると既存のファイルを上書きしてしまうかを返す。確認ダイアログを出すかの判定に使う。
     * 拡張子の自動付与後（{@link #resolvePgnPath}）のパスで判定すること。"game" と入力して
     * "game.pgn" が既にある場合は、OS のファイル選択ダイアログの上書き確認が働かないため。
     *
     * @param path 保存先のパス
     * @return 既に存在するなら true
     */
    public static boolean needsOverwriteConfirmation(Path path) {
        return Files.exists(path);
    }

    /**
     * ファイル選択ダイアログで選択されたパスから保存用のパスを解決する。
     * 拡張子 ".pgn" が無ければ自動付与する。判定は大文字小文字を区別しないため、
     * {@code game.PGN} は二重拡張子にならない。
     *
     * @param selectedPath 選択されたパス
     * @return 解決したパス
     */
    public static Path resolvePgnPath(Path selectedPath) {
        String name = selectedPath.getFileName().toString();
        // Windows のファイル選択ダイアログは拡張子の大文字入力を普通に許すため、
        // 大文字小文字を区別すると game.PGN が game.PGN.pgn になってしまう
        if (name.toLowerCase(Locale.ROOT).endsWith(PGN_EXTENSION)) {
            return selectedPath;
        }
        return selectedPath.resolveSibling(name + PGN_EXTENSION);
    }
}
