package Main;

import Main.Controller.MainScreenController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main extends Application {

    private Stage primaryStage;
    private Connection connection;
    private Policies policies = null;
    private MainScreenController mainScreenController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainWindow();
    }

    private void mainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("GUI/MainScreen.fxml"));
            AnchorPane pane = loader.load();

            Scene scene = new Scene(pane);

            mainScreenController = loader.getController();
            mainScreenController.setMain(this);

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPolicies(String serverURL, String username, String password) {
        Thread retrievePolicies = new Thread(() -> {
            try {
                if (serverURL.matches("https://[a-zA-Z0-9]*.jamfcloud.com")) {
                    String baseURL = serverURL + "/JSSResource/policies";
                    connection = new Connection(baseURL);
                    InputStream response = connection.getXMLResponse(username, password);
                    if (connection.getResponseCode() == 401) {
                        mainScreenController.setTxtPolicies("");
                        mainScreenController.setTxtOldestDate("");
                        mainScreenController.setTxtError("An error has occured, check username and password");
                        return;
                    }
                    else if (response == null) {
                        mainScreenController.setTxtPolicies("");
                        mainScreenController.setTxtOldestDate("");
                        mainScreenController.setTxtError("An error has occured, unable to retrieve policies");
                        return;
                    }
                    mainScreenController.setTxtError("");
                    XMLDocument document = new XMLDocument(response);
                    policies = new Policies(document.getXMLDocument());
                    int policiesTotal = policies.getPolicies().size();
                    mainScreenController.setTxtPolicies(String.valueOf(policiesTotal));
                    if (policiesTotal > 0) {
                        Date oldestDate = policies.getOldestPolicyDate();
                        mainScreenController.setTxtOldestDate(dateFormat.format(oldestDate));
                        return;
                    }
                    mainScreenController.setTxtOldestDate("");
                }
                mainScreenController.setTxtPolicies("");
                mainScreenController.setTxtOldestDate("");
                mainScreenController.setTxtError("Invalid URL");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        retrievePolicies.start();
    }

    public void removePolicies(String date, String username, String password) throws ParseException {
        if (policies == null) {
            return;
        }
        policies.removePolicies(date);
        Date oldestDate = policies.getOldestPolicyDate();
        ArrayList<Policy> policiesToDelete;
        policiesToDelete = policies.getPoliciesToRemove();
        Thread removePolicies = new Thread(() -> {
            for (int index = 0; index < policiesToDelete.size(); index++) {
                int responseCode;
                try {
                    int id = policiesToDelete.get(index).getId();
                    responseCode = connection.deletePolicy(id, username, password);
                    if (responseCode == 200) {
                        int newPolicyCount = Integer.parseInt(mainScreenController.getTxtPolicies()) - 1;
                        int nextIndex = index + 1;
                        Platform.runLater(() -> {mainScreenController.setTxtPolicies(String.valueOf(newPolicyCount));
                            if (nextIndex < policiesToDelete.size()) {
                                mainScreenController.setTxtOldestDate(dateFormat.format(policiesToDelete.get(nextIndex).getDate()));
                            }
                            else if (policies.getPolicies().size() > 0) {
                                mainScreenController.setTxtOldestDate(dateFormat.format(oldestDate));
                            }
                            else {
                                mainScreenController.setTxtOldestDate("");
                            }
                        });
                    }
                    else {
                        mainScreenController.setTxtError("Unable to remove policy or policies");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        removePolicies.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
