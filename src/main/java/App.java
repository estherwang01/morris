import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private GameUI g = new GameUI();
    private Node selected = null;
    private boolean aiTurn = false;
    private int state = 0;
    //0 = normal select
    // 1 = ai thinking
    // 2 = removal time

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml.fxml"));
        primaryStage.setTitle("Mill");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setScene(scene);
        primaryStage.show();

        playGame(root);
        //states:
        // ai playing
        // player selecting / moving
        // player selecting to remove node
        // game over

    }
    public void playGame(Parent root){
        rerender(g.getGameState(), root);
        Button rem = (Button) root.lookup("#remove");
        rem.setOnAction(f -> {
            if(selected != null && g.canRemove(selected.getId())){
                g.remove(selected.getId());
                g.aiMove();
                rem.setDisable(true);
                rem.setVisible(false);
                selected = null;
                rerender(g.getGameState(), root);
                state = 0;
            }
        });
        List<Button> mainButtons = getAllBoardNodes(root);
        for(Button b: mainButtons){
            b.setOnAction(e -> {
                if(state == 0) {
                    if (selected != null && g.canSelect(selected.getId())) {
                        if (g.canMove(selected.getId(), b.getId())) {
                            int result = g.move(selected.getId(), b.getId());
                            if (result == 1) {
                                state = 2;
                                selected = null;
                                rem.setVisible(true);
                                rem.setDisable(false);
                            }
                            else{
                                state = 1;
                                g.aiMove();
//                                rerender(g.getGameState(), root);
                                state = 0;
                                selected = null;
                            }
                        }
                    }
                    if(g.canSelect(b.getId())){
                        selected = b;
                    }
                }
                else if (state == 2){
                    if(g.canRemove(b.getId())){
                        selected = b;
                    }
                }
                rerender(g.getGameState(), root);
            });
        }
        List<Button> initNodes = getAllOtherNodes(root);
        for(int i =0; i<initNodes.size(); i++){
            Button b = initNodes.get(i);
            b.setOnAction(e -> {
                if(Integer.parseInt(b.getId().substring(1)) >= g.getRound() - 1){
                    if(state == 0){
                        selected = b;
                        rerender(g.getGameState(), root);
                    }
                }
            });

        }
    }

    public void rerender(Board b, Parent root){
        Platform.runLater(
                () -> {
                    if (g.hasWon() != 0) {
                        System.out.println("win state");
                        Rectangle r = (Rectangle) root.lookup("#won");
                        r.setOpacity(.5);
                        Text t = (Text) root.lookup("#gotext");
                        t.setVisible(true);

                    } else {
                        List<Button> nodes = getAllBoardNodes(root);
                        for (int i = 0; i < nodes.size(); i++) {
                            String style;
                            if (b.isEmpty(i)) {
                                style = Style.noMarble;
                            } else if (b.getPlayerA().get(i)) {
                                style = Style.whiteMarble;
                            } else {
                                style = Style.blackMarble;
                            }
                            Button current = nodes.get(i);
                            current.setStyle("-fx-background-color: " + style
                                    + "; -fx-background-radius: 50%; ");
                            if (current.equals(selected)) {
                                current.setStyle(current.getStyle() + "-fx-border-color: "
                                        + Style.highlight + "; -fx-border-radius: 50%; ");
                            }
                        }

                        List<Button> initNodes = getAllOtherNodes(root);
                        for (int i = 0; i < 9; i++) {
                            Button current = initNodes.get(i);
                            if (i >= (g).getRound() - 1) {
                                current.setStyle("-fx-background-color: " + Style.blackMarble
                                        + "; -fx-background-radius: 50%; ");
                            } else {
                                current.setStyle("-fx-background-color: " + Style.noMarble
                                        + "; -fx-background-radius: 50%; ");
                            }
                            if (current.equals(selected)) {
                                current.setStyle(current.getStyle() + "-fx-border-color: "
                                        + Style.highlight + "; -fx-border-radius: 50%; ");
                            }
                        }
                    }
                }
        );
    }

    public static List<Button> getAllBoardNodes(Parent root){
        ArrayList<Button> nodes = new ArrayList<>();
        for(int i =0; i<24; i++){
            Button b = (Button)root.lookup("#b" + i);
            nodes.add(b);
        }
        return nodes;
    }

    public static List<Button> getAllOtherNodes(Parent root){
        ArrayList<Button> nodes = new ArrayList<>();
        for(int i =0; i<9; i++){
            Button b = (Button)root.lookup("#u" + i);
            nodes.add(b);
        }
        return nodes;
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
}
