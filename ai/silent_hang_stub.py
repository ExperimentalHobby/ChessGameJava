#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

標準入力を読み終えた後、何も出力せずにハングし続ける。AIPlayer.runPython が
標準出力の1行目をブロッキング読み取りしてから（修正前は）プロセス終了を
タイムアウト付きで待つ実装だと、1行も出力しないまま止まるこのスタブでは
readLine() 自体が無限にブロックしてしまいタイムアウト処理に到達できない。
既存の hanging_stub.py（出力してから固まる）とは異なるこの経路を検証するために使う。
"""
import sys
import time


def main():
    sys.stdin.read()
    time.sleep(3600)


if __name__ == "__main__":
    main()
