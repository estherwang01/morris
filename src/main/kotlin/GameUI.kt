import java.util.*

class GameUI {
    private val u = Utility()
    private val ai = Ai()
    var minimaxDepth = 2
    var board = Board()
    var round = 1

    private fun idToInt(id: String): Int {
        if (round <= 9 && id[0] == 'u') {
            return -1
        }
        return id.substring(1).toInt()
    }

    fun hasWon(): Int {
        return 0;
//        return board.winCheck()
    }
    fun aiMove(): Unit{
        var a = if (round <= 9)
            ai.miniMaxOpening(board, 1, minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE)
        else
            ai.negaMaxMainPlay(board, minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1)

        val old = board.playerA.clone() as BitSet
        val newB = a.board.playerA.clone() as BitSet

        newB.xor(old)
        var recentlyMoved = newB.nextSetBit(0)

        assert(recentlyMoved != -1) // sanity check

        if(!a.board.playerA.get(recentlyMoved)){
            recentlyMoved = newB.nextSetBit(recentlyMoved + 1)
        }
        board = a.board
        board.setAiRecentlyPlayed(recentlyMoved)
    }

    fun canSelect(id: String): Boolean {
        if(round > 9){
            return board.playerB.get(idToInt(id)) && idToInt(id) != board.playerRecentlyPlayedVal
        }
        return id[0] == 'u' && idToInt(id) < round - 1
    }

    fun canMove(selected: String, destination: String): Boolean {
        val sel = idToInt(selected)
        val dest = idToInt(destination)
        return (sel == -1 || board.c.neighbors[sel].contains(dest)) && board.isEmpty(dest)
    }

    fun canRemove(selected: String): Boolean {
        val sel = idToInt(selected)
        return board.playerA.get(sel)
    }

    //precondition: canMove(selected, destination) == true
    fun move(selected: String, destination: String): Int{
        round++
        val sel = idToInt(selected)
        val dest = idToInt(destination)
        if(sel != -1){
            board.flipB(sel)
        }
        board.flipB(dest)
        if(board.formsMill(dest, board.playerB)){
            return 1
        }
        board.setPlayerRecentlyPlayed(dest);
        return 0
    }

    //precondition: canRemove(selected) == true
    fun remove(selected: String): Unit{
        val sel = idToInt(selected)
        board.flipA(sel)
    }

    fun getGameState(): Board{
        return u.cloneBoard(board)
    }
}