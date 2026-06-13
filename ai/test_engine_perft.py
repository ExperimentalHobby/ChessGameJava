"""engine.py の合法手生成を perft（既知のノード数）で検証する。

perft はチェスエンジンの move-gen 検証の定番手法。標準局面の既知の値と一致すれば、
キャスリング・アンパッサン・昇格・ピン・王手回避を含む move-gen が正しいと言える。
期待値は chessprogramming wiki の標準 perft 局面に基づく。

実行: py -m unittest discover -s ai -p "test_*.py"
"""
import unittest

import engine

KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
POSITION_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"
POSITION_4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"
POSITION_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"


class PerftTest(unittest.TestCase):
    def assert_perft(self, fen, depth, expected):
        self.assertEqual(engine.perft_fen(fen, depth), expected,
                         msg=f"perft({depth}) for {fen}")

    def test_startpos(self):
        self.assert_perft(engine.STARTPOS, 1, 20)
        self.assert_perft(engine.STARTPOS, 2, 400)
        self.assert_perft(engine.STARTPOS, 3, 8902)

    def test_kiwipete(self):
        # キャスリング・各種取りを多く含む有名なテスト局面
        self.assert_perft(KIWIPETE, 1, 48)
        self.assert_perft(KIWIPETE, 2, 2039)

    def test_position_3(self):
        # アンパッサン・ピンを含む
        self.assert_perft(POSITION_3, 1, 14)
        self.assert_perft(POSITION_3, 2, 191)
        self.assert_perft(POSITION_3, 3, 2812)

    def test_position_4(self):
        # 昇格・キャスリングを含む
        self.assert_perft(POSITION_4, 1, 6)
        self.assert_perft(POSITION_4, 2, 264)

    def test_position_5(self):
        self.assert_perft(POSITION_5, 1, 44)
        self.assert_perft(POSITION_5, 2, 1486)


if __name__ == "__main__":
    unittest.main()
