import java.io.File
import java.util.*

class Board{
    var playerAPrint = "W"
    var playerBPrint = "B"

    var aiRecentlyPlayedVal = -2
    var playerRecentlyPlayedVal = -2

    //constants
    val c = Constants()
    private val u = Utility()

    //board rep
    //ai is always player A - add external funct to let either ai or human player go first
    var playerA: BitSet = BitSet(c.boardSize) //see board.PNG for index arrangement
    var playerB: BitSet = BitSet(c.boardSize)

    var recentlyClosed = 0 //0 = no one closed a mill in the last move,
    // 1 = player A closed in last move, -1 = player b


    fun flipB(i: Int){
        playerB.flip(i)
    }

    fun flipA(i: Int){
        playerA.flip(i)
    }

    fun setAiRecentlyPlayed(i:Int){
        aiRecentlyPlayedVal = i
    }
    fun setPlayerRecentlyPlayed(i:Int){
        playerRecentlyPlayedVal = i
    }

    class MoveIndex(var b: Board, var index: Int)

    fun getValidAMoves(): MutableList<MoveIndex>{
        var ls = MutableList<MoveIndex>(0){_ -> MoveIndex(Board(), -1)}
        var i = -2
        while(i != -1){
            i = if(i == -2) playerA.nextSetBit(0) else playerA.nextSetBit(i+1)
            if(i == aiRecentlyPlayedVal || i == -1){ // cannot play same thing twice in a row
                continue
            }
            val neighbors = c.neighbors[i]
            for(x in neighbors){
                if(isEmpty(x)){
                    var nextB = Board()
                    nextB.playerA = playerA.clone() as BitSet
                    nextB.playerB = playerB.clone() as BitSet
                    nextB.flipA(i)
                    nextB.flipA(x)
                    nextB.setPlayerRecentlyPlayed(playerRecentlyPlayedVal)
                    nextB.setAiRecentlyPlayed(x)
                    ls.add(MoveIndex(nextB, x))
                }
            }

        }
        return ls
    }
    fun getValidBMoves(): MutableList<MoveIndex>{
        var ls = MutableList<MoveIndex>(0){_ -> MoveIndex(Board(), -1)}
        var i = -2
        while(i != -1){
            i = if(i == -2) playerB.nextSetBit(0) else playerB.nextSetBit(i+1)
            if(i == playerRecentlyPlayedVal || i == -1){ // cannot play same thing twice in a row
                continue
            }
            val neighbors = c.neighbors[i]
            for(x in neighbors){
                if(isEmpty(x)){
                    var nextB = Board()
                    nextB.playerA = playerA.clone() as BitSet
                    nextB.playerB = playerB.clone() as BitSet
                    nextB.flipB(i)
                    nextB.flipB(x)
                    nextB.setAiRecentlyPlayed(aiRecentlyPlayedVal)
                    nextB.setPlayerRecentlyPlayed(x)
                    ls.add(MoveIndex(nextB, x))
                }
            }

        }
        return ls
    }

    //-----------------------------------------------
    //evaluation functions
    //-----------------------------------------------
    //recently closed morris
    fun recentlyClosedMorris():Int{
        return recentlyClosed
    }

    //number of morrises
    fun numMorrises(a: Boolean):Int{
        val player = if(a) playerA else playerB
        val ret = c.mills.foldIndexed(0){
                index: Int, acc: Int, items: List<List<Int>> ->
                acc + items.fold(0){
                        acc2: Int, item: List<Int> -> acc2 +  if
                                                                      (item.fold(player.get(index)){
                            acc3: Boolean, num: Int -> acc3 && player.get(num)
                    }) 1 else 0 } } / 3
        return ret
    }

    //difference number of blocked pieces
    fun blocked(a:Boolean):Int{
        val player = if(a) playerA else playerB
        val otherPlayer = if (a) playerB else playerA

        return c.neighbors.foldIndexed(0){
                index: Int, acc: Int, item: List<Int> ->
            val currentBlocked = player.get(index) && item.fold(true){
                    acc2: Boolean, num:Int -> acc2 && (player.get(num) || otherPlayer.get(num))
            }
            acc + (if(currentBlocked) 1 else 0)
        }
    }

