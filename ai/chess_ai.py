#!/usr/bin/env python3
"""ChessGame の AI 着手選択ロジック（Java からのサブプロセス連携用）。

stdin から 1 件の JSON を読み取り、難易度に応じた手を選んで stdout に 1 行で返す。

難易度1〜3（合法手リストから index を選択）— Java の AIPlayer を忠実に移植:
    入力 {"difficulty": 2, "moves": [{"capture": true, "captureValue": 5}, ...]}
    出力 選択した手の index（合法手が無い場合は -1）
    難易度 1: 全合法手からランダム
    難易度 2: 駒を取る手を優先（無ければランダム）
    難易度 3: 取れる駒の素材価値が最大の手（同値なら最初の手）

難易度4（minimax + alpha-beta、engine.py に委譲）:
    入力 {"difficulty": 4, "depth": 3, "fen": "<FEN>"}
    出力 最善手の UCI 文字列（例 e2e4 / e7e8q、合法手が無い場合は空行）

整合性テスト用コマンド:
    入力 {"command": "movegen", "fen": "<FEN>"}
    出力 合法手を UCI 文字列で空白区切り列挙（ソート済み）
"""
import json
import random
import sys


def select_index(difficulty, moves, rng=random):
    """選択した手の index を返す。合法手が無い場合は -1。

    Args:
        difficulty: 難易度（1=ランダム, 2=駒取り優先, 3=最善手優先）。
        moves: 各手を表す dict のリスト。各 dict は ``capture``（bool）と
            ``captureValue``（int）を持つ。
        rng: 乱数源（テスト用に差し替え可能）。
    """
    if not moves:
        return -1

    if difficulty == 2:
        captures = [i for i, m in enumerate(moves) if m.get("capture")]
        if captures:
            return rng.choice(captures)
        return rng.randrange(len(moves))

    if difficulty == 3:
        best_index = 0
        best_score = None
        for i, move in enumerate(moves):
            score = move.get("captureValue", 0)
            # strict > のため、同値のときは先に出現した手を保持する
            if best_score is None or score > best_score:
                best_score = score
                best_index = i
        return best_index

    # 難易度 1 および未知の難易度: 純粋なランダム選択
    return rng.randrange(len(moves))


def main():
    data = json.load(sys.stdin)
    command = data.get("command", "select")

    # 整合性テスト用: FEN の合法手を UCI で列挙する
    if command == "movegen":
        import engine
        print(" ".join(sorted(engine.legal_moves_uci(data["fen"]))))
        return

    difficulty = data.get("difficulty", 1)

    # 難易度4: minimax + alpha-beta エンジンに委譲し、最善手を UCI で返す
    if difficulty == 4:
        import engine
        depth = data.get("depth", 3)
        move = engine.best_move(data["fen"], depth)
        print(move if move else "")
        return

    # 難易度1〜3: 合法手リストから選択した index を返す
    moves = data.get("moves", [])
    print(select_index(difficulty, moves))


if __name__ == "__main__":
    main()
