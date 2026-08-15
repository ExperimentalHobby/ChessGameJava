#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

入力(JSON)の内容によらず、常に合法手数を超える範囲外のindexを返す。
AIPlayer.trySelectWithPython が範囲外の応答を受け取った際に
Javaフォールバックへ正しく切り替わることを検証するために使う。
"""
import sys


def main():
    sys.stdin.read()
    print(999)


if __name__ == "__main__":
    main()
