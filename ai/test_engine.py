"""engine.py の評価関数と minimax + alpha-beta 探索を検証する。

実行: py -m unittest discover -s ai -p "test_*.py"
"""
import unittest

import engine


def full_negamax(state, depth, ply):
    """枝刈りなしの参照 negamax。alpha-beta の結果照合に使う。"""
    moves = engine.legal_moves(state)
    if not moves:
        if engine.in_check(state):
            return -engine.MATE + ply
        return 0
    if depth == 0:
        return engine.evaluate(state)
    best = -engine.INF
    for mv in moves:
        score = -full_negamax(engine.make_move(state, mv), depth - 1, ply + 1)
        if score > best:
            best = score
    return best


class EvaluateTest(unittest.TestCase):
    def test_startpos_is_balanced(self):
        # 初期局面は左右・上下対称なので評価は 0
        self.assertEqual(engine.evaluate(engine.parse_fen(engine.STARTPOS)), 0)

    def test_extra_material_favors_side(self):
        # 黒のクイーンが無い局面は白（手番）有利＝正の評価
        fen = "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        self.assertGreater(engine.evaluate(engine.parse_fen(fen)), 0)


class SearchTest(unittest.TestCase):
    def test_finds_mate_in_one(self):
        # バックランクメイト: Ra1-a8# が唯一の詰み
        fen = "6k1/5ppp/8/8/8/8/5PPP/R5K1 w - - 0 1"
        self.assertEqual(engine.best_move(fen, 2), "a1a8")

    def test_captures_hanging_queen(self):
        # e4 のポーンが無防備の黒クイーン d5 を取る exd5 が最善
        fen = "7k/8/8/3q4/4P3/8/8/7K w - - 0 1"
        self.assertEqual(engine.best_move(fen, 2), "e4d5")

    def test_best_move_is_legal(self):
        for fen in (engine.STARTPOS,
                    "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"):
            move = engine.best_move(fen, 2)
            self.assertIn(move, engine.legal_moves_uci(fen))

    def test_alpha_beta_matches_plain_minimax(self):
        # alpha-beta 枝刈りはルート評価値を変えてはならない
        cases = [
            (engine.STARTPOS, 2),
            (engine.STARTPOS, 3),
            ("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 2),
            ("7k/8/8/3q4/4P3/8/8/7K w - - 0 1", 3),
        ]
        for fen, depth in cases:
            pruned = engine.search_value(fen, depth)
            reference = full_negamax(engine.parse_fen(fen), depth, 0)
            self.assertEqual(pruned, reference, msg=f"{fen} depth {depth}")


if __name__ == "__main__":
    unittest.main()
