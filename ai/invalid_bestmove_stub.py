#!/usr/bin/env python3
"""AIPlayerの結合テスト用スタブ。

入力(JSON)の内容によらず、常に不正なUCI文字列を返す。
AIPlayer.trySelectWithEngine が不正な応答を受け取った際に
Javaフォールバック（難易度3相当）へ正しく切り替わることを検証するために使う。
"""
import sys


def main():
    sys.stdin.read()
    print("zz99")


if __name__ == "__main__":
    main()
