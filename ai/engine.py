#!/usr/bin/env python3
"""自己完結型のチェスエンジン（難易度4の minimax + alpha-beta 用）。

Java の ChessGame から FEN を受け取り、合法手生成・評価・探索をすべて Python 側で
行って最善手を UCI 形式（例 ``e2e4`` / ``e7e8q``）で返す。Java のルール層には依存
しない（その代わり ``perft`` と Java↔Python 整合性テストで正しさを担保する）。

盤面のインデックス規約は Java 側に合わせる:
    sq = row * 8 + col,  row 0 = ランク8（最上段）, col 0 = ファイルa
    白駒は大文字 (KQRBNP)、黒駒は小文字 (kqrbnp)、空マスは None
"""

STARTPOS = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

# ---------------------------------------------------------------------------
# マス ⇄ 座標変換
# ---------------------------------------------------------------------------

def sq_to_uci(idx):
    """マスのインデックスを代数表記（例 ``e4``）に変換する。"""
    r, c = divmod(idx, 8)
    return chr(ord('a') + c) + str(8 - r)


def uci_to_idx(square):
    """代数表記（例 ``e4``）をマスのインデックスに変換する。"""
    col = ord(square[0]) - ord('a')
    rank = int(square[1])
    return (8 - rank) * 8 + col


# ---------------------------------------------------------------------------
# 局面（State）と FEN
# ---------------------------------------------------------------------------

class State:
    """探索対象の局面。盤面・手番・キャスリング権・アンパッサン対象を持つ。"""

    __slots__ = ("board", "white_to_move", "castling", "ep", "halfmove", "fullmove")

    def __init__(self, board, white_to_move, castling, ep, halfmove, fullmove):
        self.board = board                  # list[64] of char or None
        self.white_to_move = white_to_move  # bool
        self.castling = castling            # set of 'K','Q','k','q'
        self.ep = ep                        # en passant target square index or None
        self.halfmove = halfmove
        self.fullmove = fullmove


def parse_fen(fen):
    """FEN 文字列を {@link State} に変換する。"""
    parts = fen.split()
    board = [None] * 64
    row = 0
    col = 0
    for ch in parts[0]:
        if ch == '/':
            row += 1
            col = 0
        elif ch.isdigit():
            col += int(ch)
        else:
            board[row * 8 + col] = ch
            col += 1

    white_to_move = (parts[1] == 'w') if len(parts) > 1 else True
    castling = set(c for c in parts[2] if c in "KQkq") if len(parts) > 2 and parts[2] != '-' else set()
    ep = uci_to_idx(parts[3]) if len(parts) > 3 and parts[3] != '-' else None
    halfmove = int(parts[4]) if len(parts) > 4 else 0
    fullmove = int(parts[5]) if len(parts) > 5 else 1
    return State(board, white_to_move, castling, ep, halfmove, fullmove)


# ---------------------------------------------------------------------------
# 利き・移動の方向ベクトル
# ---------------------------------------------------------------------------

KNIGHT_OFFSETS = [(-2, -1), (-2, 1), (-1, -2), (-1, 2),
                  (1, -2), (1, 2), (2, -1), (2, 1)]
KING_OFFSETS = [(-1, -1), (-1, 0), (-1, 1), (0, -1),
                (0, 1), (1, -1), (1, 0), (1, 1)]
BISHOP_DIRS = [(-1, -1), (-1, 1), (1, -1), (1, 1)]
ROOK_DIRS = [(-1, 0), (1, 0), (0, -1), (0, 1)]
QUEEN_DIRS = BISHOP_DIRS + ROOK_DIRS

# キャスリング権を失わせるコーナー（ルークの原位置）
CASTLE_CORNERS = ((7 * 8 + 0, 'Q'), (7 * 8 + 7, 'K'),
                  (0 * 8 + 0, 'q'), (0 * 8 + 7, 'k'))


