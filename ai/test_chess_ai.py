"""chess_ai.main の入出力（stdin の JSON → stdout の1行）を検証する unittest テスト。

pip 不要（標準ライブラリのみ）。プロジェクトルートから
    py -m unittest discover -s ai -p "test_*.py"
で実行できる。難易度1〜3は Java 側（AIPlayer）で完結するため、このスクリプトは
難易度4と整合性テスト用の movegen のみを扱う。
"""
import io
import json
import unittest
from contextlib import redirect_stdout
from unittest import mock

import chess_ai
import engine

STARTPOS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"


def _run(request):
    """request（dict）を stdin として main() を実行し、stdout を返す。"""
    out = io.StringIO()
    with mock.patch("sys.stdin", io.StringIO(json.dumps(request))), redirect_stdout(out):
        chess_ai.main()
    return out.getvalue()


class MainTest(unittest.TestCase):
    def test_difficulty4_returns_a_legal_move_in_uci(self):
        output = _run({"difficulty": 4, "depth": 2, "fen": STARTPOS}).strip()

        self.assertIn(output, engine.legal_moves_uci(STARTPOS))

    def test_difficulty4_returns_empty_line_when_no_legal_moves(self):
        # ステイルメイト局面: 合法手が無い場合は空行
        output = _run({"difficulty": 4, "depth": 2, "fen": "k7/8/8/8/8/8/5q2/7K w - - 0 1"})

        self.assertEqual(output, "\n")

    def test_movegen_lists_sorted_legal_moves(self):
        output = _run({"command": "movegen", "fen": STARTPOS}).strip().split(" ")

        self.assertEqual(output, sorted(engine.legal_moves_uci(STARTPOS)))
        self.assertEqual(len(output), 20)

    def test_difficulty_1_to_3_are_not_handled_by_python(self):
        # 難易度1〜3は Java 実装のみ。Python に来た場合は黙って誤った index を返さずエラー終了する
        for difficulty in (1, 2, 3):
            with self.subTest(difficulty=difficulty), self.assertRaises(SystemExit):
                _run({"difficulty": difficulty, "moves": [{"capture": False, "captureValue": 0}]})


if __name__ == "__main__":
    unittest.main()
