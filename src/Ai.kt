class Ai {
    class Node(var estimate: Int, val board: Board){}
    private val u = Utility()

    //flag = 0 when min, 1 when max
    //ALWAYS clone board before recursive calls
    fun miniMaxOpening(argBoard: Board, flag: Int, depth: Int, alpha: Int, beta: Int): Node{
        var nAlpha = alpha
        var nBeta = beta

        var board = u.cloneBoard(argBoard)

        if(depth == 0){
            val value = evalOpening(board)
            return Node(value, board)
        }
        var best = u.cloneBoard(board)

        if(flag == 1){
            var re = Integer.MIN_VALUE
            for(i in 0 until board.c.boardSize){
                if(!board.isEmpty(i)){
                    continue
                }
                var child = u.cloneBoard(board)
                child.flipA(i)
                if(board.formsMill(i, child.playerA)){ //remove
                    for(j in 0 until board.c.boardSize){
                        if(child.playerB.get(j)){
                            var child2 = u.cloneBoard(child)
                            child2.flipB(j)
                            var temp = miniMaxOpening(child2, 0, depth - 1, nAlpha, nBeta)
                            if (temp.estimate > re) {
                                best = u.cloneBoard(child2)
                            }
                            re = re.coerceAtLeast(temp.estimate)
                            if (re >= nBeta) {
                                return Node(re, u.cloneBoard(best))
                            }
                            nAlpha = nAlpha.coerceAtLeast(re)
                        }
                    }
                }
                else {
                    var temp = miniMaxOpening(child, 0, depth - 1, nAlpha, nBeta)
                    if (temp.estimate > re) {
                        best = u.cloneBoard(child)
                    }
                    re = temp.estimate.coerceAtLeast(re)
                    if (re >= nBeta) {
                        return Node(re, u.cloneBoard(best))
                    }
                    nAlpha = nAlpha.coerceAtLeast(re)
                }
            }
            return Node(re, u.cloneBoard(best))
        }
        else{
            var re = Integer.MAX_VALUE
            for(i in 0 until board.c.boardSize){
                if(!board.isEmpty(i)){
                    continue
                }
                var child = u.cloneBoard(board)
                child.flipB(i)
                if(board.formsMill(i, child.playerB)){ //remove
                    for(j in 0 until board.c.boardSize){
                        if(child.playerA.get(j)){
                            var child2 = u.cloneBoard(child)
                            child2.flipA(j)

                            var temp = miniMaxOpening(child2, 1, depth - 1, nAlpha, nBeta)

                            if (temp.estimate < re) {
                                best = u.cloneBoard(child2)
                            }

                            re = re.coerceAtMost(temp.estimate)
                            if (re <= nAlpha) {
                                return Node(re, u.cloneBoard(best))
                            }
                            nBeta = nBeta.coerceAtMost(re)
                        }
                    }
                }
                else {
                    var temp = miniMaxOpening(child, 1, depth - 1, nAlpha, nBeta)

                    if (temp.estimate < re) {
                        best = u.cloneBoard(child)
                    }

                    re = re.coerceAtMost(temp.estimate)

                    if (re <= nAlpha) {
                        return Node(re, u.cloneBoard(best))
                    }
                    nBeta = nBeta.coerceAtMost(re)
                }
            }
            return Node(re, u.cloneBoard(best))
        }
    }

    //optimizations todo:
    // If you can form a mill, do it
    // otherwise, if your opponent is about to form a mill and you can block it, do so
    // otherwise do the standard static estimates

    fun negaMaxMainPlay(argBoard: Board, depth: Int, alpha: Int, beta:Int, color: Int): Node{
        var nAlpha = alpha
        var nBeta = beta

        var board = u.cloneBoard(argBoard)

        if(depth == 0){
            return Node(evalMainPlay(board)*color, u.cloneBoard(board))
        }
        var value = Integer.MIN_VALUE
        val nextMoves = if(color == 1) board.getValidAMoves() else board.getValidBMoves()
        var best = board
        for(move in nextMoves){
            if(move.b.formsMill(move.index, (if(color == 1) move.b.playerA else move.b.playerB))){
                //if condition for removal on creating a mill
                for(j in 0 until board.c.boardSize){
                    if((color == 1 && move.b.playerB.get(j))
                        ||(color == -1 && move.b.playerA.get(j))){
                        //this is the piece to remove
                        var child = u.cloneBoard(move.b)
                        if (color == 1){
                            child.playerB.flip(j)
                        }else{
                            child.playerA.flip(j)
                        }
                        val neg = negaMaxMainPlay(child, depth-1, -nBeta, -nAlpha, -color)
                        if(-1 * neg.estimate > value){
                            best = u.cloneBoard(child)
                        }
                        value = value.coerceAtLeast(neg.estimate)
                        nAlpha = nAlpha.coerceAtLeast(value)
                        if(nAlpha >= nBeta){
                            break
                        }
                    }
                }
            }
            else{
                val neg = negaMaxMainPlay(move.b, depth-1, -nBeta, -nAlpha, -color)
                if(-1 * neg.estimate > value){
                    best = u.cloneBoard(move.b)
                }
                value = value.coerceAtLeast(neg.estimate)
                nAlpha = nAlpha.coerceAtLeast(value)
                if(nAlpha >= nBeta){
                    break
                }
            }
        }
        return Node(nAlpha, best)
    }

    private fun evalOpening(board: Board): Int {
        return board.countPieces(true) - (2 * board.countPieces(false))
    }

    private fun evalMainPlay(board: Board): Int {
        val bMove = board.getValidBMoves().size
        val mills = board.numMorrises(true)
        return (board.countPieces(true) - board.countPieces(false)) - bMove + (100*mills)
    }

}