def is_attacked(state, sq, by_white):
    """マス ``sq`` が ``by_white`` 側の駒に攻撃されているかを返す。"""
    board = state.board
    r, c = divmod(sq, 8)

    # ポーンの利き: by_white のポーンは (r+1, c±1) から (r,c) を攻撃する
    pawn_row = r + 1 if by_white else r - 1
    if 0 <= pawn_row < 8:
        pawn_char = 'P' if by_white else 'p'
        for dc in (-1, 1):
            pc = c + dc
            if 0 <= pc < 8 and board[pawn_row * 8 + pc] == pawn_char:
                return True

    # ナイト
    knight_char = 'N' if by_white else 'n'
    for dr, dc in KNIGHT_OFFSETS:
        nr, nc = r + dr, c + dc
        if 0 <= nr < 8 and 0 <= nc < 8 and board[nr * 8 + nc] == knight_char:
            return True

    # キング
    king_char = 'K' if by_white else 'k'
    for dr, dc in KING_OFFSETS:
        nr, nc = r + dr, c + dc
        if 0 <= nr < 8 and 0 <= nc < 8 and board[nr * 8 + nc] == king_char:
            return True

    # ビショップ／クイーン（斜め）
    for dr, dc in BISHOP_DIRS:
        nr, nc = r + dr, c + dc
        while 0 <= nr < 8 and 0 <= nc < 8:
            p = board[nr * 8 + nc]
            if p is not None:
                if p.isupper() == by_white and p.upper() in ('B', 'Q'):
                    return True
                break
            nr += dr
            nc += dc

    # ルーク／クイーン（直線）
    for dr, dc in ROOK_DIRS:
        nr, nc = r + dr, c + dc
        while 0 <= nr < 8 and 0 <= nc < 8:
            p = board[nr * 8 + nc]
            if p is not None:
                if p.isupper() == by_white and p.upper() in ('R', 'Q'):
                    return True
                break
            nr += dr
            nc += dc

    return False


# ---------------------------------------------------------------------------
# 擬似合法手の生成
# ---------------------------------------------------------------------------

def _gen_pawn(state, idx, moves):
    board = state.board
    white = state.white_to_move
    r, c = divmod(idx, 8)
    dr = -1 if white else 1
    start_row = 6 if white else 1
    promo_row = 0 if white else 7
    nr = r + dr

    # 前進
    if 0 <= nr < 8 and board[nr * 8 + c] is None:
        if nr == promo_row:
            for p in ('q', 'r', 'b', 'n'):
                moves.append((idx, nr * 8 + c, p))
        else:
            moves.append((idx, nr * 8 + c, None))
            # 2マス前進
            if r == start_row and board[(r + 2 * dr) * 8 + c] is None:
                moves.append((idx, (r + 2 * dr) * 8 + c, None))

    # 斜め取り（アンパッサン含む）
    for dc in (-1, 1):
        nc = c + dc
        if 0 <= nr < 8 and 0 <= nc < 8:
            tidx = nr * 8 + nc
            target = board[tidx]
            if target is not None and target.isupper() != white:
                if nr == promo_row:
                    for p in ('q', 'r', 'b', 'n'):
                        moves.append((idx, tidx, p))
                else:
                    moves.append((idx, tidx, None))
            elif tidx == state.ep and state.ep is not None:
                moves.append((idx, tidx, None))


def _gen_knight(state, idx, moves):
    board = state.board
    white = state.white_to_move
    r, c = divmod(idx, 8)
    for dr, dc in KNIGHT_OFFSETS:
        nr, nc = r + dr, c + dc
        if 0 <= nr < 8 and 0 <= nc < 8:
            t = board[nr * 8 + nc]
            if t is None or t.isupper() != white:
                moves.append((idx, nr * 8 + nc, None))


def _gen_slider(state, idx, moves, dirs):
    board = state.board
    white = state.white_to_move
    r, c = divmod(idx, 8)
    for dr, dc in dirs:
        nr, nc = r + dr, c + dc
        while 0 <= nr < 8 and 0 <= nc < 8:
            t = board[nr * 8 + nc]
            if t is None:
                moves.append((idx, nr * 8 + nc, None))
            else:
                if t.isupper() != white:
                    moves.append((idx, nr * 8 + nc, None))
                break
            nr += dr
            nc += dc


