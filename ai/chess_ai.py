#!/usr/bin/env python3
"""ChessGame の AI 着手選択ロジック（Java からのサブプロセス連携用）。

stdin から 1 件の JSON を読み取り、難易度4（Expert）の最善手を選んで stdout に 1 行で返す。
難易度1〜3は Java 側（AIPlayer）で完結するため、このスクリプトは関与しない。

難易度4（minimax + alpha-beta、engine.py に委譲）:
    入力 {"difficulty": 4, "depth": 3, "fen": "<FEN>", "timeout": 20}
    出力 最善手の UCI 文字列（例 e2e4 / e7e8q、合法手が無い場合は空行）
    timeout（省略可、秒）: 指定時は反復深化がその時間予算内で自発的に打ち切る

整合性テスト用コマンド:
    入力 {"command": "movegen", "fen": "<FEN>"}
    出力 合法手を UCI 文字列で空白区切り列挙（ソート済み）
"""
import json
import sys


def main():
    """stdin から JSON を1件読み取り、command/difficulty に応じた結果を stdout に1行で返す。"""
    data = json.load(sys.stdin)
    command = data.get("command")

    # 整合性テスト用: FEN の合法手を UCI で列挙する
    if command == "movegen":
        import engine
        print(" ".join(sorted(engine.legal_moves_uci(data["fen"]))))
        return

    # 難易度4: minimax + alpha-beta エンジンに委譲し、最善手を UCI で返す。
    # 難易度1〜3は Java 実装のみで動作するため、ここでは受け付けない
    if data.get("difficulty") != 4:
        sys.exit("unsupported request: only difficulty 4 and command 'movegen' are handled")
    import engine
    depth = data.get("depth", 3)
    timeout_seconds = data.get("timeout")
    move = engine.best_move(data["fen"], depth, timeout_seconds)
    print(move if move else "")


if __name__ == "__main__":
    main()
