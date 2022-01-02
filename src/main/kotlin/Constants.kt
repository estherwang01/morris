import java.io.File

class Constants {
    val boardSize : Int = 24
    private val separator: String = System.getProperty("file.separator")
    val dataDir: String = "data$separator"

    //neighbors[i] are the neighbors of ith index into the board
    var neighbors: MutableList<List<Int>> = MutableList<List<Int>>(24){_ -> List<Int>(0){_ -> 0} }
    init{
        File(dataDir + "neighbors").useLines { lines -> lines.forEachIndexed { index, line ->
            neighbors[index] = line.split(",").map {item -> item.toInt()}
        }}
    }
    //mills[i] is pairs of locations with which i forms a mill
    var mills: MutableList<List<List<Int>>> =
            MutableList<List<List<Int>>>(24){_ -> List<List<Int>>(0){_ -> List<Int>(0){_ -> 0} } }
    init{
        File(dataDir + "mills").useLines{
            lines -> lines.forEachIndexed {
            index, line ->
            mills[index] = line.split(";").map()
                {item -> item.split(",")}.map{l -> l.map()
                    {num -> num.toInt()} } }
        }
    }
}