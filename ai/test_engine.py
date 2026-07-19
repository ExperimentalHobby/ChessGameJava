"""engine.py の評価関数と minimax + alpha-beta 探索を検証する。

実行: py -m unittest discover -s ai -p "test_*.py"
"""
import unittest

import engine


def full_negamax(state, depth, ply):
    """枝刈りなしの参照 negamax。alpha-beta の結果照合に使う。

    depth 0 では engine.negamax と同じく quiescence を呼ぶ（静止探索自体は
    alpha-beta 込みのため、ここで evaluate に戻すと比較対象が食い違う）。
    """
    moves = engine.legal_moves(state)
    if not moves:
        if engine.in_check(state):
            return -engine.MATE + ply
        return 0
    if depth == 0:
        return engine.quiescence(state, -engine.INF, engine.INF)
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
        # 黒のクイーンが無い局面。他の駒は全て開始位置のまま(PSTは白黒対称に相殺)なので、
        # 差分は白クイーンの材料価値(900)+その開始マスd1のPST補正(-5)=895に一致するはず
        fen = "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        self.assertEqual(engine.evaluate(engine.parse_fen(fen)), 895)


class ZobristHashTest(unittest.TestCase):
    def test_hash_is_deterministic(self):
        # 同じFENから2回計算しても同じハッシュになる
        state_a = engine.parse_fen(engine.STARTPOS)
        state_b = engine.parse_fen(engine.STARTPOS)

        self.assertEqual(engine.zobrist_hash(state_a), engine.zobrist_hash(state_b))

    def test_hash_differs_by_side_to_move(self):
        # 盤面・キャスリング権・アンパッサンが同一でも手番が違えば異なるハッシュになる
        white_to_move = engine.parse_fen(
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1")
        black_to_move = engine.parse_fen(
            "4k3/8/8/8/8/8/8/4K3 b - - 0 1")

        self.assertNotEqual(engine.zobrist_hash(white_to_move), engine.zobrist_hash(black_to_move))

    def test_hash_differs_for_different_positions(self):
        # 明らかに異なる局面同士は異なるハッシュになる
        startpos = engine.parse_fen(engine.STARTPOS)
        after_e4 = engine.parse_fen(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")

        self.assertNotEqual(engine.zobrist_hash(startpos), engine.zobrist_hash(after_e4))


class TranspositionTableTest(unittest.TestCase):
    def test_negamax_populates_transposition_table(self):
        state = engine.parse_fen(engine.STARTPOS)
        tt = {}

        engine.negamax(state, 2, -engine.INF, engine.INF, 0, tt)

        self.assertGreater(len(tt), 0)
        self.assertIn(engine.zobrist_hash(state), tt)

    def test_negamax_returns_cached_value_from_transposition_table(self):
        # ルート局面のエントリに本来の探索結果とは異なる番兵値を仕込み、
        # negamaxがそれをそのまま返す（＝実際にTTを参照している）ことを確認する
        state = engine.parse_fen(engine.STARTPOS)
        sentinel_score = 123456
        key = engine.zobrist_hash(state)
        tt = {key: (100, sentinel_score, engine.TT_EXACT, None)}

        result = engine.negamax(state, 2, -engine.INF, engine.INF, 0, tt)

        self.assertEqual(result, sentinel_score)


class QuiescenceTest(unittest.TestCase):
    # 検証局面: 白Q e4、白K e1、黒R e8、黒K f8、黒P e6。
    # 唯一の駒取り Qxe6 は Rxe6 で回収される悪手（水平線効果の典型例）。
    HORIZON_FEN = "4rk2/8/4p3/8/4Q3/8/8/4K3 w - - 0 1"

    def test_quiescence_matches_static_eval_when_quiet(self):
        # 駒の取り合いが無い局面では stand pat のまま static eval と一致する
        fen = "4k3/8/8/8/8/8/8/4K2R w K - 0 1"
        state = engine.parse_fen(fen)
        self.assertEqual(
            engine.quiescence(state, -engine.INF, engine.INF),
            engine.evaluate(state))

    def test_quiescence_stands_pat_on_bad_capture(self):
        # ルート局面（白番）: 唯一の駒取り Qxe6 は回収されて大損なので取らない
        state = engine.parse_fen(self.HORIZON_FEN)
        self.assertEqual(
            engine.quiescence(state, -engine.INF, engine.INF),
            engine.evaluate(state))
        self.assertEqual(engine.evaluate(state), 290)

    def test_quiescence_finds_good_recapture(self):
        # Qxe6 適用後（黒番）: static eval は -390 で悪そうに見えるが、
        # 静止探索は Rxe6 の回収を発見し大幅に良い値を返す
        state = engine.parse_fen(self.HORIZON_FEN)
        after_qxe6 = engine.make_move(
            state, (engine.uci_to_idx("e4"), engine.uci_to_idx("e6"), None))
        self.assertEqual(engine.evaluate(after_qxe6), -390)
        self.assertGreater(
            engine.quiescence(after_qxe6, -engine.INF, engine.INF), 0)

    def test_best_move_avoids_horizon_effect_blunder(self):
        # 静止探索導入前は depth=1 で Qxe6（e4e6）を最善手と誤判定してしまう
        self.assertNotEqual(engine.best_move(self.HORIZON_FEN, 1), "e4e6")


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


class StalemateTest(unittest.TestCase):
    # 白キングh1・黒クイーンf2・黒キングa8。白番だが王手はされておらず合法手が0の
    # ステイルメイト局面（g1・g2・h2はすべてクイーンに攻撃されている）
    STALEMATE_FEN = "k7/8/8/8/8/8/5q2/7K w - - 0 1"

    def test_negamax_returns_zero_on_stalemate(self):
        self.assertEqual(engine.search_value(self.STALEMATE_FEN, 2), 0)

    def test_best_move_returns_none_on_stalemate(self):
        self.assertIsNone(engine.best_move(self.STALEMATE_FEN, 2))


if __name__ == "__main__":
    unittest.main()
