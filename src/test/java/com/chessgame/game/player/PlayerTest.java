package com.chessgame.game.player;

import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link Player} のファクトリメソッドと属性を検証する。
 */
public class PlayerTest {

    @Test
    void humanFactoryCreatesHumanPlayer() {
        Player player = Player.human(Color.WHITE, "Alice");

        assertThat(player.isHuman()).isTrue();
        assertThat(player.isAI()).isFalse();
    }

    @Test
    void humanPlayerHasCorrectColorAndName() {
        Player player = Player.human(Color.BLACK, "Bob");

        assertThat(player.getColor()).isEqualTo(Color.BLACK);
        assertThat(player.getName()).isEqualTo("Bob");
    }

    @Test
    void aiPlayerIsNotHuman() {
        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 2);

        assertThat(ai.isHuman()).isFalse();
        assertThat(ai.isAI()).isTrue();
    }

    @Test
    void playersWithSameColorAreEqual() {
        Player p1 = Player.human(Color.WHITE, "Alice");
        Player p2 = Player.human(Color.WHITE, "Bob");

        // Player の equals は色のみで判定する
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void playersWithDifferentColorsAreNotEqual() {
        Player white = Player.human(Color.WHITE, "Alice");
        Player black = Player.human(Color.BLACK, "Bob");

        assertThat(white).isNotEqualTo(black);
    }
}
