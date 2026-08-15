#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

入力(JSON)の内容によらず、常に数値でない出力を返す。
AIPlayer.trySelectWithPython が非数値の応答（NumberFormatException）を
受け取った際にJavaフォールバックへ正しく切り替わることを検証するために使う。
"""
import sys


def main():
    sys.stdin.read()
    print("not-a-number")


if __name__ == "__main__":
    main()
