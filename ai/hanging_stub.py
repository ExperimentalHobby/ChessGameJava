#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

出力を1行返した直後、終了せずハングし続ける。AIPlayer.runPython は標準出力の
1行目をブロッキング読み取りしてからプロセス終了をタイムアウト付きで待つため、
何も出力しないまま止まるスタブでは readLine() 自体が無限にブロックしてしまい
タイムアウト処理を検証できない。出力を返してから止まることで、
process.waitFor(timeout) のタイムアウト検出とdestroyForcibly()による
プロセス強制終了、その後のJavaフォールバックへの切り替えを正しく検証する。
"""
import sys
import time


def main():
    sys.stdin.read()
    print(0)
    sys.stdout.flush()
    time.sleep(3600)


if __name__ == "__main__":
    main()
