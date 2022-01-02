import java.util.*

class Utility {
    fun cloneBoard(b: Board): Board{
        var b2 = Board()
        b2.playerA = b.playerA.clone() as BitSet
        b2.playerB = b.playerB.clone() as BitSet
        b2.playerRecentlyPlayedVal = b.playerRecentlyPlayedVal
        b2.aiRecentlyPlayedVal = b.aiRecentlyPlayedVal
        return b2
    }

    //midway game state where players have just finished the opening phase
    fun partialGame(): Board{
        var board = Board()
        val a = listOf(0, 1, 3, 4, 6, 10, 11, 18)
        val b = listOf(2, 5, 7, 9, 13, 15, 20)
        for(vala in a){
            board.flipA(vala)
        }
        for(valb in b){
            board.flipB(valb)
        }
        return board
    }
}