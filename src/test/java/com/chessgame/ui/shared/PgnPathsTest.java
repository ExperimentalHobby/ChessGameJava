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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PgnPaths} のユニットテスト。
 */
class PgnPathsTest {

    @Test
    void testResolvePgnPathKeepsPathWhenExtensionAlreadyPresent() {
        Path selected = Path.of("game.pgn");

        Path resolved = PgnPaths.resolvePgnPath(selected);

        assertThat(resolved).isEqualTo(Path.of("game.pgn"));
    }

    @Test
    void testResolvePgnPathAppendsExtensionWhenMissing() {
        Path selected = Path.of("game");

        Path resolved = PgnPaths.resolvePgnPath(selected);

        assertThat(resolved).isEqualTo(Path.of("game.pgn"));
    }

    @Test
    void testResolvePgnPathTreatsExtensionCaseInsensitively() {
        // Issue #239: 大文字小文字を区別すると game.PGN が game.PGN.pgn になる。
        // Windows のファイル選択ダイアログは拡張子の大文字入力を普通に許す
        assertThat(PgnPaths.resolvePgnPath(Path.of("game.PGN"))).isEqualTo(Path.of("game.PGN"));
        assertThat(PgnPaths.resolvePgnPath(Path.of("game.Pgn"))).isEqualTo(Path.of("game.Pgn"));
    }

    @Test
    void testResolvePgnPathAppendsExtensionUnderParentDirectory() {
        Path selected = Path.of("saves", "game");

        Path resolved = PgnPaths.resolvePgnPath(selected);

        assertThat(resolved).isEqualTo(Path.of("saves", "game.pgn"));
    }

    @Test
    void testNeedsOverwriteConfirmationWhenFileAlreadyExists(@TempDir Path dir) throws IOException {
        // Issue #286: JFileChooser は上書き確認を出さず、拡張子の自動付与後は OS の確認も効かない
        Path existing = Files.writeString(dir.resolve("game.pgn"), "1. e4 *");

        assertThat(PgnPaths.needsOverwriteConfirmation(existing)).isTrue();
    }

    @Test
    void testNoOverwriteConfirmationWhenFileDoesNotExist(@TempDir Path dir) {
        assertThat(PgnPaths.needsOverwriteConfirmation(dir.resolve("new.pgn"))).isFalse();
    }

    @Test
    void testOverwriteConfirmationIsJudgedAfterExtensionIsAppended(@TempDir Path dir) throws IOException {
        // "game" と入力して選ぶと保存先は game.pgn になる。既存の game.pgn を上書きするので確認が要る
        Files.writeString(dir.resolve("game.pgn"), "1. e4 *");

        Path resolved = PgnPaths.resolvePgnPath(dir.resolve("game"));

        assertThat(PgnPaths.needsOverwriteConfirmation(resolved)).isTrue();
    }
}
