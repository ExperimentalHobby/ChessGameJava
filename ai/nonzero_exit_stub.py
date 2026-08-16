#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

入力(JSON)の内容によらず、常に非ゼロ終了コード(1)で終了する。
ai/chess_ai.py・ai/engine.py側で例外や構文エラーが起きた場合を模し、
AIPlayer.runPython が非ゼロ終了時に警告ログを出してJavaフォールバックへ
正しく切り替わることを検証するために使う（Issue #179）。
"""
import sys


def main():
    sys.stdin.read()
    sys.exit(1)


if __name__ == "__main__":
    main()
