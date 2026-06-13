"""chess_ai.select_index のロジックを検証する unittest テスト。

pip 不要（標準ライブラリのみ）。プロジェクトルートまたは ai/ ディレクトリから
    py -m unittest discover -s ai
で実行できる。
"""
import unittest

import chess_ai


def _moves(*specs):
    """(capture, captureValue) のタプル列から moves リストを生成するヘルパー。"""
    return [{"capture": c, "captureValue": v} for (c, v) in specs]


class SelectIndexTest(unittest.TestCase):
    def test_empty_moves_returns_minus_one(self):
        # 合法手がない場合は -1（Java 側で null 相当）
        self.assertEqual(chess_ai.select_index(1, []), -1)

    def test_difficulty1_always_returns_valid_index(self):
        moves = _moves((False, 0), (False, 0), (True, 5), (False, 0), (True, 9))
        for _ in range(100):
            idx = chess_ai.select_index(1, moves)
            self.assertTrue(0 <= idx < len(moves))

    def test_difficulty2_prefers_capture(self):
        # capture は 1 手だけ → 必ずその index が選ばれる
        moves = _moves((False, 0), (True, 3), (False, 0))
        for _ in range(100):
            self.assertEqual(chess_ai.select_index(2, moves), 1)

    def test_difficulty2_only_picks_captures_when_present(self):
        # 複数の capture があるとき、選ばれる index は必ず capture 手
        moves = _moves((False, 0), (True, 3), (False, 0), (True, 1))
        capture_indices = {1, 3}
        for _ in range(100):
            self.assertIn(chess_ai.select_index(2, moves), capture_indices)

    def test_difficulty2_falls_back_to_random_when_no_capture(self):
        moves = _moves((False, 0), (False, 0), (False, 0), (False, 0))
        for _ in range(100):
            idx = chess_ai.select_index(2, moves)
            self.assertTrue(0 <= idx < len(moves))

    def test_difficulty3_picks_max_capture_value(self):
        moves = _moves((True, 1), (True, 9), (True, 5))
        self.assertEqual(chess_ai.select_index(3, moves), 1)

    def test_difficulty3_first_index_on_tie(self):
        # 同値のときは最初に見つかった手（Java の strict > と一致）
        moves = _moves((True, 5), (True, 5))
        self.assertEqual(chess_ai.select_index(3, moves), 0)

    def test_difficulty3_no_capture_picks_first(self):
        # capture が無い（全て 0）場合、Java は moves.get(0) を返すので index 0
        moves = _moves((False, 0), (False, 0), (False, 0))
        self.assertEqual(chess_ai.select_index(3, moves), 0)

    def test_unknown_difficulty_behaves_like_random(self):
        moves = _moves((False, 0), (True, 4), (False, 0))
        for _ in range(50):
            idx = chess_ai.select_index(99, moves)
            self.assertTrue(0 <= idx < len(moves))


if __name__ == "__main__":
    unittest.main()