def _gen_king(state, idx, moves):
    board = state.board
    white = state.white_to_move
    r, c = divmod(idx, 8)
    for dr, dc in KING_OFFSETS:
        nr, nc = r + dr, c + dc
        if 0 <= nr < 8 and 0 <= nc < 8:
            t = board[nr * 8 + nc]
            if t is None or t.isupper() != white:
                moves.append((idx, nr * 8 + nc, None))

    # キャスリング（キングが原位置にあるときのみ）
    row = 7 if white else 0
    if idx != row * 8 + 4:
        return
    opp = not white
    rook_char = 'R' if white else 'r'
    ks = 'K' if white else 'k'
    qs = 'Q' if white else 'q'

    if ks in state.castling and board[row * 8 + 5] is None and board[row * 8 + 6] is None \
            and board[row * 8 + 7] == rook_char:
        if not is_attacked(state, row * 8 + 4, opp) \
                and not is_attacked(state, row * 8 + 5, opp) \
                and not is_attacked(state, row * 8 + 6, opp):
            moves.append((idx, row * 8 + 6, None))

    if qs in state.castling and board[row * 8 + 3] is None and board[row * 8 + 2] is None \
            and board[row * 8 + 1] is None and board[row * 8 + 0] == rook_char:
        if not is_attacked(state, row * 8 + 4, opp) \
                and not is_attacked(state, row * 8 + 3, opp) \
                and not is_attacked(state, row * 8 + 2, opp):
            moves.append((idx, row * 8 + 2, None))


def generate_pseudo_moves(state):
    """自王が取られる手も含む擬似合法手をすべて生成する。"""
    moves = []
    board = state.board
    white = state.white_to_move
    for idx in range(64):
        piece = board[idx]
        if piece is None or piece.isupper() != white:
            continue
        pt = piece.upper()
        if pt == 'P':
            _gen_pawn(state, idx, moves)
        elif pt == 'N':
            _gen_knight(state, idx, moves)
        elif pt == 'B':
            _gen_slider(state, idx, moves, BISHOP_DIRS)
        elif pt == 'R':
            _gen_slider(state, idx, moves, ROOK_DIRS)
        elif pt == 'Q':
            _gen_slider(state, idx, moves, QUEEN_DIRS)
        elif pt == 'K':
            _gen_king(state, idx, moves)
    return moves


