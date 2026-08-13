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

import java.nio.file.Path;

/**
 * PGN ファイルの保存パス解決を行う。Swing版・JavaFX版の両フレームで同一の実装が
 * 重複していたため共通化した（Issue #174）。
 */
public final class PgnPaths {

    private PgnPaths() {
    }

    /**
     * ファイル選択ダイアログで選択されたパスから保存用のパスを解決する。
     * 拡張子 ".pgn" が無ければ自動付与する。
     *
     * @param selectedPath 選択されたパス
     * @return 解決したパス
     */
    public static Path resolvePgnPath(Path selectedPath) {
        String name = selectedPath.getFileName().toString();
        if (name.endsWith(".pgn")) {
            return selectedPath;
        }
        return selectedPath.resolveSibling(name + ".pgn");
    }
}
