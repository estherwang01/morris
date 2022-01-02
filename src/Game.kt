import java.util.*

class Game {
//    var board = Board()
//    var round = 1
    val u = Utility()
    var selected = -2
    private val ai = Ai()
    var minimaxDepth = 4
    var board = u.partialGame()
    var round = 10

    //instructions
    //SELECT -1 = select unplayed marble if such exists
    //SELECT x = select that index in the board if a marble is there
    //MOVE x = move selected marble to index x
    //if nothing is selected, then selected = -2 and MOVE does nothing until you select
    //REMOVE removes selected thing if applicable / prompted

    private fun processUserInput(i: Int){
        if(board.formsMill(i, board.playerB)){
            println("You may now select a piece of your opponent to remove. Use SELECT and REMOVE commands.")
            var validSelected = false
            while(true){
                try{
                    print("Enter a valid SELECT or REMOVE command: ")
                    var com = readLine()
                    if(com != null && com.contains("SELECT")){
                        var num = com.substring(com.indexOf("SELECT") + 6).trim().toInt()
                        if(num >= 0 && board.playerA.get(num)){
                            selected = num
                            validSelected = true
                        }
                        else{
                            println("Please select on of your opponent's marbles to remove.")
                        }
                    }
                    else if (com != null && com.contains("REMOVE") && validSelected){
                        board.flipA(selected)
                        return
                    }
                }catch(e: Exception){
                    println("Invalid command specified.")
                    continue
                }

            }
        }
    }
    private fun getUserInput(): Int{
        var validSelected = selected == -1 && round <= 9
        while(true){
            try{
                print("Currently selected: $selected. Please enter a MOVE or SELECT command: ")
                var com = readLine()
                if (com != null && com.contains("SELECT")) {
                    var num = com.substring(com.indexOf("SELECT") + 6).trim().toInt()
                    if(((num == -1 && round <= 9) || board.playerB.get(num)) && num != board.playerRecentlyPlayedVal){
                        selected = num
                        validSelected = true
                    }else{
                        println("Please only select one of your marbles. " +
                                "Remember you cannot move the same marble twice in a row")
                    }
                }
                else if(com != null && com.contains("MOVE") && validSelected){
                    var num = com.substring(com.indexOf("MOVE") + 4).trim().toInt()
                    if(!board.isEmpty(num)){
                        println("You may only move to an empty location on the board")
                    }
                    else{
                        if(selected == -1){
                            board.flipB(num)
                            return num
                        }
                        else{
                            if(!board.c.neighbors[selected].contains(num)){
                                println("You may only move pieces to adjacent locations")
                            }
                            else{
                                board.flipB(selected)
                                board.flipB(num)
                                return num
                            }
                        }
                    }
                }
            }catch(e: Exception){
                println("Invalid command specified")
                continue
            }
        }
    }

    fun play(){
        print("AI plays white, you play black. Would you like to go first or second? Enter 1 or 2: ")
        val inp = readLine()
        //1 is ai, 2 is user
        var current = if(inp == "2") 2 else 1

        //every time a play is made, update board so that that marble cannot immediately be moved in the next step
        while(true){
            if(current == 2){
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

                println("Static estimate:" + a.estimate)
            }
            else{
                var x = getUserInput()
                processUserInput(x)
                board.setPlayerRecentlyPlayed(x)
                round++
            }
            current = (current) % 2 + 1
            print(board.stringRep())
        }
    }
}