def make_move(state, move):
    """手を適用した新しい {@link State} を返す（元の state は変更しない）。"""
    frm, to, promo = move
    board = state.board[:]
    piece = board[frm]
    is_white = piece.isupper()
    pt = piece.upper()
    castling = set(state.castling)
    new_ep = None

    board[to] = piece
    board[frm] = None

    if pt == 'P':
        # アンパッサンによる取り（斜めに ep ターゲットへ移動）
        if to == state.ep and state.ep is not None and (to % 8) != (frm % 8):
            cap_sq = (frm // 8) * 8 + (to % 8)
            board[cap_sq] = None
        # 2マス前進で ep ターゲットを設定
        if abs(to // 8 - frm // 8) == 2:
            mid_row = (to // 8 + frm // 8) // 2
            new_ep = mid_row * 8 + (frm % 8)
        # 昇格
        if promo is not None:
            board[to] = promo.upper() if is_white else promo.lower()

    elif pt == 'K':
        # キャスリング時のルーク移動
        if abs(to % 8 - frm % 8) == 2:
            row = frm // 8
            if to % 8 > frm % 8:
                rook_from, rook_to = row * 8 + 7, row * 8 + 5
            else:
                rook_from, rook_to = row * 8 + 0, row * 8 + 3
            board[rook_to] = board[rook_from]
            board[rook_from] = None
        if is_white:
            castling.discard('K')
            castling.discard('Q')
        else:
            castling.discard('k')
            castling.discard('q')

    elif pt == 'R':
        if is_white:
            if frm == 7 * 8 + 0:
                castling.discard('Q')
            elif frm == 7 * 8 + 7:
                castling.discard('K')
        else:
            if frm == 0 * 8 + 0:
                castling.discard('q')
            elif frm == 0 * 8 + 7:
                castling.discard('k')

    # コーナーのルークが取られた場合も対応するキャスリング権を失う
    for corner, right in CASTLE_CORNERS:
        if to == corner:
            castling.discard(right)

    fullmove = state.fullmove + (0 if state.white_to_move else 1)
    return State(board, not state.white_to_move, castling, new_ep, 0, fullmove)


def in_check(state):
    """手番側のキングが王手されているかを返す。"""
    white = state.white_to_move
    ksq = state.board.index('K' if white else 'k')
    return is_attacked(state, ksq, not white)


def legal_moves(state):
    """自王を王手に晒さない合法手のみを返す。"""
    white = state.white_to_move
    king_char = 'K' if white else 'k'
    result = []
    for mv in generate_pseudo_moves(state):
        nxt = make_move(state, mv)
        ksq = nxt.board.index(king_char)
        if not is_attacked(nxt, ksq, not white):
            result.append(mv)
    return result


def move_to_uci(move):
    """内部表現の手を UCI 文字列に変換する。"""
    frm, to, promo = move
    uci = sq_to_uci(frm) + sq_to_uci(to)
    if promo:
        uci += promo
    return uci


def legal_moves_uci(fen):
    """FEN の局面の合法手を UCI 文字列のリストで返す。"""
    state = parse_fen(fen)
    return [move_to_uci(m) for m in legal_moves(state)]


def perft(state, depth):
    """指定深さまでの合法手ノード数を数える（move-gen の正しさ検証用）。"""
    if depth == 0:
        return 1
    if depth == 1:
        return len(legal_moves(state))
    total = 0
    for mv in legal_moves(state):
        total += perft(make_move(state, mv), depth - 1)
    return total


def perft_fen(fen, depth):
    return perft(parse_fen(fen), depth)


# ---------------------------------------------------------------------------
# 評価関数（マテリアル + ピーススクエアテーブル）
# ---------------------------------------------------------------------------

PIECE_VALUE = {'P': 100, 'N': 320, 'B': 330, 'R': 500, 'Q': 900, 'K': 20000}

# 以下の PST は Tomasz Michniewski の "Simplified Evaluation Function" に基づく。
# 白視点・row 0 = ランク8 で記述しており、黒駒は上下反転（mirror）して参照する。
_PST_PAWN = [
    0, 0, 0, 0, 0, 0, 0, 0,
    50, 50, 50, 50, 50, 50, 50, 50,
    10, 10, 20, 30, 30, 20, 10, 10,
    5, 5, 10, 25, 25, 10, 5, 5,
    0, 0, 0, 20, 20, 0, 0, 0,
    5, -5, -10, 0, 0, -10, -5, 5,
    5, 10, 10, -20, -20, 10, 10, 5,
    0, 0, 0, 0, 0, 0, 0, 0,
]
_PST_KNIGHT = [
    -50, -40, -30, -30, -30, -30, -40, -50,
    -40, -20, 0, 0, 0, 0, -20, -40,
    -30, 0, 10, 15, 15, 10, 0, -30,
    -30, 5, 15, 20, 20, 15, 5, -30,
    -30, 0, 15, 20, 20, 15, 0, -30,
    -30, 5, 10, 15, 15, 10, 5, -30,
    -40, -20, 0, 5, 5, 0, -20, -40,
    -50, -40, -30, -30, -30, -30, -40, -50,
]
_PST_BISHOP = [
    -20, -10, -10, -10, -10, -10, -10, -20,
    -10, 0, 0, 0, 0, 0, 0, -10,
    -10, 0, 5, 10, 10, 5, 0, -10,
    -10, 5, 5, 10, 10, 5, 5, -10,
    -10, 0, 10, 10, 10, 10, 0, -10,
    -10, 10, 10, 10, 10, 10, 10, -10,
    -10, 5, 0, 0, 0, 0, 5, -10,
    -20, -10, -10, -10, -10, -10, -10, -20,
]
_PST_ROOK = [
    0, 0, 0, 0, 0, 0, 0, 0,
    5, 10, 10, 10, 10, 10, 10, 5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    -5, 0, 0, 0, 0, 0, 0, -5,
    0, 0, 0, 5, 5, 0, 0, 0,
]
_PST_QUEEN = [
    -20, -10, -10, -5, -5, -10, -10, -20,
    -10, 0, 0, 0, 0, 0, 0, -10,
    -10, 0, 5, 5, 5, 5, 0, -10,
    -5, 0, 5, 5, 5, 5, 0, -5,
    0, 0, 5, 5, 5, 5, 0, -5,
    -10, 5, 5, 5, 5, 5, 0, -10,
    -10, 0, 5, 0, 0, 0, 0, -10,
    -20, -10, -10, -5, -5, -10, -10, -20,
]
_PST_KING = [
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -30, -40, -40, -50, -50, -40, -40, -30,
    -20, -30, -30, -40, -40, -30, -30, -20,
    -10, -20, -20, -20, -20, -20, -20, -10,
    20, 20, 0, 0, 0, 0, 20, 20,
    20, 30, 10, 0, 0, 10, 30, 20,
]
PST = {'P': _PST_PAWN, 'N': _PST_KNIGHT, 'B': _PST_BISHOP,
       'R': _PST_ROOK, 'Q': _PST_QUEEN, 'K': _PST_KING}


def evaluate(state):
    """手番側から見た局面の静的評価値を返す（正＝手番側有利）。"""
    white_total = 0
    black_total = 0
    board = state.board
    for sq in range(64):
        p = board[sq]
        if p is None:
            continue
        pt = p.upper()
        value = PIECE_VALUE[pt]
        if p.isupper():
            white_total += value + PST[pt][sq]
        else:
            mirror = (7 - (sq // 8)) * 8 + (sq % 8)
            black_total += value + PST[pt][mirror]
    score = white_total - black_total
    return score if state.white_to_move else -score


# ---------------------------------------------------------------------------
# 探索（negamax + alpha-beta）
# ---------------------------------------------------------------------------

MATE = 1_000_000
INF = 10_000_000


def _order_key(state, move):
    """ムーブオーダリング用のスコア（取り・昇格を優先して枝刈り効率を上げる）。"""
    frm, to, promo = move
    board = state.board
    score = 0
    victim = board[to]
    if victim is not None:
        score += 10 * PIECE_VALUE[victim.upper()] - PIECE_VALUE[board[frm].upper()]
    if promo:
        score += PIECE_VALUE[promo.upper()]
    return score


def negamax(state, depth, alpha, beta, ply):
    """negamax + alpha-beta。手番側から見た最善評価値を返す。"""
    moves = legal_moves(state)
    if not moves:
        if in_check(state):
            return -MATE + ply   # 王手で合法手なし＝詰み（早い詰みほど評価が悪い）
        return 0                  # ステールメイト（引き分け）
    if depth == 0:
        return evaluate(state)

    moves.sort(key=lambda m: _order_key(state, m), reverse=True)
    best = -INF
    for mv in moves:
        score = -negamax(make_move(state, mv), depth - 1, -beta, -alpha, ply + 1)
        if score > best:
            best = score
        if best > alpha:
            alpha = best
        if alpha >= beta:
            break
    return best


def search_value(fen, depth):
    """ルート局面の探索評価値を返す（alpha-beta 検証用）。"""
    state = parse_fen(fen)
    return negamax(state, depth, -INF, INF, 0)


def best_move(fen, depth):
    """FEN の局面で最善手を UCI 文字列で返す。合法手が無ければ None。"""
    state = parse_fen(fen)
    moves = legal_moves(state)
    if not moves:
        return None
    moves.sort(key=lambda m: _order_key(state, m), reverse=True)
    best = None
    best_score = -INF
    alpha = -INF
    for mv in moves:
        score = -negamax(make_move(state, mv), depth - 1, -INF, -alpha, 1)
        if score > best_score:
            best_score = score
            best = mv
        if best_score > alpha:
            alpha = best_score
    return move_to_uci(best)