    fun countPieces(a: Boolean): Int{
        val player = if(a) playerA else playerB
        var count = 0
        for(i in 0..23){
            count += if (player.get(i)) 1 else 0
        }
        return count
    }
    //number 2 piece configurations (currently checking that third place is empty)
    fun num2PieceConfigs(a:Boolean):Int{
        val player = if(a) playerA else playerB
        val otherPlayer = if (a) playerB else playerA

        return c.mills.foldIndexed(0){
                index: Int, acc: Int, items: List<List<Int>> ->
            val occupied = player.get(index) || otherPlayer.get(index)
            if(occupied) acc else
                items.fold(0){
                        acc2: Int, item: List<Int> ->
                    acc2 + (if(item.fold(true)
                        {acc3: Boolean, num:Int -> player.get(num) && acc3}) 1 else 0)
                }
        }
    }

    //number of 3 piece configurations
    //add this in maybe if eval function isn't performing very well

    //double morris
    fun numDoubleMorris(a:Boolean):Int{
        //make a list of morrises
        //count pairs
        var morris = MutableList<MutableList<Int>>(0){ _ -> MutableList<Int>(0){ _ -> 0} }
        val player = if(a) playerA else playerB
        c.mills.forEachIndexed(){
                index: Int, items: List<List<Int>> ->
            items.forEach(){
                    item: List<Int> -> if
                                               (item.fold(player.get(index)){
                        acc3: Boolean, num: Int -> acc3 && player.get(num)
                }) {
                var m = MutableList<Int>(0) { _ -> 0 }
                m.add(index)
                for (i in item) {
                    m.add(i)
                }
                m.sort()
                morris.add(m)
            }

            } }
        morris.distinct().toList()
        var count = 0
        for(mor in morris){
            for(other in morris){
                if(mor == other){
                    continue
                }
                count += singleOverlap(mor, other)
            }
        }
        return count/2

    }
    private fun singleOverlap(a: MutableList<Int>, b: MutableList<Int>): Int{
        return a.fold(0){acc: Int, item: Int -> acc + if (b.contains(item)) 1 else 0}
    }

    //winning configuration check
    fun winCheck(): Int{
        if(countPieces(false) <= 2 || countPieces(false) == blocked(false)){
            return 1
        }
        else if(countPieces(true) <= 2 || countPieces(true) == blocked(true)){
            return -1
        }
        return 0
    }

    //random other utility functions
    private fun getEmpty(): BitSet{
        var p1 = playerA.clone() as BitSet
        var p2 = playerB.clone() as BitSet
        p1.or(p2)
        p1.flip(0, c.boardSize)
        return p1
    }

    fun isEmpty(i: Int): Boolean{
        return !playerA.get(i) && !playerB.get(i)
    }

    fun formsMill(i: Int, player: BitSet): Boolean {
        return c.mills[i].fold(false) { acc: Boolean, item: List<Int> ->
            acc or (player.get(item[0]) && player.get(item[1]))
        }
    }

    private fun translateIndex(i: Int): Int{
        return when (i) {
            9,10,11,12,13,14 -> i
            21,22,23 -> i-21
            18,19,20 -> i-15
            15,16,17 -> i-9
            6,7,8 -> i+9
            3,4,5-> i+ 15
            else -> {
                i + 21
            }
        }
    }
    fun stringRep(): String{
        var ret = ""
        var index = 0
        File(c.dataDir + "print").useLines{ lines -> lines.forEach{
                line -> line.forEach {
                c -> if (c != '.') ret += c
        else {
            val ind = translateIndex(index)
            ret += if(playerA.get(ind)){
                playerAPrint
            } else if(playerB.get(ind)){
                playerBPrint
            } else{
                "."
            }
            index++;
        }
        }
            ret += "\n"
        }}
        return ret
    }
}